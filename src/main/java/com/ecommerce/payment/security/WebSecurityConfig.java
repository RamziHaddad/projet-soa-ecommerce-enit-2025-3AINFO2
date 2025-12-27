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
            .csrf(csrf -> csrf.disable())
            
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                // Autorise l'accès public au Health Check pour le monitoring
                .requestMatchers("/api/payments/health").permitAll()
                
                //mais on a tester le blocage dans swagger pour verifier que le jwt fonctionne (ici on a utiliser le jwt de test genere dans JwtUtils)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Bloque tout le reste (process, refund, transactions, etc.)
                // Un JWT valide sera obligatoirement requis pour ces appels
                .anyRequest().authenticated()
            );

        // Ajoute notre filtre personnalisé (AuthTokenFilter) avant le filtre standard de Spring Security
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}