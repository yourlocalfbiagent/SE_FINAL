package com.sefinal.erp.admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:erpsystem-default-secret-key-for-dev-only-must-be-32-chars-2024}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    public String generateToken(int userId, int companyId, String email, Integer roleId, String roleName) {
        return generateToken(userId, companyId, email, roleId, roleName, Collections.emptyList());
    }

    public String generateToken(int userId, int companyId, String email, Integer roleId, String roleName, List<String> permissions) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("companyId", companyId)
                .claim("roleId", roleId)
                .claim("role", roleName)
                .claim("perms", permissions)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
