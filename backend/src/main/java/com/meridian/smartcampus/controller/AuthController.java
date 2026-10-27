package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Student;
import com.meridian.smartcampus.model.Subject;
import com.meridian.smartcampus.model.User;
import com.meridian.smartcampus.repository.StudentRepository;
import com.meridian.smartcampus.repository.SubjectRepository;
import com.meridian.smartcampus.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public AuthController(UserRepository userRepository,
                          StudentRepository studentRepository,
                          SubjectRepository subjectRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    private void enrichUserData(User user, Map<String, Object> targetMap) {
        if (user == null) return;
        targetMap.put("id", user.getNumericId() != null ? user.getNumericId() : 1L);
        targetMap.put("mongoId", user.getId());
        targetMap.put("username", user.getUsername());
        targetMap.put("name", user.getName());
        targetMap.put("email", user.getEmail());
        targetMap.put("role", user.getRole());
        targetMap.put("department", user.getDepartment());

        if ("student".equalsIgnoreCase(user.getRole())) {
            Optional<Student> studentOpt = studentRepository.findByEmail(user.getEmail());
            if (studentOpt.isEmpty()) {
                studentOpt = studentRepository.findAll().stream()
                        .filter(s -> s.getEmail() != null && s.getEmail().toLowerCase().startsWith(user.getUsername().toLowerCase() + "@"))
                        .findFirst();
            }
            if (studentOpt.isPresent()) {
                Student st = studentOpt.get();
                targetMap.put("studentId", st.getStudentId());
                targetMap.put("semester", st.getSemester());
                targetMap.put("year", st.getYear());
                targetMap.put("section", st.getSection());
                targetMap.put("attendance", st.getAttendance());
            } else {
                targetMap.put("semester", 1);
                targetMap.put("year", 1);
                targetMap.put("section", "A");
            }
        } else if ("faculty".equalsIgnoreCase(user.getRole())) {
            List<Subject> mySubs = subjectRepository.findAll().stream()
                    .filter(s -> s.getFacultyUsername() != null && s.getFacultyUsername().equalsIgnoreCase(user.getUsername()))
                    .toList();
            if (!mySubs.isEmpty()) {
                Subject sub = mySubs.get(0);
                targetMap.put("subjectCode", sub.getCode());
                targetMap.put("subjectName", sub.getName());
                targetMap.put("subjectSemester", sub.getSemester());
                targetMap.put("subjectDepartment", sub.getDepartment());
            }
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || username.trim().isEmpty() || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Username and password are required"));
        }

        String cleanUsername = username.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(cleanUsername);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(cleanUsername);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
        }

        User user = userOpt.get();
        // Check password - default password is 123
        String expectedPassword = user.getPassword() != null ? user.getPassword() : "123";
        if (!expectedPassword.equals(password.trim())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
        }

        Map<String, Object> userData = new HashMap<>();
        enrichUserData(user, userData);

        Map<String, Object> response = new HashMap<>();
        response.put("token", "token_" + user.getUsername() + "_" + System.currentTimeMillis());
        response.put("user", userData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    @GetMapping({"/me", "/auth/me"})
    public ResponseEntity<Map<String, Object>> getMe(
            @RequestHeader(value = "X-Username", required = false) String usernameHeader,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "student") String userRole) {
        
        User user = null;
        if (usernameHeader != null && !usernameHeader.trim().isEmpty()) {
            user = userRepository.findByUsernameIgnoreCase(usernameHeader.trim()).orElse(null);
        }

        if (user == null) {
            String role = userRole != null ? userRole.toLowerCase().trim() : "student";
            user = userRepository.findFirstByRoleIgnoreCase(role).orElse(null);
        }

        Map<String, Object> resp = new HashMap<>();
        if (user != null) {
            enrichUserData(user, resp);
        } else {
            String role = userRole != null ? userRole.toLowerCase().trim() : "student";
            resp.put("id", 1L);
            resp.put("username", "user1" + role);
            resp.put("name", role.substring(0, 1).toUpperCase() + role.substring(1) + " User");
            resp.put("email", role + "@meridian.edu");
            resp.put("role", role);
            resp.put("department", "CSE");
            resp.put("semester", 1);
            resp.put("year", 1);
            resp.put("section", "A");
        }

        return ResponseEntity.ok(resp);
    }
}
