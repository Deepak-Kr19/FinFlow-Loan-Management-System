package com.capg.applicationservice.dto;

public class ApplicationRequest {
    private String personalDetails;
    private String employmentDetails;
    private String loanDetails;

    public ApplicationRequest() {}

    public String getPersonalDetails() { return personalDetails; }
    public void setPersonalDetails(String personalDetails) { this.personalDetails = personalDetails; }
    public String getEmploymentDetails() { return employmentDetails; }
    public void setEmploymentDetails(String employmentDetails) { this.employmentDetails = employmentDetails; }
    public String getLoanDetails() { return loanDetails; }
    public void setLoanDetails(String loanDetails) { this.loanDetails = loanDetails; }
}
