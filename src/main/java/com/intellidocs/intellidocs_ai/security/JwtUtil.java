package com.intellidocs.intellidocs_ai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {

        // HS512 needs minimum 64 bytes
        /**byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 64) {
            keyBytes = java.util.Arrays.copyOf(keyBytes, 64);
        }**/
        // Keys.hmacShaKeyFor auto-picks the right algorithm based on key size
        // 64 bytes = 512 bits → JJWT automatically uses HS512
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String generateToken(String userId, String tenantId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("tenantId", tenantId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)   // no algorithm needed — JJWT infers from key size
                .compact();
    }

    public Claims validateAndExtract(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(Claims claims) {
        return claims.getSubject();
    }

    public String extractTenantId(Claims claims) {
        return claims.get("tenantId", String.class);
    }

    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }
}