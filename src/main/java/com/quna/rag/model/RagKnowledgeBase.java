package com.quna.rag.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * RAG 知识库模型，表示一组可检索、可授权、可管理的企业知识集合。
 */
@Data
public class RagKnowledgeBase {
    private Long id;
    private String kbCode;
    private String kbName;
    private Integer kbType;
    private String description;
    private Integer status;
    private Integer isDeleted;
    private String createUser;
    private String updateUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
