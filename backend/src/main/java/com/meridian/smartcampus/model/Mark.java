package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "marks")
public class Mark {
    @Id
    private String mongoId;

    @JsonProperty("id")
    private Long id;

    private Long studentId;
    private Long subjectId;
    private String subjectCode;
    private String subject;
    private Integer semester;
    private Integer year;
    private Integer credits = 4;
    private String assessment;
    private Double score;
    private Double maxScore;
    private String grade;
    private Boolean passed;

    public Mark() {}

    public Mark(Long id, Long studentId, Long subjectId, String subjectCode, String subject, Integer semester, Integer year, Integer credits, String assessment, Double score, Double maxScore, String grade, Boolean passed) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subject = subject;
        this.semester = semester;
        this.year = year != null ? year : (semester != null ? (semester + 1) / 2 : 1);
        this.credits = credits != null ? credits : 4;
        this.assessment = assessment;
        this.score = score;
        this.maxScore = maxScore;
        this.grade = grade;
        this.passed = passed;
    }

    public Mark(Long id, Long studentId, Long subjectId, String subject, String assessment, Double score, Double maxScore, String grade, Boolean passed) {
        this(id, studentId, subjectId, "SUB" + (subjectId != null ? subjectId : 101), subject, 1, 1, 4, assessment, score, maxScore, grade, passed);
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

    public Integer getYear() {
        return year != null ? year : (semester != null ? (semester + 1) / 2 : 1);
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getCredits() {
        return credits != null ? credits : 4;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getAssessment() {
        return assessment;
    }

    public void setAssessment(String assessment) {
        this.assessment = assessment;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }
}
