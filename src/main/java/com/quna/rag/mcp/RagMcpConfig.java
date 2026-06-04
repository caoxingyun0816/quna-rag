package com.quna.rag.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI MCP Server 配置
 * 自动将 @Tool 和 @Resource 注解的方法暴露为 MCP 工具和资源
 */
@Slf4j
@Configuration
public class RagMcpConfig {

    /**
     * 注册 MCP 工具
     * Spring AI 会自动扫描所有 @Tool 注解的方法并注册为 MCP 工具
     */
    @Bean
    public ToolCallbackProvider mcpToolCallbackProvider(RagMcpTools mcpTools) {
        log.info("注册 Spring AI MCP 工具提供者");
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpTools)
                .build();
    }

}
