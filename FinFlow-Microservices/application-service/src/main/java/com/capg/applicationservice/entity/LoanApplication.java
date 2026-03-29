package com.capg.applicationservice.entity;

import jakarta.persistence.*;

/**
 * JPA Entity representing a loan application in the FinFlow system.
 * Mapped to the "loan_applications" table in the finflow_app database.
 *
 * Status lifecycle: Draft → Submitted → APPROVED/REJECTED
 */
@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    /** Auto-generated primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Foreign key reference to the user who created this application */
    private Long userId;

    /** JSON string containing personal details (name, address, DOB, etc.) */
    @Column(columnDefinition = "TEXT")
    private String personalDetails;

    /** JSON string containing employment details (company, salary, designation, etc.) */
    @Column(columnDefinition = "TEXT")
    private String employmentDetails;

    /** JSON string containing loan details (amount, tenure, purpose, etc.) */
    @Column(columnDefinition = "TEXT")
    private String loanDetails;

    /** Current status: Draft, Submitted, APPROVED, or REJECTED */
    private String status; 

    public LoanApplication() {}

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPersonalDetails() { return personalDetails; }
    public void setPersonalDetails(String personalDetails) { this.personalDetails = personalDetails; }
    public String getEmploymentDetails() { return employmentDetails; }
    public void setEmploymentDetails(String employmentDetails) { this.employmentDetails = employmentDetails; }
    public String getLoanDetails() { return loanDetails; }
    public void setLoanDetails(String loanDetails) { this.loanDetails = loanDetails; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
