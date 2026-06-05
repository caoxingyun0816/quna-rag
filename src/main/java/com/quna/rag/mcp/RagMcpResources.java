package com.quna.rag.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quna.rag.service.RagKnowledgeBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 标准 RAG MCP 资源服务。
 */
@Slf4j
@Service
public class RagMcpResources {
    private final RagKnowledgeBaseService knowledgeBaseService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RagMcpResources(RagKnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @McpResource(
            uri = "quna-rag://server/info",
            name = "quna-rag 服务信息",
            description = "当前 quna-rag MCP 服务和后端地址信息",
            mimeType = "application/json"
    )
    public String getServerInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "quna-rag");
        info.put("version", "2.0.0");
        info.put("baseUrl", "http://127.0.0.1:8080");
        info.put("mcpEndpoint", "/mcp");
        info.put("tools", new String[]{"quna_rag_doc_list", "quna_rag_search", "quna_rag_ask"});
        return json(info);
    }

    @McpResource(
            uri = "quna-rag://kb/list",
            name = "RAG 知识库列表",
            description = "列出当前系统可用知识库，供 MCP 客户端选择 kbId",
            mimeType = "application/json"
    )
    public String getKnowledgeBaseList() {
        return json(Map.of("knowledgeBases", knowledgeBaseService.list(null)));
    }

    private String json(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception e) {
            log.error("MCP 资源 JSON 序列化失败", e);
            return "{}";
        }
    }
}
