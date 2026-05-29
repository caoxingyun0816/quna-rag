package com.quna.rag.springrag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Spring RAG 功能的集中配置入口，承接 application.yml 中 rag.springrag 前缀的检索、切片和集合参数。
 */
@Data
@Component
@ConfigurationProperties(prefix = "rag.springrag")
public class SpringRagProperties {
    private int vectorTopK = 20;
    private int keywordTopK = 20;
    private int rerankTopK = 5;
    private double minScore = 0.65;
    private Chunk chunk = new Chunk();
    private Collections collections = new Collections();

    @Data
    public static class Chunk {
        private int techSize = 700;
        private int businessSize = 1000;
        private int overlap = 100;
        private int minLength = 80;
        private int markdownSoftMultiplier = 2;
        private int markdownHardMultiplier = 8;
        private int markdownAtomicHeadingLevel = 4;
        private int markdownMaxMergeSections = 3;
        private int embeddingMaxLength = 6000;
    }

    @Data
    public static class Collections {
        private String businessDoc = "rag_business_doc";
        private String techDoc = "rag_tech_doc";
    }
}
