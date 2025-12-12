package com.cshp.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret:a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3h4i5j6}")
    private String secret;
    
    public JwtUtil() {
        // 构造函数保持简单，密钥将在getSigningKey方法中基于secret生成
    }
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private SecretKey getSigningKey() {
        // 使用配置的secret字符串生成密钥，确保每次重启应用都使用相同的密钥
        // 确保密钥长度满足HS512要求（至少64字节）
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 64) {
            // 如果密钥太短，进行填充
            byte[] paddedBytes = new byte[64];
            System.arraycopy(keyBytes, 0, paddedBytes, 0, keyBytes.length);
            return Keys.hmacShaKeyFor(paddedBytes);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String studentId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("studentId", studentId);
        return createToken(claims, studentId);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getStudentIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        
        // 优先从claims中获取studentId字段
        Object studentIdObj = claims.get("studentId");
        if (studentIdObj != null) {
            return studentIdObj.toString();
        }
        
        // 如果claims中没有，再从subject中获取
        return claims.getSubject();
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.err.println("Token解析失败: " + e.getMessage());
            return null;
        }
    }

    public Boolean validateToken(String token, String studentId) {
        if (token == null) {
            return false;
        }
        
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return false;
        }
        
        // 检查token是否过期
        if (isTokenExpired(token)) {
            return false;
        }
        
        // 如果提供了studentId，则验证studentId匹配
        if (studentId != null && !studentId.isEmpty()) {
            String tokenStudentId = getStudentIdFromToken(token);
            return studentId.equals(tokenStudentId);
        }
        
        // 如果没有提供studentId，只验证token格式和是否过期
        return true;
    }

    private Boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return true;
            }
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            System.err.println("Token过期检查异常: " + e.getMessage());
            return true;
        }
    }
}

