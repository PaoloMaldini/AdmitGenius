package com.admitgenius.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Base64;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    private SecretKey signingKey;

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            try {
                // 使用配置的密钥生成签名密钥
                byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
                signingKey = Keys.hmacShaKeyFor(keyBytes);
            } catch (Exception e) {
                logger.error("生成JWT签名密钥失败: {}", e.getMessage());
                throw new RuntimeException("无法生成安全的JWT签名密钥", e);
            }
        }
        return signingKey;
    }

    // 生成 JWT
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // 使用邮箱作为 subject
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 从 JWT 中获取用户名 (邮箱)
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // 验证 JWT
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("无效的 JWT 签名: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("无效的 JWT 令牌: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("JWT 令牌已过期: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("不支持的 JWT 令牌: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims 字符串为空: {}", ex.getMessage());
        }
        return false;
    }
}