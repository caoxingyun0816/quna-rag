package com.quna.rag.springrag.simple.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 简单 RAG 上传结果，返回文档、切片和文件存储信息。
 */
@Data
@AllArgsConstructor
public class SimpleUploadResult {
    private Long docId;
    private String collectionCode;
    private Integer chunkCount;
    private String status;
    private Boolean duplicate;
    private String contentHash;
    private String fileUrl;
}
