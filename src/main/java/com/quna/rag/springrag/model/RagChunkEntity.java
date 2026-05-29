package com.quna.rag.springrag.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RagChunkEntity {
    private Long id;
    private Long docId;
    private String collectionCode;
    private Integer chunkIndex;
    private String titlePath;
    private String content;
    private String contentHash;
    private String keywords;
    private Double keywordScore;
    private String filename;
    private String fileType;
    private String project;
    private String module;
    private String docType;
    private String tags;
    private LocalDateTime createTime;
}
