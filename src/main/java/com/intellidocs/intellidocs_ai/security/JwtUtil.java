package com.intellidocs.intellidocs_ai.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

//creates tokens and validates them
@Component
public class JwtUtil {

    private final SecretKey key;
    private  final long expirationMs;

    // Values injected from application.yml
    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs){
        // HMAC-SHA256 key derived from your secret string
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs  = expirationMs;
    }

    //Called at login - creates a signed JWT
    public  String generateToken(String userId, String tenantId, String role){
        return Jwts.builder()
                .subject(userId) // user ID in the "sub" claim
                .claim("tenantId", tenantId) // custom claim for tenant ID
                .claim("role", role) // custom claim for user role
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // sign with HMAC-SHA256
                .signWith(key)
                .compact();
    }

    //Called on every request - validates Signature + expiry
    public Claims validateAndExtract(String token){
        //Throws JwtException if signature invalid or token expired
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //Convenience method to extract user ID from claims
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
