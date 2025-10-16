package com.file_exchange.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY =
            System.getenv("JWT_SECRET") != null
                    ? System.getenv("JWT_SECRET")
                    : "super-secret-key-for-dev-mode-123456789012345678901234567890";
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    public static String generateToken(Long userId) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or expired JWT token", e);
        }
    }
}
