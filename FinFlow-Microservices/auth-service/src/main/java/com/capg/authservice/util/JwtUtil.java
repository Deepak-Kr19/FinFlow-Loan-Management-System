package com.capg.authservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for JWT (JSON Web Token) operations.
 * Handles token generation, validation, and claim extraction.
 * Used by auth-service to issue tokens on login.
 *
 * Note: The same SECRET key must be used in the API Gateway's JwtUtil
 * for token validation to work across services.
 */
@Component
public class JwtUtil {

    /**
     * 256-bit secret key for HMAC-SHA256 signing.
     * IMPORTANT: In production, this should be externalized to environment variables or a vault.
     */
    private static final String SECRET = "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b";

    /** Token validity period: 24 hours in milliseconds */
    private static final long EXPIRATION_TIME = 86400000;

    /**
     * Creates an HMAC-SHA256 signing key from the secret string.
     * @return the signing key used for JWT operations
     */
    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /**
     * Generates a JWT token with the user's role and ID as custom claims.
     *
     * @param username the user's email (used as the token subject)
     * @param role     the user's role (e.g., ROLE_APPLICANT, ROLE_ADMIN)
     * @param userId   the user's database ID (embedded as a custom claim)
     * @return signed JWT token string
     */
    public String generateToken(String username, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);     // Custom claim: user role
        claims.put("userId", userId); // Custom claim: user ID
        return createToken(claims, username);
    }

    /**
     * Builds and signs the JWT token with the given claims and subject.
     *
     * @param claims  custom claims to embed in the token (role, userId)
     * @param subject the token subject (user email)
     * @return compact JWT string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)                                                    // Set custom claims
                .setSubject(subject)                                                  // Set subject (email)
                .setIssuedAt(new Date(System.currentTimeMillis()))                    // Token creation time
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Expiry time
                .signWith(getSignKey(), SignatureAlgorithm.HS256)                     // Sign with HMAC-SHA256
                .compact();
    }

    /**
     * Extracts the username (subject) from a JWT token.
     * @param token the JWT token string
     * @return the email/username stored as the token subject
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic method to extract any claim from a JWT token.
     * @param token          the JWT token string
     * @param claimsResolver function to extract the desired claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT token and extracts all claims.
     * Throws an exception if the token is invalid or expired.
     * @param token the JWT token string
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
     * If the token is expired, malformed, or has an invalid signature,
     * an exception is thrown which is caught by the API Gateway filter.
     *
     * @param token the JWT token string to validate
     */
    public void validateToken(final String token) {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }
}
