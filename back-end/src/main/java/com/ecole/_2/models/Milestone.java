package com.ecole._2.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Milestone {
    private int level;
    private String deadline;
    @JsonProperty("validated_at")
    private String validated_at;
    private int user_id;
    private int milestone_id;

    // Getters and setters
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getValidatedAt() {
        return validated_at;
    }

    public void setValidatedAt(String validated_at) {
        this.validated_at = validated_at;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public int getMilestoneId() {
        return milestone_id;
    }

    public void setMilestoneId(int milestone_id) {
        this.milestone_id = milestone_id;
    }
}