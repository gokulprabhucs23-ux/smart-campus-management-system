package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.FeeInfo;
import com.meridian.smartcampus.model.Student;
import com.meridian.smartcampus.repository.FeeRepository;
import com.meridian.smartcampus.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fees")
public class FeeController {

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;

    public FeeController(FeeRepository feeRepository, StudentRepository studentRepository) {
        this.feeRepository = feeRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public ResponseEntity<FeeInfo> getFees(
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestParam(value = "studentId", required = false) Long studentId) {

        Long resolvedStudentId = studentId;
        if (resolvedStudentId == null && username != null && !username.trim().isEmpty()) {
            String clean = username.trim().toLowerCase();
            Optional<Student> st = studentRepository.findByEmail(clean + "@meridian.edu");
            if (st.isEmpty()) {
                st = studentRepository.findAll().stream()
                        .filter(s -> s.getEmail() != null && s.getEmail().toLowerCase().contains(clean))
                        .findFirst();
            }
            if (st.isPresent()) {
                resolvedStudentId = st.get().getId();
            }
        }

        if (resolvedStudentId == null) {
            resolvedStudentId = 1L;
        }

        final Long targetStudentId = resolvedStudentId;
        return feeRepository.findByStudentId(targetStudentId).map(ResponseEntity::ok)
                .orElseGet(() -> {
                    FeeInfo defaultFee = new FeeInfo();
                    defaultFee.setStudentId(targetStudentId);
                    defaultFee.setTotal(60000.0);
                    defaultFee.setPaid(45000.0);
                    defaultFee.setPending(15000.0);
                    defaultFee.setHistory(List.of(
                            Map.of("id", 1, "date", "2024-08-10", "amount", 15000, "method", "Online Banking", "status", "Success", "academicYear", "Year 1"),
                            Map.of("id", 2, "date", "2025-08-12", "amount", 15000, "method", "Card Payment", "status", "Success", "academicYear", "Year 2"),
                            Map.of("id", 3, "date", "2026-08-05", "amount", 15000, "method", "Wire Transfer", "status", "Success", "academicYear", "Year 3")
                    ));
                    List<Map<String, Object>> fallbackBreakdown = new ArrayList<>();
                    Map<String, Object> y1 = new LinkedHashMap<>();
                    y1.put("year", 1);
                    y1.put("label", "Year 1 (Sem 1 & 2)");
                    y1.put("tuition", 12000);
                    y1.put("lab", 1500);
                    y1.put("exam", 1000);
                    y1.put("library", 500);
                    y1.put("total", 15000);
                    y1.put("paid", 15000);
                    y1.put("pending", 0);
                    y1.put("status", "Paid");
                    y1.put("receiptId", "REC-2023-Y1");
                    fallbackBreakdown.add(y1);

                    Map<String, Object> y2 = new LinkedHashMap<>();
                    y2.put("year", 2);
                    y2.put("label", "Year 2 (Sem 3 & 4)");
                    y2.put("tuition", 12000);
                    y2.put("lab", 1500);
                    y2.put("exam", 1000);
                    y2.put("library", 500);
                    y2.put("total", 15000);
                    y2.put("paid", 15000);
                    y2.put("pending", 0);
                    y2.put("status", "Paid");
                    y2.put("receiptId", "REC-2024-Y2");
                    fallbackBreakdown.add(y2);

                    Map<String, Object> y3 = new LinkedHashMap<>();
                    y3.put("year", 3);
                    y3.put("label", "Year 3 (Sem 5 & 6)");
                    y3.put("tuition", 12000);
                    y3.put("lab", 1500);
                    y3.put("exam", 1000);
                    y3.put("library", 500);
                    y3.put("total", 15000);
                    y3.put("paid", 15000);
                    y3.put("pending", 0);
                    y3.put("status", "Paid");
                    y3.put("receiptId", "REC-2025-Y3");
                    fallbackBreakdown.add(y3);

                    Map<String, Object> y4 = new LinkedHashMap<>();
                    y4.put("year", 4);
                    y4.put("label", "Year 4 (Sem 7 & 8)");
                    y4.put("tuition", 12000);
                    y4.put("lab", 1500);
                    y4.put("exam", 1000);
                    y4.put("library", 500);
                    y4.put("total", 15000);
                    y4.put("paid", 0);
                    y4.put("pending", 15000);
                    y4.put("status", "Due");
                    y4.put("receiptId", "-");
                    fallbackBreakdown.add(y4);

                    defaultFee.setYearlyBreakdown(fallbackBreakdown);
                    return ResponseEntity.ok(defaultFee);
                });
    }
}
