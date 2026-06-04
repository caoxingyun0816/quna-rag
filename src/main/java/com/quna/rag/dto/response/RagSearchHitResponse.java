package com.quna.rag.dto.response;

import lombok.Data;

/**
 * RAG 检索命中响应。
 */
@Data
public class RagSearchHitResponse {
    private Long docId;
    private Long chunkId;
    private String docName;
    private String docType;
    private String projectCode;
    private String bizModule;
    private String tags;
    private Integer chunkIndex;
    private String title;
    private String content;
    private Double score;
    private Double vectorScore;
    private Double keywordScore;
    private Boolean vectorHit;
    private Boolean keywordHit;
}
