package com.ecommerce.payment.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm; // Optionnel selon la version
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date; // <--- L'IMPORT MANQUANT

@Component
public class JwtUtils {
    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private final String jwtSecret = "votreSecretTresLongEtSecuriseDePlusDe32Caracteres!";

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (Exception e) {
            log.error("JWT invalide : {}", e.getMessage());
        }
        return false;
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @PostConstruct
    public void generateTestToken() {
        try {
            String testToken = Jwts.builder()
                    .setSubject("ghofrane")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date((new Date()).getTime() + 86400000)) // 24h
                    .signWith(getSigningKey()) // Utilise la clé directement
                    .compact();
            
            System.out.println("=======================================");
            System.out.println("TON JETON DE TEST VALIDE :");
            System.out.println(testToken);
            System.out.println("=======================================");
        } catch (Exception e) {
            log.error("Erreur lors de la génération du token de test : {}", e.getMessage());
        }
    }
}