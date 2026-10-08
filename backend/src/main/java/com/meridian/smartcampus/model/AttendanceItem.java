package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "attendance")
public class AttendanceItem {
    @Id
    private String mongoId;

    @JsonProperty("id")
    private Long id;

    private Long studentId;
    private Long subjectId;
    private String subjectCode;
    private String subject;
    private Integer semester = 1;
    private Integer present;
    private Integer total;
    private String status; // "good", "average", "low", "warning"
    private Integer percentage;
    private String lastUpdated;

    public AttendanceItem() {}

    public AttendanceItem(Long id, Long studentId, Long subjectId, String subjectCode, String subject, Integer semester, Integer present, Integer total, String status, Integer percentage, String lastUpdated) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subject = subject;
        this.semester = semester != null ? semester : 1;
        this.present = present;
        this.total = total;
        this.status = status;
        this.percentage = percentage;
        this.lastUpdated = lastUpdated;
    }

    public AttendanceItem(Long id, Long studentId, Long subjectId, String subject, Integer present, Integer total, String status, Integer percentage, String lastUpdated) {
        this(id, studentId, subjectId, "SUB" + (subjectId != null ? subjectId : 101), subject, 1, present, total, status, percentage, lastUpdated);
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

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public Integer getPresent() {
        return present;
    }

    public void setPresent(Integer present) {
        this.present = present;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
