package com.quna.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt.config")
public class QunaSsoJwtProperties {
    public static final String JWT_SESSION_KEY = "dnjkwu3dje";

    private String accessUser = "QZ-Access-User";
    private String accessSign = "QZ-Access-Sign";
    private String sessionToken = "QZ-Session-Token";
    private String tokenHead = "C2";
    private String secret = "rag-enterprise-jwt-secret-2026";
    private Integer expiration = 86400;
    private Long accessExpiration = 7200L;
    private Long refreshExpiration = 604800L;
}
