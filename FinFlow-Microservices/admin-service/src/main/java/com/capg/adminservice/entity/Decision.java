package com.capg.adminservice.entity;

import jakarta.persistence.*;

/**
 * JPA Entity representing an admin's decision on a loan application.
 * Mapped to the "decisions" table in the finflow_admin database.
 * Each decision is linked to a specific loan application.
 */
@Entity
@Table(name = "decisions")
public class Decision {

    /** Auto-generated primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Foreign key reference to the loan application being decided upon */
    private Long applicationId;

    /** Decision outcome: APPROVED or REJECTED */
    private String decision;

    /** Admin's remarks explaining the decision */
    private String remarks;

    public Decision() {}

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
