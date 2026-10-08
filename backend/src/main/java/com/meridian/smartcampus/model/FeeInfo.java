package com.meridian.smartcampus.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "fees")
public class FeeInfo {
    @Id
    private String id;
    private Long studentId;
    private Double total = 60000.0;
    private Double paid = 45000.0;
    private Double pending = 15000.0;
    private List<Map<String, Object>> history = new ArrayList<>();
    private List<Map<String, Object>> yearlyBreakdown = new ArrayList<>();

    public FeeInfo() {}

    public FeeInfo(Long studentId, Double total, Double paid, Double pending, List<Map<String, Object>> history) {
        this.studentId = studentId;
        this.total = total;
        this.paid = paid;
        this.pending = pending;
        this.history = history;
    }

    public FeeInfo(Long studentId, Double total, Double paid, Double pending, List<Map<String, Object>> history, List<Map<String, Object>> yearlyBreakdown) {
        this.studentId = studentId;
        this.total = total;
        this.paid = paid;
        this.pending = pending;
        this.history = history;
        this.yearlyBreakdown = yearlyBreakdown;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getPaid() {
        return paid;
    }

    public void setPaid(Double paid) {
        this.paid = paid;
    }

    public Double getPending() {
        return pending;
    }

    public void setPending(Double pending) {
        this.pending = pending;
    }

    public List<Map<String, Object>> getHistory() {
        return history;
    }

    public void setHistory(List<Map<String, Object>> history) {
        this.history = history;
    }

    public List<Map<String, Object>> getYearlyBreakdown() {
        return yearlyBreakdown;
    }

    public void setYearlyBreakdown(List<Map<String, Object>> yearlyBreakdown) {
        this.yearlyBreakdown = yearlyBreakdown;
    }
}
