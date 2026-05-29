package com.quna.rag.springrag.model;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

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
