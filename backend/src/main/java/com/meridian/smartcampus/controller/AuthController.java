package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.User;
import com.meridian.smartcampus.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        userData.put("id", user.getNumericId() != null ? user.getNumericId() : 1L);
        userData.put("mongoId", user.getId());
        userData.put("username", user.getUsername());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("department", user.getDepartment());

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
            resp.put("id", user.getNumericId() != null ? user.getNumericId() : 1L);
            resp.put("mongoId", user.getId());
            resp.put("username", user.getUsername());
            resp.put("name", user.getName());
            resp.put("email", user.getEmail());
            resp.put("role", user.getRole());
            resp.put("department", user.getDepartment());
        } else {
            String role = userRole != null ? userRole.toLowerCase().trim() : "student";
            resp.put("id", 1L);
            resp.put("username", "user1" + role);
            resp.put("name", role.substring(0, 1).toUpperCase() + role.substring(1) + " User");
            resp.put("email", role + "@meridian.edu");
            resp.put("role", role);
            resp.put("department", "CSE");
        }

        return ResponseEntity.ok(resp);
    }
}
