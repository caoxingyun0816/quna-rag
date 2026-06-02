package com.quna.rag.springrag.retrieval;

import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.model.RagHit;
import com.quna.rag.springrag.model.RagSearchRequest;
import com.quna.rag.springrag.store.RagVectorStoreRouter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 向量检索器，基于 Spring AI VectorStore 从 Milvus 集合中召回语义相近切片。
 */
@Component
public class VectorRetriever {
    private final RagVectorStoreRouter vectorStoreRouter;

    public VectorRetriever(RagVectorStoreRouter vectorStoreRouter) {
        this.vectorStoreRouter = vectorStoreRouter;
    }

    public List<RagHit> retrieve(RagCollectionType collectionType, RagSearchRequest request, int topK) {
        // 过滤条件直接下推给 Milvus，避免跨项目、跨模块的向量结果被召回后再丢弃。
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(semanticQuery(request.getQuestion()))
                .topK(topK)
                .similarityThresholdAll();
        String filterExpression = filterExpression(request);
        if (!filterExpression.isBlank()) {
            builder.filterExpression(filterExpression);
        }
        return vectorStoreRouter.get(collectionType)
                .similaritySearch(builder.build())
                .stream()
                .map(document -> toHit(collectionType, document))
                .toList();
    }

    private RagHit toHit(RagCollectionType collectionType, Document document) {
        // VectorStore 返回的是向量库文档，这里转换成统一的 RagHit，后续才能和关键词结果合并。
        Map<String, Object> metadata = document.getMetadata();
        RagHit hit = new RagHit();
        hit.setChunkId(longValue(metadata.get("chunkId")));
        hit.setDocId(longValue(metadata.get("docId")));
        hit.setCollectionCode(collectionType.getCode());
        hit.setFilename(stringValue(metadata.get("filename")));
        hit.setFileType(stringValue(metadata.get("fileType")));
        hit.setProject(stringValue(metadata.get("project")));
        hit.setModule(stringValue(metadata.get("module")));
        hit.setDocType(stringValue(metadata.get("docType")));
        hit.setTags(stringValue(metadata.get("tags")));
        hit.setChunkIndex(intValue(metadata.get("chunkIndex")));
        hit.setTitlePath(stringValue(metadata.get("titlePath")));
        hit.setContent(document.getText());
        hit.setVectorScore(document.getScore() == null ? 0d : document.getScore());
        hit.setScore(hit.getVectorScore());
        hit.setVectorHit(true);
        hit.setMetadata(metadata);
        return hit;
    }

    private String filterExpression(RagSearchRequest request) {
        StringBuilder filter = new StringBuilder();
        appendEquals(filter, "project", request.getProject());
        appendEquals(filter, "module", request.getModule());
        appendEquals(filter, "docType", request.getDocType());
        return filter.toString();
    }

    private String semanticQuery(String question) {
        if (question == null || question.isBlank()) {
            return "";
        }
        String text = question.trim();
        Set<String> expansions = new LinkedHashSet<>();
        addIfContains(expansions, text, "候选", "候选人", "简历", "招聘评估", "候选人评估", "匹配分析");
        addIfContains(expansions, text, "简历", "候选人", "简历筛选", "简历评估", "学历", "工作年限", "当前职位");
        addIfContains(expansions, text, "打分", "评分", "分数", "综合评分", "加权评分", "评分权重", "total_score", "score");
        addIfContains(expansions, text, "评分", "打分", "分数", "综合评分", "加权评分", "评分权重", "total_score", "score");
        addIfContains(expansions, text, "规则", "必要条件评分", "加分项", "扣分项", "分数段", "等级", "grade", "label");
        addIfContains(expansions, text, "推荐", "强烈推荐", "不推荐", "recommend", "reject", "pending");
        if (expansions.isEmpty()) {
            return text;
        }
        return text + "\n\n相关语义：" + String.join(" ", expansions);
    }

    private void addIfContains(Set<String> expansions, String query, String trigger, String... values) {
        if (!query.contains(trigger)) {
            return;
        }
        expansions.add(trigger);
        expansions.addAll(List.of(values));
    }

    private void appendEquals(StringBuilder filter, String key, String value) {
        if (value == null || value.isBlank()) return;
        if (!filter.isEmpty()) filter.append(" && ");
        filter.append(key).append(" == '").append(value.replace("'", "\\'")).append("'");
    }

    private Long longValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private Integer intValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
