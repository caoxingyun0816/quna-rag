package com.quna.rag.springrag.simple.model;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 简单 RAG 向量命中结果，字段直接来自 Milvus 文档 metadata 和 score。
 */
@Data
public class SimpleVectorHit {
    private Long chunkId;
    private Long docId;
    private String collectionCode;
    private String filename;
    private String fileType;
    private String project;
    private String module;
    private String docType;
    private String tags;
    private Integer chunkIndex;
    private String titlePath;
    private String content;
    private Double score;
    private Map<String, Object> metadata = new LinkedHashMap<>();
}
