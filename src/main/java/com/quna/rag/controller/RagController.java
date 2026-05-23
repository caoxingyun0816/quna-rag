package com.quna.rag.controller;

import com.quna.rag.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/rag")
public class RagController {
    private final ChatService chatService;

    public RagController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/ask")
    public Map<String, Object> ask(@RequestParam String q, HttpServletRequest request) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        return Map.of("code", 200, "answer", chatService.ask(q, userId));
    }

    /**
     * 流式问答接口 - Server-Sent Events
     */
    @GetMapping(value = "/ask-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askStream(@RequestParam String q, HttpServletRequest request) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        
        // 创建 SSE emitter，设置超时时间为 60 秒
        SseEmitter emitter = new SseEmitter(60000L);
        
        // 异步执行流式问答
        CompletableFuture.runAsync(() -> {
            try {
                chatService.askStream(q, userId, chunk -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .data(chunk)
                                .name("message"));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        
        // 处理连接关闭和超时
        emitter.onCompletion(() -> System.out.println("SSE 连接完成"));
        emitter.onTimeout(() -> emitter.complete());
        emitter.onError((ex) -> System.err.println("SSE 错误: " + ex.getMessage()));
        
        return emitter;
    }
}