package com.quna.rag.springrag.model;

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
    private String titlePath;
    private String content;
    private double vectorScore;
    private double keywordScore;
    private double score;
    private boolean vectorHit;
    private boolean keywordHit;
    private Map<String, Object> metadata = new LinkedHashMap<>();
}
