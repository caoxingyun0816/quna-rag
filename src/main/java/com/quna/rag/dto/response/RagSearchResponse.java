package com.quna.rag.dto.response;

import lombok.Data;

import java.util.List;

/**
 * RAG 检索响应，只返回召回切片，不调用大模型生成答案。
 */
@Data
public class RagSearchResponse {
    private String question;
    private Integer totalCandidates;
    private Integer total;
    private List<RagSearchHitResponse> hits;
}
