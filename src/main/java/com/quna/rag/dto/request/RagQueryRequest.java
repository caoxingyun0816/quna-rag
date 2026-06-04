package com.quna.rag.dto.request;

import lombok.Data;

/**
 * RAG 检索与问答请求。
 */
@Data
public class RagQueryRequest {
    private Long kbId;
    private String question;
    private String projectCode;
    private String bizModule;
    private String docType;
    private Integer topK;
    private Integer vectorTopK;
    private Integer keywordTopK;
    private Boolean enableRewrite = false;
    private Boolean enableRerank = true;
    private Boolean includeSources = true;
    private Double minScore;
}
