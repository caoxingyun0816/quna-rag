package com.quna.rag.dto.request;

import lombok.Data;

/**
 * 知识库创建请求。当前版本先保留接口 DTO，底层仍使用内置 business_doc/tech_doc 两个知识库。
 */
@Data
public class RagKnowledgeBaseCreateRequest {
    private String kbCode;
    private String kbName;
    private Integer kbType;
    private String description;
}
