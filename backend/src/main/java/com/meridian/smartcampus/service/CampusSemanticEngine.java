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

    public Map<String, Object> generateGroundedResponse(String userRole, String username, String rawMessage, Student currentStudent) {
        String query = rawMessage != null ? rawMessage.trim().toLowerCase() : "";
        StringBuilder answer = new StringBuilder();
        List<String> suggestions = new ArrayList<>();
        Map<String, Object> actionTaken = null;

        // Precompute matching subject or faculty
        List<Subject> allSubjects = subjectRepository.findAll();
        Subject matchedSub = allSubjects.stream()
                .filter(s -> query.contains(s.getCode().toLowerCase()) ||
                        (s.getName().length() > 4 && query.contains(s.getName().toLowerCase())))
                .findFirst().orElse(null);

        List<Faculty> allFaculty = facultyRepository.findAll();
        Faculty matchedFac = allFaculty.stream()
                .filter(f -> {
                    String cleanName = f.getName().toLowerCase().replace("dr. ", "").replace("dr ", "").trim();
                    return query.contains(f.getName().toLowerCase()) ||
                            (cleanName.length() > 3 && query.contains(cleanName)) ||
                            (f.getFacultyId() != null && query.contains(f.getFacultyId().toLowerCase()));
                })
                .findFirst().orElse(null);

        // =========================================================================
        // 1. AGENTIC ACTION: EVENT REGISTRATION
        // =========================================================================
        if ((query.contains("register") || query.contains("sign up") || query.contains("attend") || query.contains("rsvp"))
                && (query.contains("event") || query.contains("symposium") || query.contains("hackathon") || query.contains("workshop") || query.contains("fest") || query.contains("tech"))) {

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

                // Create a notification for the student
                long maxNotifId = notificationRepository.findAll().stream().mapToLong(n -> n.getId() != null ? n.getId() : 0).max().orElse(0);
                Notification notif = new Notification();
                notif.setId(maxNotifId + 1);
                notif.setTitle("Registration Confirmed: " + targetEvent.getTitle());
                notif.setMessage("You have been confirmed for " + targetEvent.getTitle() + " at " + targetEvent.getVenue() + " on " + targetEvent.getDate() + " (" + targetEvent.getTime() + ").");
                notif.setCreatedAt("Just now");
                notif.setRead(false);
                notif.setTargetRole("student");
                if (username != null) notif.setTargetUsername(username);
                notificationRepository.save(notif);

                int remaining = Math.max(0, targetEvent.getCapacity() - targetEvent.getRegistered());
                actionTaken = new LinkedHashMap<>();
                actionTaken.put("type", "EVENT_REGISTERED");
                actionTaken.put("title", targetEvent.getTitle());
                actionTaken.put("venue", targetEvent.getVenue());
                actionTaken.put("date", targetEvent.getDate() + " (" + targetEvent.getTime() + ")");
                actionTaken.put("seatsLeft", remaining);
                actionTaken.put("status", "Confirmed");

                answer.append("### 🎉 Registration Confirmed: **").append(targetEvent.getTitle()).append("**\n\n");
                answer.append("You are officially registered for this campus event!\n\n");
                answer.append("- **Event**: ").append(targetEvent.getTitle()).append("\n");
                answer.append("- **Date & Time**: `").append(targetEvent.getDate()).append(" ").append(targetEvent.getTime()).append("`\n");
                answer.append("- **Venue**: ").append(targetEvent.getVenue()).append("\n");
                answer.append("- **Organizer**: ").append(targetEvent.getOrganizer()).append("\n");
                answer.append("- **Capacity**: ").append(targetEvent.getRegistered()).append(" / ").append(targetEvent.getCapacity()).append(" registered (").append(remaining).append(" seats remaining)\n\n");
                answer.append("👉 A confirmation pass has been added to your profile. You can see your ticket badge in the **Campus Events** tab.");

                suggestions.add("What other campus events are upcoming?");
                suggestions.add("What is my timetable for today?");
                suggestions.add("Check my attendance status");
            } else {
                answer.append("There are currently no upcoming events matching your request.");
            }
        }

        // =========================================================================
        // 2. AGENTIC ACTION: FILE SERVICE GRIEVANCE / COMPLAINT TICKET
        // =========================================================================
        else if ((query.contains("complaint") || query.contains("grievance") || query.contains("ticket") || query.contains("report") || query.contains("issue") || query.contains("broken") || query.contains("fix"))
                && (query.contains("raise") || query.contains("file") || query.contains("submit") || query.contains("create") || query.contains("not working") || query.contains("problem") || query.contains("down"))) {

            String category = "Facility";
            if (query.contains("wifi") || query.contains("wi-fi") || query.contains("internet") || query.contains("network") || query.contains("portal") || query.contains("login") || query.contains("speed")) {
                category = "IT & Network";
            } else if (query.contains("hostel") || query.contains("room") || query.contains("mess") || query.contains("bed") || query.contains("water") || query.contains("food")) {
                category = "Hostel";
            } else if (query.contains("lab") || query.contains("pc") || query.contains("computer") || query.contains("equipment") || query.contains("monitor")) {
                category = "Laboratory";
            } else if (query.contains("exam") || query.contains("mark") || query.contains("course") || query.contains("teacher") || query.contains("class") || query.contains("grade")) {
                category = "Academic";
            }

            String title = rawMessage;
            if (title.length() > 60) {
                title = title.substring(0, 57) + "...";
            }

            long maxComplaintId = complaintRepository.findAll().stream().mapToLong(c -> c.getId() != null ? c.getId() : 0).max().orElse(0);
            Complaint complaint = new Complaint();
            complaint.setId(maxComplaintId + 1);
            complaint.setTitle(title);
            complaint.setCategory(category);
            complaint.setStatus("submitted");
            complaint.setPriority(query.contains("urgent") || query.contains("immediate") || query.contains("emergency") ? "high" : "medium");
            complaint.setStudent(currentStudent != null ? currentStudent.getName() : (username != null ? username : "Campus Colleague"));
            complaint.setSenderRole(userRole != null ? userRole : "student");
            complaint.setSenderUsername(username != null ? username : "student1cse");
            complaint.setDepartment(currentStudent != null ? currentStudent.getDepartment() : "CSE");
            complaint.setDescription(rawMessage);
            complaint.setCreatedAt("Just now");
            complaintRepository.save(complaint);

            // Notify Administrator
            long maxNotifId = notificationRepository.findAll().stream().mapToLong(n -> n.getId() != null ? n.getId() : 0).max().orElse(0);
            Notification adminNotif = new Notification();
            adminNotif.setId(maxNotifId + 1);
            adminNotif.setTitle("Grievance Ticket #" + complaint.getId() + " Filed: " + category);
            adminNotif.setMessage("[" + category + "] " + title + " from " + complaint.getStudent());
            adminNotif.setCreatedAt("Just now");
            adminNotif.setRead(false);
            adminNotif.setTargetRole("admin");
            notificationRepository.save(adminNotif);

            actionTaken = new LinkedHashMap<>();
            actionTaken.put("type", "COMPLAINT_FILED");
            actionTaken.put("ticketId", complaint.getId());
            actionTaken.put("category", category);
            actionTaken.put("priority", complaint.getPriority().toUpperCase());
            actionTaken.put("status", "Submitted");
            actionTaken.put("title", title);

            answer.append("### 🛠️ Service Desk Ticket Created: **#").append(complaint.getId()).append("**\n\n");
            answer.append("Your grievance ticket has been successfully registered and dispatched to the facility team.\n\n");
            answer.append("- **Ticket ID**: `#").append(complaint.getId()).append("`\n");
            answer.append("- **Category**: ").append(category).append("\n");
            answer.append("- **Priority**: `").append(complaint.getPriority().toUpperCase()).append("`\n");
            answer.append("- **Status**: `🟡 Submitted`\n");
            answer.append("- **Filed By**: ").append(complaint.getStudent()).append(" (@").append(complaint.getSenderUsername()).append(")\n\n");
            answer.append("⏱️ **Estimated Turnaround**: Campus IT & Facilities typically review requests within **24 to 48 hours**. You can monitor ticket progress in the **Complaints & Grievances** tab.");

            suggestions.add("Check status of my complaints");
            suggestions.add("Who is the Head of Department?");
            suggestions.add("What is my timetable for today?");
        }

        // =========================================================================
        // 3. AGENTIC ACTION: FACULTY BROADCAST REMINDER (FOR TEACHERS)
        // =========================================================================
        else if ("faculty".equalsIgnoreCase(userRole) && (query.contains("alert") || query.contains("remind") || query.contains("incomplete") || query.contains("pending submission"))) {
            List<Assignment> assignments = assignmentRepository.findAll();
            long maxNotifId = notificationRepository.findAll().stream().mapToLong(n -> n.getId() != null ? n.getId() : 0).max().orElse(0);
            int sentCount = 0;

            for (Assignment a : assignments) {
                if (!"closed".equalsIgnoreCase(a.getStatus())) {
                    sentCount++;
                    Notification notif = new Notification();
                    notif.setId(maxNotifId + sentCount);
                    notif.setTitle("Professor Reminder: Deadline for " + a.getTitle());
                    notif.setMessage("Faculty alert: Please upload your solution for " + a.getTitle() + " (" + a.getSubject() + ") before the due date (" + a.getDueDate() + ").");
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
            suggestions.add("Show pending marks submissions");
            suggestions.add("List active assignments and submission counts");
        }

        // =========================================================================
        // 4. TIMETABLE & CLASS SCHEDULE LOOKUP
        // =========================================================================
        else if (query.contains("timetable") || query.contains("schedule") || query.contains("routine") || query.contains("what class") || query.contains("period") || query.contains("today's class") || query.contains("class today")) {
            String dept = currentStudent != null ? currentStudent.getDepartment() : "CSE";
            int sem = currentStudent != null ? currentStudent.getSemester() : 1;

            String targetDay = null;
            for (String d : List.of("monday", "tuesday", "wednesday", "thursday", "friday", "saturday")) {
                if (query.contains(d)) {
                    targetDay = d.substring(0, 1).toUpperCase() + d.substring(1);
                    break;
                }
            }
            if (targetDay == null) {
                DayOfWeek dow = LocalDate.now().getDayOfWeek();
                targetDay = switch (dow) {
                    case TUESDAY -> "Tuesday";
                    case WEDNESDAY -> "Wednesday";
                    case THURSDAY -> "Thursday";
                    case FRIDAY -> "Friday";
                    case SATURDAY -> "Saturday";
                    default -> "Monday";
                };
            }

            final String dayFilter = targetDay;
            List<TimetableEntry> entries = timetableRepository.findByDepartmentAndSemesterOrderByPeriodAsc(dept, sem).stream()
                    .filter(e -> dayFilter.equalsIgnoreCase(e.getDay()))
                    .toList();

            answer.append("### 📅 Academic Timetable: **").append(targetDay).append("** (").append(dept).append(" - Semester ").append(sem).append(")\n\n");

            if (!entries.isEmpty()) {
                answer.append("| Period | Time Slot | Course Code | Course Name | Room | Faculty In-Charge |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
                for (TimetableEntry te : entries) {
                    String timeSlot = switch (te.getPeriod() != null ? te.getPeriod() : 1) {
                        case 1 -> "09:00 AM - 09:45 AM";
                        case 2 -> "09:45 AM - 10:30 AM";
                        case 3 -> "10:45 AM - 11:30 AM";
                        case 4 -> "11:30 AM - 12:15 PM";
                        case 5 -> "01:00 PM - 01:45 PM";
                        case 6 -> "01:45 PM - 02:30 PM";
                        case 7 -> "02:30 PM - 03:15 PM";
                        default -> "09:00 AM - 09:45 AM";
                    };
                    answer.append("| Period ").append(te.getPeriod()).append(" | `").append(timeSlot).append("` | `")
                          .append(te.getSubjectCode() != null ? te.getSubjectCode() : "-").append("` | **")
                          .append(te.getSubjectName() != null ? te.getSubjectName() : "Lecture").append("** | ")
                          .append(te.getRoom() != null ? te.getRoom() : "Hall A").append(" | ")
                          .append(te.getFacultyName() != null ? te.getFacultyName() : "Faculty").append(" |\n");
                }
                answer.append("\n");
                answer.append("💡 *Schedule Notes: Morning tea break: 10:30 AM - 10:45 AM. Lunch recess: 12:15 PM - 01:00 PM.*");
            } else {
                answer.append("No lectures scheduled for ").append(targetDay).append(". Have a restful self-study session!");
            }

            suggestions.add("What is my timetable for Tuesday?");
            suggestions.add("How is my attendance across these subjects?");
            suggestions.add("What assignments are due this week?");
        }

        // =========================================================================
        // 5. ATTENDANCE SAFE-SKIP / BUNK ANALYSIS & RECOVERY MATH
        // =========================================================================
        else if (query.contains("bunk") || query.contains("skip") || query.contains("safe to miss") || query.contains("how many classes") ||
                (query.contains("attendance") && (query.contains("buffer") || query.contains("shortage") || query.contains("cutoff") || query.contains("can i miss")))) {

            if (currentStudent != null) {
                List<AttendanceItem> items = attendanceRepository.findByStudentId(currentStudent.getId());
                answer.append("### 🎯 Live Attendance Buffer & Safe-Skip Intelligence\n\n");
                answer.append("Mathematical clearance analysis for **").append(currentStudent.getName())
                      .append("** against the **75% minimum semester exam threshold**:\n\n");

                if (!items.isEmpty()) {
                    answer.append("| Course Code | Subject | Attended / Total | % | Safe to Skip | Recovery Required | Exam Clearance |\n");
                    answer.append("| :--- | :--- | :--- | :--- | :--- | :--- | :--- |\n");

                    for (AttendanceItem it : items) {
                        int p = it.getPresent();
                        int t = it.getTotal();
                        int pct = it.getPercentage() != null ? it.getPercentage() : (int) Math.round((double) p / Math.max(1, t) * 100);

                        if (pct >= 75) {
                            int canSkip = (int) Math.floor((p - 0.75 * t) / 0.75);
                            String skipText = canSkip > 0 ? "🟢 **" + canSkip + " class(es)**" : "🟡 Buffer: 0";
                            answer.append("| `").append(it.getSubjectCode()).append("` | ")
                                  .append(it.getSubject()).append(" | ")
                                  .append(p).append(" / ").append(t).append(" | **")
                                  .append(pct).append("%** | ")
                                  .append(skipText).append(" | — | ✅ Eligible |\n");
                        } else {
                            int needed = (int) Math.ceil((0.75 * t - p) / 0.25);
                            if (needed <= 0) needed = 1;
                            answer.append("| `").append(it.getSubjectCode()).append("` | ")
                                  .append(it.getSubject()).append(" | ")
                                  .append(p).append(" / ").append(t).append(" | 🔴 **")
                                  .append(pct).append("%** | ❌ None | ⚠️ Attend **" + needed + " consecutive** | ⚠️ Detention Risk |\n");
                        }
                    }
                    answer.append("\n");
                    answer.append("📌 **University Regulations**: Attendance between 65% - 74% requires HoD-approved medical condonation ($50 fee). Attendance strictly below 65% results in semester detention.");
                }

                suggestions.add("What is the medical leave attendance condonation policy?");
                suggestions.add("Simulate my target GPA");
                suggestions.add("Show my pending assignments");
            } else {
                answer.append("Meridian University requires a mandatory **75% minimum attendance** in each registered course. Medical condonation covers attendance between 65% and 74%.");
                suggestions.add("Show attendance policy");
            }
        }

        // =========================================================================
        // 6. TARGET GPA & EXAMINATION SCORE SIMULATOR
        // =========================================================================
        else if (query.contains("simulate") || query.contains("target gpa") || query.contains("target cgpa") || query.contains("how to get") ||
                (query.contains("gpa") && (query.contains("calculate") || query.contains("formula") || query.contains("9.") || query.contains("8.") || query.contains("reach")))) {

            if (currentStudent != null) {
                StudentDetails details = studentDetailsRepository.findByStudentId(currentStudent.getId()).orElse(null);
                double currentCgpa = details != null && details.getCgpa() != null ? details.getCgpa() : 8.75;
                int currentSem = currentStudent.getSemester() != null ? currentStudent.getSemester() : 2;

                answer.append("### 🎯 Academic Target GPA & Score Simulator\n\n");
                answer.append("Current Standing for **").append(currentStudent.getName()).append("**:\n");
                answer.append("- **Current Cumulative CGPA**: `").append(currentCgpa).append(" / 10.0`\n");
                answer.append("- **Current Semester**: Semester ").append(currentSem).append(" (").append(currentStudent.getDepartment()).append(")\n\n");

                answer.append("#### 📊 Target Milestone Projections:\n\n");
                answer.append("| Target CGPA | Required Semester SGPA | Grade Strategy Needed | Feasibility |\n");
                answer.append("| :--- | :--- | :--- | :--- |\n");

                double neededFor85 = Math.round(((8.5 * currentSem) - (currentCgpa * (currentSem - 1))) * 100.0) / 100.0;
                double neededFor90 = Math.round(((9.0 * currentSem) - (currentCgpa * (currentSem - 1))) * 100.0) / 100.0;
                double neededFor95 = Math.round(((9.5 * currentSem) - (currentCgpa * (currentSem - 1))) * 100.0) / 100.0;

                answer.append("| **8.50 CGPA** | `").append(Math.max(0, neededFor85)).append(" SGPA` | Mostly 'A' & 'B+' grades across 20 credits | 🟢 High |\n");
                answer.append("| **9.00 CGPA** | `").append(Math.min(10.0, Math.max(0, neededFor90))).append(" SGPA` | 'O' grades in 4-credit courses, 'A+' in others | 🟡 Achievable |\n");
                answer.append("| **9.50 CGPA** | `").append(Math.min(10.0, Math.max(0, neededFor95))).append(" SGPA` | Outstanding 'O' grades (>90% score) in all courses | 🔴 Demanding |\n\n");

                answer.append("💡 **Strategic Tip**: 4-credit core subjects carry **33% greater weight** than 3-credit subjects. Securing 'O' grades in 4-credit modules provides the greatest GPA boost.");

                suggestions.add("Show my current marks across all subjects");
                suggestions.add("Check my attendance buffer");
                suggestions.add("Who are my course professors?");
            } else {
                answer.append("CGPA is calculated by dividing total credit-weighted grade points by the total credits registered. Minimum passing mark is 50%.");
                suggestions.add("Show grading scale");
            }
        }

        // =========================================================================
        // 7. MENTOR & ACADEMIC COUNSELOR CONSULTATION
        // =========================================================================
        else if (query.contains("mentor") || query.contains("advisor") || query.contains("counselor") || query.contains("guidance")) {
            if (currentStudent != null) {
                StudentDetails details = studentDetailsRepository.findByStudentId(currentStudent.getId()).orElse(null);
                answer.append("### 👨‍🏫 Assigned Academic Mentor & Counselor\n\n");
                if (details != null && details.getMentor() != null) {
                    Map<String, Object> mentor = details.getMentor();
                    answer.append("- **Faculty Mentor**: **").append(mentor.getOrDefault("name", "Dr. Alan Turing")).append("**\n");
                    answer.append("- **Direct Phone**: `").append(mentor.getOrDefault("phone", "+1 (555) 011-2301")).append("`\n");
                    answer.append("- **Department**: ").append(currentStudent.getDepartment()).append(" Faculty Wing\n");
                    answer.append("- **Office Location**: Cabin 304, Academic Block 2\n");
                    answer.append("- **Consultation Hours**: Tuesdays & Thursdays, 3:30 PM - 5:00 PM\n\n");
                    answer.append("📌 Contact your mentor for academic leave certificates, remedial subject enrollment, or semester project approvals.");
                } else {
                    answer.append("Your department Head of Department (HoD) acts as primary academic mentor.");
                }
                suggestions.add("Who is the Head of Department?");
                suggestions.add("Show my pending assignments");
                suggestions.add("Check my attendance status");
            } else {
                answer.append("Each enrolled student is assigned a dedicated faculty mentor from their engineering department for semester-long guidance.");
                suggestions.add("Show faculty directory");
            }
        }

        // =========================================================================
        // 8. ADMIN EXECUTIVE HEALTH & SYSTEM OVERVIEW (FOR ADMINS)
        // =========================================================================
        else if ("admin".equalsIgnoreCase(userRole) && (query.contains("health") || query.contains("system") || query.contains("overview") || query.contains("summary") || query.contains("statistics"))) {
            long studentCount = studentRepository.count();
            long facultyCount = facultyRepository.count();
            long deptCount = departmentRepository.count();
            long subjectCount = subjectRepository.count();
            long complaintCount = complaintRepository.count();
            long openComplaints = complaintRepository.findAll().stream().filter(c -> !"resolved".equalsIgnoreCase(c.getStatus())).count();

            answer.append("### 🏛️ Meridian Campus Executive System Health\n\n");
            answer.append("| Metric | Current Value | Status |\n");
            answer.append("| :--- | :--- | :--- |\n");
            answer.append("| **Registered Students** | `").append(studentCount).append("` | 🟢 Active |\n");
            answer.append("| **Faculty Members** | `").append(facultyCount).append("` | 🟢 Active |\n");
            answer.append("| **Engineering Departments** | `").append(deptCount).append("` (CSE, ECE, MECH, CIVIL, EEE) | 🟢 Operating |\n");
            answer.append("| **Active Courses Taught** | `").append(subjectCount).append("` subjects | 🟢 Standardized |\n");
            answer.append("| **Service Grievances** | `").append(openComplaints).append(" open` (Total: ").append(complaintCount).append(") | ").append(openComplaints > 0 ? "🟡 In Triage" : "🟢 Clear").append(" |\n");
            answer.append("| **MongoDB Cluster** | `localhost:27017/smart_campus` | 🟢 99.9% Uptime |\n\n");
            answer.append("All academic and administrative services are operating smoothly.");

            suggestions.add("How many open complaints need attention?");
            suggestions.add("Show department distribution");
            suggestions.add("List upcoming campus symposiums");
        }

        // =========================================================================
        // 9. SPECIFIC COURSE OR FACULTY LOOKUP
        // =========================================================================
        else if (matchedSub != null || matchedFac != null) {
            if (matchedSub != null) {
                answer.append("### 📖 Course Details: **").append(matchedSub.getCode()).append(" - ").append(matchedSub.getName()).append("**\n\n");
                answer.append("- **Department**: ").append(matchedSub.getDepartment()).append("\n");
                answer.append("- **Semester / Year**: Semester ").append(matchedSub.getSemester()).append(" (Year ").append(matchedSub.getYear()).append(")\n");
                answer.append("- **Academic Credits**: ").append(matchedSub.getCredits()).append(" Credits\n");
                answer.append("- **Assigned Faculty**: **").append(matchedSub.getFaculty()).append("** (").append(matchedSub.getFacultyDesignation() != null ? matchedSub.getFacultyDesignation() : "Professor").append(")\n");
                answer.append("- **Faculty Email**: `").append(matchedSub.getFacultyEmail()).append("`\n");
                answer.append("- **Faculty Phone**: ").append(matchedSub.getFacultyPhone()).append("\n\n");
                answer.append("💡 Office hours: Monday & Wednesday, 2:00 PM - 4:00 PM in Faculty Wing.");
                suggestions.add("What is my attendance in " + matchedSub.getCode() + "?");
                suggestions.add("Are there any assignments for " + matchedSub.getName() + "?");
                suggestions.add("List all subjects in my department");
            } else {
                answer.append("### 👨‍🏫 Faculty Profile: **").append(matchedFac.getName()).append("**\n\n");
                answer.append("- **Designation**: ").append(matchedFac.getDesignation()).append("\n");
                answer.append("- **Department**: ").append(matchedFac.getDepartment()).append("\n");
                answer.append("- **Highest Qualification**: ").append(matchedFac.getQualification()).append("\n");
                answer.append("- **Email**: `").append(matchedFac.getEmail()).append("`\n");
                answer.append("- **Phone**: ").append(matchedFac.getPhone()).append("\n");
                answer.append("- **Courses Taught**: ").append(String.join(", ", matchedFac.getSubjects())).append("\n");
                suggestions.add("List all faculty members in " + matchedFac.getDepartment());
                suggestions.add("Who is the Head of Department?");
                suggestions.add("Show course schedule");
            }
        }

        // =========================================================================
        // 10. MEDICAL LEAVE & CONDONATION POLICIES
        // =========================================================================
        else if (query.contains("condonation") || query.contains("medical leave") || (query.contains("policy") && query.contains("attendance"))) {
            answer.append("### 📜 Meridian University Attendance & Condonation Regulations\n\n");
            answer.append("1. **Mandatory Attendance Cutoff**:\n");
            answer.append("   - **>= 75%**: Full exam eligibility without condonation required.\n");
            answer.append("   - **65% - 74%**: Eligible for medical condonation subject to:\n");
            answer.append("     * Genuine hospitalized/medical reason documented by a registered medical practitioner.\n");
            answer.append("     * Medical certificate submitted within 3 working days of returning to campus.\n");
            answer.append("     * Approval and signature by Course Professor and Head of Department (HoD).\n");
            answer.append("     * Payment of prescribed condonation fee of $50 per course.\n");
            answer.append("   - **< 65%**: Strictly detained; candidate must re-register and repeat the course in the next semester.\n\n");
            if (currentStudent != null) {
                int att = currentStudent.getAttendance() != null ? currentStudent.getAttendance() : 95;
                answer.append("📊 **Your Current Status**: Overall attendance is **").append(att)
                      .append("%** (").append(att >= 75 ? "✅ Above 75% - No condonation required" : "⚠️ Condonation required").append(").\n");
            }
            suggestions.add("How is my attendance trending?");
            suggestions.add("What is the grading policy?");
            suggestions.add("Show my examination marks");
        }

        // =========================================================================
        // 11. GENERAL ATTENDANCE STATUS
        // =========================================================================
        else if (query.contains("attendance") || query.contains("absent") || query.contains("present")) {
            if (currentStudent != null) {
                List<AttendanceItem> items = attendanceRepository.findByStudentId(currentStudent.getId());
                int overall = currentStudent.getAttendance() != null ? currentStudent.getAttendance() : 88;

                answer.append("### 📊 Live Attendance Breakdown: **").append(currentStudent.getName()).append("**\n\n");
                answer.append("Your overall recorded campus attendance is **").append(overall).append("%** ");
                if (overall >= 75) {
                    answer.append("(✅ **Eligible** for semester examinations, requirement threshold is 75%).\n\n");
                } else {
                    answer.append("(⚠️ **Warning**: Below the 75% university examination threshold).\n\n");
                }

                if (!items.isEmpty()) {
                    answer.append("| Course Code | Subject | Attended / Total | Percentage | Exam Status |\n");
                    answer.append("| :--- | :--- | :--- | :--- | :--- |\n");
                    for (AttendanceItem it : items) {
                        int pct = it.getPercentage() != null ? it.getPercentage() : (int) Math.round((double) it.getPresent() / Math.max(1, it.getTotal()) * 100);
                        String badge = pct >= 85 ? "🟢 Good" : (pct >= 75 ? "🟡 Moderate" : "🔴 Shortage");
                        answer.append("| `").append(it.getSubjectCode()).append("` | ")
                              .append(it.getSubject()).append(" | ")
                              .append(it.getPresent()).append(" / ").append(it.getTotal()).append(" | **")
                              .append(pct).append("%** | ").append(badge).append(" |\n");
                    }
                }

                suggestions.add("How many classes can I skip without getting detained?");
                suggestions.add("Show my pending assignments and deadlines");
                suggestions.add("Breakdown of my SGPA and examination marks");
            } else {
                answer.append("Campus policy dictates a mandatory **75% minimum attendance** in each registered course to be eligible for end-semester examinations.");
                suggestions.add("Show student roster attendance");
                suggestions.add("Who has attendance below 75%?");
            }
        }

        // =========================================================================
        // 12. GRADES, MARKS, SGPA, CGPA, ARREARS
        // =========================================================================
        else if (query.contains("grade") || query.contains("mark") || query.contains("gpa") || query.contains("sgpa") || query.contains("cgpa") || query.contains("score") || query.contains("arrear") || query.contains("result")) {
            if (currentStudent != null) {
                StudentDetails details = studentDetailsRepository.findByStudentId(currentStudent.getId()).orElse(null);
                List<Mark> marks = markRepository.findByStudentId(currentStudent.getId());

                answer.append("### 🎓 Academic Performance Summary: **").append(currentStudent.getName()).append("**\n\n");
                if (details != null) {
                    answer.append("- **Cumulative GPA (CGPA)**: `").append(details.getCgpa() != null ? details.getCgpa() : 8.8).append(" / 10.0`\n");
                    answer.append("- **Standing Arrears**: `").append(details.getArrearCount()).append("` active backlog(s)\n");
                    if (details.getSgpa() != null && !details.getSgpa().isEmpty()) {
                        answer.append("- **Latest Semester SGPA**: `")
                              .append(details.getSgpa().get(details.getSgpa().size() - 1).get("gpa"))
                              .append("` (Semester ").append(details.getSgpa().get(details.getSgpa().size() - 1).get("sem")).append(")\n\n");
                    }
                }

                if (!marks.isEmpty()) {
                    answer.append("#### 📝 Course Examination Marks:\n");
                    answer.append("| Course Code | Subject | Assessment | Score | Max | Grade | Result |\n");
                    answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
                    for (Mark m : marks) {
                        String res = Boolean.TRUE.equals(m.getPassed()) ? "✅ PASS" : "❌ ARREAR";
                        answer.append("| `").append(m.getSubjectCode()).append("` | ")
                              .append(m.getSubject()).append(" | ")
                              .append(m.getAssessment() != null ? m.getAssessment() : "Semester Exam").append(" | ")
                              .append(m.getScore()).append(" | ")
                              .append(m.getMaxScore()).append(" | **")
                              .append(m.getGrade()).append("** | ")
                              .append(res).append(" |\n");
                    }
                    answer.append("\n");
                }

                answer.append("💡 *Grading scale: 90-100 (O Outstanding), 80-89 (A+), 70-79 (A), 60-69 (B+), 50-59 (B/C), <50 (F Fail).*");
                suggestions.add("Simulate what score I need for a 9.0 CGPA");
                suggestions.add("Check my attendance buffer");
                suggestions.add("View fee ledger and dues");
            } else {
                answer.append("Meridian University follows a 10.0 absolute CGPA grading scale. Semester grades are calculated using credit weightings.");
                suggestions.add("Explain 10-point GPA calculation");
                suggestions.add("What is the passing cutoff mark?");
            }
        }

        // =========================================================================
        // 13. ASSIGNMENTS & HOMEWORK
        // =========================================================================
        else if (query.contains("assignment") || query.contains("homework") || query.contains("submission") || query.contains("task") || query.contains("deadline") || query.contains("due")) {
            List<Assignment> assignments = assignmentRepository.findAll();
            answer.append("### 📚 Active Academic Assignments & Deadlines\n\n");

            if (!assignments.isEmpty()) {
                answer.append("| Title | Course | Deadline | Max Marks | Status | Submissions |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
                for (Assignment asg : assignments) {
                    answer.append("| **").append(asg.getTitle()).append("** | ")
                          .append(asg.getSubject()).append(" | `")
                          .append(asg.getDueDate()).append("` | ")
                          .append(asg.getMaxMarks()).append(" pts | `")
                          .append(asg.getStatus()).append("` | ")
                          .append(asg.getSubmissions() != null ? asg.getSubmissions() : 0).append(" / ")
                          .append(asg.getTotalStudents() != null ? asg.getTotalStudents() : 30).append(" |\n");
                }
                answer.append("\n");
                answer.append("📌 **Submission Portal**: Navigate to **Assignments** from the sidebar to upload your solution before 11:59 PM on the due date.");
            } else {
                answer.append("There are currently no active assignments pending submission for your enrolled courses.");
            }

            suggestions.add("How are assignments graded?");
            suggestions.add("What are my course subjects this semester?");
            suggestions.add("Check my overall attendance");
        }

        // =========================================================================
        // 14. FEES & FINANCIALS
        // =========================================================================
        else if (query.contains("fee") || query.contains("tuition") || query.contains("payment") || query.contains("dues") || query.contains("ledger") || query.contains("receipt")) {
            if (currentStudent != null) {
                FeeInfo fee = feeRepository.findByStudentId(currentStudent.getId()).orElse(null);
                answer.append("### 💳 Tuition Fee Ledger: **").append(currentStudent.getName()).append("**\n\n");
                if (fee != null) {
                    answer.append("- **Total Academic Year Fee**: `$").append(String.format("%,.2f", fee.getTotal())).append("`\n");
                    answer.append("- **Amount Paid to Date**: `$").append(String.format("%,.2f", fee.getPaid())).append("` (✅ Verified)\n");
                    answer.append("- **Outstanding Balance Due**: `$").append(String.format("%,.2f", fee.getPending())).append("`\n\n");

                    if (fee.getPending() > 0) {
                        answer.append("⚠️ **Notice**: Outstanding balance must be settled prior to end-semester hall ticket issuance.\n\n");
                    } else {
                        answer.append("🎉 **Clear Account**: All semester tuition fees are paid in full.\n\n");
                    }

                    if (fee.getHistory() != null && !fee.getHistory().isEmpty()) {
                        answer.append("#### 🧾 Payment Transaction History:\n");
                        answer.append("| Transaction ID | Date | Amount | Method | Status |\n");
                        answer.append("| :--- | :--- | :--- | :--- | :--- |\n");
                        for (Map<String, Object> h : fee.getHistory()) {
                            answer.append("| `").append(h.getOrDefault("receiptNo", "REC-" + h.get("id"))).append("` | ")
                                  .append(h.getOrDefault("date", "2026-08-15")).append(" | $")
                                  .append(h.getOrDefault("amount", "25000")).append(" | ")
                                  .append(h.getOrDefault("method", "Online Banking")).append(" | ")
                                  .append(h.getOrDefault("status", "SUCCESS")).append(" |\n");
                        }
                    }
                } else {
                    answer.append("Your tuition account is in good standing with zero pending hold flags.");
                }

                suggestions.add("How can I download my official fee receipt?");
                suggestions.add("Are there merit scholarships available?");
                suggestions.add("What is the fee payment deadline?");
            } else {
                answer.append("Standard tuition fee across 4-year engineering programs is $60,000 annually, payable in semester installments. Contact the Finance Office in Admin Block.");
                suggestions.add("What are the accepted payment modes?");
            }
        }

        // =========================================================================
        // 15. GENERAL FACULTY & SUBJECT DIRECTORY
        // =========================================================================
        else if (query.contains("faculty") || query.contains("professor") || query.contains("teacher") || query.contains("subject") || query.contains("course") || query.contains("syllabus") || query.contains("hod")) {
            String dept = currentStudent != null ? currentStudent.getDepartment() : "CSE";
            List<Subject> deptSubs = subjectRepository.findByDepartment(dept);
            List<Department> depts = departmentRepository.findAll();

            answer.append("### 🏫 Academic Faculty & Subject Directory (").append(dept).append(")\n\n");
            Department currentDeptObj = depts.stream().filter(d -> d.getCode().equalsIgnoreCase(dept)).findFirst().orElse(null);
            if (currentDeptObj != null) {
                answer.append("📌 **Head of Department (HoD)**: **").append(currentDeptObj.getHod()).append("**\n");
                answer.append("📌 **Department Mission**: *").append(currentDeptObj.getDescription()).append("*\n\n");
            }

            if (!deptSubs.isEmpty()) {
                answer.append("| Code | Course Name | Credits | Sem | Faculty In-Charge |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- |\n");
                for (Subject s : deptSubs.stream().limit(8).toList()) {
                    answer.append("| `").append(s.getCode()).append("` | ")
                          .append(s.getName()).append(" | ")
                          .append(s.getCredits()).append(" | Sem ")
                          .append(s.getSemester()).append(" | ")
                          .append(s.getFaculty()).append(" |\n");
                }
            }
            suggestions.add("Who teaches Data Structures?");
            suggestions.add("What is my timetable for today?");
            suggestions.add("Check my attendance across all subjects");
        }

        // =========================================================================
        // 16. CAMPUS EVENTS & SYMPOSIUMS
        // =========================================================================
        else if (query.contains("event") || query.contains("symposium") || query.contains("hackathon") || query.contains("fest") || query.contains("workshop")) {
            List<CampusEvent> events = eventRepository.findAll();
            answer.append("### 🎪 Upcoming Campus Events & Symposiums\n\n");

            if (!events.isEmpty()) {
                answer.append("| Event | Date & Time | Venue | Organizer | Seats Left |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- |\n");
                for (CampusEvent ev : events) {
                    int remaining = Math.max(0, ev.getCapacity() - (ev.getRegistered() != null ? ev.getRegistered() : 0));
                    answer.append("| **").append(ev.getTitle()).append("** | `")
                          .append(ev.getDate()).append(" ").append(ev.getTime()).append("` | ")
                          .append(ev.getVenue()).append(" | ")
                          .append(ev.getOrganizer()).append(" | **")
                          .append(remaining).append("** seats |\n");
                }
                answer.append("\n");
                answer.append("👉 **Instant Registration**: You can say *\"Register me for [Event Name]\"* right here to register automatically!");
            } else {
                answer.append("No campus events scheduled for this week. Keep an eye on campus announcements for updates!");
            }

            suggestions.add("Register me for National AI Symposium");
            suggestions.add("What are the latest campus announcements?");
            suggestions.add("Show my pending assignments");
        }

        // =========================================================================
        // 17. ANNOUNCEMENTS & CIRCULARS
        // =========================================================================
        else if (query.contains("announcement") || query.contains("circular") || query.contains("notice") || query.contains("news") || query.contains("bulletin")) {
            List<Announcement> announcements = announcementRepository.findAll();
            answer.append("### 📢 Active Campus Announcements & Circulars\n\n");

            if (!announcements.isEmpty()) {
                for (Announcement a : announcements) {
                    String badge = "urgent".equalsIgnoreCase(a.getPriority()) ? "🔴 URGENT" : ("important".equalsIgnoreCase(a.getPriority()) ? "🟡 IMPORTANT" : "🔵 INFO");
                    answer.append("#### ").append(badge).append(": ").append(a.getTitle()).append("\n");
                    answer.append("- **Date**: `").append(a.getPublishedAt()).append("` | **Audience**: ").append(a.getAudience()).append("\n");
                    answer.append("- **Content**: ").append(a.getContent()).append("\n\n");
                }
            } else {
                answer.append("All administrative circulars are up to date. No emergency notifications active.");
            }

            suggestions.add("When are the mid-term examinations?");
            suggestions.add("Find upcoming campus events");
            suggestions.add("Check my attendance status");
        }

        // =========================================================================
        // 18. SERVICE TICKETS STATUS
        // =========================================================================
        else if (query.contains("complaint") || query.contains("ticket") || query.contains("service desk")) {
            List<Complaint> complaints = complaintRepository.findAll();
            answer.append("### 🛠️ Campus Grievance & Service Desk\n\n");

            if (!complaints.isEmpty()) {
                answer.append("| Ticket ID | Title | Category | Status | Priority | Date |\n");
                answer.append("| :--- | :--- | :--- | :--- | :--- | :--- |\n");
                for (Complaint c : complaints.stream().limit(5).toList()) {
                    String statusIcon = "resolved".equalsIgnoreCase(c.getStatus()) ? "🟢 Resolved" : ("progress".equalsIgnoreCase(c.getStatus()) ? "🟡 In Progress" : "⚪ Submitted");
                    answer.append("| #").append(c.getId()).append(" | ")
                          .append(c.getTitle()).append(" | ")
                          .append(c.getCategory()).append(" | ")
                          .append(statusIcon).append(" | `")
                          .append(c.getPriority()).append("` | `")
                          .append(c.getCreatedAt()).append("` |\n");
                }
                answer.append("\n");
            }

            answer.append("👉 **Submit a Ticket**: You can type *\"File a complaint: [Details]\"* right here to create a new ticket automatically.");
            suggestions.add("File a complaint: Wi-Fi disconnected in Lab");
            suggestions.add("Where is the IT Helpdesk located?");
            suggestions.add("Check my pending fees");
        }

        // =========================================================================
        // 19. DEFAULT FALLBACK & ASSISTANT CAPABILITY DIRECTORY
        // =========================================================================
        else {
            answer.append("### 🏛️ Meridian Smart Campus AI Assistant\n\n");
            answer.append("I am your integrated campus intelligence assistant, directly connected to the university MongoDB database. ");
            if (currentStudent != null) {
                answer.append("I have real-time access to your records (**").append(currentStudent.getName())
                      .append("**, Department of **").append(currentStudent.getDepartment()).append("**).\n\n");
            } else {
                answer.append("I can query student directories, faculty rosters, timetables, grades, and campus events.\n\n");
            }

            answer.append("#### 🚀 What I can do for you:\n");
            answer.append("- **⚡ Autonomous Actions**: *\"Register me for the Tech Symposium\"*, *\"File a complaint: Library AC broken\"*\n");
            answer.append("- **📊 Attendance & Safe-Skips**: *\"How many classes can I skip in CS101?\"*, *\"Attendance shortage check\"*\n");
            answer.append("- **🎯 Target GPA Simulator**: *\"Simulate my score for a 9.0 CGPA\"*, *\"What is my latest SGPA?\"*\n");
            answer.append("- **📅 Timetables & Classes**: *\"What is my schedule for today?\"*, *\"Timetable for Tuesday\"*\n");
            answer.append("- **📚 Academic Deadlines**: *\"What assignments are due this week?\"*\n");
            answer.append("- **👨‍🏫 Faculty & Mentorship**: *\"Who is my mentor?\"*, *\"Who teaches Problem Solving?\"*\n");
            answer.append("- **💳 Accounts & Fees**: *\"Show my tuition fee ledger\"*\n");

            suggestions.add("What is my timetable for today?");
            suggestions.add("How many classes can I skip?");
            suggestions.add("Simulate my target GPA");
            suggestions.add("Register me for the National AI Symposium");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answer", answer.toString());
        result.put("suggestions", suggestions);
        result.put("source", "campus_knowledge_engine");
        result.put("model", "Meridian Grounded Campus Engine v3.0");
        if (actionTaken != null) {
            result.put("actionTaken", actionTaken);
        }
        return result;
    }
}
