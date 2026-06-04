package com.quna.rag.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档上传响应，上传成功后先返回构建中状态，解析和向量化异步完成。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagDocumentUploadResponse {
    private Long documentId;
    private Long kbId;
    private String kbCode;
    private String status;
    private Integer chunkCount;
    private Boolean duplicate;
    private String contentHash;
    private String fileUrl;
}
