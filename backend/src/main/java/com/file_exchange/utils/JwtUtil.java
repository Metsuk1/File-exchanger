package com.file_exchange.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

public class JwtUtil {
    private static final SecretKey SECRET_KEY = initSecretKey();
    private static final long EXPIRATION_TIME = 86400000L; // 24 hours

    private static SecretKey initSecretKey() {
        String key = System.getenv("JWT_SECRET");
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException("SECURITY ERROR: JWT_SECRET environment variable not found!");
        }
        if (key.length() < 32) {
            throw new IllegalArgumentException("JWT_SECRET is too short! Minimum 32 characters for HS256.");
        }
        return Keys.hmacShaKeyFor(key.trim().getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .id(UUID.randomUUID().toString())
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new IllegalArgumentException("JWT token has expired", e);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new IllegalArgumentException("Invalid JWT signature", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }
}
