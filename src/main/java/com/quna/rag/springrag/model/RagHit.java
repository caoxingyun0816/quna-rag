package com.quna.rag.springrag.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;
/**
 * 检索命中结果模型，承载向量检索、关键词检索和重排后的统一返回结构。
 */

@Data
public class RagHit {
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
    private double vectorScore;
    private double keywordScore;
    private double score;
    private boolean vectorHit;
    private boolean keywordHit;
    @JsonIgnore
    private Map<String, Object> metadata = new LinkedHashMap<>();
}
