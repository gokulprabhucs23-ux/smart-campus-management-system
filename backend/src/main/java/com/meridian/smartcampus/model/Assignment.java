package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "assignments")
public class Assignment {
    @Id
    private String mongoId;

    @JsonProperty("id")
    private Long id;

    private Long subjectId;
    private String subject;
    private String title;
    private String description;
    private String dueDate;
    private Integer maxMarks;
    private String status = "open"; // "open", "due", "submitted", "closed"
    private Integer submissions = 0;
    private Integer totalStudents = 30;
    private Boolean submitted = false;
    private AssignmentSubmission mySubmission;
    private Map<String, List<String>> completionStatus = new HashMap<>();

    public Assignment() {
        this.completionStatus.put("completed", new ArrayList<>());
        this.completionStatus.put("incomplete", new ArrayList<>());
    }

    public Assignment(Long id, Long subjectId, String subject, String title, String description, String dueDate, Integer maxMarks, String status, Integer submissions, Integer totalStudents, Boolean submitted) {
        this();
        this.id = id;
        this.subjectId = subjectId;
        this.subject = subject;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.maxMarks = maxMarks;
        this.status = status;
        this.submissions = submissions;
        this.totalStudents = totalStudents;
        this.submitted = submitted;
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

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(Integer maxMarks) {
        this.maxMarks = maxMarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSubmissions() {
        return submissions;
    }

    public void setSubmissions(Integer submissions) {
        this.submissions = submissions;
    }

    public Integer getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Integer totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public Map<String, List<String>> getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(Map<String, List<String>> completionStatus) {
        this.completionStatus = completionStatus;
    }

    public AssignmentSubmission getMySubmission() {
        return mySubmission;
    }

    public void setMySubmission(AssignmentSubmission mySubmission) {
        this.mySubmission = mySubmission;
    }
}
