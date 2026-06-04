package com.quna.rag.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档列表响应。
 */
@Data
public class RagDocumentResponse {
    private Long id;
    private Long kbId;
    private String kbCode;
    private String docName;
    private String docType;
    private String projectCode;
    private String bizModule;
    private String tags;
    private String status;
    private Integer chunkCount;
    private Long fileSize;
    private String fileUrl;
    private String contentHash;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
