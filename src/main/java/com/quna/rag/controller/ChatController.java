package com.quna.rag.controller;

import com.quna.rag.entity.ChatHistory;
import com.quna.rag.service.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/history")
    public List<ChatHistory> history(HttpServletRequest request) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        return chatService.history(userId);
    }
}