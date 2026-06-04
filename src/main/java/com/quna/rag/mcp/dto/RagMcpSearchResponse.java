package com.quna.rag.mcp.dto;

import com.quna.rag.dto.response.RagSearchHitResponse;
import lombok.Data;

import java.util.List;

/**
 * MCP 检索响应。
 */
@Data
public class RagMcpSearchResponse {
    private boolean success;
    private String question;
    private Integer totalCandidates;
    private Integer total;
    private List<RagSearchHitResponse> hits;
}
