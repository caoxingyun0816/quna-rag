package com.quna.rag.springrag.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档主表实体，对应 rag_document 表，记录上传文件、归属集合和解析状态。
 */
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
    private String fileUrl;
    private String contentHash;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
