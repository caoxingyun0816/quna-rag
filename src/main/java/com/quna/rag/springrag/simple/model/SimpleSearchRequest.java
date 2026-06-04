package com.quna.rag.springrag.simple.model;

import lombok.Data;

/**
 * 简单 RAG 向量查询请求，只保留集合、问题、过滤条件和 TopK 参数。
 */
@Data
public class SimpleSearchRequest {
    private String collectionCode;
    private String question;
    private String project;
    private String module;
    private String docType;
    private Integer topK;
    private Double minScore;
}
