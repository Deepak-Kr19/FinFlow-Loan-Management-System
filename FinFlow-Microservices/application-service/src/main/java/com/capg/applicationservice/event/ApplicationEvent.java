package com.capg.applicationservice.event;

import java.io.Serializable;

public class ApplicationEvent implements Serializable {

    private Long applicationId;
    private Long userId;
    private String status;
    private String eventType;

    public ApplicationEvent() {}

    public ApplicationEvent(Long applicationId, Long userId, String status, String eventType) {
        this.applicationId = applicationId;
        this.userId = userId;
        this.status = status;
        this.eventType = eventType;
    }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    @Override
    public String toString() {
        return "ApplicationEvent{appId=" + applicationId + ", userId=" + userId + ", status=" + status + ", type=" + eventType + "}";
    }
}
