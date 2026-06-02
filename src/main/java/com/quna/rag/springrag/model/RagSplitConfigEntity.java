package com.quna.rag.springrag.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * SpringRAG 分段策略配置，对应 rag_split_config 表。
 */
@Data
public class RagSplitConfigEntity {
    private Long id;
    private String collectionCode;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private Integer minLength;
    private Integer markdownSoftMultiplier;
    private Integer markdownHardMultiplier;
    private Integer markdownAtomicHeadingLevel;
    private Integer markdownMaxMergeSections;
    private String separators;
    private Boolean protectMarkdownTable;
    private Boolean protectCodeFence;
    private Boolean protectApiSection;
    private Boolean protectJsonBlock;
    private Boolean protectSqlBlock;
    private LocalDateTime updateTime;
}
