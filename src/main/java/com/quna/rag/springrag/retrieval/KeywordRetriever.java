package com.quna.rag.springrag.retrieval;

import com.quna.rag.springrag.model.RagChunkEntity;
import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.model.RagSearchRequest;
import com.quna.rag.springrag.store.RagChunkMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        List<RagChunkEntity> chunks;
        try {
            // 优先使用 MySQL FULLTEXT；环境未建全文索引或分词异常时，降级为 LIKE 保证接口可用。
            chunks = chunkMapper.keywordSearch(collectionType.getCode(), request.getQuestion(), request.getProject(),
                    request.getModule(), request.getDocType(), topK);
        } catch (Exception e) {
            log.warn("FULLTEXT 检索失败，降级 LIKE 检索: {}", e.getMessage());
            chunks = List.of();
        }
        if (chunks.isEmpty()) {
            chunks = chunkMapper.likeSearch(collectionType.getCode(), request.getQuestion(), request.getProject(),
                    request.getModule(), request.getDocType(), topK);
        }
        return chunks.stream().map(this::toHit).toList();
    }

    private RagHit toHit(RagChunkEntity chunk) {
        // 关键词检索来自数据库切片表，需要补齐 metadata，保持和向量检索返回结构一致。
        Map<String, Object> metadata = metadata(chunk);
        RagHit hit = new RagHit();
        hit.setChunkId(chunk.getId());
        hit.setDocId(chunk.getDocId());
        hit.setCollectionCode(chunk.getCollectionCode());
        hit.setFilename(chunk.getFilename());
        hit.setTitlePath(chunk.getTitlePath());
        hit.setContent(chunk.getContent());
        hit.setKeywordScore(chunk.getKeywordScore() == null ? 0.1d : chunk.getKeywordScore());
        hit.setScore(hit.getKeywordScore());
        hit.setKeywordHit(true);
        hit.setMetadata(metadata);
        return hit;
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
