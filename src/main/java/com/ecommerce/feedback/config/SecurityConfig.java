package com.ecommerce.feedback.config;

import com.ecommerce.feedback.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF pour les API REST
            .csrf(csrf -> csrf.disable())
            
            // Configuration de la session (stateless pour JWT)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configuration des autorisations
            .authorizeHttpRequests(auth -> auth
                // Routes racine et erreurs
                .requestMatchers("/", "/error", "/favicon.ico").permitAll()
                
                // Swagger/OpenAPI (optionnel, pour le développement)
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                
                // Actuator endpoints (monitoring Spring Boot)
                .requestMatchers("/actuator/**").permitAll()
                
                // Routes publiques (lecture des notes) - DOIT être avant /api/ratings/**
                .requestMatchers(HttpMethod.GET, "/api/ratings/product/**").permitAll()
                
                // Routes protégées (création/modification/suppression)
                .requestMatchers(HttpMethod.POST, "/api/ratings").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/ratings/**").authenticated()
                
                // Toutes les autres requêtes nécessitent une authentification
                .anyRequest().authenticated()
            )
            
            // Ajouter le filtre JWT avant le filtre d'authentification par défaut
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

