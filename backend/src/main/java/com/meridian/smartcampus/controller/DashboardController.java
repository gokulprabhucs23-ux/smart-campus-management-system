package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Department;
import com.meridian.smartcampus.model.Student;
import com.meridian.smartcampus.model.StudentDetails;
import com.meridian.smartcampus.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentDetailsRepository studentDetailsRepository;

    public DashboardController(StudentRepository studentRepository,
                               FacultyRepository facultyRepository,
                               ComplaintRepository complaintRepository,
                               DepartmentRepository departmentRepository,
                               StudentDetailsRepository studentDetailsRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.complaintRepository = complaintRepository;
        this.departmentRepository = departmentRepository;
        this.studentDetailsRepository = studentDetailsRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "student") String userRole,
            @RequestHeader(value = "X-Username", required = false) String username) {
        
        String role = userRole != null ? userRole.toLowerCase().trim() : "student";
        Map<String, Object> resp = new HashMap<>();
        resp.put("role", role);

        long studentCount = studentRepository.count();
        long facultyCount = facultyRepository.count();
        long openComplaints = complaintRepository.findAll().stream()
                .filter(c -> !"resolved".equalsIgnoreCase(c.getStatus()) && !"rejected".equalsIgnoreCase(c.getStatus()))
                .count();

        if ("admin".equals(role)) {
            resp.put("greeting", "Good morning, Admin.");

            List<Map<String, Object>> stats = new ArrayList<>();
            stats.add(Map.of("label", "Total Students", "value", studentCount > 0 ? studentCount : 1250));
            stats.add(Map.of("label", "Active Faculty", "value", facultyCount > 0 ? facultyCount : 84));
            stats.add(Map.of("label", "Open Complaints", "value", openComplaints, "tone", "critical"));
            stats.add(Map.of("label", "System Health", "value", "99.9%", "tone", "positive"));
            resp.put("stats", stats);

            List<Map<String, String>> activities = List.of(
                    Map.of("title", "New Faculty Registered", "detail", "Dr. Alan Turing joined CS Dept", "time", "1 hr ago"),
                    Map.of("title", "System Backup", "detail", "Automated MongoDB snapshot completed successfully", "time", "3 hrs ago")
            );
            resp.put("activities", activities);

        } else if ("faculty".equals(role)) {
            resp.put("greeting", "Good morning, Professor.");

            List<Map<String, Object>> stats = new ArrayList<>();
            stats.add(Map.of("label", "My Students", "value", studentCount > 0 ? studentCount : 120));
            stats.add(Map.of("label", "Classes Today", "value", 3));
            stats.add(Map.of("label", "Pending Assignments", "value", 45, "tone", "warning"));
            stats.add(Map.of("label", "Avg Attendance", "value", "92%", "tone", "positive"));
            resp.put("stats", stats);

            List<Map<String, String>> activities = List.of(
                    Map.of("title", "Assignment Submitted", "detail", "John Doe submitted Binary Tree Traversal", "time", "10 mins ago"),
                    Map.of("title", "Leave Request", "detail", "Alice Smith requested academic leave", "time", "1 hr ago")
            );
            resp.put("activities", activities);

        } else {
            // Default student
            resp.put("greeting", "Good morning.");

            // Resolve student
            Optional<Student> studentOpt = Optional.empty();
            if (username != null && !username.trim().isEmpty()) {
                String clean = username.trim().toLowerCase();
                studentOpt = studentRepository.findByEmail(clean + "@meridian.edu");
                if (studentOpt.isEmpty()) {
                    studentOpt = studentRepository.findAll().stream()
                            .filter(s -> s.getEmail() != null && s.getEmail().toLowerCase().contains(clean))
                            .findFirst();
                }
            }
            if (studentOpt.isEmpty()) {
                studentOpt = studentRepository.findById(1L);
            }

            Long studentId = studentOpt.map(Student::getId).orElse(1L);
            int studentAttendance = studentOpt.map(Student::getAttendance).orElse(94);
            int studentYear = studentOpt.map(Student::getYear).orElse(1);
            int studentSem = studentOpt.map(Student::getSemester).orElse(1);
            String studentDept = studentOpt.map(Student::getDepartment).orElse("CSE");
            String studentName = studentOpt.map(Student::getName).orElse("Student");

            Optional<StudentDetails> detailsOpt = studentDetailsRepository.findByStudentId(studentId);
            StudentDetails details = detailsOpt.orElse(null);

            double cgpa = details != null && details.getCgpa() != null ? details.getCgpa() : 8.75;
            int arrearCount = details != null && details.getArrearCount() != null ? details.getArrearCount() : 0;
            List<Map<String, Object>> currentArrears = details != null && details.getCurrentArrears() != null ? details.getCurrentArrears() : Collections.emptyList();
            List<Map<String, Object>> arrearsHistory = details != null && details.getArrearsHistory() != null ? details.getArrearsHistory() : Collections.emptyList();
            List<Map<String, Object>> semesterPercentages = details != null && details.getSemesterPercentages() != null ? details.getSemesterPercentages() : Collections.emptyList();
            List<Map<String, Object>> sgpaList = details != null && details.getSgpa() != null ? details.getSgpa() : Collections.emptyList();

            resp.put("studentName", studentName);
            resp.put("department", studentDept);
            resp.put("year", studentYear);
            resp.put("semester", studentSem);
            resp.put("cgpa", cgpa);
            resp.put("arrearCount", arrearCount);
            resp.put("currentArrears", currentArrears);
            resp.put("arrearsHistory", arrearsHistory);
            resp.put("semesterPercentages", semesterPercentages);
            resp.put("sgpa", sgpaList);

            List<Map<String, Object>> stats = new ArrayList<>();
            stats.add(Map.of("label", "Overall Attendance", "value", studentAttendance + "%", "tone", "positive"));
            stats.add(Map.of("label", "Cumulative CGPA", "value", String.valueOf(cgpa)));
            stats.add(Map.of("label", "Current Arrears", "value", currentArrears.size(), "tone", currentArrears.isEmpty() ? "positive" : "critical"));
            stats.add(Map.of("label", "Current Semester", "value", "Sem " + studentSem + " (Yr " + studentYear + ")"));
            resp.put("stats", stats);

            List<Map<String, String>> activities = List.of(
                    Map.of("title", "Grade Summary", "detail", "Cumulative CGPA maintained at " + cgpa + " across completed semesters", "time", "1 hr ago"),
                    Map.of("title", "Academic Advisory", "detail", "Course registration and faculty consultation open for Semester " + studentSem, "time", "3 hrs ago")
            );
            resp.put("activities", activities);
        }

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        Map<String, Object> resp = new HashMap<>();

        List<Department> departments = departmentRepository.findAll();
        List<Map<String, Object>> deptStudents = new ArrayList<>();
        if (!departments.isEmpty()) {
            for (Department d : departments) {
                deptStudents.add(Map.of("label", d.getName(), "value", d.getStudents() != null ? d.getStudents() : 100));
            }
        } else {
            deptStudents.add(Map.of("label", "Computer Science", "value", 450));
            deptStudents.add(Map.of("label", "Engineering", "value", 320));
            deptStudents.add(Map.of("label", "Business", "value", 200));
        }
        resp.put("departmentStudents", deptStudents);

        long resolved = complaintRepository.findAll().stream().filter(c -> "resolved".equalsIgnoreCase(c.getStatus())).count();
        long pending = complaintRepository.findAll().stream().filter(c -> !"resolved".equalsIgnoreCase(c.getStatus())).count();

        List<Map<String, Object>> complaints = List.of(
                Map.of("label", "Resolved", "value", resolved > 0 ? resolved : 120, "color", "#10b981"),
                Map.of("label", "Pending", "value", pending > 0 ? pending : 45, "color", "#f59e0b")
        );
        resp.put("complaints", complaints);

        List<Map<String, Object>> attendanceTrend = List.of(
                Map.of("label", "W1", "value", 90),
                Map.of("label", "W2", "value", 92),
                Map.of("label", "W3", "value", 88),
                Map.of("label", "W4", "value", 95)
        );
        resp.put("attendanceTrend", attendanceTrend);

        return ResponseEntity.ok(resp);
    }
}
