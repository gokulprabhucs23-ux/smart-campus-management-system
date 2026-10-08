package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.*;
import com.meridian.smartcampus.repository.*;
import com.meridian.smartcampus.service.AiService;
import com.meridian.smartcampus.service.CampusContextService;
import com.meridian.smartcampus.service.LlmClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class AiController {

    private final AiService aiService;
    private final LlmClient llmClient;
    private final CampusContextService campusContextService;
    private final StudentDetailsRepository studentDetailsRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;
    private final FeeRepository feeRepository;
    private final AssignmentRepository assignmentRepository;

    public AiController(AiService aiService,
                        LlmClient llmClient,
                        CampusContextService campusContextService,
                        StudentDetailsRepository studentDetailsRepository,
                        AttendanceRepository attendanceRepository,
                        MarkRepository markRepository,
                        FeeRepository feeRepository,
                        AssignmentRepository assignmentRepository) {
        this.aiService = aiService;
        this.llmClient = llmClient;
        this.campusContextService = campusContextService;
        this.studentDetailsRepository = studentDetailsRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
        this.feeRepository = feeRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @PostMapping({"/ai/chat", "/chat"})
    public ResponseEntity<Map<String, Object>> chatWithAssistant(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "student") String userRole,
            @RequestHeader(value = "X-Username", required = false) String username) {

        String message = payload.containsKey("message") ? String.valueOf(payload.get("message")) : "";
        List<Map<String, String>> history = new ArrayList<>();

        if (payload.containsKey("history") && payload.get("history") instanceof List<?> rawList) {
            for (Object obj : rawList) {
                if (obj instanceof Map<?, ?> rawMap) {
                    Map<String, String> turn = new HashMap<>();
                    turn.put("from", String.valueOf(rawMap.get("from")));
                    turn.put("text", String.valueOf(rawMap.get("text")));
                    history.add(turn);
                }
            }
        }

        String model = payload.containsKey("model") ? String.valueOf(payload.get("model")) : null;
        String provider = payload.containsKey("provider") ? String.valueOf(payload.get("provider")) : null;

        Map<String, Object> response = aiService.processChat(userRole, username, message, history, model, provider);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ai/config")
    public ResponseEntity<Map<String, Object>> getAiConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("provider", llmClient.getProvider());
        config.put("model", llmClient.getModel());
        config.put("configured", llmClient.isConfigured());
        config.put("maskedKey", llmClient.getMaskedApiKey());
        config.put("customEndpoint", llmClient.getCustomEndpoint());
        config.put("availableProviders", List.of("gemini", "openai", "grounded_campus_engine"));
        config.put("availableModels", List.of("gemini-1.5-flash", "gemini-2.0-flash", "gpt-4o-mini", "gpt-4o"));
        return ResponseEntity.ok(config);
    }

    @PostMapping("/ai/config")
    public ResponseEntity<Map<String, Object>> updateAiConfig(@RequestBody Map<String, String> payload) {
        if (payload.containsKey("apiKey")) {
            llmClient.setApiKey(payload.get("apiKey"));
        }
        if (payload.containsKey("provider")) {
            llmClient.setProvider(payload.get("provider"));
        }
        if (payload.containsKey("model")) {
            llmClient.setModel(payload.get("model"));
        }
        if (payload.containsKey("customEndpoint")) {
            llmClient.setCustomEndpoint(payload.get("customEndpoint"));
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("configured", llmClient.isConfigured());
        resp.put("provider", llmClient.getProvider());
        resp.put("model", llmClient.getModel());
        resp.put("maskedKey", llmClient.getMaskedApiKey());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/ai/test")
    public ResponseEntity<Map<String, Object>> testAiConnection() {
        boolean success = llmClient.testConnection();
        return ResponseEntity.ok(Map.of("connected", success, "provider", llmClient.getProvider()));
    }

    @GetMapping({"/ai/insights", "/academic-insights"})
    public ResponseEntity<Map<String, Object>> getAcademicInsights(
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "student") String userRole) {

        Student s = campusContextService.findActiveStudent(username);
        Map<String, Object> resp = new HashMap<>();

        if (s == null) {
            resp.put("score", 85);
            resp.put("level", "Strong standing");
            resp.put("recommendations", List.of(
                    "Review database normalization for upcoming quiz",
                    "Maintain consistent attendance across technical modules"
            ));
            resp.put("risks", Collections.emptyList());
            return ResponseEntity.ok(resp);
        }

        // Calculate dynamic real score based on student's live data
        int baseScore = 95;
        List<String> recommendations = new ArrayList<>();
        List<Map<String, Object>> risks = new ArrayList<>();

        // Course-wise attendance buffer & recovery math
        List<Map<String, Object>> attendanceBuffer = new ArrayList<>();
        List<AttendanceItem> attendance = attendanceRepository.findByStudentId(s.getId());
        for (AttendanceItem a : attendance) {
            int p = a.getPresent() != null ? a.getPresent() : 0;
            int t = a.getTotal() != null ? a.getTotal() : 1;
            int pct = a.getPercentage() != null ? a.getPercentage() : (int) Math.round((double) p / Math.max(1, t) * 100);

            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("subject", a.getSubject());
            itemMap.put("subjectCode", a.getSubjectCode());
            itemMap.put("present", p);
            itemMap.put("total", t);
            itemMap.put("percentage", pct);

            if (pct >= 75) {
                int canSkip = (int) Math.floor((p - 0.75 * t) / 0.75);
                itemMap.put("canSkip", canSkip);
                itemMap.put("neededConsecutive", 0);
                itemMap.put("status", "Safe");
            } else {
                baseScore -= 15;
                int needed = (int) Math.ceil((0.75 * t - p) / 0.25);
                if (needed <= 0) needed = 1;
                itemMap.put("canSkip", 0);
                itemMap.put("neededConsecutive", needed);
                itemMap.put("status", "Shortage");
                recommendations.add("Attend next consecutive classes in " + a.getSubject() + " (currently " + pct + "%) to cross the 75% exam cutoff");
                risks.add(Map.of(
                        "subject", a.getSubject(),
                        "level", "High",
                        "description", "Attendance is below 75% threshold (" + pct + "%)"
                ));
            }
            attendanceBuffer.add(itemMap);
        }

        // Check arrears and marks
        StudentDetails details = studentDetailsRepository.findByStudentId(s.getId()).orElse(null);
        double cgpa = details != null && details.getCgpa() != null ? details.getCgpa() : 8.75;
        int arrearCount = details != null && details.getArrearCount() != null ? details.getArrearCount() : 0;
        if (arrearCount > 0) {
            baseScore -= 10 * arrearCount;
            recommendations.add("Register for remedial tutorial sessions to clear " + arrearCount + " standing arrear(s)");
            risks.add(Map.of(
                    "subject", "Academic Backlog",
                    "level", "High",
                    "description", arrearCount + " standing arrear course(s)"
            ));
        }

        // Check assignments
        List<Assignment> assignments = assignmentRepository.findAll();
        long openAssignments = assignments.stream().filter(asg -> !"closed".equalsIgnoreCase(asg.getStatus())).count();
        if (openAssignments > 0) {
            recommendations.add("Complete and submit " + openAssignments + " active assignment(s) before upcoming deadlines");
        }

        // Check fees
        FeeInfo fee = feeRepository.findByStudentId(s.getId()).orElse(null);
        double pendingFee = fee != null && fee.getPending() != null ? fee.getPending() : 0.0;
        if (pendingFee > 0) {
            recommendations.add("Clear pending tuition fee of $" + String.format("%,.0f", pendingFee) + " before the semester exam window opens");
        }

        baseScore = Math.max(40, Math.min(100, baseScore));
        String standing = baseScore >= 85 ? "Strong standing" : (baseScore >= 70 ? "Satisfactory standing" : "Academic intervention needed");

        if (recommendations.isEmpty()) {
            recommendations.add("Maintain consistent study schedule and high attendance above 85%");
            recommendations.add("Explore technical hackathons and paper presentation symposiums");
        }

        resp.put("score", baseScore);
        resp.put("level", standing);
        resp.put("studentName", s.getName());
        resp.put("department", s.getDepartment());
        resp.put("year", s.getYear());
        resp.put("semester", s.getSemester());
        resp.put("cgpa", cgpa);
        resp.put("arrearCount", arrearCount);
        resp.put("openAssignments", openAssignments);
        resp.put("pendingFee", pendingFee);
        resp.put("mentor", details != null && details.getMentor() != null ? details.getMentor() : Map.of("name", "Dr. Alan Turing", "phone", "+1 555-0100"));
        resp.put("attendanceBuffer", attendanceBuffer);
        resp.put("recommendations", recommendations);
        resp.put("risks", risks);

        return ResponseEntity.ok(resp);
    }
}
