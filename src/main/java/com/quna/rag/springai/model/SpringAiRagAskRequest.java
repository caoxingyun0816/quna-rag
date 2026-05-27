package com.quna.rag.springai.model;

import lombok.Data;

@Data
public class SpringAiRagAskRequest {

    private String question;
    private Integer topK;
    private Double similarityThreshold;
}
