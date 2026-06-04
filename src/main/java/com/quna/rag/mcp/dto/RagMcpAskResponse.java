package com.quna.rag.mcp.dto;

import com.quna.rag.dto.response.RagReferenceResponse;
import lombok.Data;

import java.util.List;

/**
 * MCP 问答响应。
 */
@Data
public class RagMcpAskResponse {
    private boolean success;
    private String question;
    private String answer;
    private Integer totalCandidates;
    private Integer total;
    private List<RagReferenceResponse> references;
}
