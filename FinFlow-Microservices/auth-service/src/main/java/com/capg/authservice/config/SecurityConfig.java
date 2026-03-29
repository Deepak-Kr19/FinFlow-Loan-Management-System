package com.capg.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for the Auth Service.
 *
 * Key decisions:
 * - CSRF is disabled since we use JWT tokens (stateless, not cookie-based).
 * - All /auth/** endpoints are public (signup, login).
 * - Swagger UI endpoints are public for API documentation access.
 * - CORS is configured to allow requests from any origin (for gateway + frontend).
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the HTTP security filter chain.
     * Permits all auth and Swagger endpoints, requires authentication for everything else.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())  // Disable CSRF — using stateless JWT, not session cookies
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()                                   // Public: login, signup, admin user endpoints
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll() // Public: Swagger UI
                .requestMatchers("/auth/v3/api-docs/**", "/auth/swagger-ui.html").permitAll()         // Public: service-specific Swagger
                .anyRequest().authenticated()  // Everything else requires authentication
            );
        return http.build();
    }

    /**
     * CORS configuration to allow cross-origin requests.
     * Required because the API Gateway and frontend may run on different origins.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));          // Allow all origins
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));                 // Allow all headers (including Authorization)
        config.setAllowCredentials(true);                       // Allow cookies/credentials
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);        // Apply to all paths
        return source;
    }

    /**
     * Password encoder bean using BCrypt hashing algorithm.
     * BCrypt automatically salts passwords, making them resistant to rainbow table attacks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
