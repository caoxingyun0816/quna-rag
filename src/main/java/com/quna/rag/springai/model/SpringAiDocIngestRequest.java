package com.quna.rag.springai.model;

import lombok.Data;

@Data
public class SpringAiDocIngestRequest {

    private String text;
    private String filename;
    private String source;
    private Long userId;
}
