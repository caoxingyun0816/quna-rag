package com.quna.rag.mcp.springai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quna.rag.service.DocService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于 Spring AI 的 MCP 资源服务
 * 使用 @Resource 注解暴露资源
 */
@Slf4j
@Service
public class SpringAiMcpResources {

    private final DocService docService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpringAiMcpResources(DocService docService) {
        this.docService = docService;
    }

    /**
     * 服务器信息资源
     */
    @McpResource(
            uri = "quna-rag://server/info",
            name = "quna-rag 服务信息",
            description = "当前 quna-rag MCP 服务和后端地址信息",
            mimeType = "application/json"
    )
    public String getServerInfo() {
        log.info("读取 MCP 资源: quna-rag://server/info");

        Map<String, Object> info = new HashMap<>();
        info.put("name", "quna-rag");
        info.put("version", "1.0.0");
        info.put("baseUrl", "http://127.0.0.1:8080");
        info.put("protocol", "MCP");
        info.put("framework", "Spring AI");

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(info);
        } catch (Exception e) {
            log.error("JSON 序列化失败", e);
            return "{}";
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从 Spring Security Context 或 ThreadLocal 中获取
        return 1L;
    }
}
