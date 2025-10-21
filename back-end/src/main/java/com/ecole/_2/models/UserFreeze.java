package com.ecole._2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFreeze {
    
    private Integer id;
    private String reason;
    private String category;
    private String status;
    @JsonProperty("is_free_freeze")
    private boolean isFreeFreeze;
    @JsonProperty("begin_date")
    private String beginDate;
    @JsonProperty("expected_end_date")
    private String expectedEndDate;
    @JsonProperty("effective_end_date")
    private String effectiveEndDate;
    @JsonProperty("student_description")
    private String studentDescription;
    @JsonProperty("staff_description")
    private String staffDescription;
    @JsonProperty("approved_by")
    private String approvedBy;
    @JsonProperty("approved_at")
    private String approvedAt;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    
    // Getters and setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isFreeFreeze() {
        return isFreeFreeze;
    }
    
    public void setFreeFreeze(boolean freeFreeze) {
        isFreeFreeze = freeFreeze;
    }
    
    public String getBeginDate() {
        return beginDate;
    }
    
    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }
    
    public String getExpectedEndDate() {
        return expectedEndDate;
    }
    
    public void setExpectedEndDate(String expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }
    
    public String getEffectiveEndDate() {
        return effectiveEndDate;
    }
    
    public void setEffectiveEndDate(String effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }
    
    public String getStudentDescription() {
        return studentDescription;
    }
    
    public void setStudentDescription(String studentDescription) {
        this.studentDescription = studentDescription;
    }
    
    public String getStaffDescription() {
        return staffDescription;
    }
    
    public void setStaffDescription(String staffDescription) {
        this.staffDescription = staffDescription;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public String getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(String approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}