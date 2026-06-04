package com.quna.rag.dto.request;

import lombok.Data;

/**
 * 文档切片列表查询请求。
 */
@Data
public class RagChunkListRequest {
    private Long docId;
}
