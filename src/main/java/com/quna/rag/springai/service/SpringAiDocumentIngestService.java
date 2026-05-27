package com.quna.rag.springai.service;

import com.quna.rag.springai.model.SpringAiDocIngestRequest;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SpringAiDocumentIngestService {

    private final VectorStore vectorStore;
    private final int defaultChunkSize;

    public SpringAiDocumentIngestService(@Qualifier("springAiVectorStore") VectorStore vectorStore,
                                         @Value("${rag.spring-ai.chunk-size:500}") int defaultChunkSize) {
        this.vectorStore = vectorStore;
        this.defaultChunkSize = defaultChunkSize;
    }

    public Map<String, Object> ingestText(SpringAiDocIngestRequest request) {
        String text = request.getText() == null ? "" : request.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("text 不能为空");
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("filename", valueOrDefault(request.getFilename(), "spring-ai-text"));
        metadata.put("source", valueOrDefault(request.getSource(), "Spring AI测试"));
        metadata.put("userId", request.getUserId() == null ? 0L : request.getUserId());
        metadata.put("status", "已入库");

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(Math.max(100, defaultChunkSize))
                .withMinChunkSizeChars(100)
                .withMinChunkLengthToEmbed(50)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();

        AtomicInteger index = new AtomicInteger(0);
        List<Document> chunks = splitter.split(new Document(text, metadata)).stream()
                .map(doc -> {
                    Map<String, Object> chunkMetadata = new LinkedHashMap<>(doc.getMetadata());
                    chunkMetadata.put("chunkIndex", index.incrementAndGet());
                    return new Document(doc.getText(), chunkMetadata);
                })
                .toList();

        vectorStore.add(chunks);

        return Map.of(
                "filename", metadata.get("filename"),
                "source", metadata.get("source"),
                "chunkCount", chunks.size()
        );
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
