package com.capg.adminservice.entity;

import jakarta.persistence.*;

/**
 * JPA Entity representing a system report in the FinFlow admin panel.
 * Mapped to the "reports" table in the finflow_admin database.
 * Stores generated reports as JSON text data.
 */
@Entity
@Table(name = "reports")
public class Report {

    /** Auto-generated primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Report type identifier (e.g., MONTHLY_SUMMARY, LOAN_STATS) */
    private String type;
    
    /** Report data stored as JSON text */
    @Column(columnDefinition = "TEXT")
    private String data;

    public Report() {}

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
