package com.quna.rag.service;

import org.springframework.stereotype.Service;

/**
 * 向量化文本构造服务，负责把标题、正文和关键词转成更适合 embedding 的语义检索文本。
 */
@Service
public class RagEmbeddingService {
    public String buildEmbeddingText(String title, String content, String keywords) {
        StringBuilder text = new StringBuilder();
        if (title != null && !title.isBlank()) {
            text.append("标题：").append(title).append('\n');
        }
        if (keywords != null && !keywords.isBlank()) {
            text.append("关键词：").append(keywords).append('\n');
        }
        text.append("正文：\n").append(content == null ? "" : content);
        return text.toString();
    }
}
