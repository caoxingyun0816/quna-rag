package com.quna.rag.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * RAG 文档切片模型，保存可关键词检索的原文切片，并通过 vectorId 对齐向量库记录。
 */
@Data
public class RagDocumentChunk {
    private Long id;
    private Long kbId;
    private Long docId;
    private String chunkCode;
    private Integer chunkIndex;
    private String title;
    private String content;
    private String summary;
    private Integer tokenCount;
    private String vectorId;
    private String contentHash;
    private String metadata;
    private String keywords;
    private Integer status;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
