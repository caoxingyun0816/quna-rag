package com.quna.rag.springai.adapter;

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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DashScopeSpringAiChatModel implements ChatModel {

    @Value("${llm.api-key}")
    private String apiKey;
    @Value("${springai.rag.chat-model:qwen-turbo}")
    private String chatModel;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public ChatResponse call(Prompt prompt) {
        String answer = complete(prompt.getContents(), false, null);
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
            complete(prompt.getContents(), true, chunk ->
                    sink.next(new ChatResponse(List.of(new Generation(new AssistantMessage(chunk)))))
            );
            sink.complete();
        });
    }

    private String complete(String prompt, boolean stream, java.util.function.Consumer<String> onChunk) {
        try {
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
                StringBuilder answer = new StringBuilder();
                httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofLines())
                        .body()
                        .forEach(line -> handleStreamLine(line, answer, onChunk));
                return answer.toString();
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            JSONObject output = JSON.parseObject(response.body()).getJSONObject("output");
            return output == null ? "" : output.getString("text");
        } catch (Exception e) {
            log.error("Spring AI Chat 调用失败: {}", e.getMessage(), e);
            return "大模型异常：" + e.getMessage();
        }
    }

    private void handleStreamLine(String line, StringBuilder answer, java.util.function.Consumer<String> onChunk) {
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
                answer.append(text);
                onChunk.accept(text);
            }
        } catch (Exception e) {
            log.warn("Spring AI Chat 流式响应解析失败: {}", e.getMessage());
        }
    }
}
