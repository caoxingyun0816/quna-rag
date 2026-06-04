package com.quna.rag.vector;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * DashScope ChatModel 适配器，让标准 RAG 链路通过 Spring AI ChatClient 调用通义千问。
 */
@Slf4j
@Primary
@Component
public class DashScopeChatModel implements ChatModel {
    private final String apiKey;
    private final String chatModel;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public DashScopeChatModel(@Value("${llm.api-key}") String apiKey,
                              @Value("${rag.config.chat-model:qwen-turbo}") String chatModel) {
        this.apiKey = apiKey;
        this.chatModel = chatModel;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        String answer = complete(prompt.getContents(), false);
        return new ChatResponse(List.of(new Generation(new AssistantMessage(answer))));
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return ChatOptions.builder()
                .model(chatModel)
                .temperature(0.1)
                .build();
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return Flux.create(sink -> {
            try {
                HttpRequest request = request(prompt.getContents(), true);
                httpClient.send(request, HttpResponse.BodyHandlers.ofLines())
                        .body()
                        .forEach(line -> handleStreamLine(line, text ->
                                sink.next(new ChatResponse(List.of(new Generation(new AssistantMessage(text)))))
                        ));
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    private String complete(String prompt, boolean stream) {
        try {
            HttpResponse<String> response = httpClient.send(request(prompt, stream), HttpResponse.BodyHandlers.ofString());
            JSONObject output = JSON.parseObject(response.body()).getJSONObject("output");
            return output == null ? "" : output.getString("text");
        } catch (Exception e) {
            log.error("RAG Chat 调用失败: {}", e.getMessage(), e);
            return "大模型异常：" + e.getMessage();
        }
    }

    private HttpRequest request(String prompt, boolean stream) {
        Map<String, Object> parameters = stream
                ? Map.of("temperature", 0.1, "incremental_output", true)
                : Map.of("temperature", 0.1);
        Map<String, Object> body = Map.of(
                "model", chatModel,
                "input", Map.of("messages", List.of(Map.of(
                        "role", "user",
                        "content", prompt
                ))),
                "parameters", parameters
        );
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body)))
                .timeout(Duration.ofSeconds(stream ? 60 : 30));
        if (stream) {
            builder.header("X-DashScope-SSE", "enable");
        }
        return builder.build();
    }

    private void handleStreamLine(String line, java.util.function.Consumer<String> onChunk) {
        if (!line.startsWith("data:")) {
            return;
        }
        String data = line.substring(5).trim();
        if (data.isEmpty() || "[DONE]".equals(data)) {
            return;
        }
        try {
            JSONObject output = JSON.parseObject(data).getJSONObject("output");
            if (output == null) {
                return;
            }
            String text = output.getString("text");
            if (text != null && !text.isEmpty()) {
                onChunk.accept(text);
            }
        } catch (Exception e) {
            log.warn("RAG Chat 流式响应解析失败: {}", e.getMessage());
        }
    }
}
