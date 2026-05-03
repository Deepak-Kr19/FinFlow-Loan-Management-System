package com.capg.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;


/**
 * JWT utility class for the API Gateway.
 * Used by AuthenticationFilter to validate incoming JWT tokens
 * and extract user claims (userId, role).
 *
 * IMPORTANT: The SECRET key must match the one in auth-service's JwtUtil,
 * otherwise token validation will fail with a SignatureException.
 */
@Component
public class JwtUtil {

    /**
     * 256-bit secret key for HMAC-SHA256.
     * Must be identical to the key used in auth-service for token generation.
     */
    private static final String SECRET = "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b";

    /** Creates the HMAC-SHA256 signing key from the secret string */
    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /**
     * Parses the JWT token and extracts all claims (userId, role, subject, etc.).
     * @param token the JWT token string (without "Bearer " prefix)
     * @return all claims contained in the token
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Validates a JWT token by attempting to parse it.
     * Throws an exception if the token is expired, malformed,
     * or signed with a different key.
     *
     * @param token the JWT token string to validate
     */
    public void validateToken(final String token) {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }
}
