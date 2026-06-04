package com.quna.rag.mcp.dto;

import com.quna.rag.dto.response.RagDocumentResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MCP 文档列表响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagMcpDocumentListResponse {
    private boolean success;
    private int total;
    private List<RagDocumentResponse> documents;
}
