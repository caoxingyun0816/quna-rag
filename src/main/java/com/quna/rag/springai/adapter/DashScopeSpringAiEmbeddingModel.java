package com.quna.rag.springai.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DashScopeSpringAiEmbeddingModel implements EmbeddingModel {

    @Value("${llm.api-key}")
    private String apiKey;
    @Value("${llm.embedding-url}")
    private String embeddingUrl;
    @Value("${llm.embedding-model:text-embedding-v2}")
    private String embeddingModel;
    @Value("${milvus.dim:1536}")
    private int dimensions;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        List<String> instructions = request.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            embeddings.add(new Embedding(callEmbeddingApi(instructions.get(i)), i));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return callEmbeddingApi(document.getText());
    }

    @Override
    public List<float[]> embed(List<Document> documents, EmbeddingOptions options, BatchingStrategy batchingStrategy) {
        return documents.stream().map(this::embed).toList();
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    private float[] callEmbeddingApi(String text) {
        try {
            Map<String, Object> body = Map.of(
                    "model", embeddingModel,
                    "input", Map.of("texts", List.of(text))
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(embeddingUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body)))
                    .timeout(Duration.ofSeconds(20))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("DashScope Embedding API 状态码异常: "
                        + response.statusCode() + ", body=" + response.body());
            }
            JSONObject output = JSON.parseObject(response.body()).getJSONObject("output");
            if (output == null) {
                throw new IllegalStateException("DashScope Embedding API 响应缺少 output");
            }
            com.alibaba.fastjson.JSONArray embeddingArray = output
                    .getJSONArray("embeddings")
                    .getJSONObject(0)
                    .getJSONArray("embedding");
            float[] vector = new float[embeddingArray.size()];
            for (int i = 0; i < embeddingArray.size(); i++) {
                vector[i] = embeddingArray.getFloat(i);
            }
            return vector;
        } catch (Exception e) {
            log.error("Spring AI Embedding 调用失败: {}", e.getMessage(), e);
            throw new IllegalStateException("Spring AI Embedding 调用失败: " + e.getMessage(), e);
        }
    }
}
