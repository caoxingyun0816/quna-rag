package com.quna.rag.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * RAG 查询日志模型，用于记录用户问题、命中切片、模型回答和耗时。
 */
@Data
public class RagQueryLog {
    private Long id;
    private String userId;
    private String userName;
    private Long kbId;
    private String question;
    private String rewrittenQuestion;
    private String answer;
    private String hitChunkIds;
    private String modelName;
    private String embeddingModel;
    private Long costTimeMs;
    private Integer success;
    private String errorMsg;
    private LocalDateTime createTime;
}
