package com.quna.rag.dto.request;

import lombok.Data;

/**
 * 文档切片列表查询请求。
 */
@Data
public class RagChunkListRequest {
    private Long kbId;
    private Long docId;
    private String title;
    private String keyword;
}
