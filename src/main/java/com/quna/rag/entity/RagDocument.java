package com.quna.rag.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * RAG 文档模型，记录原始文件、业务归属、解析状态和向量化状态。
 */
@Data
public class RagDocument {
    private Long id;
    private Long kbId;
    private String docCode;
    private String docName;
    private String docType;
    private String bizModule;
    private String projectCode;
    private Integer sourceType;
    private String sourceUrl;
    private String fileUrl;
    private Long fileSize;
    private String fileMd5;
    private Integer parseStatus;
    private Integer vectorStatus;
    private Integer chunkCount;
    private String errorMsg;
    private Integer versionNo;
    private Integer status;
    private Integer isDeleted;
    private String createUser;
    private String updateUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
