package com.quna.rag.mcp.springai;

import com.quna.rag.entity.Document;
import com.quna.rag.service.DocService;
import com.quna.rag.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 Spring AI 的 MCP 工具服务
 * 使用 @Tool 注解自动暴露为 MCP 工具
 */
@Slf4j
@Service
public class SpringAiMcpTools {

    private final UserService userService;
    private final DocService docService;

    public SpringAiMcpTools(UserService userService, DocService docService) {
        this.userService = userService;
        this.docService = docService;
    }

    /**
     * 登录工具
     */
    @Tool(description = "登录趣拿 RAG 平台并获取 JWT token")
    public Map<String, Object> qunaLogin(
            @ToolParam(description = "平台账号") String username,
            @ToolParam(description = "平台密码") String password) {

        log.info("MCP 工具调用: qunaLogin, username={}", username);

        Map<String, Object> result = userService.login(username, password);

        if (Integer.valueOf(200).equals(result.get("code"))) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", result.get("token"));
            return response;
        } else {
            throw new RuntimeException(String.valueOf(result.get("msg")));
        }
    }

    /**
     * 文档列表查询工具
     */
    @Tool(description = "查询当前用户的文档列表，支持按关键词、来源、状态筛选")
    public Map<String, Object> qunaDocList(
            @ToolParam(description = "文档名称关键词", required = false) String keyword,
            @ToolParam(description = "来源，例如：本地上传、钉钉", required = false) String source,
            @ToolParam(description = "状态，例如：已入库、停用", required = false) String status) {

        log.info("MCP 工具调用: qunaDocList, keyword={}, source={}, status={}", keyword, source, status);

        // 注意：实际使用时需要从 SecurityContext 获取当前用户ID
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("未登录：请先调用 qunaLogin 获取 token");
        }

        List<Document> docs = docService.list(userId, keyword, source, status);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("total", docs.size());
        response.put("documents", docs);

        return response;
    }

    /**
     * 知识库检索工具
     */
    @Tool(description = "执行知识库检索测试。默认只返回命中片段，需要时可打开 includeAnswer 生成回答")
    public Map<String, Object> qunaDocSearch(
            @ToolParam(description = "检索问题") String question,
            @ToolParam(description = "返回命中数量，默认 3", required = false) Integer topK,
            @ToolParam(description = "是否额外调用大模型生成回答，默认 false", required = false) Boolean includeAnswer) {

        log.info("MCP 工具调用: qunaDocSearch, question={}, topK={}, includeAnswer={}",
                question, topK, includeAnswer);

        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("未登录：请先调用 qunaLogin 获取 token");
        }

        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("检索问题不能为空");
        }

        Map<String, Object> searchArgs = new HashMap<>();
        searchArgs.put("question", question);
        searchArgs.put("topK", topK != null ? topK : 3);
        searchArgs.put("includeAnswer", includeAnswer != null && includeAnswer);

        Map<String, Object> result = docService.searchTest(userId, searchArgs);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.putAll(result);

        return response;
    }

    /**
     * 获取当前用户ID
     * TODO: 从 Spring Security Context 或 ThreadLocal 中获取
     */
    private Long getCurrentUserId() {
        // 这里需要结合实际的认证机制实现
        // 可以通过 SecurityContextHolder 或自定义的 UserContext 获取
        return 1L;
    }
}
