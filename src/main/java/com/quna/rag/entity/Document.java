package com.quna.rag.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Document {
    private Long id;
    private String filename;
    private Long userId;
    private String source;
    private String status;
    private String fileType;
    private Long fileSize;
    private Integer chunkCount;
    private String permissionScope;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
