package com.quna.rag.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class QunaSsoAuthenticationFilter extends OncePerRequestFilter {
    private static final String ONLINE_OPER_PREFIX = "cms:online:oper";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RedisTemplate<String, Object> redisTemplate;
    private final QunaSsoJwtProperties jwtProperties;
    private final QunaSsoJwtHelper jwtHelper;

    @Value("${spring.profiles.active:dev}")
    private String env;

    public QunaSsoAuthenticationFilter(RedisTemplate<String, Object> redisTemplate,
                                       QunaSsoJwtProperties jwtProperties,
                                       QunaSsoJwtHelper jwtHelper) {
        this.redisTemplate = redisTemplate;
        this.jwtProperties = jwtProperties;
        this.jwtHelper = jwtHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            authenticate(request);
        } catch (Exception e) {
            log.warn("[QUNA SSO] 请求认证失败: {}, uri={}", e.getMessage(), request.getRequestURI());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request) throws IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        if (authenticateActuatorBasic(request)) {
            return;
        }
        String app = request.getHeader("app");
        if ("Tm60z4".equals(app) && authenticateGatewayOperator(request)) {
            return;
        }
        authenticateSessionHeaders(request);
    }

    private boolean authenticateActuatorBasic(HttpServletRequest request) {
        if (!request.getRequestURI().startsWith("/actuator/")) {
            return false;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Basic ")) {
            return false;
        }
        String credentials = new String(Base64.getDecoder().decode(auth.substring("Basic ".length())));
        String[] parts = credentials.split(":", 2);
        if (parts.length != 2 || !"admin".equals(parts[0]) || !"bc4pV7".equals(parts[1])) {
            return false;
        }
        setAuthentication(request, testOperator());
        return true;
    }

    private boolean authenticateGatewayOperator(HttpServletRequest request) throws IOException {
        String operator = request.getHeader(jwtProperties.getAccessUser());
        if (!StringUtils.hasText(operator)) {
            return false;
        }
        if (!isProduct() && "test".equals(operator)) {
            setAuthentication(request, testOperator());
            return true;
        }
        OnlineOperator onlineOperator = readOnlineOperator(cacheKey(operator));
        if (onlineOperator == null) {
            log.warn("[QUNA SSO] Redis 登录态不存在: {}", operator);
            return false;
        }
        setAuthentication(request, onlineOperator);
        return true;
    }

    private boolean authenticateSessionHeaders(HttpServletRequest request) throws IOException {
        String accessUser = headerOrParam(request, jwtProperties.getAccessUser(), "accessUser");
        String accessSign = headerOrParam(request, jwtProperties.getAccessSign(), "accessSign");
        String sessionTokenHeader = headerOrParam(request, jwtProperties.getSessionToken(), "sessionToken");

        if (!isProduct() && "test".equals(accessUser) && "test".equals(accessSign) && "test".equals(sessionTokenHeader)) {
            setAuthentication(request, testOperator());
            return true;
        }
        if (!StringUtils.hasText(accessUser) || !StringUtils.hasText(accessSign)
                || !StringUtils.hasText(sessionTokenHeader)
                || !sessionTokenHeader.startsWith(jwtProperties.getTokenHead())) {
            return false;
        }

        String sessionTokenId = sessionTokenId(accessSign, remoteClientAgent(request));
        String redisKey = cacheKey(accessUser, sessionTokenId);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            log.warn("[QUNA SSO] 用户缓存登录信息不存在: {}", accessUser);
            return false;
        }

        OnlineOperator onlineOperator = readOnlineOperator(redisKey);
        if (onlineOperator == null) {
            return false;
        }
        if (!sessionTokenHeader.equals(jwtProperties.getTokenHead() + " " + onlineOperator.getPassword())) {
            log.warn("[QUNA SSO] 用户 SessionToken 信息不一致: {}", accessUser);
            return false;
        }
        if (!jwtHelper.validateToken(onlineOperator.getSessionToken(), onlineOperator)) {
            log.warn("[QUNA SSO] 用户 SessionToken 过期: {}", accessUser);
            return false;
        }
        refreshSessionIfNecessary(accessUser, sessionTokenId, onlineOperator);
        setAuthentication(request, onlineOperator);
        return true;
    }

    private void refreshSessionIfNecessary(String accessUser, String sessionTokenId, OnlineOperator onlineOperator) {
        Claims claims = jwtHelper.getClaimsFromToken(onlineOperator.getSessionToken());
        if (claims == null || claims.getExpiration() == null || jwtProperties.getExpiration() == null) {
            return;
        }
        long leftSeconds = Math.max((claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000L, 0);
        if (leftSeconds <= jwtProperties.getExpiration() / 2L) {
            onlineOperator.setSessionToken(jwtHelper.refreshToken(claims));
            redisTemplate.opsForValue().set(cacheKey(accessUser, sessionTokenId), onlineOperator);
            log.info("[QUNA SSO] session 有效期不足 50%，自动刷新: {}", accessUser);
        }
    }

    private OnlineOperator readOnlineOperator(String redisKey) throws IOException {
        Object value = redisTemplate.opsForValue().get(redisKey);
        if (value == null) {
            return null;
        }
        if (value instanceof OnlineOperator onlineOperator) {
            return onlineOperator;
        }
        String json = value instanceof String ? (String) value : OBJECT_MAPPER.writeValueAsString(value);
        if (!StringUtils.hasText(json)) {
            return null;
        }
        return OBJECT_MAPPER.readValue(json, OnlineOperator.class);
    }

    private void setAuthentication(HttpServletRequest request, OnlineOperator onlineOperator) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                onlineOperator, onlineOperator.getSessionToken(), onlineOperator.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute("userId", 1L);
        request.setAttribute("username", onlineOperator.getUsername());
        log.info("[QUNA SSO] 认证成功: {}", onlineOperator.getUsername());
    }

    private Long resolveUserId(OnlineOperator onlineOperator) {
        if (StringUtils.hasText(onlineOperator.getOperId())) {
            try {
                return Long.parseLong(onlineOperator.getOperId());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0L;
    }

    private OnlineOperator testOperator() {
        OnlineOperator onlineOperator = new OnlineOperator();
        onlineOperator.setOperId("1593");
        onlineOperator.setOperator("admin");
        onlineOperator.setRealname("超级管理员");
        onlineOperator.setSessionToken("test");
        onlineOperator.setPassword("test");
        onlineOperator.setAccessId("test");
        onlineOperator.setLoginTime(new Date());
        return onlineOperator;
    }

    private String sessionTokenId(String accessSign, String client) {
        String sessionStr = accessSign + client + QunaSsoJwtProperties.JWT_SESSION_KEY;
        return DigestUtils.md5DigestAsHex(sessionStr.getBytes(StandardCharsets.UTF_8));
    }

    private String remoteClientAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("user-agent");
        if (StringUtils.hasText(userAgent)) {
            return DigestUtils.md5DigestAsHex(userAgent.getBytes(StandardCharsets.UTF_8));
        }
        return DigestUtils.md5DigestAsHex("QUNA".getBytes(StandardCharsets.UTF_8));
    }

    private String headerOrParam(HttpServletRequest request, String headerName, String paramName) {
        String value = request.getHeader(headerName);
        if (StringUtils.hasText(value)) {
            return value;
        }
        return request.getParameter(paramName);
    }

    private String cacheKey(String... values) {
        StringBuilder key = new StringBuilder(ONLINE_OPER_PREFIX);
        for (String value : values) {
            key.append(':').append(value);
        }
        return key.toString();
    }

    private boolean isProduct() {
        return "prod".equals(env) || "preprod".equals(env);
    }
}
