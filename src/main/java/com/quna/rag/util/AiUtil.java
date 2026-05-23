package com.quna.rag.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiUtil {
    @Value("${llm.api-key}")
    private String apiKey;
    @Value("${llm.embedding-url}")
    private String embedUrl;
    @Value("${llm.embedding-model:text-embedding-v2}")
    private String embedModel;
    
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<Float> embedding(String text) {
        try {
            Map<String, Object> body = Map.of(
                    "model", embedModel,
                    "input", Map.of("texts", List.of(text))
            );
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(embedUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body)))
                    .timeout(Duration.ofSeconds(20))
                    .build();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            
            // 打印原始响应，方便调试
            log.info("Embedding API 响应状态码: {}", res.statusCode());
            log.info("Embedding API 响应内容: {}", res.body());
            
            if (res.statusCode() != 200) {
                throw new RuntimeException("API 请求失败，状态码: " + res.statusCode());
            }
            
            JSONObject json = JSON.parseObject(res.body());
            
            // 检查是否有错误信息
            if (json.containsKey("code") || json.containsKey("message")) {
                throw new RuntimeException("API 返回错误: " + json.getString("message"));
            }
            
            // 阿里云返回格式: {"output":{"embeddings":[{"embedding":[...],"text_index":0}]}}
            JSONObject output = json.getJSONObject("output");
            if (output == null) {
                throw new RuntimeException("响应格式错误，缺少 output 字段");
            }
            
            com.alibaba.fastjson.JSONArray embeddingArray = output
                    .getJSONArray("embeddings")
                    .getJSONObject(0)
                    .getJSONArray("embedding");
            
            // 手动转换为 List<Float>，Milvus 严格要求 Float 类型
            List<Float> result = new java.util.ArrayList<>();
            for (int i = 0; i < embeddingArray.size(); i++) {
                result.add(embeddingArray.getFloat(i));
            }
            return result;
        } catch (Exception e) {
            log.error("embedding error: " + e.getMessage());
            throw new RuntimeException("向量生成失败: " + e.getMessage(), e);
        }
    }

    public String chat(String question, String context) {
        try {
            String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
            Map<String, Object> body = Map.of(
                    "model", "qwen-turbo",
                    "input", Map.of("messages", List.of(Map.of(
                            "role", "user",
                            "content", "根据资料回答：\n" + context + "\n问题：" + question
                    ))),
                    "parameters", Map.of("temperature", 0.1)
            );
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body)))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            JSONObject json = JSON.parseObject(res.body());
            return json.getJSONObject("output").getString("text");
        } catch (Exception e) {
            log.error("chat error: " + e.getMessage());
            return "大模型异常：" + e.getMessage();
        }
    }

    /**
     * 流式对话 - 使用 Server-Sent Events
     */
    public void chatStream(String question, String context, java.util.function.Consumer<String> onChunk) {
        try {
            String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
            Map<String, Object> body = Map.of(
                    "model", "qwen-turbo",
                    "input", Map.of("messages", List.of(Map.of(
                            "role", "user",
                            "content", "根据资料回答：\n" + context + "\n问题：" + question
                    ))),
                    "parameters", Map.of(
                            "temperature", 0.1,
                            "incremental_output", true  // 启用增量输出
                    )
            );
            
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("X-DashScope-SSE", "enable")  // 启用 SSE
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body)))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            
            // 使用流式响应处理器
            HttpClient streamingClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            
            streamingClient.sendAsync(req, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        response.body().forEach(line -> {
                            if (line.startsWith("data:")) {
                                String data = line.substring(5).trim();
                                if (data.isEmpty() || "[DONE]".equals(data)) {
                                    return;
                                }
                                try {
                                    JSONObject json = JSON.parseObject(data);
                                    JSONObject output = json.getJSONObject("output");
                                    if (output != null) {
                                        String text = output.getString("text");
                                        if (text != null && !text.isEmpty()) {
                                            onChunk.accept(text);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.warn("解析流式数据失败: {}", e.getMessage());
                                }
                            }
                        });
                    })
                    .join();
                    
        } catch (Exception e) {
            log.error("chat stream error: " + e.getMessage());
            onChunk.accept("\n\n[错误] 大模型异常：" + e.getMessage());
        }
    }
}