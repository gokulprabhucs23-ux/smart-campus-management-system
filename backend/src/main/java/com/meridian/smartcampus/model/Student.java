package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "students")
public class Student {
    @Id
    private String mongoId;

    @JsonProperty("id")
    private Long id;

    private String studentId;
    private String name;
    private String email;
    private String department;
    private Integer year;
    private Integer semester;
    private String section;
    private String status = "active";
    private Integer attendance = 90;
    private Long mentorId = 1L;
    private String mentorName;
    private String mentorEmail;

    public Student() {}

    public Student(Long id, String studentId, String name, String email, String department, Integer year, Integer semester, String section, String status, Integer attendance, Long mentorId) {
        this.id = id;
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.year = year;
        this.semester = semester;
        this.section = section;
        this.status = status;
        this.attendance = attendance;
        this.mentorId = mentorId;
    }

    public Student(Long id, String studentId, String name, String email, String department, Integer year, String section, String status, Integer attendance, Long mentorId) {
        this(id, studentId, name, email, department, year, year != null ? (year * 2 - 1) : 1, section, status, attendance, mentorId);
    }

    public Student(Long id, String name, String email, String department, Integer year, String section, String status, Integer attendance, Long mentorId) {
        this.id = id;
        this.studentId = "S" + (1000 + (id != null ? id : 0));
        this.name = name;
        this.email = email;
        this.department = department;
        this.year = year;
        this.semester = year != null ? (year * 2 - 1) : 1;
        this.section = section;
        this.status = status;
        this.attendance = attendance;
        this.mentorId = mentorId;
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

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getSemester() {
        return semester != null ? semester : (year != null ? year * 2 - 1 : 1);
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAttendance() {
        return attendance;
    }

    public void setAttendance(Integer attendance) {
        this.attendance = attendance;
    }

    public Long getMentorId() {
        return mentorId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    public String getMentorEmail() {
        return mentorEmail;
    }

    public void setMentorEmail(String mentorEmail) {
        this.mentorEmail = mentorEmail;
    }
}
