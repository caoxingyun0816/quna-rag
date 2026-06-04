//package com.quna.rag.mq;
//
//import com.quna.rag.service.RagDocumentService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
///**
// * 文档解析任务消费者。
// * 当前项目 pom 暂不接入 RabbitMQ，所以这里作为标准 MQ 消费者占位，由异步任务直接调用同样的构建逻辑。
// */
//@Slf4j
//@Component
//public class RagDocumentParseConsumer {
//    private final RagDocumentService documentService;
//
//    public RagDocumentParseConsumer(RagDocumentService documentService) {
//        this.documentService = documentService;
//    }
//
//    public void consume(Long docId) {
//        log.info("接收到 RAG 文档解析任务: docId={}", docId);
//        documentService.buildDocument(docId);
//    }
//}
