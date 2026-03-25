package com.capg.applicationservice.dto;

public class ApplicationResponse {
    private Long id;
    private String status;
    private String personalDetails;
    private String employmentDetails;
    private String loanDetails;

    public ApplicationResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPersonalDetails() { return personalDetails; }
    public void setPersonalDetails(String personalDetails) { this.personalDetails = personalDetails; }
    public String getEmploymentDetails() { return employmentDetails; }
    public void setEmploymentDetails(String employmentDetails) { this.employmentDetails = employmentDetails; }
    public String getLoanDetails() { return loanDetails; }
    public void setLoanDetails(String loanDetails) { this.loanDetails = loanDetails; }
}
