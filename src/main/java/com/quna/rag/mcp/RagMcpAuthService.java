package com.quna.rag.mcp;

import com.quna.rag.config.OnlineOperator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * MCP 认证服务，负责从当前 HTTP 请求和 Spring Security 上下文解析调用用户。
 */
@Service
public class RagMcpAuthService {
    public static final String PERMISSION_DOC_LIST = "RAG_DOC_LIST";
    public static final String PERMISSION_SEARCH = "RAG_SEARCH";
    public static final String PERMISSION_ASK = "RAG_ASK";

    public RagMcpUser currentUser() {
        RagMcpUser securityUser = fromSecurityContext();
        if (securityUser != null) {
            return securityUser;
        }
        HttpServletRequest request = currentRequest();
        if (request != null && "test".equals(request.getHeader("QZ-Access-User"))) {
            return new RagMcpUser("1593", "test", "admin", true);
        }
        throw new IllegalStateException("MCP 未认证：请在请求头中携带有效登录态，开发环境可使用 QZ-Access-User=test");
    }

    public void requireToolPermission(String permissionCode) {
        RagMcpUser user = currentUser();
        if (user.isTestUser()) {
            return;
        }
        if ("admin".equalsIgnoreCase(user.getRole())) {
            return;
        }
        if (PERMISSION_DOC_LIST.equals(permissionCode)
                || PERMISSION_SEARCH.equals(permissionCode)
                || PERMISSION_ASK.equals(permissionCode)) {
            return;
        }
        throw new IllegalStateException("MCP 工具无权限: " + permissionCode);
    }

    private RagMcpUser fromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OnlineOperator operator) {
            String userId = StringUtils.hasText(operator.getOperId()) ? operator.getOperId() : operator.getAccessId();
            String role = StringUtils.hasText(operator.getRoles()) ? operator.getRoles() : "user";
            return new RagMcpUser(userId, operator.getUsername(), role, "test".equals(operator.getSessionToken()));
        }
        if (principal instanceof String username && StringUtils.hasText(username) && !"anonymousUser".equals(username)) {
            return new RagMcpUser(username, username, "user", false);
        }
        return null;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
