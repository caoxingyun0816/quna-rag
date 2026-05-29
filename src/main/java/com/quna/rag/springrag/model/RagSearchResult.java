package com.quna.rag.springrag.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG 检索接口返回模型，替代 Map 结构，方便前端和调用方明确字段含义。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagSearchResult {
    private String question;
    private Integer totalCandidates;
    private Integer total;
    private List<RagHit> hits;
}
