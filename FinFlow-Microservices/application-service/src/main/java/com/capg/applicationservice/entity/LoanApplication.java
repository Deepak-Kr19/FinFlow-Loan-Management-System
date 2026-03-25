package com.capg.applicationservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String personalDetails;

    @Column(columnDefinition = "TEXT")
    private String employmentDetails;

    @Column(columnDefinition = "TEXT")
    private String loanDetails;

    private String status; 

    public LoanApplication() {}

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
