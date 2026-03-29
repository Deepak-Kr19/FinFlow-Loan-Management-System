package com.capg.documentservice.entity;

import jakarta.persistence.*;

/**
 * JPA Entity representing an uploaded document in the FinFlow system.
 * Mapped to the "documents" table in the finflow_doc database.
 *
 * Status lifecycle: PENDING → VERIFIED / REJECTED
 */
@Entity
@Table(name = "documents")
public class Document {

    /** Auto-generated primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Foreign key reference to the loan application this document belongs to */
    private Long applicationId;

    /** Document type: ID_PROOF, SALARY_SLIP, ADDRESS_PROOF, etc. */
    private String type;

    /** File system path where the uploaded document is stored */
    private String filePath;

    /** Verification status: PENDING, VERIFIED, or REJECTED */
    private String status;

    public Document() {}

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
