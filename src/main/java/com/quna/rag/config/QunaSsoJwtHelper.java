package com.quna.rag.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class QunaSsoJwtHelper {
    private final QunaSsoJwtProperties properties;

    public QunaSsoJwtHelper(QunaSsoJwtProperties properties) {
        this.properties = properties;
    }

    public boolean validateToken(String token, OnlineOperator userDetails) {
        try {
            Claims claims = getClaims(token);
            return userDetails != null && userDetails.getUsername() != null
                    && userDetails.getUsername().equals(claims.getSubject());
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return getClaims(token);
        } catch (Exception e) {
            log.warn("[QUNA SSO] JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    public String refreshToken(Claims claims) {
        claims.put("created", new Date());
        return Jwts.builder()
                .setClaims(claims)
                .setId(claims.getId())
                .setExpiration(new Date(System.currentTimeMillis() + properties.getExpiration() * 1000L))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, properties.getSecret())
                .compact();
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(properties.getSecret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("[QUNA SSO] JWT 已过期");
            throw e;
        } catch (SignatureException e) {
            log.warn("[QUNA SSO] JWT 签名错误");
            throw e;
        }
    }
}
