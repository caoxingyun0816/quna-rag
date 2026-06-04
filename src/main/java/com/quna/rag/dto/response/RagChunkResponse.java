package com.quna.rag.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档切片响应。
 */
@Data
public class RagChunkResponse {
    private Long id;
    private Long docId;
    private Long kbId;
    private String kbCode;
    private Integer chunkIndex;
    private String title;
    private String content;
    private String keywords;
    private String contentHash;
    private LocalDateTime createTime;
}
