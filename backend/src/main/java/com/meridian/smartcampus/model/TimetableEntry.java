package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "timetables")
public class TimetableEntry {
    @Id
    private String id;

    @JsonProperty("numericId")
    private Long numericId;

    private String department;     // "CSE", "ECE", "MECH", "CIVIL", "EEE"
    private Integer semester;      // 1 to 8
    private String section = "A";  // "A", "B", etc.
    private String day;            // "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    private Integer period;        // 1 to 7
    private String startTime;      // "09:00 AM"
    private String endTime;        // "09:45 AM"
    private String subjectCode;    // "CS101"
    private String subjectName;    // "Problem Solving & Python Programming"
    private String facultyName;    // "Dr. Alan Turing"
    private String facultyUsername;// "teacher1cse"
    private String room;           // "LH-101", "CS-Lab 1"
    private String type = "lecture"; // "lecture", "lab", "tutorial"

    public TimetableEntry() {}

    public TimetableEntry(Long numericId, String department, Integer semester, String section,
                          String day, Integer period, String startTime, String endTime,
                          String subjectCode, String subjectName, String facultyName,
                          String facultyUsername, String room, String type) {
        this.numericId = numericId;
        this.department = department;
        this.semester = semester;
        this.section = section != null ? section : "A";
        this.day = day;
        this.period = period;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.facultyName = facultyName;
        this.facultyUsername = facultyUsername;
        this.room = room;
        this.type = type != null ? type : "lecture";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getNumericId() {
        return numericId;
    }

    public void setNumericId(Long numericId) {
        this.numericId = numericId;
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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getFacultyUsername() {
        return facultyUsername;
    }

    public void setFacultyUsername(String facultyUsername) {
        this.facultyUsername = facultyUsername;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
