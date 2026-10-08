package com.meridian.smartcampus.controller;

import com.meridian.smartcampus.model.Student;
import com.meridian.smartcampus.model.Subject;
import com.meridian.smartcampus.model.TimetableEntry;
import com.meridian.smartcampus.model.User;
import com.meridian.smartcampus.repository.StudentRepository;
import com.meridian.smartcampus.repository.SubjectRepository;
import com.meridian.smartcampus.repository.TimetableRepository;
import com.meridian.smartcampus.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    private final TimetableRepository timetableRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    public static final List<String> DAYS = List.of(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    );

    public static final List<Map<String, Object>> PERIOD_METADATA = List.of(
            Map.of("period", 1, "startTime", "09:00 AM", "endTime", "09:45 AM", "duration", "45 min"),
            Map.of("period", 2, "startTime", "09:45 AM", "endTime", "10:30 AM", "duration", "45 min"),
            Map.of("period", 3, "startTime", "10:45 AM", "endTime", "11:30 AM", "duration", "45 min"),
            Map.of("period", 4, "startTime", "11:30 AM", "endTime", "12:15 PM", "duration", "45 min"),
            Map.of("period", 5, "startTime", "01:00 PM", "endTime", "01:45 PM", "duration", "45 min"),
            Map.of("period", 6, "startTime", "01:45 PM", "endTime", "02:30 PM", "duration", "45 min"),
            Map.of("period", 7, "startTime", "02:30 PM", "endTime", "03:15 PM", "duration", "45 min")
    );

    public TimetableController(TimetableRepository timetableRepository,
                               StudentRepository studentRepository,
                               UserRepository userRepository,
                               SubjectRepository subjectRepository) {
        this.timetableRepository = timetableRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        return ResponseEntity.ok(Map.of(
                "days", DAYS,
                "periods", PERIOD_METADATA,
                "periodDurationMinutes", 45,
                "totalPeriodsPerDay", 7
        ));
    }

    @GetMapping
    public ResponseEntity<List<TimetableEntry>> getTimetable(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer semester,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "student") String userRole,
            @RequestHeader(value = "X-Username", required = false) String username) {

        String dept = department;
        Integer sem = semester;

        // Auto-resolve for student
        if ((dept == null || sem == null) && "student".equalsIgnoreCase(userRole)) {
            Student s = resolveStudent(username);
            if (s != null) {
                if (dept == null) dept = s.getDepartment();
                if (sem == null) sem = s.getSemester();
            }
        }

        if (dept == null) dept = "CSE";
        if (sem == null) sem = 1;

        List<TimetableEntry> entries = timetableRepository.findByDepartmentAndSemesterOrderByPeriodAsc(dept.toUpperCase(), sem);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/teacher")
    public ResponseEntity<Map<String, Object>> getTeacherTimetable(
            @RequestParam(required = false) String username,
            @RequestHeader(value = "X-Username", required = false) String headerUsername) {

        String targetUser = username != null && !username.trim().isEmpty() ? username.trim() : headerUsername;
        if (targetUser == null || targetUser.isEmpty()) {
            targetUser = "teacher1cse";
        }

        final String finalTargetUser = targetUser;
        final String targetClean = targetUser.toLowerCase().trim();
        List<TimetableEntry> allEntries = timetableRepository.findAll();
        List<TimetableEntry> allocatedSlots = allEntries.stream()
                .filter(s -> {
                    if (s.getFacultyUsername() != null && s.getFacultyUsername().equalsIgnoreCase(targetClean)) return true;
                    if (s.getFacultyName() != null && s.getFacultyName().equalsIgnoreCase(finalTargetUser)) return true;
                    if (s.getFacultyName() != null && s.getFacultyName().toLowerCase().contains(targetClean)) return true;
                    if (s.getFacultyUsername() != null && targetClean.contains(s.getFacultyUsername().toLowerCase())) return true;
                    return false;
                }).toList();

        // Also build a 6-day x 7-period grid structure (empty if not allocated)
        Map<String, List<Map<String, Object>>> weekGrid = new LinkedHashMap<>();
        for (String day : DAYS) {
            List<Map<String, Object>> dayPeriods = new ArrayList<>();
            for (Map<String, Object> pMeta : PERIOD_METADATA) {
                int periodNum = (int) pMeta.get("period");
                TimetableEntry match = allocatedSlots.stream()
                        .filter(s -> day.equalsIgnoreCase(s.getDay()) && s.getPeriod() != null && s.getPeriod() == periodNum)
                        .findFirst().orElse(null);

                Map<String, Object> cell = new HashMap<>();
                cell.put("period", periodNum);
                cell.put("startTime", pMeta.get("startTime"));
                cell.put("endTime", pMeta.get("endTime"));
                cell.put("day", day);

                if (match != null) {
                    cell.put("allocated", true);
                    cell.put("slot", match);
                    cell.put("subjectCode", match.getSubjectCode());
                    cell.put("subjectName", match.getSubjectName());
                    cell.put("department", match.getDepartment());
                    cell.put("semester", match.getSemester());
                    cell.put("section", match.getSection() != null ? match.getSection() : "A");
                    cell.put("room", match.getRoom());
                    cell.put("classNumber", match.getRoom());
                    cell.put("className", match.getDepartment() + " Sem " + match.getSemester() + " (Sec " + (match.getSection() != null ? match.getSection() : "A") + ")");
                    cell.put("type", match.getType());
                } else {
                    cell.put("allocated", false);
                    cell.put("slot", null);
                    cell.put("subjectCode", null);
                    cell.put("subjectName", "Free Period / Office Hour");
                    cell.put("room", null);
                    cell.put("classNumber", null);
                }
                dayPeriods.add(cell);
            }
            weekGrid.put(day, dayPeriods);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("teacherUsername", targetUser);
        resp.put("allocatedSlots", allocatedSlots);
        resp.put("grid", weekGrid);
        resp.put("periods", PERIOD_METADATA);
        resp.put("days", DAYS);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/slot")
    public ResponseEntity<TimetableEntry> saveSlot(@RequestBody TimetableEntry entry) {
        if (entry.getDepartment() == null || entry.getSemester() == null || entry.getDay() == null || entry.getPeriod() == null) {
            return ResponseEntity.badRequest().build();
        }

        entry.setDepartment(entry.getDepartment().toUpperCase());

        // Set start/end times if missing from metadata
        int pIndex = entry.getPeriod() - 1;
        if (pIndex >= 0 && pIndex < PERIOD_METADATA.size()) {
            if (entry.getStartTime() == null || entry.getStartTime().isEmpty()) {
                entry.setStartTime((String) PERIOD_METADATA.get(pIndex).get("startTime"));
            }
            if (entry.getEndTime() == null || entry.getEndTime().isEmpty()) {
                entry.setEndTime((String) PERIOD_METADATA.get(pIndex).get("endTime"));
            }
        }

        // Check if slot already exists
        Optional<TimetableEntry> existingOpt = timetableRepository.findByDepartmentAndSemesterAndDayAndPeriod(
                entry.getDepartment(), entry.getSemester(), entry.getDay(), entry.getPeriod()
        );

        TimetableEntry toSave;
        if (existingOpt.isPresent()) {
            toSave = existingOpt.get();
            toSave.setSubjectCode(entry.getSubjectCode());
            toSave.setSubjectName(entry.getSubjectName());
            toSave.setFacultyName(entry.getFacultyName());
            toSave.setFacultyUsername(entry.getFacultyUsername());
            toSave.setRoom(entry.getRoom());
            toSave.setType(entry.getType() != null ? entry.getType() : "lecture");
            toSave.setStartTime(entry.getStartTime());
            toSave.setEndTime(entry.getEndTime());
        } else {
            toSave = entry;
            if (toSave.getNumericId() == null) {
                toSave.setNumericId(System.currentTimeMillis());
            }
        }

        TimetableEntry saved = timetableRepository.save(toSave);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/slot")
    public ResponseEntity<Map<String, Object>> deleteSlot(
            @RequestParam String department,
            @RequestParam Integer semester,
            @RequestParam String day,
            @RequestParam Integer period) {

        timetableRepository.deleteByDepartmentAndSemesterAndDayAndPeriod(department.toUpperCase(), semester, day, period);
        return ResponseEntity.ok(Map.of("success", true, "message", "Slot cleared successfully"));
    }

    private Student resolveStudent(String username) {
        if (username != null && !username.trim().isEmpty()) {
            User user = userRepository.findByUsernameIgnoreCase(username.trim()).orElse(null);
            if (user != null) {
                Optional<Student> studentOpt = studentRepository.findByEmail(user.getEmail());
                if (studentOpt.isPresent()) return studentOpt.get();
                List<Student> list = studentRepository.findByDepartment(user.getDepartment());
                if (!list.isEmpty()) return list.get(0);
            }
        }
        return studentRepository.findById(1L).orElse(null);
    }
}
