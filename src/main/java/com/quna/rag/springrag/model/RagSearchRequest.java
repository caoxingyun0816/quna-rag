package com.quna.rag.springrag.model;

import lombok.Data;
/**
 * 检索请求模型，包含问题、集合、项目模块过滤条件以及 TopK 等可调参数。
 */

@Data
public class RagSearchRequest {
    private String question;
    private String collectionCode;
    private String project;
    private String module;
    private String docType;
    private Integer vectorTopK;
    private Integer keywordTopK;
    private Integer rerankTopK;
    private Double minScore;
}
