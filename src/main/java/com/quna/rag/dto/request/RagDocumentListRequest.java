package com.quna.rag.dto.request;

import lombok.Data;

/**
 * 文档列表查询请求，按知识库、项目、模块和文档类型过滤。
 */
@Data
public class RagDocumentListRequest {
    private Long kbId;
    private String projectCode;
    private String bizModule;
    private String docType;
}
