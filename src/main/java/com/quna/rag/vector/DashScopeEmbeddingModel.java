package com.quna.rag.vector;

import com.quna.rag.util.AiUtil;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DashScope EmbeddingModel 适配器，用于 Spring AI VectorStore 自动生成向量。
 */
@Primary
@Component
public class DashScopeEmbeddingModel implements EmbeddingModel {
    private final AiUtil aiUtil;
    private final int dimensions;

    public DashScopeEmbeddingModel(AiUtil aiUtil, @Value("${milvus.dim:1536}") int dimensions) {
        this.aiUtil = aiUtil;
        this.dimensions = dimensions;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        List<String> instructions = request.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            embeddings.add(new Embedding(toFloatArray(aiUtil.embedding(instructions.get(i))), i));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return toFloatArray(aiUtil.embedding(document.getText()));
    }

    @Override
    public List<float[]> embed(List<Document> documents, EmbeddingOptions options, BatchingStrategy batchingStrategy) {
        return documents.stream().map(this::embed).toList();
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    private float[] toFloatArray(List<Float> values) {
        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }
}
