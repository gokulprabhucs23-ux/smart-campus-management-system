package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "subjects")
public class Subject {
    @Id
    private String mongoId;

    @JsonProperty("id")
    private Long id;

    private String code;
    private String name;
    private String department;
    private Integer semester;
    private Integer year;
    private Integer credits;
    private String faculty;
    private String facultyUsername;
    private String facultyEmail;
    private String facultyPhone;
    private String facultyDesignation;

    public Subject() {}

    public Subject(Long id, String code, String name, String department, Integer semester, Integer credits, String faculty) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.department = department;
        this.semester = semester;
        this.year = semester != null ? (semester + 1) / 2 : 1;
        this.credits = credits;
        this.faculty = faculty;
    }

    public Subject(Long id, String code, String name, String department, Integer semester, Integer year, Integer credits,
                   String faculty, String facultyUsername, String facultyEmail, String facultyPhone, String facultyDesignation) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.department = department;
        this.semester = semester;
        this.year = year != null ? year : (semester != null ? (semester + 1) / 2 : 1);
        this.credits = credits;
        this.faculty = faculty;
        this.facultyUsername = facultyUsername;
        this.facultyEmail = facultyEmail;
        this.facultyPhone = facultyPhone;
        this.facultyDesignation = facultyDesignation;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getFacultyUsername() {
        return facultyUsername;
    }

    public void setFacultyUsername(String facultyUsername) {
        this.facultyUsername = facultyUsername;
    }

    public String getFacultyEmail() {
        return facultyEmail;
    }

    public void setFacultyEmail(String facultyEmail) {
        this.facultyEmail = facultyEmail;
    }

    public String getFacultyPhone() {
        return facultyPhone;
    }

    public void setFacultyPhone(String facultyPhone) {
        this.facultyPhone = facultyPhone;
    }

    public String getFacultyDesignation() {
        return facultyDesignation;
    }

    public void setFacultyDesignation(String facultyDesignation) {
        this.facultyDesignation = facultyDesignation;
    }
}
