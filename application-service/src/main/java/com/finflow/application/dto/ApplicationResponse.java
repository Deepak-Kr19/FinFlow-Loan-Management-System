package com.finflow.application.dto;

import com.finflow.application.entity.ApplicationStatus;

public class ApplicationResponse {

    private Long id;
    private Double amount;
    private String purpose;
    private ApplicationStatus status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
}