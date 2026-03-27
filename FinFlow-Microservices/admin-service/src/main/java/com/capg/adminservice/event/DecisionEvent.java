package com.capg.adminservice.event;

import java.io.Serializable;

public class DecisionEvent implements Serializable {

    private Long applicationId;
    private String decision;
    private String remarks;

    public DecisionEvent() {}

    public DecisionEvent(Long applicationId, String decision, String remarks) {
        this.applicationId = applicationId;
        this.decision = decision;
        this.remarks = remarks;
    }

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
