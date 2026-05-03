package com.capg.authservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * JPA Entity representing a user in the FinFlow system.
 * Mapped to the "users" table in the finflow_auth database.
 * Stores user credentials and role information for authentication.
 */
@Entity
@Table(name = "users")
public class User {

    /** Auto-generated primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Full name of the user */
    private String name;

    /** Unique email used as the login identifier */
    @Column(unique = true)
    private String email;

    /** BCrypt-hashed password (never exposed in JSON responses) */
    @JsonIgnore
    private String password;

    /** User role: ROLE_APPLICANT or ROLE_ADMIN */
    private String role; 

    public User() {}

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    @JsonIgnore
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

