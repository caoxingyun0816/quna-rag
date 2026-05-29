package com.quna.rag.springrag.model;

import lombok.Data;

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
