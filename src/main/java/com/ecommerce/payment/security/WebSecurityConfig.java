package com.ecommerce.payment.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Déclaration du filtre JWT en tant que Bean pour qu'il soit injecté 
     * dans la chaîne de sécurité de Spring.
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactive le CSRF car nous sommes en architecture Stateless (sans état) avec JWT
            .csrf(csrf -> csrf.disable())
            
            // Définit la gestion des sessions sur STATELESS (pas de stockage de session sur le serveur)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                // Autorise l'accès public au Health Check pour le monitoring
                .requestMatchers("/api/payments/health").permitAll()
                
                // Autorise l'affichage visuel de Swagger (UI et docs JSON)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // BLOQUE tout le reste (process, refund, transactions, etc.)
                // Un JWT valide sera obligatoirement requis pour ces appels
                .anyRequest().authenticated()
            );

        // Ajoute notre filtre personnalisé (AuthTokenFilter) avant le filtre standard de Spring Security
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}