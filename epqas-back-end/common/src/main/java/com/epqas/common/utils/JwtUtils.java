package com.epqas.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtUtils {

    // 应该在配置中，但如果配置服务尚未准备好，则为简单/演示而硬编码
    // HS256 需要至少 32 个字符
    private static final String SECRET = "EpqasSecretKeyForJwtSigningMustBeLongEnough123";
    private static final long EXPIRATION = 86400000L; // 1 天

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * 生成JWT令牌
     * 
     * @param username 用户名
     * @param claims   声明
     * @return JWT令牌
     */
    public String generateToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT令牌
     * 
     * @param token JWT令牌
     * @return 声明
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证JWT令牌
     * 
     * @param token JWT令牌
     * @return 是否验证成功
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /**
     * 获取JWT令牌中的用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }
}
