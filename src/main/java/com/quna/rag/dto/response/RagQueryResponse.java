package com.quna.rag.dto.response;

import lombok.Data;

import java.util.List;

/**
 * RAG 问答响应，包含答案和引用来源。
 */
@Data
public class RagQueryResponse {
    private String question;
    private String answer;
    private Integer totalCandidates;
    private Integer total;
    private List<RagReferenceResponse> references;
}
