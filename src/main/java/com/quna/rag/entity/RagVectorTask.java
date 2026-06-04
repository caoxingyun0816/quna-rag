package com.quna.rag.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * RAG 向量任务模型，预留给 MQ 或任务表，当前版本通过本地异步任务执行。
 */
@Data
public class RagVectorTask {
    private Long id;
    private Long kbId;
    private Long docId;
    private String taskType;
    private Integer status;
    private String errorMsg;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
