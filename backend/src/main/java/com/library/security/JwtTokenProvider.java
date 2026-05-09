package com.library.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            log.error("JWT密钥未配置！请设置环境变量 JWT_SECRET，至少64个字符的强密钥");
            throw new IllegalStateException("JWT密钥未配置！请设置环境变量 JWT_SECRET");
        }
        if (jwtSecret.length() < 32) {
            log.error("JWT密钥长度不足！当前{}字符，至少需要32字符", jwtSecret.length());
            throw new IllegalStateException("JWT密钥长度不足！至少需要32字符");
        }
        log.info("JWT配置验证通过，密钥长度: {}字符", jwtSecret.length());
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT已过期: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("JWT格式错误: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("JWT签名验证失败: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT参数非法: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
