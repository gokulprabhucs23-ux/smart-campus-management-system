package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "faculty")
public class Faculty {
    @Id
    private String mongoId;

    @JsonProperty("id")
    private Long id;

    private String facultyId;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String designation;
    private String qualification;
    private String status = "active";
    private String username;
    private List<String> subjects = new ArrayList<>();

    public Faculty() {}

    public Faculty(Long id, String facultyId, String name, String email, String phone, String department, String designation, String qualification, String status, List<String> subjects) {
        this.id = id;
        this.facultyId = facultyId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.designation = designation;
        this.qualification = qualification;
        this.status = status;
        this.subjects = subjects;
    }

    public Faculty(Long id, String facultyId, String name, String email, String department, String designation, String qualification, String status, List<String> subjects) {
        this(id, facultyId, name, email, "+1 (555) 01" + (id != null && id < 10 ? "0" + id : id), department, designation, qualification, status, subjects);
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

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
