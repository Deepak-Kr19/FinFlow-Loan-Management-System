package com.capg.adminservice.event;

import java.io.Serializable;

/**
 * Event DTO published to RabbitMQ when an admin makes a decision on a loan application.
 * Consumed by the Application Service to update the application status.
 * Implements Serializable for RabbitMQ message serialization.
 */
public class DecisionEvent implements Serializable {

    private Long applicationId;
    private String decision; // APPROVED or REJECTED
    private String remarks;  // Admin's explanation for the decision

    public DecisionEvent() {}

    public DecisionEvent(Long applicationId, String decision, String remarks) {
        this.applicationId = applicationId;
        this.decision = decision;
        this.remarks = remarks;
    }

    // --- Getters and Setters ---
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    @Override
    public String toString() {
        return "DecisionEvent{appId=" + applicationId + ", decision=" + decision + ", remarks=" + remarks + "}";
    }
}
