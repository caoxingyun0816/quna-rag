package com.quna.rag.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 向量化任务消费者占位。
 * 当前版本解析、切片、向量化在同一个异步构建流程中完成，后续接入 MQ 后可拆成独立消费者。
 */
@Slf4j
@Component
public class RagEmbeddingConsumer {
    public void consume(Long docId) {
        log.info("接收到 RAG 向量化任务: docId={}", docId);
    }
}
