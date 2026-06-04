package com.quna.rag.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 标准 RAG 向量库配置。
 */
@Configuration
public class RagVectorConfig {
    @Bean
    public VectorStore ragStandardVectorStore(@Qualifier("ragMilvusServiceClient") MilvusServiceClient milvusClient,
                                              EmbeddingModel embeddingModel,
                                              @Value("${milvus.rag-standard-collection:rag_chunk_vector}") String collection,
                                              @Value("${milvus.dim:1536}") int dimensions) {
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName(collection)
                .embeddingDimension(dimensions)
                .indexType(IndexType.IVF_FLAT)
                .metricType(MetricType.COSINE)
                .initializeSchema(true)
                .build();
    }
}
