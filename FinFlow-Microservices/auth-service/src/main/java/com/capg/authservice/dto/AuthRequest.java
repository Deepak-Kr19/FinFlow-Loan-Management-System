package com.capg.authservice.dto;

/**
 * DTO for login requests.
 * Contains the email and password submitted by the user.
 */
public class AuthRequest {
    private String email;
    private String password;

    public AuthRequest() {}

    // --- Getters and Setters ---
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
