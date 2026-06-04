package com.quna.rag.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP 当前调用用户。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagMcpUser {
    private String userId;
    private String username;
    private String role;
    private boolean testUser;
}
