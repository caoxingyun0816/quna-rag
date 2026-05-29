package com.quna.rag.springrag.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RagDocumentEntity {
    private Long id;
    private String collectionCode;
    private String filename;
    private String fileType;
    private String source;
    private String project;
    private String module;
    private String docType;
    private String tags;
    private String permissionScope;
    private String visibleRoles;
    private String status;
    private Integer chunkCount;
    private Long fileSize;
    private String contentHash;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
