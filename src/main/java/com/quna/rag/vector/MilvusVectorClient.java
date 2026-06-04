package com.quna.rag.vector;

import com.quna.rag.springrag.model.RagCollectionType;
import com.quna.rag.springrag.store.RagVectorStoreRouter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Milvus 向量库客户端，复用当前 Spring AI VectorStore 配置。
 */
@Component
public class MilvusVectorClient implements VectorClient {
    private final RagVectorStoreRouter vectorStoreRouter;

    public MilvusVectorClient(RagVectorStoreRouter vectorStoreRouter) {
        this.vectorStoreRouter = vectorStoreRouter;
    }

    @Override
    public void add(String collectionCode, List<Document> documents) {
        vectorStoreRouter.get(RagCollectionType.fromCode(collectionCode)).add(documents);
    }

    @Override
    public void delete(String collectionCode, List<String> vectorIds) {
        vectorStoreRouter.get(RagCollectionType.fromCode(collectionCode)).delete(vectorIds);
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
        return vectorStoreRouter.get(RagCollectionType.fromCode(collectionCode)).similaritySearch(builder.build());
    }
}
