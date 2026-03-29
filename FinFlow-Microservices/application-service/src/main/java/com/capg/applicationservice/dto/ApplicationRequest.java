package com.capg.applicationservice.dto;

/**
 * DTO for creating or updating a loan application.
 * All fields are JSON strings containing structured details.
 */
public class ApplicationRequest {

    /** Personal details: name, address, DOB, etc. (JSON string) */
    private String personalDetails;

    /** Employment details: company, salary, designation, etc. (JSON string) */
    private String employmentDetails;

    /** Loan details: amount, tenure, purpose, etc. (JSON string) */
    private String loanDetails;

    public ApplicationRequest() {}

    // --- Getters and Setters ---
    public String getPersonalDetails() { return personalDetails; }
    public void setPersonalDetails(String personalDetails) { this.personalDetails = personalDetails; }
    public String getEmploymentDetails() { return employmentDetails; }
    public void setEmploymentDetails(String employmentDetails) { this.employmentDetails = employmentDetails; }
    public String getLoanDetails() { return loanDetails; }
    public void setLoanDetails(String loanDetails) { this.loanDetails = loanDetails; }
}
