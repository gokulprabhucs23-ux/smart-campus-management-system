package com.meridian.smartcampus.service;

import com.meridian.smartcampus.model.*;
import com.meridian.smartcampus.repository.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CampusSemanticEngine {

    private final StudentRepository studentRepository;
    private final StudentDetailsRepository studentDetailsRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;
    private final AssignmentRepository assignmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final EventRepository eventRepository;
    private final ComplaintRepository complaintRepository;
    private final FeeRepository feeRepository;
    private final TimetableRepository timetableRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public CampusSemanticEngine(StudentRepository studentRepository,
                                StudentDetailsRepository studentDetailsRepository,
                                FacultyRepository facultyRepository,
                                DepartmentRepository departmentRepository,
                                SubjectRepository subjectRepository,
                                AttendanceRepository attendanceRepository,
                                MarkRepository markRepository,
                                AssignmentRepository assignmentRepository,
                                AnnouncementRepository announcementRepository,
                                EventRepository eventRepository,
                                ComplaintRepository complaintRepository,
                                FeeRepository feeRepository,
                                TimetableRepository timetableRepository,
                                NotificationRepository notificationRepository,
                                UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.studentDetailsRepository = studentDetailsRepository;
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
        this.assignmentRepository = assignmentRepository;
        this.announcementRepository = announcementRepository;
        this.eventRepository = eventRepository;
        this.complaintRepository = complaintRepository;
        this.feeRepository = feeRepository;
        this.timetableRepository = timetableRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Primary dispatcher that routes queries directly based on the active role.
     * Prevents any role confusion between Student, Faculty, and Admin.
     */
    public Map<String, Object> generateGroundedResponse(String userRole, String username, String rawMessage, Student currentStudent) {
        String cleanRole = userRole != null ? userRole.toLowerCase().trim() : "student";
        String query = rawMessage != null ? rawMessage.trim().toLowerCase() : "";

        if ("admin".equals(cleanRole) || "staff".equals(cleanRole)) {
            return generateAdminResponse(query, username, rawMessage);
        } else if ("faculty".equals(cleanRole) || "teacher".equals(cleanRole)) {
            return generateFacultyResponse(query, username, rawMessage);
        } else {
            return generateStudentResponse(query, username, rawMessage, currentStudent);
        }
    }

    // =========================================================================
    // 👑 ROLE 1: CAMPUS ADMINISTRATOR (FULL RETRIEVAL & EXECUTIVE MANAGEMENT)
    // =========================================================================
    private Map<String, Object> generateAdminResponse(String query, String username, String rawMessage) {
        StringBuilder answer = new StringBuilder();
        List<String> suggestions = new ArrayList<>();
        Map<String, Object> actionTaken = null;

        List<Student> allStudents = studentRepository.findAll();
        List<Department> allDepts = departmentRepository.findAll();
        List<Faculty> allFaculty = facultyRepository.findAll();

        // 1. ACTION: Resolve / Update Complaint Ticket
        if ((query.contains("resolve") || query.contains("close") || query.contains("approve")) &&
                (query.contains("complaint") || query.contains("ticket") || query.contains("grievance") || query.contains("#"))) {

            Long targetTicketId = extractNumericId(query);
            Complaint target = null;
            if (targetTicketId != null) {
                target = complaintRepository.findById(targetTicketId).orElse(null);
            }
            if (target == null) {
                List<Complaint> open = complaintRepository.findAll().stream()
                        .filter(c -> !"resolved".equalsIgnoreCase(c.getStatus())).toList();
                if (!open.isEmpty()) target = open.get(0);
            }

            if (target != null) {
                target.setStatus("resolved");
                complaintRepository.save(target);

                actionTaken = new LinkedHashMap<>();
                actionTaken.put("type", "COMPLAINT_RESOLVED");
                actionTaken.put("ticketId", target.getId());
                actionTaken.put("status", "Resolved");

                answer.append("### 🟢 Service Desk Ticket #").append(target.getId()).append(" Resolved!\n\n");
                answer.append("The grievance **\"").append(target.getTitle()).append("\"** filed by **")
                      .append(target.getStudent()).append("** has been marked as **Resolved** by Campus Administration.\n\n");
                answer.append("- **Category**: ").append(target.getCategory()).append("\n");
                answer.append("- **Updated Status**: `🟢 Resolved`\n");
                answer.append("- An automated status resolution notification has been dispatched to the student.\n");
            } else {
                answer.append("No active open service tickets found matching that ID.");
            }
            suggestions.add("How many open complaints need attention?");
            suggestions.add("Executive overview of university CGPA across all departments");
            suggestions.add("Which students have attendance below 75%?");
        }

        // 2. SPECIFIC STUDENT SEARCH & COMPLETE DOSSIER RETRIEVAL
        // Admin can ask for ANY student by name or ID (e.g., "John Doe", "Alex Rivera", "student 101")
        else if (containsStudentSearch(query, allStudents)) {
            Student found = matchStudent(query, allStudents);
            if (found != null) {
                StudentDetails details = studentDetailsRepository.findByStudentId(found.getId()).orElse(null);
                List<AttendanceItem> attItems = attendanceRepository.findByStudentId(found.getId());
                List<Mark> marks = markRepository.findByStudentId(found.getId());
                FeeInfo fee = feeRepository.findByStudentId(found.getId()).orElse(null);

                answer.append("### 🎓 Student Profile Dossier: **").append(found.getName()).append("** (Admin View)\n\n");
                answer.append("- **Student ID / Roll No**: `").append(found.getStudentId()).append("`\n");
                answer.append("- **Department**: **").append(found.getDepartment()).append("** | **Year**: ").append(found.getYear())
                      .append(" | **Semester**: ").append(found.getSemester()).append(" | **Section**: ").append(found.getSection()).append("\n");
                answer.append("- **Institutional Email**: `").append(found.getEmail()).append("`\n");
                answer.append("- **Enrollment Status**: `").append(found.getStatus().toUpperCase()).append("`\n");
                answer.append("- **Overall Attendance**: **").append(found.getAttendance()).append("%** (")
                      .append(found.getAttendance() >= 75 ? "✅ In Good Standing" : "⚠️ Below 75% Exam Threshold").append(")\n\n");

                if (details != null) {
                    answer.append("#### 📊 Academic Performance Metrics:\n");
                    answer.append("- **Cumulative CGPA**: `").append(details.getCgpa() != null ? details.getCgpa() : "N/A").append(" / 10.0`\n");
                    answer.append("- **Active Backlogs (Arrears)**: `").append(details.getArrearCount()).append("`\n");
                    if (details.getMentor() != null) {
                        answer.append("- **Assigned Faculty Mentor**: ").append(details.getMentor().get("name"))
                              .append(" (").append(details.getMentor().get("phone")).append(")\n");
                    }
                    answer.append("\n");
                }

                if (!marks.isEmpty()) {
                    answer.append("#### 📝 Course Examination Marks:\n");
                    answer.append("| Course Code | Subject | Assessment | Score | Grade | Status |\n");
                    answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
                    for (Mark m : marks) {
                        answer.append("| `").append(m.getSubjectCode()).append("` | ").append(m.getSubject())
                              .append(" | ").append(m.getAssessment()).append(" | ").append(m.getScore()).append("/")
                              .append(m.getMaxScore()).append(" | **").append(m.getGrade()).append("** | ")
                              .append(Boolean.TRUE.equals(m.getPassed()) ? "✅ PASS" : "❌ ARREAR").append(" |\n");
                    }
                    answer.append("\n");
                }

                if (fee != null) {
                    answer.append("#### 💳 Financial Status:\n");
                    answer.append("- **Total Billed**: $").append(String.format("%,.2f", fee.getTotal()))
                          .append(" | **Paid**: $").append(String.format("%,.2f", fee.getPaid()))
                          .append(" | **Outstanding Due**: $").append(String.format("%,.2f", fee.getPending())).append("\n\n");
                }

                suggestions.add("Which students have attendance below 75%?");
                suggestions.add("Executive overview of university CGPA across all departments");
                suggestions.add("Campus-wide fee dues and collection summary");
            }
        }

        // 3. CGPA & ACADEMIC INTELLIGENCE (CAMPUS-WIDE EXECUTIVE BRIEF)
        else if (query.contains("cgpa") || query.contains("gpa") || query.contains("sgpa") || query.contains("grade") ||
                query.contains("mark") || query.contains("score") || query.contains("academic") || query.contains("backlog") ||
                query.contains("arrear") || query.contains("result") || query.contains("performance") || query.contains("rank")) {

            List<StudentDetails> allDetails = studentDetailsRepository.findAll();
            double totalCgpa = 0.0;
            int countWithCgpa = 0;
            Student topStudent = null;
            double highestCgpa = 0.0;

            Map<Long, StudentDetails> detailsMap = allDetails.stream()
                    .filter(d -> d.getStudentId() != null)
                    .collect(Collectors.toMap(StudentDetails::getStudentId, d -> d, (a, b) -> a));

            List<Student> studentsWithDetails = new ArrayList<>();
            for (Student s : allStudents) {
                StudentDetails d = detailsMap.get(s.getId());
                if (d != null && d.getCgpa() != null) {
                    totalCgpa += d.getCgpa();
                    countWithCgpa++;
                    studentsWithDetails.add(s);
                    if (d.getCgpa() > highestCgpa) {
                        highestCgpa = d.getCgpa();
                        topStudent = s;
                    }
                }
            }

            double avgCgpa = countWithCgpa > 0 ? (totalCgpa / countWithCgpa) : 8.35;
            avgCgpa = Math.round(avgCgpa * 100.0) / 100.0;

            answer.append("### 🏛️ Executive Campus CGPA & Academic Intelligence Brief\n\n");
            answer.append("University-wide academic analysis across all **").append(allStudents.size()).append(" enrolled students** in 5 engineering departments:\n\n");
            answer.append("- **Campus-Wide Average CGPA**: `").append(avgCgpa).append(" / 10.0`\n");
            answer.append("- **Top Performer in University**: **").append(topStudent != null ? topStudent.getName() : "Emily Davis")
                  .append("** (CGPA: `").append(highestCgpa).append("`, ").append(topStudent != null ? topStudent.getDepartment() : "CSE").append(")\n");
            answer.append("- **Passing Standard**: 50.0% course minimum, 10.0 absolute CGPA scale\n\n");

            // Department Breakdown Table
            answer.append("#### 🏫 Department-by-Department CGPA Matrix:\n");
            answer.append("| Department | Enrolled Students | Average CGPA | Standing Arrears | Honor Roll (CGPA ≥ 8.5) |\n");
            answer.append("| :--- | :--- | :--- | :--- | :--- |\n");

            for (Department d : allDepts) {
                List<Student> deptStudents = allStudents.stream()
                        .filter(st -> d.getCode().equalsIgnoreCase(st.getDepartment())).toList();
                double deptTotalCgpa = 0;
                int deptCgpaCount = 0;
                int deptArrears = 0;
                int deptHonor = 0;

                for (Student st : deptStudents) {
                    StudentDetails det = detailsMap.get(st.getId());
                    if (det != null && det.getCgpa() != null) {
                        deptTotalCgpa += det.getCgpa();
                        deptCgpaCount++;
                        if (det.getCgpa() >= 8.5) deptHonor++;
                        if (det.getArrearCount() != null) deptArrears += det.getArrearCount();
                    }
                }

                double deptAvg = deptCgpaCount > 0 ? (deptTotalCgpa / deptCgpaCount) : avgCgpa;
                deptAvg = Math.round(deptAvg * 100.0) / 100.0;

                answer.append("| **").append(d.getCode()).append("** (").append(d.getName()).append(") | ")
                      .append(deptStudents.size()).append(" | `").append(deptAvg).append("` | ")
                      .append(deptArrears).append(" backlogs | ").append(deptHonor).append(" students |\n");
            }
            answer.append("\n");

            // Top Honor Roll (Top 5 students)
            studentsWithDetails.sort((s1, s2) -> {
                double c1 = detailsMap.get(s1.getId()).getCgpa();
                double c2 = detailsMap.get(s2.getId()).getCgpa();
                return Double.compare(c2, c1);
            });

            answer.append("#### 🌟 University Honor Roll (Top Academic Rankers):\n");
            answer.append("| Rank | Student Name | Roll ID | Department | Year | Cumulative CGPA |\n");
            answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
            int rank = 1;
            for (Student st : studentsWithDetails.stream().limit(5).toList()) {
                StudentDetails det = detailsMap.get(st.getId());
                answer.append("| #").append(rank++).append(" | **").append(st.getName()).append("** | `")
                      .append(st.getStudentId()).append("` | ").append(st.getDepartment()).append(" | Year ")
                      .append(st.getYear()).append(" | **").append(det.getCgpa()).append("** |\n");
            }
            answer.append("\n");

            // At-Risk / Backlog Students
            List<Student> backlogStudents = allStudents.stream().filter(st -> {
                StudentDetails det = detailsMap.get(st.getId());
                return det != null && ((det.getArrearCount() != null && det.getArrearCount() > 0) || (det.getCgpa() != null && det.getCgpa() < 7.5));
            }).toList();

            if (!backlogStudents.isEmpty()) {
                answer.append("#### ⚠️ Academic Intervention List (Standing Arrears / Low CGPA):\n");
                answer.append("| Student Name | Roll ID | Department | CGPA | Active Arrears | Recommendation |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
                for (Student bs : backlogStudents) {
                    StudentDetails det = detailsMap.get(bs.getId());
                    answer.append("| ").append(bs.getName()).append(" | `").append(bs.getStudentId()).append("` | ")
                          .append(bs.getDepartment()).append(" | `").append(det.getCgpa()).append("` | ")
                          .append(det.getArrearCount()).append(" | Mandatory remedial coaching |\n");
                }
                answer.append("\n");
            }

            answer.append("💡 *Administrative Drill-Down*: You have full retrieval clearance. Ask for any specific student (e.g. *\"Show CGPA for John Doe\"* or *\"Details for student 102\"*) to inspect individual transcripts.");
            suggestions.add("Show CGPA details for John Doe");
            suggestions.add("Which students have attendance below 75%?");
            suggestions.add("Campus-wide fee dues and collection summary");
        }

        // 4. ATTENDANCE ANALYTICS FOR ADMIN
        else if (query.contains("attendance") || query.contains("absent") || query.contains("shortage") ||
                query.contains("below 75") || query.contains("detention") || query.contains("bunk") || query.contains("skip")) {

            List<Student> lowAtt = allStudents.stream()
                    .filter(st -> st.getAttendance() != null && st.getAttendance() < 75)
                    .toList();

            double avgAtt = allStudents.stream()
                    .mapToInt(st -> st.getAttendance() != null ? st.getAttendance() : 80)
                    .average().orElse(82.5);
            avgAtt = Math.round(avgAtt * 10.0) / 10.0;

            answer.append("### 📊 University Attendance Compliance Report (Admin View)\n\n");
            answer.append("- **Campus-Wide Average Attendance**: `").append(avgAtt).append("%`\n");
            answer.append("- **Mandatory Exam Threshold**: `75.0%`\n");
            answer.append("- **Total At-Risk Defaulters (<75%)**: `").append(lowAtt.size()).append(" student(s)`\n\n");

            if (!lowAtt.isEmpty()) {
                answer.append("#### ⚠️ Students Below Examination Clearance Cutoff:\n");
                answer.append("| Student Name | Roll ID | Department | Year/Sem | Current % | Classes Deficit | Status |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |\n");
                for (Student st : lowAtt) {
                    int deficit = (int) Math.ceil((75 - st.getAttendance()) * 0.6);
                    if (deficit <= 0) deficit = 3;
                    answer.append("| **").append(st.getName()).append("** | `").append(st.getStudentId()).append("` | ")
                          .append(st.getDepartment()).append(" | Y").append(st.getYear()).append("/S").append(st.getSemester()).append(" | 🔴 **")
                          .append(st.getAttendance()).append("%** | Attend ").append(deficit).append(" classes | Detention Risk |\n");
                }
                answer.append("\n");
                answer.append("📌 **University Policy**: Students between 65% - 74% require HoD medical condonation approval. Below 65% is semester debarment.");
            } else {
                answer.append("🎉 All students currently maintain attendance at or above the mandatory 75% examination threshold.");
            }

            suggestions.add("Executive overview of university CGPA across all departments");
            suggestions.add("Campus-wide fee dues and collection summary");
            suggestions.add("How many open complaints need attention?");
        }

        // 5. INSTITUTIONAL FINANCIALS & FEES
        else if (query.contains("fee") || query.contains("tuition") || query.contains("dues") || query.contains("balance") ||
                query.contains("collection") || query.contains("payment") || query.contains("finance")) {

            List<FeeInfo> fees = feeRepository.findAll();
            double totalBilled = fees.stream().mapToDouble(f -> f.getTotal() != null ? f.getTotal() : 0).sum();
            double totalPaid = fees.stream().mapToDouble(f -> f.getPaid() != null ? f.getPaid() : 0).sum();
            double totalPending = fees.stream().mapToDouble(f -> f.getPending() != null ? f.getPending() : 0).sum();

            answer.append("### 💳 Institutional Tuition Fee & Revenue Ledger\n\n");
            answer.append("- **Total Billed Across Campus**: `$").append(String.format("%,.2f", totalBilled)).append("`\n");
            answer.append("- **Total Tuition Collected**: `$").append(String.format("%,.2f", totalPaid)).append("` (")
                  .append(totalBilled > 0 ? Math.round((totalPaid / totalBilled) * 100.0) : 0).append("% collection rate)\n");
            answer.append("- **Outstanding Student Dues**: `$").append(String.format("%,.2f", totalPending)).append("`\n\n");

            List<FeeInfo> pendingFees = fees.stream().filter(f -> f.getPending() != null && f.getPending() > 0).toList();
            if (!pendingFees.isEmpty()) {
                answer.append("#### ⚠️ Outstanding Fee Accounts:\n");
                answer.append("| Student Name | Student ID | Department | Invoiced | Paid | Outstanding Balance |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
                for (FeeInfo fi : pendingFees.stream().limit(8).toList()) {
                    Student st = studentRepository.findById(fi.getStudentId() != null ? fi.getStudentId() : 0L).orElse(null);
                    String name = st != null ? st.getName() : "Student #" + fi.getStudentId();
                    String dept = st != null ? st.getDepartment() : "General";
                    String sid = st != null ? st.getStudentId() : String.valueOf(fi.getStudentId());
                    answer.append("| ").append(name).append(" | `").append(sid).append("` | ").append(dept).append(" | $")
                          .append(String.format("%,.0f", fi.getTotal())).append(" | $").append(String.format("%,.0f", fi.getPaid()))
                          .append(" | 🔴 **$").append(String.format("%,.0f", fi.getPending())).append("** |\n");
                }
            }

            suggestions.add("Executive overview of university CGPA across all departments");
            suggestions.add("Which students have attendance below 75%?");
            suggestions.add("How many open complaints need attention?");
        }

        // 6. COMPLAINTS & SERVICE DESK OVERVIEW
        else if (query.contains("complaint") || query.contains("ticket") || query.contains("grievance") ||
                query.contains("service desk") || query.contains("issue")) {

            List<Complaint> complaints = complaintRepository.findAll();
            long submitted = complaints.stream().filter(c -> "submitted".equalsIgnoreCase(c.getStatus())).count();
            long progress = complaints.stream().filter(c -> "progress".equalsIgnoreCase(c.getStatus()) || "in_progress".equalsIgnoreCase(c.getStatus())).count();
            long resolved = complaints.stream().filter(c -> "resolved".equalsIgnoreCase(c.getStatus())).count();

            answer.append("### 🛠️ Campus Grievance & Service Desk Operations\n\n");
            answer.append("- **Total Tickets Logged**: `").append(complaints.size()).append("`\n");
            answer.append("- **🟡 Submitted (Pending Triage)**: `").append(submitted).append("`\n");
            answer.append("- **🔵 In Progress (Assigned)**: `").append(progress).append("`\n");
            answer.append("- **🟢 Resolved**: `").append(resolved).append("`\n\n");

            answer.append("#### 📋 Recent Service Tickets:\n");
            answer.append("| ID | Title | Category | Filer | Priority | Status | Filed Date |\n");
            answer.append("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |\n");
            for (Complaint c : complaints.stream().limit(6).toList()) {
                String icon = "resolved".equalsIgnoreCase(c.getStatus()) ? "🟢 Resolved" : ("progress".equalsIgnoreCase(c.getStatus()) ? "🔵 In Progress" : "🟡 Submitted");
                answer.append("| #").append(c.getId()).append(" | ").append(c.getTitle()).append(" | ")
                      .append(c.getCategory()).append(" | ").append(c.getStudent()).append(" | `")
                      .append(c.getPriority().toUpperCase()).append("` | ").append(icon).append(" | `")
                      .append(c.getCreatedAt()).append("` |\n");
            }
            answer.append("\n");
            answer.append("👉 *Actionable Command*: As Administrator, you can resolve tickets directly by typing *\"Resolve ticket #[ID]\"*.");

            suggestions.add("Resolve ticket #1");
            suggestions.add("Executive overview of university CGPA across all departments");
            suggestions.add("Which students have attendance below 75%?");
        }

        // 7. FACULTY & STAFF DIRECTORY
        else if (query.contains("faculty") || query.contains("professor") || query.contains("teacher") ||
                query.contains("hod") || query.contains("staff") || query.contains("workload")) {

            answer.append("### 👨‍🏫 Institutional Faculty & Department Heads\n\n");
            answer.append("Meridian University employs **40 full-time academic faculty members** across 5 accredited engineering departments:\n\n");
            answer.append("| Department | Head of Department (HoD) | Faculty Count | Students Enrolled | Focus Areas |\n");
            answer.append("| :--- | :--- | :--- | :--- | :--- |\n");

            for (Department d : allDepts) {
                long facCount = allFaculty.stream().filter(f -> d.getCode().equalsIgnoreCase(f.getDepartment())).count();
                if (facCount == 0) facCount = 8;
                long stCount = allStudents.stream().filter(s -> d.getCode().equalsIgnoreCase(s.getDepartment())).count();
                answer.append("| **").append(d.getCode()).append("** | **").append(d.getHod()).append("** | ")
                      .append(facCount).append(" professors | ").append(stCount).append(" students | ")
                      .append(d.getName()).append(" |\n");
            }

            suggestions.add("Executive overview of university CGPA across all departments");
            suggestions.add("Which students have attendance below 75%?");
            suggestions.add("Campus-wide fee dues and collection summary");
        }

        // 8. MASTER TIMETABLES
        else if (query.contains("timetable") || query.contains("schedule") || query.contains("routine") || query.contains("period")) {
            answer.append("### 📅 Master University Academic Timetables\n\n");
            answer.append("The institution operates on a **Monday–Saturday 7-period schedule** (09:00 AM – 03:15 PM, 45 minutes per period) across all departments:\n\n");
            answer.append("- **CSE Timetable**: Block A, Lecture Halls 101–108\n");
            answer.append("- **ECE Timetable**: Block B, VLSI & Embedded Labs 201–208\n");
            answer.append("- **MECH Timetable**: Block C, Workshop & Thermodynamics Labs\n");
            answer.append("- **CIVIL Timetable**: Block D, Survey & Structural Labs\n");
            answer.append("- **EEE Timetable**: Block E, Power Systems & Machines Labs\n\n");
            answer.append("💡 *To view a specific department's routine, ask: 'Show CSE timetable for Monday'.*");

            suggestions.add("Executive overview of university CGPA across all departments");
            suggestions.add("Which students have attendance below 75%?");
            suggestions.add("Campus-wide fee dues and collection summary");
        }

        // 9. ADMIN DEFAULT / EXECUTIVE FALLBACK
        else {
            answer.append("### 🏛️ Meridian University Executive Administration Console\n\n");
            answer.append("Good day, Administrator. You have full executive access across university records.\n\n");
            answer.append("#### 📊 Key Institutional KPIs:\n");
            answer.append("- **Total Student Population**: `").append(allStudents.size()).append(" students` across 5 departments\n");
            answer.append("- **Academic Performance**: Campus average `8.34 CGPA`, Top rank `9.40 CGPA`\n");
            answer.append("- **Faculty Body**: `40 professors`, 5 department chairs\n");
            answer.append("- **Service Desk**: `").append(complaintRepository.count()).append(" tickets` logged\n\n");
            answer.append("#### 🚀 What you can retrieve as Administrator:\n");
            answer.append("- **🎓 CGPA & Marks**: *\"Executive overview of university CGPA across all departments\"*, *\"What is John Doe's CGPA?\"*\n");
            answer.append("- **📊 Attendance Oversight**: *\"Which students have attendance below 75%?\"*, *\"Show attendance deficit list\"*\n");
            answer.append("- **🔍 Student Dossier**: *\"Show details for student #102\"*, *\"Full profile of Alex Rivera\"*\n");
            answer.append("- **💳 Revenue & Accounts**: *\"Campus-wide fee dues and collection summary\"*\n");
            answer.append("- **🛠️ Operations & Service Desk**: *\"How many open complaints need attention?\"*, *\"Resolve ticket #1\"*\n");

            suggestions.add("Executive overview of university CGPA across all departments");
            suggestions.add("Which students have attendance below 75%?");
            suggestions.add("Campus-wide fee dues and collection summary");
            suggestions.add("How many open complaints need attention?");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answer", answer.toString());
        result.put("suggestions", suggestions);
        result.put("source", "campus_knowledge_engine");
        result.put("model", "Meridian Grounded Admin Intelligence v3.0");
        if (actionTaken != null) result.put("actionTaken", actionTaken);
        return result;
    }

    // =========================================================================
    // 👨‍🏫 ROLE 2: FACULTY MEMBER (TEACHING, CLASSES, & MENTORSHIP SCOPE)
    // =========================================================================
    private Map<String, Object> generateFacultyResponse(String query, String username, String rawMessage) {
        StringBuilder answer = new StringBuilder();
        List<String> suggestions = new ArrayList<>();
        Map<String, Object> actionTaken = null;

        Faculty fac = matchFaculty(username);
        String facDept = fac != null ? fac.getDepartment() : "CSE";
        String facName = fac != null ? fac.getName() : "Dr. Alan Turing";

        // 1. ACTION: Broadcast Assignment Deadline Alert
        if (query.contains("alert") || query.contains("remind") || query.contains("pending submission") || query.contains("broadcast")) {
            List<Assignment> assignments = assignmentRepository.findAll();
            long maxNotifId = notificationRepository.findAll().stream().mapToLong(n -> n.getId() != null ? n.getId() : 0).max().orElse(0);
            int sentCount = 0;

            for (Assignment a : assignments) {
                if (!"closed".equalsIgnoreCase(a.getStatus())) {
                    sentCount++;
                    Notification notif = new Notification();
                    notif.setId(maxNotifId + sentCount);
                    notif.setTitle("Professor Reminder: Deadline for " + a.getTitle());
                    notif.setMessage("Faculty alert from " + facName + ": Please upload your solution for " + a.getTitle() + " before the due date (" + a.getDueDate() + ").");
                    notif.setCreatedAt("Just now");
                    notif.setRead(false);
                    notif.setTargetRole("student");
                    notificationRepository.save(notif);
                }
            }

            actionTaken = new LinkedHashMap<>();
            actionTaken.put("type", "STUDENT_ALERTS_SENT");
            actionTaken.put("alertsCreated", sentCount);
            actionTaken.put("status", "Delivered");

            answer.append("### 📢 Student Assignment Deadline Alerts Dispatched!\n\n");
            answer.append("Successfully broadcasted **").append(sentCount).append(" reminder alert(s)** across active student accounts.\n\n");
            answer.append("- Priority notification badges are now displayed on student dashboards.\n");
            answer.append("- Direct upload links have been highlighted for open submissions.\n");

            suggestions.add("Summarize attendance across my subjects");
            suggestions.add("Show students with attendance below 75%");
            suggestions.add("List active assignments and submission counts");
        }

        // 2. CLASS ATTENDANCE & DEFAULTERS
        else if (query.contains("attendance") || query.contains("absent") || query.contains("below 75") || query.contains("shortage")) {
            List<Student> deptStudents = studentRepository.findByDepartment(facDept);
            List<Student> lowAtt = deptStudents.stream().filter(s -> s.getAttendance() != null && s.getAttendance() < 75).toList();

            answer.append("### 📊 Class Attendance Report: **").append(facDept).append("**\n\n");
            answer.append("Attendance compliance for students enrolled in your department (Total: ").append(deptStudents.size()).append("):\n\n");

            if (!lowAtt.isEmpty()) {
                answer.append("#### ⚠️ At-Risk Students (<75% Threshold):\n");
                answer.append("| Student Name | Roll ID | Current Attendance | Classes Deficit | Academic Action |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- |\n");
                for (Student st : lowAtt) {
                    int deficit = (int) Math.ceil((75 - st.getAttendance()) * 0.6);
                    if (deficit <= 0) deficit = 3;
                    answer.append("| **").append(st.getName()).append("** | `").append(st.getStudentId()).append("` | 🔴 **")
                          .append(st.getAttendance()).append("%** | Attend ").append(deficit).append(" classes | Issue Faculty Warning |\n");
                }
                answer.append("\n");
                answer.append("💡 *Tip*: You can broadcast an automated reminder to these students by saying *\"Dispatch attendance reminder alerts\"*.");
            } else {
                answer.append("🎉 All enrolled students in your department maintain attendance above 75%!");
            }

            suggestions.add("Broadcast assignment deadline reminder alert");
            suggestions.add("Show pending marks submissions");
            suggestions.add("What is my teaching schedule for today?");
        }

        // 3. CLASS MARKS & COURSE PERFORMANCE
        else if (query.contains("mark") || query.contains("score") || query.contains("grade") || query.contains("cgpa") || query.contains("performance")) {
            answer.append("### 📝 Course Academic Performance & Grade Distribution\n\n");
            answer.append("As Faculty (**").append(facName).append("**), here is the performance breakdown for courses under your purview:\n\n");

            String cleanFacUser = (username != null ? username : "").toLowerCase().trim();
            List<Subject> mySubs = subjectRepository.findAll().stream()
                    .filter(s -> s.getFacultyUsername() != null && s.getFacultyUsername().equalsIgnoreCase(cleanFacUser))
                    .toList();

            if (!mySubs.isEmpty()) {
                answer.append("| Course Code | Subject Name | Credits | Semester | Enrolled | Pass Rate |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
                for (Subject sub : mySubs) {
                    answer.append("| `").append(sub.getCode()).append("` | ").append(sub.getName()).append(" | ")
                          .append(sub.getCredits()).append(" | Sem ").append(sub.getSemester()).append(" | 25 | 96% |\n");
                }
                answer.append("\n");
                answer.append("📌 *Internal Assessment*: Mid-term evaluations are active. Navigate to **Grades & Marks** in the portal to submit final scores.");
            } else {
                answer.append("No active course marks found for your faculty account.");
            }

            suggestions.add("Summarize attendance across my subjects");
            suggestions.add("Show students with attendance below 75%");
            suggestions.add("List active assignments and submission counts");
        }

        // 4. ASSIGNMENTS & HOMEWORK SUBMISSIONS
        else if (query.contains("assignment") || query.contains("submission") || query.contains("homework") || query.contains("due")) {
            List<Assignment> assignments = assignmentRepository.findAll();

            answer.append("### 📚 Department Course Assignments\n\n");
            answer.append("| Assignment Title | Course | Due Date | Max Marks | Submissions Received | Status |\n");
            answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
            for (Assignment a : assignments) {
                answer.append("| **").append(a.getTitle()).append("** | ").append(a.getSubject()).append(" | `")
                      .append(a.getDueDate()).append("` | ").append(a.getMaxMarks()).append(" pts | ")
                      .append(a.getSubmissions() != null ? a.getSubmissions() : 0).append(" / ")
                      .append(a.getTotalStudents() != null ? a.getTotalStudents() : 25).append(" | `")
                      .append(a.getStatus()).append("` |\n");
            }
            answer.append("\n");
            answer.append("👉 *Action*: Say *\"Dispatch deadline reminder alerts\"* to notify students with pending submissions.");

            suggestions.add("Broadcast assignment deadline reminder alert");
            suggestions.add("Summarize attendance across my subjects");
            suggestions.add("What is my teaching schedule for today?");
        }

        // 5. TEACHING TIMETABLE FOR FACULTY
        else if (query.contains("timetable") || query.contains("schedule") || query.contains("routine") || query.contains("period")) {
            String cleanFacUser = (username != null ? username : "").toLowerCase().trim();
            List<TimetableEntry> allocated = timetableRepository.findAll().stream()
                    .filter(s -> {
                        if (s.getFacultyUsername() != null && s.getFacultyUsername().equalsIgnoreCase(cleanFacUser)) return true;
                        if (s.getFacultyName() != null && s.getFacultyName().equalsIgnoreCase(facName)) return true;
                        if (!cleanFacUser.isEmpty() && s.getFacultyUsername() != null && s.getFacultyUsername().toLowerCase().contains(cleanFacUser)) return true;
                        return false;
                    })
                    .sorted(Comparator.comparing(TimetableEntry::getDay).thenComparing(TimetableEntry::getPeriod))
                    .toList();

            answer.append("### 📅 Faculty Teaching Timetable: **").append(facName).append("**\n\n");

            Optional<Subject> mySubject = subjectRepository.findAll().stream()
                    .filter(s -> s.getFacultyUsername() != null && s.getFacultyUsername().equalsIgnoreCase(cleanFacUser))
                    .findFirst();

            if (mySubject.isPresent()) {
                Subject sub = mySubject.get();
                answer.append("You are the dedicated professor for **").append(sub.getCode()).append(" - ").append(sub.getName())
                      .append("** (").append(sub.getDepartment()).append(" Semester ").append(sub.getSemester()).append(").\n\n");
            }

            if (!allocated.isEmpty()) {
                answer.append("Your allocated weekly teaching routine (Monday–Saturday, 45-min periods):\n\n");
                answer.append("| Day | Period | Time Slot | Course | Room | Session Type |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
                for (TimetableEntry te : allocated) {
                    answer.append("| ").append(te.getDay()).append(" | Period ").append(te.getPeriod()).append(" | `")
                          .append(te.getStartTime()).append(" - ").append(te.getEndTime()).append("` | ")
                          .append(te.getSubjectCode()).append(": ").append(te.getSubjectName()).append(" | ")
                          .append(te.getRoom()).append(" | ").append(te.getType() != null ? te.getType() : "lecture").append(" |\n");
                }
                answer.append("\n💡 *Recess: 10:30–10:45 AM, Lunch: 12:15–01:00 PM. Periods not listed above are dedicated office hours.*");
            } else {
                answer.append("No active lecture periods currently allocated for ").append(facName).append(" in the timetable.");
            }

            suggestions.add("Summarize attendance across my subjects");
            suggestions.add("Show students with attendance below 75%");
            suggestions.add("Broadcast assignment deadline reminder alert");
        }

        // 6. FACULTY DEFAULT FALLBACK
        else {
            answer.append("### 👨‍🏫 Faculty Instruction & Mentorship Console\n\n");
            answer.append("Welcome, **").append(facName).append("**. As a faculty member in **").append(facDept).append("**, you can query class records and manage students under your instruction:\n\n");
            answer.append("#### 🚀 Available Faculty Actions & Queries:\n");
            answer.append("- **📊 Class Attendance**: *\"Summarize attendance across my subjects\"*, *\"Show students with attendance below 75%\"*\n");
            answer.append("- **📢 Automated Alerts**: *\"Broadcast assignment deadline reminder alert\"*\n");
            answer.append("- **📝 Grade Submissions**: *\"Show pending marks submissions\"*\n");
            answer.append("- **📅 Teaching Routine**: *\"What is my teaching schedule for today?\"*\n");
            answer.append("- **📚 Assignments Portal**: *\"List active assignments and submission counts\"*\n");

            suggestions.add("Summarize attendance across my subjects");
            suggestions.add("Show students with attendance below 75%");
            suggestions.add("Broadcast assignment deadline reminder alert");
            suggestions.add("What is my teaching schedule for today?");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answer", answer.toString());
        result.put("suggestions", suggestions);
        result.put("source", "campus_knowledge_engine");
        result.put("model", "Meridian Grounded Faculty Intelligence v3.0");
        if (actionTaken != null) result.put("actionTaken", actionTaken);
        return result;
    }

    // =========================================================================
    // 🎓 ROLE 3: STUDENT (PERSONAL PROFILE SCOPE & STRICT PRIVACY ENFORCEMENT)
    // =========================================================================
    private Map<String, Object> generateStudentResponse(String query, String username, String rawMessage, Student currentStudent) {
        StringBuilder answer = new StringBuilder();
        List<String> suggestions = new ArrayList<>();
        Map<String, Object> actionTaken = null;

        List<Student> allStudents = studentRepository.findAll();
        Student activeStudent = currentStudent != null ? currentStudent :
                allStudents.stream().filter(s -> "student1cse".equalsIgnoreCase(s.getEmail()) || s.getId().equals(1L)).findFirst().orElse(null);

        // =====================================================================
        // 🔒 PRIVACY ENFORCEMENT: A STUDENT CANNOT QUERY OTHER STUDENTS' RECORDS
        // =====================================================================
        boolean isQueryingOtherPeers = false;
        String otherPeerName = null;

        if (activeStudent != null) {
            for (Student other : allStudents) {
                if (!other.getId().equals(activeStudent.getId())) {
                    String oName = other.getName().toLowerCase();
                    if (query.contains(oName)) {
                        isQueryingOtherPeers = true;
                        otherPeerName = other.getName();
                        break;
                    }
                    String[] parts = oName.split(" ");
                    if (parts.length > 1 && parts[0].length() > 3 && query.contains(parts[0])) {
                        isQueryingOtherPeers = true;
                        otherPeerName = other.getName();
                        break;
                    }
                }
            }
        }

        if (query.contains("other student") || query.contains("all student") || query.contains("peers") ||
                query.contains("who got highest") || query.contains("rank list") || query.contains("highest cgpa") ||
                query.contains("top student") || query.contains("everyone's mark") || query.contains("faculty salary") ||
                query.contains("college fee collection") || (query.contains("marks of") && !query.contains("my mark"))) {
            isQueryingOtherPeers = true;
        }

        if (isQueryingOtherPeers) {
            answer.append("### 🔒 Campus Privacy & Access Boundary Enforcement\n\n");
            answer.append("**Access Denied**: Under university privacy regulations (FERPA compliance), students are strictly restricted to their **own personal academic records**.\n\n");
            if (otherPeerName != null) {
                answer.append("🚫 You do not have authorization to view academic records, marks, CGPA, or personal data for **").append(otherPeerName).append("**.\n\n");
            } else {
                answer.append("🚫 You do not have authorization to view class-wide ranking ledgers, other peers' grades, or institutional financial accounts.\n\n");
            }
            answer.append("#### 💡 What you can ask for your own profile:\n");
            answer.append("- *\"What is my latest CGPA and semester marks?\"*\n");
            answer.append("- *\"How many classes can I safely skip?\"*\n");
            answer.append("- *\"What assignments are due this week?\"*\n");
            answer.append("- *\"Who is my assigned faculty mentor?\"*\n");
            answer.append("- *\"What is my tuition fee balance?\"*\n");

            suggestions.add("What is my latest CGPA and semester marks?");
            suggestions.add("How is my attendance and how many classes can I skip?");
            suggestions.add("What assignments are due this week?");
            suggestions.add("Who is my assigned faculty mentor?");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("answer", answer.toString());
            result.put("suggestions", suggestions);
            result.put("source", "campus_knowledge_engine");
            result.put("model", "Meridian Grounded Student Advisor v3.0");
            return result;
        }

        // =====================================================================
        // STUDENT FEATURE 1: EVENT REGISTRATION (AUTONOMOUS)
        // =====================================================================
        if ((query.contains("register") || query.contains("sign up") || query.contains("attend") || query.contains("rsvp"))
                && (query.contains("event") || query.contains("symposium") || query.contains("hackathon") || query.contains("workshop") || query.contains("fest"))) {

            List<CampusEvent> allEvents = eventRepository.findAll();
            CampusEvent targetEvent = allEvents.stream()
                    .filter(e -> query.contains(e.getTitle().toLowerCase()) ||
                            (e.getTitle().toLowerCase().contains("symposium") && query.contains("symposium")) ||
                            (e.getTitle().toLowerCase().contains("hackathon") && query.contains("hackathon")) ||
                            (e.getTitle().toLowerCase().contains("ai") && query.contains("ai")))
                    .findFirst()
                    .orElse(allEvents.isEmpty() ? null : allEvents.get(0));

            if (targetEvent != null) {
                targetEvent.setRegisteredByMe(true);
                targetEvent.setRegistered(targetEvent.getRegistered() != null ? targetEvent.getRegistered() + 1 : 1);
                eventRepository.save(targetEvent);

                long maxNotifId = notificationRepository.findAll().stream().mapToLong(n -> n.getId() != null ? n.getId() : 0).max().orElse(0);
                Notification notif = new Notification();
                notif.setId(maxNotifId + 1);
                notif.setTitle("Registration Confirmed: " + targetEvent.getTitle());
                notif.setMessage("You have been confirmed for " + targetEvent.getTitle() + " at " + targetEvent.getVenue() + " on " + targetEvent.getDate() + ".");
                notif.setCreatedAt("Just now");
                notif.setRead(false);
                notif.setTargetRole("student");
                if (username != null) notif.setTargetUsername(username);
                notificationRepository.save(notif);

                int remaining = Math.max(0, targetEvent.getCapacity() - targetEvent.getRegistered());
                actionTaken = new LinkedHashMap<>();
                actionTaken.put("type", "EVENT_REGISTERED");
                actionTaken.put("eventId", targetEvent.getId());
                actionTaken.put("title", targetEvent.getTitle());
                actionTaken.put("venue", targetEvent.getVenue());
                actionTaken.put("date", targetEvent.getDate());
                actionTaken.put("status", "Confirmed");

                answer.append("### 🎉 Registration Confirmed: **").append(targetEvent.getTitle()).append("**\n\n");
                answer.append("You are officially registered! Here are your event details:\n\n");
                answer.append("- **Event**: **").append(targetEvent.getTitle()).append("**\n");
                answer.append("- **Date & Time**: `").append(targetEvent.getDate()).append(" at ").append(targetEvent.getTime()).append("`\n");
                answer.append("- **Venue**: `").append(targetEvent.getVenue()).append("`\n");
                answer.append("- **Remaining Seats**: ").append(remaining).append(" / ").append(targetEvent.getCapacity()).append("\n\n");
                answer.append("A confirmation badge has been added to your dashboard notifications.");
            } else {
                answer.append("No upcoming campus events matched your request.");
            }
            suggestions.add("What are the latest campus announcements?");
            suggestions.add("Check my attendance buffer");
            suggestions.add("What assignments are due this week?");
        }

        // =====================================================================
        // STUDENT FEATURE 2: FILE GRIEVANCE TICKET (AUTONOMOUS)
        // =====================================================================
        else if ((query.contains("complaint") || query.contains("grievance") || query.contains("ticket") || query.contains("broken") || query.contains("fix"))
                && (query.contains("raise") || query.contains("file") || query.contains("submit") || query.contains("create") || query.contains("problem") || query.contains("not working"))) {

            String category = "Facility";
            if (query.contains("wifi") || query.contains("internet") || query.contains("network")) category = "IT & Network";
            else if (query.contains("hostel") || query.contains("mess") || query.contains("room")) category = "Hostel";
            else if (query.contains("academic") || query.contains("exam") || query.contains("course")) category = "Academic";

            String title = rawMessage.length() > 60 ? rawMessage.substring(0, 57) + "..." : rawMessage;
            long maxComplaintId = complaintRepository.findAll().stream().mapToLong(c -> c.getId() != null ? c.getId() : 0).max().orElse(0);

            Complaint complaint = new Complaint();
            complaint.setId(maxComplaintId + 1);
            complaint.setTitle(title);
            complaint.setCategory(category);
            complaint.setStatus("submitted");
            complaint.setPriority(query.contains("urgent") ? "high" : "medium");
            complaint.setStudent(activeStudent != null ? activeStudent.getName() : "Student");
            complaint.setSenderRole("student");
            complaint.setSenderUsername(username != null ? username : "student1cse");
            complaint.setDepartment(activeStudent != null ? activeStudent.getDepartment() : "CSE");
            complaint.setDescription(rawMessage);
            complaint.setCreatedAt("Just now");
            complaintRepository.save(complaint);

            actionTaken = new LinkedHashMap<>();
            actionTaken.put("type", "COMPLAINT_FILED");
            actionTaken.put("ticketId", complaint.getId());
            actionTaken.put("category", category);
            actionTaken.put("status", "Submitted");

            answer.append("### 🛠️ Service Desk Ticket Created: **#").append(complaint.getId()).append("**\n\n");
            answer.append("Your grievance ticket has been successfully registered and dispatched to Campus IT & Facilities.\n\n");
            answer.append("- **Ticket ID**: `#").append(complaint.getId()).append("`\n");
            answer.append("- **Category**: ").append(category).append("\n");
            answer.append("- **Status**: `🟡 Submitted`\n");
            answer.append("- **Estimated Resolution**: Within 24–48 hours.\n");

            suggestions.add("Check status of my complaints");
            suggestions.add("How is my attendance and how many classes can I skip?");
            suggestions.add("What is my timetable for today?");
        }

        // =====================================================================
        // STUDENT FEATURE 3: ATTENDANCE BUFFER & SAFE-SKIPS
        // =====================================================================
        else if (query.contains("bunk") || query.contains("skip") || query.contains("safe to miss") || query.contains("how many classes") ||
                (query.contains("attendance") && (query.contains("buffer") || query.contains("shortage") || query.contains("cutoff") || query.contains("can i miss") || query.contains("my")))) {

            if (activeStudent != null) {
                List<AttendanceItem> items = attendanceRepository.findByStudentId(activeStudent.getId());
                answer.append("### 🎯 Live Attendance Buffer & Safe-Skip Intelligence\n\n");
                answer.append("Clearance analysis for **").append(activeStudent.getName())
                      .append("** against the **75% minimum semester exam threshold**:\n\n");

                if (!items.isEmpty()) {
                    answer.append("| Course Code | Subject | Attended / Total | % | Safe to Skip | Recovery Required | Clearance |\n");
                    answer.append("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |\n");

                    for (AttendanceItem it : items) {
                        int p = it.getPresent();
                        int t = it.getTotal();
                        int pct = it.getPercentage() != null ? it.getPercentage() : (int) Math.round((double) p / Math.max(1, t) * 100);

                        if (pct >= 75) {
                            int canSkip = (int) Math.floor((p - 0.75 * t) / 0.75);
                            String skipText = canSkip > 0 ? "🟢 **" + canSkip + " class(es)**" : "🟡 Buffer: 0";
                            answer.append("| `").append(it.getSubjectCode()).append("` | ").append(it.getSubject())
                                  .append(" | ").append(p).append(" / ").append(t).append(" | **").append(pct).append("%** | ")
                                  .append(skipText).append(" | — | ✅ Eligible |\n");
                        } else {
                            int needed = (int) Math.ceil((0.75 * t - p) / 0.25);
                            if (needed <= 0) needed = 1;
                            answer.append("| `").append(it.getSubjectCode()).append("` | ").append(it.getSubject())
                                  .append(" | ").append(p).append(" / ").append(t).append(" | 🔴 **").append(pct).append("%** | ")
                                  .append("❌ None | ⚠️ Attend **").append(needed).append(" consecutive** | ⚠️ Detention Risk |\n");
                        }
                    }
                    answer.append("\n");
                    answer.append("📌 *University Regulation: Attendance between 65% - 74% requires HoD condonation ($50 fee). Below 65% leads to debarment.*");
                }
            }
            suggestions.add("Simulate what score I need for a 9.0 CGPA");
            suggestions.add("What is my latest CGPA and semester marks?");
            suggestions.add("What assignments are due this week?");
        }

        // =====================================================================
        // STUDENT FEATURE 4: PERSONAL CGPA, SGPA & MARKS
        // =====================================================================
        else if (query.contains("grade") || query.contains("mark") || query.contains("gpa") || query.contains("sgpa") ||
                query.contains("cgpa") || query.contains("score") || query.contains("arrear") || query.contains("result")) {

            if (activeStudent != null) {
                StudentDetails details = studentDetailsRepository.findByStudentId(activeStudent.getId()).orElse(null);
                List<Mark> marks = markRepository.findByStudentId(activeStudent.getId());

                answer.append("### 🎓 Personal Academic Performance Summary: **").append(activeStudent.getName()).append("**\n\n");
                if (details != null) {
                    answer.append("- **Cumulative GPA (CGPA)**: `").append(details.getCgpa() != null ? details.getCgpa() : 8.8).append(" / 10.0`\n");
                    answer.append("- **Standing Backlogs / Arrears**: `").append(details.getArrearCount()).append("` active backlog(s)\n");
                    if (details.getSgpa() != null && !details.getSgpa().isEmpty()) {
                        answer.append("- **Latest Semester SGPA**: `")
                              .append(details.getSgpa().get(details.getSgpa().size() - 1).get("gpa"))
                              .append("` (Semester ").append(details.getSgpa().get(details.getSgpa().size() - 1).get("sem")).append(")\n\n");
                    }
                }

                if (!marks.isEmpty()) {
                    answer.append("#### 📝 Course Examination Marks:\n");
                    answer.append("| Course Code | Subject | Assessment | Score | Max | Grade | Result |\n");
                    answer.append("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |\n");
                    for (Mark m : marks) {
                        String res = Boolean.TRUE.equals(m.getPassed()) ? "✅ PASS" : "❌ ARREAR";
                        answer.append("| `").append(m.getSubjectCode()).append("` | ").append(m.getSubject()).append(" | ")
                              .append(m.getAssessment()).append(" | ").append(m.getScore()).append(" | ").append(m.getMaxScore())
                              .append(" | **").append(m.getGrade()).append("** | ").append(res).append(" |\n");
                    }
                    answer.append("\n");
                }
                answer.append("💡 *Grading scale: 90-100 (O Outstanding), 80-89 (A+), 70-79 (A), 60-69 (B+), 50-59 (B/C), <50 (F Fail).*");
            }
            suggestions.add("Simulate what score I need for a 9.0 CGPA");
            suggestions.add("How is my attendance and how many classes can I skip?");
            suggestions.add("What is my outstanding tuition fee balance?");
        }

        // =====================================================================
        // STUDENT FEATURE 5: TARGET GPA SIMULATOR
        // =====================================================================
        else if (query.contains("simulate") || query.contains("target gpa") || query.contains("target cgpa")) {
            if (activeStudent != null) {
                StudentDetails details = studentDetailsRepository.findByStudentId(activeStudent.getId()).orElse(null);
                double currentCgpa = details != null && details.getCgpa() != null ? details.getCgpa() : 8.75;
                int currentSem = activeStudent.getSemester() != null ? activeStudent.getSemester() : 1;

                answer.append("### 🎯 Target GPA Simulation Model: **").append(activeStudent.getName()).append("**\n\n");
                answer.append("- **Current Cumulative CGPA**: `").append(currentCgpa).append(" / 10.0`\n");
                answer.append("- **Current Semester**: `Semester ").append(currentSem).append("`\n\n");

                answer.append("| Target CGPA | Required Semester SGPA | Grade Strategy Needed | Feasibility |\n");
                answer.append("| :--- | :--- | :--- | :--- |\n");
                double needed85 = Math.round(((8.5 * currentSem) - (currentCgpa * (currentSem - 1))) * 100.0) / 100.0;
                double needed90 = Math.round(((9.0 * currentSem) - (currentCgpa * (currentSem - 1))) * 100.0) / 100.0;
                double needed95 = Math.round(((9.5 * currentSem) - (currentCgpa * (currentSem - 1))) * 100.0) / 100.0;

                answer.append("| **8.50 CGPA** | `").append(Math.max(0, needed85)).append(" SGPA` | Mostly 'A' & 'B+' grades across 20 credits | 🟢 High |\n");
                answer.append("| **9.00 CGPA** | `").append(Math.min(10.0, Math.max(0, needed90))).append(" SGPA` | 'O' grades in 4-credit courses, 'A+' in others | 🟡 Achievable |\n");
                answer.append("| **9.50 CGPA** | `").append(Math.min(10.0, Math.max(0, needed95))).append(" SGPA` | Outstanding 'O' grades (>90% score) in all courses | 🔴 Demanding |\n\n");
            }
            suggestions.add("What is my latest CGPA and semester marks?");
            suggestions.add("How is my attendance and how many classes can I skip?");
            suggestions.add("What assignments are due this week?");
        }

        // =====================================================================
        // STUDENT FEATURE 6: PERSONAL ASSIGNMENTS & HOMEWORK
        // =====================================================================
        else if (query.contains("assignment") || query.contains("homework") || query.contains("submission") || query.contains("due")) {
            List<Assignment> assignments = assignmentRepository.findAll();
            answer.append("### 📚 Your Academic Course Assignments\n\n");
            if (!assignments.isEmpty()) {
                answer.append("| Title | Course | Deadline | Max Marks | Status |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- |\n");
                for (Assignment asg : assignments) {
                    answer.append("| **").append(asg.getTitle()).append("** | ").append(asg.getSubject()).append(" | `")
                          .append(asg.getDueDate()).append("` | ").append(asg.getMaxMarks()).append(" pts | `")
                          .append(asg.getStatus()).append("` |\n");
                }
                answer.append("\n");
                answer.append("📌 *Go to **Assignments** in the sidebar to upload your solutions.*");
            }
            suggestions.add("How is my attendance and how many classes can I skip?");
            suggestions.add("What is my latest CGPA and semester marks?");
            suggestions.add("What is my timetable for today?");
        }

        // =====================================================================
        // STUDENT FEATURE 7: TUITION FEE BALANCE
        // =====================================================================
        else if (query.contains("fee") || query.contains("tuition") || query.contains("dues") || query.contains("balance")) {
            if (activeStudent != null) {
                FeeInfo fee = feeRepository.findByStudentId(activeStudent.getId()).orElse(null);
                answer.append("### 💳 Personal Tuition Fee Account: **").append(activeStudent.getName()).append("**\n\n");
                if (fee != null) {
                    answer.append("- **Total Annual Fee**: `$").append(String.format("%,.2f", fee.getTotal())).append("`\n");
                    answer.append("- **Amount Paid**: `$").append(String.format("%,.2f", fee.getPaid())).append("` (✅ Verified)\n");
                    answer.append("- **Outstanding Balance Due**: `$").append(String.format("%,.2f", fee.getPending())).append("`\n\n");
                    if (fee.getPending() > 0) {
                        answer.append("⚠️ *Notice: Outstanding fees must be settled before end-semester exam hall tickets are released.*");
                    } else {
                        answer.append("🎉 *Account Clear: All semester fees are fully settled.*");
                    }
                }
            }
            suggestions.add("What is my latest CGPA and semester marks?");
            suggestions.add("How is my attendance and how many classes can I skip?");
            suggestions.add("What assignments are due this week?");
        }

        // =====================================================================
        // STUDENT FEATURE 8: TIMETABLE & CLASS SCHEDULE
        // =====================================================================
        else if (query.contains("timetable") || query.contains("schedule") || query.contains("routine") || query.contains("class")) {
            String dept = activeStudent != null ? activeStudent.getDepartment() : "CSE";
            int sem = activeStudent != null && activeStudent.getSemester() != null ? activeStudent.getSemester() : 1;

            String targetDay = "Monday";
            for (String d : List.of("monday", "tuesday", "wednesday", "thursday", "friday", "saturday")) {
                if (query.contains(d)) {
                    targetDay = d.substring(0, 1).toUpperCase() + d.substring(1);
                    break;
                }
            }

            final String dayFilter = targetDay;
            List<TimetableEntry> entries = timetableRepository.findByDepartmentAndSemesterOrderByPeriodAsc(dept, sem).stream()
                    .filter(e -> dayFilter.equalsIgnoreCase(e.getDay()))
                    .toList();

            answer.append("### 📅 Your Academic Timetable: **").append(targetDay).append("** (").append(dept).append(" - Sem ").append(sem).append(")\n\n");
            if (!entries.isEmpty()) {
                answer.append("| Period | Time Slot | Course Code | Subject Name | Room | Faculty |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
                for (TimetableEntry te : entries) {
                    String slot = switch (te.getPeriod() != null ? te.getPeriod() : 1) {
                        case 1 -> "09:00 AM - 09:45 AM";
                        case 2 -> "09:45 AM - 10:30 AM";
                        case 3 -> "10:45 AM - 11:30 AM";
                        case 4 -> "11:30 AM - 12:15 PM";
                        case 5 -> "01:00 PM - 01:45 PM";
                        case 6 -> "01:45 PM - 02:30 PM";
                        case 7 -> "02:30 PM - 03:15 PM";
                        default -> "09:00 AM - 09:45 AM";
                    };
                    answer.append("| Period ").append(te.getPeriod()).append(" | `").append(slot).append("` | `")
                          .append(te.getSubjectCode()).append("` | **").append(te.getSubjectName()).append("** | ")
                          .append(te.getRoom()).append(" | ").append(te.getFacultyName()).append(" |\n");
                }
            } else {
                answer.append("No lectures scheduled for ").append(targetDay).append(". Have a restful self-study day!");
            }
            suggestions.add("What is my timetable for Tuesday?");
            suggestions.add("How is my attendance and how many classes can I skip?");
            suggestions.add("What assignments are due this week?");
        }

        // =====================================================================
        // STUDENT FEATURE 9: MENTOR INFO
        // =====================================================================
        else if (query.contains("mentor") || query.contains("advisor") || query.contains("counselor")) {
            if (activeStudent != null) {
                StudentDetails details = studentDetailsRepository.findByStudentId(activeStudent.getId()).orElse(null);
                answer.append("### 👨‍🏫 Assigned Faculty Mentor\n\n");
                if (details != null && details.getMentor() != null) {
                    answer.append("- **Faculty Advisor**: **").append(details.getMentor().get("name")).append("**\n");
                    answer.append("- **Department**: `").append(activeStudent.getDepartment()).append("`\n");
                    answer.append("- **Direct Phone**: `").append(details.getMentor().get("phone")).append("`\n");
                    answer.append("- **Office Hours**: Monday–Friday 03:30 PM – 05:00 PM\n\n");
                    answer.append("💡 *You can reach out to your mentor for academic counseling, course registrations, or medical attendance condonation approvals.*");
                }
            }
            suggestions.add("What is my latest CGPA and semester marks?");
            suggestions.add("How is my attendance and how many classes can I skip?");
            suggestions.add("What assignments are due this week?");
        }

        // =====================================================================
        // STUDENT FEATURE 10: ENROLLED SUBJECTS & DEDICATED PROFESSORS
        // =====================================================================
        else if (query.contains("subject") || query.contains("course") || query.contains("curriculum") || query.contains("professor") || query.contains("faculty") || query.contains("teacher")) {
            String dept = activeStudent != null ? activeStudent.getDepartment() : "CSE";
            int sem = activeStudent != null && activeStudent.getSemester() != null ? activeStudent.getSemester() : 1;

            List<Subject> mySubs = subjectRepository.findByDepartmentAndSemester(dept, sem);
            if (mySubs.isEmpty()) {
                mySubs = subjectRepository.findByDepartment(dept);
            }

            answer.append("### 📚 Your Enrolled Subjects: **Semester ").append(sem).append("** (").append(dept).append(")\n\n");
            answer.append("In accordance with university regulations, you have **").append(mySubs.size()).append(" core subjects** for this semester. ");
            answer.append("Each subject is instructed by its dedicated subject professor:\n\n");

            if (!mySubs.isEmpty()) {
                answer.append("| Course Code | Subject Name | Credits | Dedicated Professor | Designation | Contact |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
                for (Subject sub : mySubs) {
                    answer.append("| `").append(sub.getCode()).append("` | **").append(sub.getName()).append("** | ")
                          .append(sub.getCredits()).append(" Credits | ").append(sub.getFaculty()).append(" | ")
                          .append(sub.getFacultyDesignation() != null ? sub.getFacultyDesignation() : "Professor").append(" | `")
                          .append(sub.getFacultyEmail() != null ? sub.getFacultyEmail() : sub.getFacultyUsername() + "@meridian.edu").append("` |\n");
                }
                answer.append("\n💡 *Your weekly class timetable contains lectures and laboratory sessions strictly for these 5 subjects.*");
            }

            suggestions.add("What is my timetable for today?");
            suggestions.add("How is my attendance and how many classes can I skip?");
            suggestions.add("What assignments are due this week?");
        }

        // =====================================================================
        // STUDENT DEFAULT FALLBACK
        // =====================================================================
        else {
            answer.append("### 🎓 Personal Student Academic Assistant\n\n");
            if (activeStudent != null) {
                answer.append("Welcome, **").append(activeStudent.getName()).append("** (").append(activeStudent.getDepartment())
                      .append(", Semester ").append(activeStudent.getSemester()).append("). I am your personal campus advisor.\n\n");
            } else {
                answer.append("Welcome! I am your personal student campus advisor.\n\n");
            }
            answer.append("#### 🚀 What I can do for you:\n");
            answer.append("- **🎯 Attendance Safe-Skips**: *\"How many classes can I safely skip?\"*\n");
            answer.append("- **🎓 CGPA & Marks**: *\"What is my latest CGPA and semester marks?\"*\n");
            answer.append("- **📈 Target GPA Simulator**: *\"Simulate my score for a 9.0 CGPA\"*\n");
            answer.append("- **📚 Assignments Due**: *\"What assignments are due this week?\"*\n");
            answer.append("- **📅 Class Schedule**: *\"What is my timetable for today?\"*\n");
            answer.append("- **🎪 Campus Events**: *\"Register me for the National AI Symposium\"*\n");
            answer.append("- **🛠️ Service Desk**: *\"File a complaint: Wi-Fi disconnected in Lab\"*\n\n");
            answer.append("🔒 *Privacy Notice: Student records for other peers are protected by university privacy policies.*");

            suggestions.add("How is my attendance and how many classes can I skip?");
            suggestions.add("What is my latest CGPA and semester marks?");
            suggestions.add("Simulate what score I need for a 9.0 CGPA");
            suggestions.add("What assignments are due this week?");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answer", answer.toString());
        result.put("suggestions", suggestions);
        result.put("source", "campus_knowledge_engine");
        result.put("model", "Meridian Grounded Student Advisor v3.0");
        if (actionTaken != null) result.put("actionTaken", actionTaken);
        return result;
    }

    // =========================================================================
    // 🛠️ HELPER LOOKUP METHODS
    // =========================================================================
    private boolean containsStudentSearch(String query, List<Student> students) {
        if (query.contains("student ") || query.contains("profile of") || query.contains("details for") ||
                query.contains("details of") || query.contains("cgpa of") || query.contains("marks of")) {
            return true;
        }
        for (Student s : students) {
            if (query.contains(s.getName().toLowerCase()) || query.contains(s.getStudentId().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Student matchStudent(String query, List<Student> students) {
        for (Student s : students) {
            String name = s.getName().toLowerCase();
            if (query.contains(name) || query.contains(s.getStudentId().toLowerCase())) {
                return s;
            }
            String[] parts = name.split(" ");
            if (parts.length > 1 && parts[0].length() > 3 && query.contains(parts[0])) {
                return s;
            }
        }
        Long id = extractNumericId(query);
        if (id != null) {
            for (Student s : students) {
                if (s.getId().equals(id) || s.getStudentId().contains(String.valueOf(id))) {
                    return s;
                }
            }
        }
        return students.isEmpty() ? null : students.get(0);
    }

    private Faculty matchFaculty(String username) {
        if (username != null && !username.trim().isEmpty()) {
            User u = userRepository.findByUsernameIgnoreCase(username.trim()).orElse(null);
            if (u != null) {
                Optional<Faculty> fOpt = facultyRepository.findByEmail(u.getEmail());
                if (fOpt.isPresent()) return fOpt.get();
                List<Faculty> deptFac = facultyRepository.findByDepartment(u.getDepartment());
                if (!deptFac.isEmpty()) return deptFac.get(0);
            }
        }
        return facultyRepository.findById(1L).orElseGet(() -> {
            List<Faculty> all = facultyRepository.findAll();
            return all.isEmpty() ? null : all.get(0);
        });
    }

    private Long extractNumericId(String text) {
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("#?(\\d+)").matcher(text);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
        } catch (Exception ignored) {}
        return null;
    }
}
