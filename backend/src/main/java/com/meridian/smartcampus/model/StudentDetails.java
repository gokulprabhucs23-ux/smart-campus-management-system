package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "student_details")
public class StudentDetails {
    @Id
    private String mongoId;

    @JsonProperty("studentId")
    private Long studentId;

    private Double cgpa;
    private List<Map<String, Object>> sgpa = new ArrayList<>();
    private Integer arrearCount = 0;
    private List<Map<String, Object>> currentArrears = new ArrayList<>();
    private List<Map<String, Object>> arrearsHistory = new ArrayList<>();
    private List<Map<String, Object>> semesterPercentages = new ArrayList<>();
    private List<Map<String, Object>> marks = new ArrayList<>();
    private List<String> risks = new ArrayList<>();
    private Map<String, Object> mentor;
    private Map<String, Object> fees;
    private List<LeaveRequest> leaves = new ArrayList<>();

    public StudentDetails() {}

    public StudentDetails(Long studentId, Double cgpa, List<Map<String, Object>> sgpa, Integer arrearCount,
                          List<Map<String, Object>> currentArrears, List<Map<String, Object>> arrearsHistory,
                          List<Map<String, Object>> semesterPercentages, List<Map<String, Object>> marks,
                          List<String> risks, Map<String, Object> mentor, Map<String, Object> fees) {
        this.studentId = studentId;
        this.cgpa = cgpa;
        this.sgpa = sgpa;
        this.arrearCount = arrearCount;
        this.currentArrears = currentArrears;
        this.arrearsHistory = arrearsHistory;
        this.semesterPercentages = semesterPercentages;
        this.marks = marks;
        this.risks = risks;
        this.mentor = mentor;
        this.fees = fees;
    }

    public StudentDetails(Long studentId, Double cgpa, List<Map<String, Object>> sgpa, Integer arrearCount,
                          List<Map<String, Object>> arrearsHistory, List<Map<String, Object>> marks,
                          List<String> risks, Map<String, Object> mentor, Map<String, Object> fees) {
        this(studentId, cgpa, sgpa, arrearCount, new ArrayList<>(), arrearsHistory, new ArrayList<>(), marks, risks, mentor, fees);
    }

    public String getMongoId() {
        return mongoId;
    }

    public void setMongoId(String mongoId) {
        this.mongoId = mongoId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public List<Map<String, Object>> getSgpa() {
        return sgpa;
    }

    public void setSgpa(List<Map<String, Object>> sgpa) {
        this.sgpa = sgpa;
    }

    public Integer getArrearCount() {
        return currentArrears != null ? currentArrears.size() : (arrearCount != null ? arrearCount : 0);
    }

    public void setArrearCount(Integer arrearCount) {
        this.arrearCount = arrearCount;
    }

    public List<Map<String, Object>> getCurrentArrears() {
        return currentArrears;
    }

    public void setCurrentArrears(List<Map<String, Object>> currentArrears) {
        this.currentArrears = currentArrears;
        if (currentArrears != null) {
            this.arrearCount = currentArrears.size();
        }
    }

    public List<Map<String, Object>> getArrearsHistory() {
        return arrearsHistory;
    }

    public void setArrearsHistory(List<Map<String, Object>> arrearsHistory) {
        this.arrearsHistory = arrearsHistory;
    }

    public List<Map<String, Object>> getSemesterPercentages() {
        return semesterPercentages;
    }

    public void setSemesterPercentages(List<Map<String, Object>> semesterPercentages) {
        this.semesterPercentages = semesterPercentages;
    }

    public List<Map<String, Object>> getMarks() {
        return marks;
    }

    public void setMarks(List<Map<String, Object>> marks) {
        this.marks = marks;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks;
    }

    public Map<String, Object> getMentor() {
        return mentor;
    }

    public void setMentor(Map<String, Object> mentor) {
        this.mentor = mentor;
    }

    public Map<String, Object> getFees() {
        return fees;
    }

    public void setFees(Map<String, Object> fees) {
        this.fees = fees;
    }

    public List<LeaveRequest> getLeaves() {
        return leaves;
    }

    public void setLeaves(List<LeaveRequest> leaves) {
        this.leaves = leaves;
    }
}
