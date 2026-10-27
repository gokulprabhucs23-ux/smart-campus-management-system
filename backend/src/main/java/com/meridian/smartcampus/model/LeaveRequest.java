package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "leave_requests")
public class LeaveRequest {

    @Id
    private String mongoId;

    @JsonProperty("id")
    private Long id;

    private String applicantType; // "student" or "faculty"
    private String applicantUsername; // e.g. "student1cse" or "teacher1cse"
    private String applicantName; // e.g. "Alex Rivera", "Dr. Alan Turing"
    private String department; // e.g. "CSE"
    private String studentId; // e.g. "STU-2024-001" (if student)
    private String leaveType; // "Medical Leave", "Casual Leave", "Academic / On-Duty", "Personal / Emergency"
    private String startDate; // "2026-10-14"
    private String endDate; // "2026-10-16"
    private Integer totalDays; // e.g. 3
    private String reason;
    private String assignedApproverRole; // "faculty" for students, "admin" for faculty
    private String assignedApproverUsername; // e.g. "teacher1cse" or "admin"
    private String assignedApproverName; // e.g. "Dr. Alan Turing" or "Campus Administrator"
    private String substituteTeacher; // Class coverage / substitute teacher for faculty leaves
    private String status = "PENDING"; // "PENDING", "APPROVED", "REJECTED"
    private String reviewedBy;
    private String reviewerRemarks;
    private String reviewedAt;
    private String createdAt;

    public LeaveRequest() {}

    public LeaveRequest(Long id, String applicantType, String applicantUsername, String applicantName,
                        String department, String studentId, String leaveType, String startDate,
                        String endDate, Integer totalDays, String reason, String assignedApproverRole,
                        String assignedApproverUsername, String assignedApproverName,
                        String substituteTeacher, String status, String reviewedBy,
                        String reviewerRemarks, String reviewedAt, String createdAt) {
        this.id = id;
        this.applicantType = applicantType;
        this.applicantUsername = applicantUsername;
        this.applicantName = applicantName;
        this.department = department;
        this.studentId = studentId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
        this.reason = reason;
        this.assignedApproverRole = assignedApproverRole;
        this.assignedApproverUsername = assignedApproverUsername;
        this.assignedApproverName = assignedApproverName;
        this.substituteTeacher = substituteTeacher;
        this.status = status != null ? status : "PENDING";
        this.reviewedBy = reviewedBy;
        this.reviewerRemarks = reviewerRemarks;
        this.reviewedAt = reviewedAt;
        this.createdAt = createdAt;
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

    public String getApplicantType() {
        return applicantType;
    }

    public void setApplicantType(String applicantType) {
        this.applicantType = applicantType;
    }

    public String getApplicantUsername() {
        return applicantUsername;
    }

    public void setApplicantUsername(String applicantUsername) {
        this.applicantUsername = applicantUsername;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAssignedApproverRole() {
        return assignedApproverRole;
    }

    public void setAssignedApproverRole(String assignedApproverRole) {
        this.assignedApproverRole = assignedApproverRole;
    }

    public String getAssignedApproverUsername() {
        return assignedApproverUsername;
    }

    public void setAssignedApproverUsername(String assignedApproverUsername) {
        this.assignedApproverUsername = assignedApproverUsername;
    }

    public String getAssignedApproverName() {
        return assignedApproverName;
    }

    public void setAssignedApproverName(String assignedApproverName) {
        this.assignedApproverName = assignedApproverName;
    }

    public String getSubstituteTeacher() {
        return substituteTeacher;
    }

    public void setSubstituteTeacher(String substituteTeacher) {
        this.substituteTeacher = substituteTeacher;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewerRemarks() {
        return reviewerRemarks;
    }

    public void setReviewerRemarks(String reviewerRemarks) {
        this.reviewerRemarks = reviewerRemarks;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
