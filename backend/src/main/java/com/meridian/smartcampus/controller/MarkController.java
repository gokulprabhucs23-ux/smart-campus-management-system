package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Mark;
import com.meridian.smartcampus.model.Student;
import com.meridian.smartcampus.model.StudentDetails;
import com.meridian.smartcampus.repository.MarkRepository;
import com.meridian.smartcampus.repository.StudentDetailsRepository;
import com.meridian.smartcampus.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/marks")
public class MarkController {

    private final MarkRepository markRepository;
    private final StudentRepository studentRepository;
    private final StudentDetailsRepository studentDetailsRepository;

    public MarkController(MarkRepository markRepository, StudentRepository studentRepository, StudentDetailsRepository studentDetailsRepository) {
        this.markRepository = markRepository;
        this.studentRepository = studentRepository;
        this.studentDetailsRepository = studentDetailsRepository;
    }

    private Student resolveStudent(Long studentId, String username) {
        if (studentId != null) {
            return studentRepository.findById(studentId).orElse(null);
        }
        if (username != null && !username.trim().isEmpty()) {
            String clean = username.trim().toLowerCase();
            Optional<Student> st = studentRepository.findByEmail(clean + "@meridian.edu");
            if (st.isPresent()) return st.get();
            return studentRepository.findAll().stream()
                    .filter(s -> s.getEmail() != null && s.getEmail().toLowerCase().contains(clean))
                    .findFirst()
                    .orElse(null);
        }
        return studentRepository.findById(1L).orElse(null);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getMarksSummary(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestParam(value = "studentId", required = false) Long studentId) {

        Student student = resolveStudent(studentId, username);
        if (student == null) {
            return ResponseEntity.ok(Map.of("error", "Student not found", "semesters", Collections.emptyList()));
        }

        StudentDetails details = studentDetailsRepository.findByStudentId(student.getId()).orElse(null);
        int currentSem = student.getSemester() != null ? student.getSemester() : 1;
        int currentYear = student.getYear() != null ? student.getYear() : 1;
        double cgpa = details != null && details.getCgpa() != null ? details.getCgpa() : 8.75;

        List<Map<String, Object>> semesterList = new ArrayList<>();
        if (details != null && details.getSemesterPercentages() != null) {
            for (Map<String, Object> sp : details.getSemesterPercentages()) {
                int semNum = ((Number) sp.getOrDefault("semester", 1)).intValue();
                if (semNum <= currentSem) {
                    Map<String, Object> item = new HashMap<>(sp);
                    item.put("type", semNum == currentSem ? "current" : "history");
                    semesterList.add(item);
                }
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("studentId", student.getId());
        resp.put("studentName", student.getName());
        resp.put("department", student.getDepartment());
        resp.put("currentSemester", currentSem);
        resp.put("currentYear", currentYear);
        resp.put("cumulativeCgpa", cgpa);
        resp.put("semesters", semesterList);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<Object> getMarks(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestParam(value = "semester", required = false) Integer semester,
            @RequestParam(value = "studentId", required = false) Long studentId) {
        
        if ("faculty".equalsIgnoreCase(userRole)) {
            List<Student> students = studentRepository.findAll();
            List<Map<String, Object>> facultyStudentMarks = new ArrayList<>();

            double totalScore = 0;
            int count = 0;
            for (Student s : students) {
                double score = 70 + (s.getId() != null ? (s.getId() * 5) % 29 : 15);
                if (score > 100) score = 95;
                String grade = score >= 90 ? "A" : score >= 80 ? "B+" : score >= 70 ? "B" : score >= 60 ? "C" : "D";
                boolean passed = score >= 60;
                totalScore += score;
                count++;

                facultyStudentMarks.add(Map.of(
                        "id", s.getId() != null ? s.getId() : 1L,
                        "name", s.getName() != null ? s.getName() : "Student",
                        "score", score,
                        "maxScore", 100,
                        "grade", grade,
                        "passed", passed
                ));
            }

            double average = count > 0 ? Math.round((totalScore / count) * 10.0) / 10.0 : 84.5;
            Map<String, Object> resp = new HashMap<>();
            resp.put("subject", "Introduction to Programming (CS101)");
            resp.put("average", average);
            resp.put("students", facultyStudentMarks);
            return ResponseEntity.ok(resp);
        }

        // Student marks lookup restricted to current and past semesters
        Student student = resolveStudent(studentId, username);
        int maxAllowedSem = student != null && student.getSemester() != null ? student.getSemester() : 8;

        if (semester != null && semester > maxAllowedSem) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<Mark> list;
        if (student != null) {
            if (semester != null) {
                list = markRepository.findByStudentIdAndSemester(student.getId(), semester);
            } else {
                list = markRepository.findByStudentId(student.getId()).stream()
                        .filter(m -> m.getSemester() != null && m.getSemester() <= maxAllowedSem)
                        .toList();
            }
        } else {
            if (semester != null) {
                list = markRepository.findAll().stream()
                        .filter(m -> m.getSemester() != null && m.getSemester().equals(semester))
                        .toList();
            } else {
                list = markRepository.findAll();
            }
        }

        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveMarks(@RequestBody Map<String, Object> payload) {
        Mark mark = new Mark();
        long maxId = markRepository.findAll().stream().mapToLong(m -> m.getId() != null ? m.getId() : 0).max().orElse(0);
        mark.setId(maxId + 1);

        if (payload.containsKey("studentId")) {
            mark.setStudentId(((Number) payload.get("studentId")).longValue());
        }
        if (payload.containsKey("subjectId")) {
            mark.setSubjectId(((Number) payload.get("subjectId")).longValue());
        }
        if (payload.containsKey("assessment")) {
            mark.setAssessment((String) payload.get("assessment"));
        }
        if (payload.containsKey("score")) {
            mark.setScore(((Number) payload.get("score")).doubleValue());
        }
        if (payload.containsKey("maxScore")) {
            mark.setMaxScore(((Number) payload.get("maxScore")).doubleValue());
        }
        mark.setSubject("Subject CS" + (mark.getSubjectId() != null ? mark.getSubjectId() : 101));
        double score = mark.getScore() != null ? mark.getScore() : 80;
        mark.setGrade(score >= 90 ? "A" : score >= 80 ? "B+" : score >= 70 ? "B" : score >= 60 ? "C" : "D");
        mark.setPassed(score >= 60);

        markRepository.save(mark);
        return ResponseEntity.ok(Map.of("success", true, "message", "Marks saved successfully"));
    }
}
