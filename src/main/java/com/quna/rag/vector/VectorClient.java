package com.quna.rag.vector;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 向量库客户端抽象，屏蔽 Milvus、ES Vector 或其他向量库实现差异。
 */
public interface VectorClient {
    void add(String collectionCode, List<Document> documents);

    void delete(String collectionCode, List<String> vectorIds);

    List<Document> search(String collectionCode, String query, int topK, String filterExpression);
}
