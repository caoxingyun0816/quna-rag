package com.quna.rag.springrag.retrieval;

import com.quna.rag.springrag.model.RagChunkEntity;
import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.model.RagSearchRequest;
import com.quna.rag.springrag.store.RagChunkMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * 关键词检索器，基于 rag_chunk 表的全文索引召回候选切片。
 */

@Slf4j
@Component
public class KeywordRetriever {
    private final RagChunkMapper chunkMapper;

    public KeywordRetriever(RagChunkMapper chunkMapper) {
        this.chunkMapper = chunkMapper;
    }

    public List<RagHit> retrieve(RagCollectionType collectionType, RagSearchRequest request, int topK) {
        List<RagChunkEntity> chunks = new ArrayList<>();
        try {
            // 优先使用 MySQL FULLTEXT；环境未建全文索引或分词异常时，继续走 LIKE 保证接口可用。
            chunks.addAll(chunkMapper.keywordSearch(collectionType.getCode(), request.getQuestion(), request.getProject(),
                    request.getModule(), request.getDocType(), topK));
        } catch (Exception e) {
            log.warn("FULLTEXT 检索失败，将仅使用 LIKE 检索: {}", e.getMessage());
        }

        // 中文短语、低频词和代码片段不一定能被 FULLTEXT 召回，LIKE 精确包含检索固定叠加一轮。
        chunks.addAll(chunkMapper.likeSearch(collectionType.getCode(), request.getQuestion(), request.getProject(),
                request.getModule(), request.getDocType(), topK));

        List<String> expandedTerms = expandedTerms(request.getQuestion());
        if (!expandedTerms.isEmpty()) {
            chunks.addAll(chunkMapper.expandedSearch(collectionType.getCode(), expandedTerms, request.getProject(),
                    request.getModule(), request.getDocType(), topK));
        }

        return chunks.stream()
                .collect(Collectors.toMap(RagChunkEntity::getId, Function.identity(), this::merge, LinkedHashMap::new))
                .values()
                .stream()
                .map(chunk -> toHit(chunk, request.getQuestion()))
                .toList();
    }

    private RagChunkEntity merge(RagChunkEntity left, RagChunkEntity right) {
        double score = Math.max(left.getKeywordScore() == null ? 0d : left.getKeywordScore(),
                right.getKeywordScore() == null ? 0d : right.getKeywordScore());
        left.setKeywordScore(score);
        return left;
    }

    private RagHit toHit(RagChunkEntity chunk, String query) {
        // 关键词检索来自数据库切片表，需要补齐 metadata，保持和向量检索返回结构一致。
        Map<String, Object> metadata = metadata(chunk);
        RagHit hit = new RagHit();
        hit.setChunkId(chunk.getId());
        hit.setDocId(chunk.getDocId());
        hit.setCollectionCode(chunk.getCollectionCode());
        hit.setFilename(chunk.getFilename());
        hit.setFileType(chunk.getFileType());
        hit.setProject(chunk.getProject());
        hit.setModule(chunk.getModule());
        hit.setDocType(chunk.getDocType());
        hit.setTags(chunk.getTags());
        hit.setChunkIndex(chunk.getChunkIndex());
        hit.setTitlePath(chunk.getTitlePath());
        hit.setContent(chunk.getContent());
        hit.setKeywordScore(chunk.getKeywordScore() == null ? 0.1d : chunk.getKeywordScore());
        hit.setScore(hit.getKeywordScore());
        hit.setKeywordHit(true);
        hit.setExactKeywordHit(contains(chunk.getTitlePath(), query)
                || contains(chunk.getContent(), query)
                || contains(chunk.getKeywords(), query));
        hit.setMetadata(metadata);
        return hit;
    }

    private boolean contains(String text, String query) {
        return text != null && query != null && !query.isBlank() && text.contains(query.trim());
    }

    private List<String> expandedTerms(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String text = query.trim().toLowerCase();
        Set<String> terms = new LinkedHashSet<>();
        addIfContains(terms, text, "候选", "候选人", "简历", "姓名", "学历", "工作年限");
        addIfContains(terms, text, "简历", "候选人", "学历", "工作年限", "当前职位");
        addIfContains(terms, text, "打分", "评分", "分数", "综合评分", "total_score", "score");
        addIfContains(terms, text, "评分", "打分", "分数", "综合评分", "total_score", "score");
        addIfContains(terms, text, "规则", "必要条件评分", "加分项", "扣分项", "等级", "grade", "label");
        addIfContains(terms, text, "推荐", "强烈推荐", "不推荐", "recommend", "reject", "pending");
        for (String token : text.split("[^a-z0-9_./\\-\\u4e00-\\u9fa5]+")) {
            if (token.length() >= 2) {
                terms.add(token);
            }
        }
        return terms.stream().limit(30).toList();
    }

    private void addIfContains(Set<String> terms, String query, String trigger, String... values) {
        if (!query.contains(trigger)) {
            return;
        }
        terms.add(trigger);
        for (String value : values) {
            terms.add(value);
        }
    }

    private Map<String, Object> metadata(RagChunkEntity chunk) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("docId", chunk.getDocId());
        metadata.put("chunkId", chunk.getId());
        metadata.put("chunkIndex", chunk.getChunkIndex());
        metadata.put("titlePath", chunk.getTitlePath());
        metadata.put("collectionCode", chunk.getCollectionCode());
        metadata.put("filename", chunk.getFilename());
        metadata.put("fileType", chunk.getFileType());
        metadata.put("project", chunk.getProject());
        metadata.put("module", chunk.getModule());
        metadata.put("docType", chunk.getDocType());
        metadata.put("tags", chunk.getTags());
        metadata.put("keywords", chunk.getKeywords());
        return metadata;
    }
}
