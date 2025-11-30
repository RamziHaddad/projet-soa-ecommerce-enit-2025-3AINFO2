package com.ecommerce.feedback.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    /**
     * Valide le token JWT et retourne les claims
     */
    public Claims validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Erreur lors de la validation du token JWT: {}", e.getMessage());
            throw new RuntimeException("Token JWT invalide", e);
        }
    }
    
    /**
     * Extrait l'userId du token JWT
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        // Supposons que l'userId est stocké dans le claim "userId" ou "sub"
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            userIdObj = claims.getSubject();
        }
        
        if (userIdObj == null) {
            throw new RuntimeException("userId non trouvé dans le token JWT");
        }
        
        // Convertir en Long
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        } else if (userIdObj instanceof String) {
            return Long.parseLong((String) userIdObj);
        } else {
            throw new RuntimeException("Format d'userId invalide dans le token JWT");
        }
    }
}

