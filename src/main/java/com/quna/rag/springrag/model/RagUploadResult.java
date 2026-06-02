package com.quna.rag.springrag.model;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * 文档上传结果模型，返回文档 ID、去重状态和实际入库切片数量。
 */

@Data
@AllArgsConstructor
public class RagUploadResult {
    private Long docId;
    private String collectionCode;
    private Integer chunkCount;
    private String status;
    private Boolean duplicate;
    private String contentHash;
    private String fileUrl;
}
