package com.quna.rag.springrag.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG 问答接口返回模型，明确返回问题、答案和可选来源列表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagAskResult {
    private String question;
    private String answer;
    private List<RagHit> sources;
}
