package com.capg.authservice.dto;

/**
 * DTO returned after a successful login.
 * Contains the JWT token for subsequent authenticated requests
 * and the user's role for UI-side authorization.
 */
public class AuthResponse {
    /** JWT token to be sent in the Authorization header as "Bearer {token}" */
    private String token;

    /** User's role (e.g., ROLE_APPLICANT, ROLE_ADMIN) for frontend routing */
    private String role;

    public AuthResponse() {}

    public AuthResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }

    // --- Getters and Setters ---
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
