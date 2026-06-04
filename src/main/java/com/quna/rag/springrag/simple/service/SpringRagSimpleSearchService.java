package com.quna.rag.springrag.simple.service;

import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.simple.model.SimpleSearchRequest;
import com.quna.rag.springrag.simple.model.SimpleSearchResult;
import com.quna.rag.springrag.simple.model.SimpleVectorHit;
import com.quna.rag.springrag.store.RagVectorStoreRouter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 简单 RAG 查询服务，只做向量召回，不做关键词、混合合并和重排。
 */
@Service
public class SpringRagSimpleSearchService {
    private final RagVectorStoreRouter vectorStoreRouter;

    public SpringRagSimpleSearchService(RagVectorStoreRouter vectorStoreRouter) {
        this.vectorStoreRouter = vectorStoreRouter;
    }

    public SimpleSearchResult search(SimpleSearchRequest request) {
        if (request == null || isBlank(request.getQuestion())) {
            throw new IllegalArgumentException("查询内容不能为空");
        }
        RagCollectionType collectionType = RagCollectionType.fromCode(request.getCollectionCode());
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(request.getQuestion().trim())
                .topK(request.getTopK() == null ? 5 : Math.max(request.getTopK(), 1));
        if (request.getMinScore() == null) {
            builder.similarityThresholdAll();
        } else {
            builder.similarityThreshold(request.getMinScore());
        }
        String filterExpression = filterExpression(request);
        if (!filterExpression.isBlank()) {
            builder.filterExpression(filterExpression);
        }
        List<SimpleVectorHit> hits = vectorStoreRouter.get(collectionType)
                .similaritySearch(builder.build())
                .stream()
                .map(document -> toHit(collectionType, document))
                .toList();
        return new SimpleSearchResult(request.getQuestion(), hits.size(), hits);
    }

    private SimpleVectorHit toHit(RagCollectionType collectionType, Document document) {
        Map<String, Object> metadata = document.getMetadata();
        SimpleVectorHit hit = new SimpleVectorHit();
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
        hit.setScore(document.getScore());
        hit.setMetadata(metadata);
        return hit;
    }

    private String filterExpression(SimpleSearchRequest request) {
        StringBuilder filter = new StringBuilder();
        appendEquals(filter, "project", request.getProject());
        appendEquals(filter, "module", request.getModule());
        appendEquals(filter, "docType", request.getDocType());
        return filter.toString();
    }

    private void appendEquals(StringBuilder filter, String key, String value) {
        if (isBlank(value)) {
            return;
        }
        if (!filter.isEmpty()) {
            filter.append(" && ");
        }
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
