package com.quna.rag.springai.model;

import lombok.Data;

@Data
public class SpringAiRagSearchRequest {

    private String question;
    private Integer topK;
    private Double similarityThreshold;
}
