package com.meridian.smartcampus.service;

import com.meridian.smartcampus.model.*;
import com.meridian.smartcampus.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CampusContextService {

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
    private final UserRepository userRepository;
    private final TimetableRepository timetableRepository;

    public CampusContextService(StudentRepository studentRepository,
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
                                UserRepository userRepository,
                                TimetableRepository timetableRepository) {
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
        this.userRepository = userRepository;
        this.timetableRepository = timetableRepository;
    }

    public Student findActiveStudent(String username) {
        if (username != null && !username.trim().isEmpty()) {
            User user = userRepository.findByUsernameIgnoreCase(username.trim()).orElse(null);
            if (user != null) {
                Optional<Student> studentOpt = studentRepository.findByEmail(user.getEmail());
                if (studentOpt.isPresent()) return studentOpt.get();
                List<Student> list = studentRepository.findByDepartment(user.getDepartment());
                if (!list.isEmpty()) return list.get(0);
            }
        }
        return studentRepository.findById(1L).orElseGet(() -> {
            List<Student> all = studentRepository.findAll();
            return all.isEmpty() ? null : all.get(0);
        });
    }

    public Faculty findActiveFaculty(String username) {
        if (username != null && !username.trim().isEmpty()) {
            User user = userRepository.findByUsernameIgnoreCase(username.trim()).orElse(null);
            if (user != null) {
                Optional<Faculty> facOpt = facultyRepository.findByEmail(user.getEmail());
                if (facOpt.isPresent()) return facOpt.get();
            }
        }
        return facultyRepository.findById(1L).orElseGet(() -> {
            List<Faculty> all = facultyRepository.findAll();
            return all.isEmpty() ? null : all.get(0);
        });
    }

    public String buildGroundingContext(String userRole, String username, String query) {
        StringBuilder sb = new StringBuilder();
        String role = userRole != null ? userRole.toLowerCase().trim() : "student";

        sb.append("=== INSTITUTION KNOWLEDGE BASE: MERIDIAN UNIVERSITY ===\n");
        sb.append("Academic Year: 2025-2026. Programs: 4-Year B.Tech / B.E. (8 Semesters).\n");
        sb.append("Attendance Threshold: 75% minimum required for semester examinations. Below 65% is semester detention.\n");
        sb.append("Grading Scale: 10-point scale (O: 90-100, A+: 80-89, A: 70-79, B+: 60-69, B: 55-59, C: 50-54, F: <50 Fail).\n");
        sb.append("Current Session Role: ").append(role.toUpperCase()).append("\n\n");

        if ("student".equals(role)) {
            Student s = findActiveStudent(username);
            if (s != null) {
                sb.append("--- ACTIVE STUDENT PROFILE ---\n");
                sb.append("Name: ").append(s.getName()).append(" (ID: ").append(s.getStudentId()).append(")\n");
                sb.append("Department: ").append(s.getDepartment()).append(" | Year: ").append(s.getYear())
                  .append(" | Semester: ").append(s.getSemester()).append(" | Section: ").append(s.getSection()).append("\n");
                sb.append("Overall Attendance: ").append(s.getAttendance()).append("%\n");

                StudentDetails details = studentDetailsRepository.findByStudentId(s.getId()).orElse(null);
                if (details != null) {
                    sb.append("CGPA: ").append(details.getCgpa() != null ? details.getCgpa() : 8.5).append("\n");
                    sb.append("Standing Arrears: ").append(details.getArrearCount()).append("\n");
                    if (details.getSgpa() != null && !details.getSgpa().isEmpty()) {
                        sb.append("SGPA History: ").append(details.getSgpa()).append("\n");
                    }
                }

                // Attendance logs
                List<AttendanceItem> attList = attendanceRepository.findByStudentId(s.getId());
                if (!attList.isEmpty()) {
                    sb.append("\nCourse-wise Attendance:\n");
                    for (AttendanceItem a : attList) {
                        sb.append("- ").append(a.getSubjectCode()).append(" (").append(a.getSubject()).append("): ")
                          .append(a.getPercentage()).append("% (").append(a.getPresent()).append("/").append(a.getTotal())
                          .append(" classes attended, Status: ").append(a.getStatus()).append(")\n");
                    }
                }

                // Enrolled subjects
                List<Subject> subjects = subjectRepository.findByDepartmentAndSemester(s.getDepartment(), s.getSemester());
                if (subjects.isEmpty()) {
                    subjects = subjectRepository.findByDepartment(s.getDepartment());
                }
                if (!subjects.isEmpty()) {
                    sb.append("\nEnrolled Subjects:\n");
                    for (Subject sub : subjects) {
                        sb.append("- ").append(sub.getCode()).append(": ").append(sub.getName())
                          .append(" (Credits: ").append(sub.getCredits()).append(", Faculty: ").append(sub.getFaculty())
                          .append(" [").append(sub.getFacultyEmail()).append("])\n");
                    }
                }

                // Marks
                List<Mark> marks = markRepository.findByStudentId(s.getId());
                if (!marks.isEmpty()) {
                    sb.append("\nExamination Marks:\n");
                    for (Mark m : marks) {
                        sb.append("- ").append(m.getSubjectCode()).append(" (").append(m.getSubject()).append("): ")
                          .append(m.getScore()).append("/").append(m.getMaxScore()).append(" (Grade: ").append(m.getGrade())
                          .append(", Status: ").append(Boolean.TRUE.equals(m.getPassed()) ? "PASS" : "FAIL").append(")\n");
                    }
                }

                // Active Assignments
                List<Assignment> assignments = assignmentRepository.findAll();
                if (!assignments.isEmpty()) {
                    sb.append("\nAssignments:\n");
                    for (Assignment asg : assignments) {
                        sb.append("- ").append(asg.getTitle()).append(" [").append(asg.getSubject()).append("] - Due: ")
                          .append(asg.getDueDate()).append(", Max Marks: ").append(asg.getMaxMarks())
                          .append(", Status: ").append(asg.getStatus()).append("\n");
                    }
                }

                // Fees
                FeeInfo fee = feeRepository.findByStudentId(s.getId()).orElse(null);
                if (fee != null) {
                    sb.append("\nFee Account:\n");
                    sb.append("- Total: $").append(fee.getTotal()).append(" | Paid: $").append(fee.getPaid())
                      .append(" | Pending Balance: $").append(fee.getPending()).append("\n");
                }

                // Timetable
                List<TimetableEntry> ttList = timetableRepository.findByDepartmentAndSemesterOrderByPeriodAsc(s.getDepartment(), s.getSemester());
                if (!ttList.isEmpty()) {
                    sb.append("\nWeekly Timetable Schedule (Periods 1-7):\n");
                    for (TimetableEntry te : ttList) {
                        sb.append("- ").append(te.getDay()).append(" Period ").append(te.getPeriod()).append(": ")
                          .append(te.getSubjectCode()).append(" (").append(te.getSubjectName()).append(") in Room ")
                          .append(te.getRoom()).append(" with ").append(te.getFacultyName()).append("\n");
                    }
                }
            }
        } else if ("faculty".equals(role)) {
            Faculty f = findActiveFaculty(username);
            if (f != null) {
                sb.append("--- ACTIVE FACULTY PROFILE ---\n");
                sb.append("Name: ").append(f.getName()).append(" (ID: ").append(f.getFacultyId()).append(")\n");
                sb.append("Designation: ").append(f.getDesignation()).append(" | Department: ").append(f.getDepartment()).append("\n");
                sb.append("Qualification: ").append(f.getQualification()).append(" | Email: ").append(f.getEmail()).append("\n");
                sb.append("Assigned Subjects: ").append(String.join(", ", f.getSubjects())).append("\n");

                List<Student> deptStudents = studentRepository.findByDepartment(f.getDepartment());
                sb.append("Department Enrolled Students: ").append(deptStudents.size()).append("\n");

                List<Student> lowAtt = deptStudents.stream().filter(st -> st.getAttendance() != null && st.getAttendance() < 75).toList();
                if (!lowAtt.isEmpty()) {
                    sb.append("Students in Department with Attendance Below 75%:\n");
                    for (Student la : lowAtt) {
                        sb.append("- ").append(la.getName()).append(" (").append(la.getStudentId()).append("): ").append(la.getAttendance()).append("%\n");
                    }
                }
            }
        } else {
            // ADMIN ROLE CONTEXT
            List<Student> allStudents = studentRepository.findAll();
            List<StudentDetails> allDetails = studentDetailsRepository.findAll();
            double avgCgpa = allDetails.stream().mapToDouble(d -> d.getCgpa() != null ? d.getCgpa() : 8.0).average().orElse(8.3);
            long atRiskCount = allDetails.stream().filter(d -> (d.getArrearCount() != null && d.getArrearCount() > 0) || (d.getCgpa() != null && d.getCgpa() < 7.5)).count();

            sb.append("--- CAMPUS-WIDE EXECUTIVE ADMINISTRATIVE INTELLIGENCE ---\n");
            sb.append("Institution: Meridian University\n");
            sb.append("Total Registered Students: ").append(allStudents.size()).append("\n");
            sb.append("Campus Overall Average CGPA: ").append(String.format("%.2f", avgCgpa)).append(" / 10.0\n");
            sb.append("Total Faculty Members: ").append(facultyRepository.count()).append(" across 5 Departments\n");
            sb.append("Students with Standing Arrears / At-Risk CGPA: ").append(atRiskCount).append("\n\n");

            sb.append("Department Breakdown:\n");
            for (Department d : departmentRepository.findAll()) {
                long stdCount = allStudents.stream().filter(st -> d.getCode().equalsIgnoreCase(st.getDepartment())).count();
                sb.append("- ").append(d.getCode()).append(" (").append(d.getName()).append("): ")
                  .append(stdCount).append(" students, HoD: ").append(d.getHod()).append("\n");
            }

            List<Student> below75 = allStudents.stream().filter(st -> st.getAttendance() != null && st.getAttendance() < 75).toList();
            if (!below75.isEmpty()) {
                sb.append("\nStudents with Attendance Below Mandatory 75% Threshold:\n");
                for (Student b : below75) {
                    sb.append("- ").append(b.getName()).append(" [").append(b.getStudentId()).append(", ").append(b.getDepartment())
                      .append("]: ").append(b.getAttendance()).append("%\n");
                }
            }

            List<FeeInfo> fees = feeRepository.findAll();
            double totalBilled = fees.stream().mapToDouble(fi -> fi.getTotal() != null ? fi.getTotal() : 0).sum();
            double totalPaid = fees.stream().mapToDouble(fi -> fi.getPaid() != null ? fi.getPaid() : 0).sum();
            double totalPending = fees.stream().mapToDouble(fi -> fi.getPending() != null ? fi.getPending() : 0).sum();
            sb.append("\nCampus Tuition Financials:\n");
            sb.append("- Total Invoiced: $").append(String.format("%,.0f", totalBilled))
              .append(" | Collected: $").append(String.format("%,.0f", totalPaid))
              .append(" | Outstanding Dues: $").append(String.format("%,.0f", totalPending)).append("\n");
        }

        // Campus-wide upcoming events
        List<CampusEvent> events = eventRepository.findAll();
        if (!events.isEmpty()) {
            sb.append("\nUpcoming Campus Events:\n");
            for (CampusEvent ev : events) {
                sb.append("- ").append(ev.getTitle()).append(" (Date: ").append(ev.getDate()).append(" at ").append(ev.getTime())
                  .append(", Venue: ").append(ev.getVenue()).append(", Organizer: ").append(ev.getOrganizer())
                  .append(", Registered: ").append(ev.getRegistered()).append("/").append(ev.getCapacity()).append(")\n");
            }
        }

        // Active announcements
        List<Announcement> announcements = announcementRepository.findAll();
        if (!announcements.isEmpty()) {
            sb.append("\nActive Announcements:\n");
            for (Announcement a : announcements) {
                sb.append("- [").append(a.getPriority().toUpperCase()).append("] ").append(a.getTitle())
                  .append(" (").append(a.getPublishedAt()).append("): ").append(a.getContent()).append("\n");
            }
        }

        // Recent Complaints summary
        List<Complaint> complaints = complaintRepository.findAll();
        long openTickets = complaints.stream().filter(c -> !"resolved".equalsIgnoreCase(c.getStatus()) && !"rejected".equalsIgnoreCase(c.getStatus())).count();
        sb.append("\nCampus Service Desk / Complaints: ").append(openTickets).append(" open tickets.\n");

        return sb.toString();
    }
}
