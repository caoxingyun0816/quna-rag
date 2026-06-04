package com.quna.rag.vector;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Milvus 向量库客户端，复用当前 Spring AI VectorStore 配置。
 */
@Component
public class MilvusVectorClient implements VectorClient {
    private final VectorStore vectorStore;

    public MilvusVectorClient(@Qualifier("ragStandardVectorStore") VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void add(String collectionCode, List<Document> documents) {
        vectorStore.add(documents);
    }

    @Override
    public void delete(String collectionCode, List<String> vectorIds) {
        vectorStore.delete(vectorIds);
    }

    @Override
    public List<Document> search(String collectionCode, String query, int topK, String filterExpression) {
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThresholdAll();
        if (filterExpression != null && !filterExpression.isBlank()) {
            builder.filterExpression(filterExpression);
        }
        return vectorStore.similaritySearch(builder.build());
    }
}
