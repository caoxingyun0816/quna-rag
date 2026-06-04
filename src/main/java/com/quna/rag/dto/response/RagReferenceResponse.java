package com.quna.rag.dto.response;

import lombok.Data;

/**
 * RAG 引用来源响应，描述一次命中的文档切片。
 */
@Data
public class RagReferenceResponse {
    private Long docId;
    private Long chunkId;
    private String docName;
    private String title;
    private Double score;
    private Double vectorScore;
    private Double keywordScore;
    private Boolean vectorHit;
    private Boolean keywordHit;
}
