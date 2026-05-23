package com.quna.rag.config;

import com.alibaba.fastjson.JSON;
import com.quna.rag.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行登录接口、静态资源
        String uri = request.getRequestURI();
        log.info("JWT 拦截器 - 请求 URI: {}", uri);
        
        if (uri.contains("/api/user/login") || uri.contains("/index.html") || uri.contains("/favicon.ico") || uri.contains("/api/user/register")) {
            log.info("JWT 拦截器 - 放行公开接口: {}", uri);
            return true;
        }

        // 2. 获取 Token（优先从 Header，其次从 Query 参数）
        String token = extractToken(request);
        log.info("JWT 拦截器 - 提取的 Token: {}", token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");
        
        // 3. 校验 Token 是否合法
        if (token == null || token.isEmpty()) {
            log.warn("JWT 拦截器 - Token 为空，拒绝访问");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(JSON.toJSONString(Map.of("code", 403, "msg", "无访问权限，请先登录")));
            return false;
        }

        // 4. 校验 Token 有效性
        try {
            Claims claims = jwtUtil.parseToken(token);
            request.setAttribute("userId", claims.get("userId"));
            log.info("JWT 拦截器 - Token 验证成功，userId: {}", claims.get("userId"));
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT 拦截器 - Token 已过期，过期时间: {}", e.getClaims().getExpiration());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(JSON.toJSONString(Map.of("code", 401, "msg", "Token已过期，请重新登录")));
            return false;
        } catch (Exception e) {
            log.error("JWT 拦截器 - Token 验证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(JSON.toJSONString(Map.of("code", 403, "msg", "Token无效，请重新登录")));
            return false;
        }
    }

    /**
     * 从请求中提取 Token
     * 优先从 Authorization Header 获取，其次从 URL 参数获取（用于 SSE）
     */
    private String extractToken(HttpServletRequest request) {
        // 方式1: 从 Header 获取
        String authHeader = request.getHeader("Authorization");
        log.debug("JWT 拦截器 - Authorization Header: {}", authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + "..." : "null");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 方式2: 从 Query 参数获取（SSE EventSource 不支持自定义 Header）
        String tokenParam = request.getParameter("token");
        log.debug("JWT 拦截器 - Token 参数: {}", tokenParam != null ? tokenParam.substring(0, Math.min(20, tokenParam.length())) + "..." : "null");
        
        if (StringUtils.isNotBlank(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }
}