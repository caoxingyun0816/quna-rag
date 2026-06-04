package com.quna.rag.dto.request;

import lombok.Data;

/**
 * 文档上传请求参数，对应 multipart/form-data 中除 file 外的业务字段。
 */
@Data
public class RagDocumentUploadRequest {
    private Long kbId;
    private String bizModule;
    private String projectCode;
    private String tags;
    private String remark;
}
