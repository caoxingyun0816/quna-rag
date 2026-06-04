package com.quna.rag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 标准 RAG ChatClient 配置。
 */
@Configuration
public class RagChatConfig {
    @Bean
    public ChatClient ragChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
