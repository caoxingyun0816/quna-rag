package com.quna.rag.service;

import com.quna.rag.springrag.service.RagEmbeddingTextBuilder;
import org.springframework.stereotype.Service;

/**
 * 向量化文本构造服务，负责把标题、正文和关键词转成更适合 embedding 的语义检索文本。
 */
@Service
public class RagEmbeddingService {
    private final RagEmbeddingTextBuilder embeddingTextBuilder;

    public RagEmbeddingService(RagEmbeddingTextBuilder embeddingTextBuilder) {
        this.embeddingTextBuilder = embeddingTextBuilder;
    }

    public String buildEmbeddingText(String title, String content, String keywords) {
        return embeddingTextBuilder.build(title, content, keywords);
    }
}
