package com.file_exchange.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = getSecretKey();
    private static final long EXPIRATION_TIME = 86400000L; // 24 hours

    private static String getSecretKey() {
        String key = System.getenv("JWT_SECRET");
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException(
                    "SECURITY ERROR: JWT_SECRET not found! "
            );
        }
        if (key.length() < 32) {
            throw new IllegalArgumentException("JWT_SECRET is too short! Minimum 32 characters for HS256.");
        }
        return key.trim();
    }

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
