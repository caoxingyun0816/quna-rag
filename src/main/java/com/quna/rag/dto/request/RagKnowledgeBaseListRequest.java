package com.quna.rag.dto.request;

import lombok.Data;

/**
 * 知识库列表查询请求。
 */
@Data
public class RagKnowledgeBaseListRequest {
    private String kbCode;
    private String kbName;
    private Integer kbType;
    private Integer status;
}
