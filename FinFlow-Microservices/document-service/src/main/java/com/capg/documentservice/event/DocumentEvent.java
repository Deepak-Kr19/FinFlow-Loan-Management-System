package com.capg.documentservice.event;

import java.io.Serializable;

public class DocumentEvent implements Serializable {

    private Long documentId;
    private Long applicationId;
    private String type;
    private String status;

    public DocumentEvent() {}

    public DocumentEvent(Long documentId, Long applicationId, String type, String status) {
        this.documentId = documentId;
        this.applicationId = applicationId;
        this.type = type;
        this.status = status;
    }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "DocumentEvent{docId=" + documentId + ", appId=" + applicationId + ", type=" + type + ", status=" + status + "}";
    }
}
