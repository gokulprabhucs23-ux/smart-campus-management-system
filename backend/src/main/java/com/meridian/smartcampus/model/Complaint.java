package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "complaints")
public class Complaint {
    @Id
    private String mongoId;

    @JsonProperty("id")
    private Long id;

    private String title;
    private String category;
    private String student; // Name of student or teacher
    private String senderUsername;
    private String senderRole; // "student" or "faculty"
    private String department;
    private String description;
    private String priority = "medium"; // "low", "medium", "high"
    private String createdAt;
    private String status = "submitted"; // "submitted", "review", "progress", "resolved", "rejected"

    public Complaint() {}

    public Complaint(Long id, String title, String category, String student, String description, String priority, String createdAt, String status) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.student = student;
        this.description = description;
        this.priority = priority;
        this.createdAt = createdAt;
        this.status = status;
        this.senderRole = "student";
    }

    public Complaint(Long id, String title, String category, String student, String senderUsername, String senderRole, String department, String description, String priority, String createdAt, String status) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.student = student;
        this.senderUsername = senderUsername;
        this.senderRole = senderRole;
        this.department = department;
        this.description = description;
        this.priority = priority;
        this.createdAt = createdAt;
        this.status = status;
    }

    public String getMongoId() {
        return mongoId;
    }

    public void setMongoId(String mongoId) {
        this.mongoId = mongoId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
