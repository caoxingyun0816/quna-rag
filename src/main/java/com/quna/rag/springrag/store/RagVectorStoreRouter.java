package com.quna.rag.springrag.store;

import com.quna.rag.springrag.config.SpringRagProperties;
import com.quna.rag.springrag.model.RagCollectionType;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class RagVectorStoreRouter {
    private final Map<RagCollectionType, VectorStore> stores = new EnumMap<>(RagCollectionType.class);
    private final SpringRagProperties properties;

    public RagVectorStoreRouter(@Qualifier("springAiMilvusClient") MilvusServiceClient milvusClient,
                                EmbeddingModel embeddingModel,
                                SpringRagProperties properties,
                                @Value("${milvus.dim:1536}") int dimensions) {
        this.properties = properties;
        for (RagCollectionType type : RagCollectionType.values()) {
            MilvusVectorStore store = MilvusVectorStore.builder(milvusClient, embeddingModel)
                    .collectionName(resolveCollectionName(type))
                    .embeddingDimension(dimensions)
                    .indexType(IndexType.IVF_FLAT)
                    .metricType(MetricType.COSINE)
                    .initializeSchema(true)
                    .build();
            initialize(type, store);
            stores.put(type, store);
        }
    }

    private void initialize(RagCollectionType type, MilvusVectorStore store) {
        try {
            store.afterPropertiesSet();
            log.info("SpringRAG Milvus 集合初始化完成: type={}, collection={}",
                    type.getCode(), resolveCollectionName(type));
        } catch (Exception e) {
            throw new IllegalStateException("SpringRAG Milvus 集合初始化失败: "
                    + resolveCollectionName(type) + ", " + e.getMessage(), e);
        }
    }

    public VectorStore get(RagCollectionType type) {
        VectorStore store = stores.get(type);
        if (store == null) {
            throw new IllegalArgumentException("未配置向量集合: " + type);
        }
        return store;
    }

    public String resolveCollectionName(RagCollectionType type) {
        if (type == RagCollectionType.BUSINESS_DOC) {
            return properties.getCollections().getBusinessDoc();
        }
        if (type == RagCollectionType.TECH_DOC) {
            return properties.getCollections().getTechDoc();
        }
        return type.getMilvusCollection();
    }
}
