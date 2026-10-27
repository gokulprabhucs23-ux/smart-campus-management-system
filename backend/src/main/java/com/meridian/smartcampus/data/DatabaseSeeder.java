package com.meridian.smartcampus.data;

import com.meridian.smartcampus.model.*;
import com.meridian.smartcampus.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final StudentDetailsRepository studentDetailsRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final AnnouncementRepository announcementRepository;
    private final EventRepository eventRepository;
    private final ComplaintRepository complaintRepository;
    private final NotificationRepository notificationRepository;
    private final FeeRepository feeRepository;
    private final TimetableRepository timetableRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public DatabaseSeeder(UserRepository userRepository,
                          DepartmentRepository departmentRepository,
                          StudentRepository studentRepository,
                          StudentDetailsRepository studentDetailsRepository,
                          FacultyRepository facultyRepository,
                          SubjectRepository subjectRepository,
                          AttendanceRepository attendanceRepository,
                          MarkRepository markRepository,
                          AssignmentRepository assignmentRepository,
                          AssignmentSubmissionRepository submissionRepository,
                          AnnouncementRepository announcementRepository,
                          EventRepository eventRepository,
                          ComplaintRepository complaintRepository,
                          NotificationRepository notificationRepository,
                          FeeRepository feeRepository,
                          TimetableRepository timetableRepository,
                          LeaveRequestRepository leaveRequestRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.studentDetailsRepository = studentDetailsRepository;
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.announcementRepository = announcementRepository;
        this.eventRepository = eventRepository;
        this.complaintRepository = complaintRepository;
        this.notificationRepository = notificationRepository;
        this.feeRepository = feeRepository;
        this.timetableRepository = timetableRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public record TeacherSpec(
            String username, String name, String code, String subjectName,
            int semester, int year, int credits, String qualification,
            String designation, String phone, String email) {}

    @Override
    public void run(String... args) {
        log.info("Purging old collections and reseeding clean 4-year engineering data across 5 departments...");

        // 1. Wipe all collections
        userRepository.deleteAll();
        departmentRepository.deleteAll();
        facultyRepository.deleteAll();
        subjectRepository.deleteAll();
        studentRepository.deleteAll();
        studentDetailsRepository.deleteAll();
        attendanceRepository.deleteAll();
        markRepository.deleteAll();
        assignmentRepository.deleteAll();
        submissionRepository.deleteAll();
        announcementRepository.deleteAll();
        eventRepository.deleteAll();
        complaintRepository.deleteAll();
        notificationRepository.deleteAll();
        feeRepository.deleteAll();
        timetableRepository.deleteAll();
        leaveRequestRepository.deleteAll();

        // 2. Administrator Users
        userRepository.save(new User(1L, "admin", "123", "Campus Administrator", "admin@meridian.edu", "admin", "Administration"));
        userRepository.save(new User(2L, "admin1", "123", "Head Administrator", "admin1@meridian.edu", "admin", "Administration"));

        // 3. 5 Engineering Departments (4-Year B.Tech / B.E. Program, 8 Semesters)
        departmentRepository.save(new Department(1L, "Computer Science & Engineering", "CSE", "Dr. Alan Turing", "Computing systems, software architecture, algorithms, and artificial intelligence.", 150, 8));
        departmentRepository.save(new Department(2L, "Electronics & Communication Engineering", "ECE", "Dr. Claude Shannon", "Circuits, microcontrollers, VLSI, signal processing, and communication networks.", 130, 8));
        departmentRepository.save(new Department(3L, "Mechanical Engineering", "MECH", "Dr. Rudolf Diesel", "Thermodynamics, robotics, fluid mechanics, CAD/CAM, and automotive design.", 115, 8));
        departmentRepository.save(new Department(4L, "Civil Engineering", "CIVIL", "Dr. Arthur Casagrande", "Structural analysis, geotechnical surveying, environmental engineering, and BIM.", 95, 8));
        departmentRepository.save(new Department(5L, "Electrical & Electronics Engineering", "EEE", "Dr. Michael Faraday", "Power systems, renewable grids, electrical machines, and control engineering.", 100, 8));

        // 4. Seeding Curriculum: 5 Departments * 8 Semesters * 5 Subjects = 200 Subjects with 200 Dedicated Professors
        // Rule: Each staff member has strictly ONE subject as professor and teaches only that subject.
        Map<String, List<TeacherSpec>> deptMap = new LinkedHashMap<>();
        deptMap.put("CSE", createCseTeachers());
        deptMap.put("ECE", createEceTeachers());
        deptMap.put("MECH", createMechTeachers());
        deptMap.put("CIVIL", createCivilTeachers());
        deptMap.put("EEE", createEeeTeachers());

        long teacherUserNumericId = 10L;
        long facultyIdCounter = 1L;
        long subjectIdCounter = 1L;

        for (Map.Entry<String, List<TeacherSpec>> entry : deptMap.entrySet()) {
            String deptCode = entry.getKey();
            List<TeacherSpec> teachers = entry.getValue();

            for (TeacherSpec ts : teachers) {
                // Save Teacher login credentials (password 123)
                userRepository.save(new User(teacherUserNumericId, ts.username(), "123", ts.name(), ts.email(), "faculty", deptCode));

                // Save Faculty entity with phone number (1 subject handled)
                Faculty facultyEntity = new Faculty(facultyIdCounter, "FAC" + (1000 + facultyIdCounter), ts.name(), ts.email(), deptCode, ts.designation(), ts.qualification(), "active", List.of(ts.code()));
                facultyEntity.setPhone(ts.phone());
                facultyRepository.save(facultyEntity);

                // Save Subject entity with semester (1-8), year (1-4), and faculty details
                subjectRepository.save(new Subject(
                        subjectIdCounter, ts.code(), ts.subjectName(), deptCode,
                        ts.semester(), ts.year(), ts.credits(),
                        ts.name(), ts.username(), ts.email(), ts.phone(), ts.designation()
                ));

                teacherUserNumericId++;
                facultyIdCounter++;
                subjectIdCounter++;
            }
        }

        // 5. Students across all 8 semesters (40 students total, 8 per department)
        record StudentSpec(
                String studentId, String username, String alias, String name,
                String dept, int year, int semester, String section, int attendance) {}

        List<StudentSpec> studentList = List.of(
                // CSE Students across all 8 Semesters
                new StudentSpec("S1001", "student1cse", "user1cse", "John Doe", "CSE", 1, 1, "A", 95),
                new StudentSpec("S1002", "student2cse", "user2cse", "Emma Wilson", "CSE", 1, 2, "A", 88),
                new StudentSpec("S1003", "student3cse", "user3cse", "Lucas Brown", "CSE", 2, 3, "B", 94),
                new StudentSpec("S1004", "student4cse", "user4cse", "Sophia Miller", "CSE", 2, 4, "B", 86),
                new StudentSpec("S1005", "student5cse", "user5cse", "Mia Davis", "CSE", 3, 5, "A", 91),
                new StudentSpec("S1006", "student6cse", "user6cse", "Alex Morgan", "CSE", 3, 6, "A", 93), // Semester 6 Student!
                new StudentSpec("S1007", "student7cse", "user7cse", "Ethan Miller", "CSE", 4, 7, "C", 89),
                new StudentSpec("S1008", "student8cse", "user8cse", "Olivia Clark", "CSE", 4, 8, "C", 92),

                // ECE Students across all 8 Semesters
                new StudentSpec("S2001", "student1ece", "user1ece", "Alex Rivera", "ECE", 1, 1, "A", 94),
                new StudentSpec("S2002", "student2ece", "user2ece", "Chloe Bennett", "ECE", 1, 2, "A", 88),
                new StudentSpec("S2003", "student3ece", "user3ece", "Daniel Kim", "ECE", 2, 3, "B", 92),
                new StudentSpec("S2004", "student4ece", "user4ece", "James Wilson", "ECE", 2, 4, "B", 85),
                new StudentSpec("S2005", "student5ece", "user5ece", "Grace Hopper", "ECE", 3, 5, "A", 96),
                new StudentSpec("S2006", "student6ece", "user6ece", "Maya Lin", "ECE", 3, 6, "A", 90),
                new StudentSpec("S2007", "student7ece", "user7ece", "Liam Johnson", "ECE", 4, 7, "C", 78),
                new StudentSpec("S2008", "student8ece", "user8ece", "Elena Rostova", "ECE", 4, 8, "C", 94),

                // MECH Students across all 8 Semesters
                new StudentSpec("S3001", "student1mech", "user1mech", "Marcus Vance", "MECH", 1, 1, "A", 90),
                new StudentSpec("S3002", "student2mech", "user2mech", "Olivia Taylor", "MECH", 1, 2, "A", 85),
                new StudentSpec("S3003", "student3mech", "user3mech", "Ryan Patel", "MECH", 2, 3, "B", 93),
                new StudentSpec("S3004", "student4mech", "user4mech", "Kevin Hart", "MECH", 2, 4, "B", 88),
                new StudentSpec("S3005", "student5mech", "user5mech", "Sarah Connor", "MECH", 3, 5, "A", 87),
                new StudentSpec("S3006", "student6mech", "user6mech", "Leo Watson", "MECH", 3, 6, "A", 91),
                new StudentSpec("S3007", "student7mech", "user7mech", "Tyler Durden", "MECH", 4, 7, "C", 76),
                new StudentSpec("S3008", "student8mech", "user8mech", "Arthur Pendelton", "MECH", 4, 8, "C", 89),

                // CIVIL Students across all 8 Semesters
                new StudentSpec("S4001", "student1civil", "user1civil", "Nathan Drake", "CIVIL", 1, 1, "A", 92),
                new StudentSpec("S4002", "student2civil", "user2civil", "Sophia Martinez", "CIVIL", 1, 2, "A", 89),
                new StudentSpec("S4003", "student3civil", "user3civil", "Victor Vance", "CIVIL", 2, 3, "B", 95),
                new StudentSpec("S4004", "student4civil", "user4civil", "Chloe Frazer", "CIVIL", 2, 4, "B", 87),
                new StudentSpec("S4005", "student5civil", "user5civil", "Wendy Wu", "CIVIL", 3, 5, "A", 91),
                new StudentSpec("S4006", "student6civil", "user6civil", "Samuel Stone", "CIVIL", 3, 6, "A", 88),
                new StudentSpec("S4007", "student7civil", "user7civil", "Zachary Bell", "CIVIL", 4, 7, "C", 84),
                new StudentSpec("S4008", "student8civil", "user8civil", "Diana Ross", "CIVIL", 4, 8, "C", 93),

                // EEE Students across all 8 Semesters
                new StudentSpec("S5001", "student1eee", "user1eee", "Benjamin Franklin", "EEE", 1, 1, "A", 96),
                new StudentSpec("S5002", "student2eee", "user2eee", "Diana Prince", "EEE", 1, 2, "A", 92),
                new StudentSpec("S5003", "student3eee", "user3eee", "Edward Norton", "EEE", 2, 3, "B", 85),
                new StudentSpec("S5004", "student4eee", "user4eee", "Bruce Wayne", "EEE", 2, 4, "B", 90),
                new StudentSpec("S5005", "student5eee", "user5eee", "Fiona Gallagher", "EEE", 3, 5, "A", 89),
                new StudentSpec("S5006", "student6eee", "user6eee", "Barry Allen", "EEE", 3, 6, "A", 95),
                new StudentSpec("S5007", "student7eee", "user7eee", "George Clark", "EEE", 4, 7, "C", 79),
                new StudentSpec("S5008", "student8eee", "user8eee", "Hal Jordan", "EEE", 4, 8, "C", 91)
        );

        long studentUserNumericId = 100L;
        long studentEntityIdCounter = 1L;

        for (StudentSpec st : studentList) {
            // Save student credentials (both studentXdept and userXdept aliases with password 123)
            userRepository.save(new User(studentUserNumericId, st.username(), "123", st.name(), st.username() + "@meridian.edu", "student", st.dept()));
            userRepository.save(new User(studentUserNumericId + 500, st.alias(), "123", st.name(), st.alias() + "@meridian.edu", "student", st.dept()));

            // Save Student entity with semester and year
            studentRepository.save(new Student(studentEntityIdCounter, st.studentId(), st.name(), st.username() + "@meridian.edu", st.dept(), st.year(), st.semester(), st.section(), "active", st.attendance(), 1L));

            // Generate realistic semesterPercentages, currentArrears, arrearsHistory for this student
            List<Map<String, Object>> semesterPercentages = new ArrayList<>();
            List<Map<String, Object>> sgpaList = new ArrayList<>();
            double cumulativeGradePoints = 0;
            int totalCreditsAccumulated = 0;
            for (int s = 1; s <= st.semester(); s++) {
                int yr = (s + 1) / 2;
                double pct = 82.0 + ((s * 3.7 + studentEntityIdCounter * 2.1) % 11.5);
                double sgpa = Math.round((pct / 10.0) * 10.0) / 10.0;
                int semCredits = 22;
                cumulativeGradePoints += (sgpa * semCredits);
                totalCreditsAccumulated += semCredits;
                double cumCgpa = Math.round((cumulativeGradePoints / totalCreditsAccumulated) * 100.0) / 100.0;
                String status = (s == st.semester()) ? "Ongoing" : "Passed";
                semesterPercentages.add(Map.of(
                        "semester", s,
                        "year", yr,
                        "percentage", Math.round(pct * 10.0) / 10.0,
                        "sgpa", sgpa,
                        "cgpa", cumCgpa,
                        "creditsEarned", semCredits,
                        "status", status
                ));
                sgpaList.add(Map.of("sem", s, "gpa", sgpa, "cgpa", cumCgpa));
            }

            // Arrears history (sample cleared arrears)
            List<Map<String, Object>> arrearsHistory = new ArrayList<>();
            if (st.year() >= 2) {
                arrearsHistory.add(Map.of(
                        "subjectCode", st.dept() + "101",
                        "subjectName", "Foundations of Engineering Science",
                        "semester", 1,
                        "clearedSemester", 2,
                        "grade", "B+",
                        "credits", 4,
                        "status", "Cleared",
                        "clearedDate", "May 2024"
                ));
            }

            // Current active arrears (backlogs)
            List<Map<String, Object>> currentArrears = new ArrayList<>();
            if (studentEntityIdCounter == 3L) {
                currentArrears.add(Map.of(
                        "subjectCode", "CS201",
                        "subjectName", "Data Structures & Algorithms",
                        "semester", 2,
                        "credits", 4,
                        "status", "Active Backlog",
                        "internalMarks", 18,
                        "examDate", "Nov 2026"
                ));
            }

            double cgpa = totalCreditsAccumulated > 0 ? Math.round((cumulativeGradePoints / totalCreditsAccumulated) * 100.0) / 100.0 : 8.75;

            // Save Student Details
            StudentDetails details = new StudentDetails();
            details.setStudentId(studentEntityIdCounter);
            details.setCgpa(cgpa);
            details.setSgpa(sgpaList);
            details.setArrearCount(currentArrears.size());
            details.setCurrentArrears(currentArrears);
            details.setArrearsHistory(arrearsHistory);
            details.setSemesterPercentages(semesterPercentages);
            details.setMentor(Map.of("name", "Dr. Alan Turing", "phone", "+1 (555) 011-2301"));
            int annualFee = 15000;
            int paidFee = st.year() == 1 ? 10000 : (st.year() * 15000 - 10000);
            int pendingFee = st.year() == 1 ? 5000 : 10000;
            details.setFees(Map.of("pending", pendingFee, "paid", paidFee, "total", annualFee * st.year()));
            studentDetailsRepository.save(details);

            studentUserNumericId++;
            studentEntityIdCounter++;
        }

        // 6. Systematic Marks Seeding across all 40 students strictly up to their active semester
        long markIdCounter = 1L;
        long studentLoopId = 1L;
        for (StudentSpec st : studentList) {
            long currentStudentId = studentLoopId;
            for (int sem = 1; sem <= st.semester(); sem++) {
                List<Subject> semSubjects = subjectRepository.findByDepartmentAndSemester(st.dept(), sem);
                for (Subject sub : semSubjects) {
                    boolean isHistory = sem < st.semester();
                    int subCredits = sub.getCredits() != null ? sub.getCredits() : 4;

                    if (isHistory) {
                        // History: Continuous Internal Assessment (CIA) - out of 50
                        double ciaScore = Math.round((41.0 + ((sem * 5 + sub.getId() * 3 + currentStudentId * 2) % 8.5)) * 10.0) / 10.0;
                        markRepository.save(new Mark(
                                markIdCounter++, currentStudentId, sub.getId(), sub.getCode(),
                                sub.getName() + " (" + sub.getCode() + ")", sem, (sem + 1) / 2, subCredits,
                                "Continuous Internal Assessment (CIA)", ciaScore, 50.0,
                                ciaScore >= 45 ? "O" : "A+", true
                        ));

                        // History: End Semester Examination - out of 100
                        double endSemScore = Math.round((79.0 + ((sem * 7 + sub.getId() * 4 + currentStudentId * 3) % 18.0)) * 10.0) / 10.0;
                        String endSemGrade = endSemScore >= 90 ? "O" : endSemScore >= 80 ? "A+" : endSemScore >= 70 ? "A" : "B+";
                        markRepository.save(new Mark(
                                markIdCounter++, currentStudentId, sub.getId(), sub.getCode(),
                                sub.getName() + " (" + sub.getCode() + ")", sem, (sem + 1) / 2, subCredits,
                                "End Semester Examination", endSemScore, 100.0,
                                endSemGrade, true
                        ));
                    } else {
                        // Current Semester (Ongoing Assessment)
                        // 1. Continuous Assessment 1 - out of 50
                        double ca1Score = Math.round((43.0 + ((sub.getId() * 3 + currentStudentId * 2) % 6.5)) * 10.0) / 10.0;
                        markRepository.save(new Mark(
                                markIdCounter++, currentStudentId, sub.getId(), sub.getCode(),
                                sub.getName() + " (" + sub.getCode() + ")", sem, (sem + 1) / 2, subCredits,
                                "Continuous Assessment 1", ca1Score, 50.0,
                                ca1Score >= 45 ? "O" : "A+", true
                        ));

                        // 2. Midterm Examination - out of 100
                        double midtermScore = Math.round((85.0 + ((sub.getId() * 4 + currentStudentId * 3) % 12.0)) * 10.0) / 10.0;
                        String midGrade = midtermScore >= 90 ? "O" : midtermScore >= 80 ? "A+" : "A";
                        markRepository.save(new Mark(
                                markIdCounter++, currentStudentId, sub.getId(), sub.getCode(),
                                sub.getName() + " (" + sub.getCode() + ")", sem, (sem + 1) / 2, subCredits,
                                "Midterm Examination", midtermScore, 100.0,
                                midGrade, true
                        ));

                        // 3. Lab Practical Exam (for practical courses)
                        if (subCredits <= 2) {
                            double labScore = Math.round((93.0 + ((sub.getId() * 2 + currentStudentId) % 6.0)) * 10.0) / 10.0;
                            markRepository.save(new Mark(
                                    markIdCounter++, currentStudentId, sub.getId(), sub.getCode(),
                                    sub.getName() + " (" + sub.getCode() + ")", sem, (sem + 1) / 2, subCredits,
                                    "Lab Practical Exam & Viva", labScore, 100.0,
                                    "O", true
                            ));
                        }
                    }
                }
            }
            studentLoopId++;
        }

        // 7. Seed Attendance for Student 1 (Semester 1) in their 5 subjects
        attendanceRepository.save(new AttendanceItem(1L, 1L, 1L, "CS101", "Problem Solving & Python Programming", 1, 38, 40, "good", 95, "Today"));
        attendanceRepository.save(new AttendanceItem(2L, 1L, 2L, "CS102", "Engineering Mathematics I", 1, 36, 40, "good", 90, "Yesterday"));
        attendanceRepository.save(new AttendanceItem(3L, 1L, 3L, "CS103", "Digital Principles & Logic Design", 1, 37, 40, "good", 92, "Today"));
        attendanceRepository.save(new AttendanceItem(4L, 1L, 4L, "CS104", "Engineering Physics & Circuit Theory", 1, 35, 40, "good", 88, "2 days ago"));
        attendanceRepository.save(new AttendanceItem(5L, 1L, 5L, "CS105", "Python & Computing Laboratory", 1, 20, 20, "good", 100, "Today"));

        // Seed Attendance for Student 6 (Semester 6 - Alex Morgan) in their 5 subjects
        attendanceRepository.save(new AttendanceItem(6L, 6L, 26L, "CS601", "Artificial Intelligence & Expert Systems", 6, 38, 40, "good", 95, "Today"));
        attendanceRepository.save(new AttendanceItem(7L, 6L, 27L, "CS602", "Cryptography & Network Security", 6, 37, 40, "good", 92, "Yesterday"));
        attendanceRepository.save(new AttendanceItem(8L, 6L, 28L, "CS603", "Mobile Application Development", 6, 36, 40, "good", 90, "Today"));
        attendanceRepository.save(new AttendanceItem(9L, 6L, 29L, "CS604", "Cloud Computing & Virtualization", 6, 38, 40, "good", 95, "3 days ago"));
        attendanceRepository.save(new AttendanceItem(10L, 6L, 30L, "CS605", "AI & Security Implementation Lab", 6, 20, 20, "good", 100, "Today"));

        // 8. Assignments across Semesters 1 and 6
        assignmentRepository.save(new Assignment(
                1L, 1L, "Problem Solving & Python Programming (CS101)",
                "Binary Tree Traversal & Recursion Analysis",
                "Implement preorder, inorder, and postorder traversals with runtime complexity proofs. Upload solution PDF.",
                "2026-10-15", 100, "due", 2, 5, false
        ));
        assignmentRepository.save(new Assignment(
                2L, 2L, "Engineering Mathematics I (CS102)",
                "Eigenvalues & Differential Equations Modeling",
                "Solve systems of second-order homogeneous differential equations. Submit step-by-step PDF.",
                "2026-10-20", 100, "due", 1, 5, false
        ));
        assignmentRepository.save(new Assignment(
                3L, 26L, "Artificial Intelligence & Expert Systems (CS601)",
                "Heuristic Search & Alpha-Beta Minimax Implementation",
                "Build an adversarial search agent utilizing alpha-beta pruning with benchmarking on Game Trees. Upload Python source code and report.",
                "2026-10-22", 100, "due", 1, 5, false
        ));
        assignmentRepository.save(new Assignment(
                4L, 27L, "Cryptography & Network Security (CS602)",
                "RSA & Elliptic Curve Cryptography Key Exchange",
                "Implement asymmetric key generation and signature verification with modular arithmetic proof.",
                "2026-10-25", 100, "due", 1, 5, false
        ));
        assignmentRepository.save(new Assignment(
                5L, 28L, "Mobile Application Development (CS603)",
                "Cross-Platform State Management Architecture",
                "Develop reactive dashboard prototype with offline-first local caching and push notifications.",
                "2026-10-28", 100, "due", 0, 5, false
        ));
        assignmentRepository.save(new Assignment(
                6L, 29L, "Cloud Computing & Virtualization (CS604)",
                "Kubernetes Cluster Orchestration & Ingress Setup",
                "Configure declarative deployment manifests with horizontal pod autoscaling and TLS ingress routing.",
                "2026-10-30", 100, "due", 0, 5, false
        ));

        // Sample PDF data URL for initial testing
        String samplePdfData = "data:application/pdf;base64,JVBERi0xLjQKJeLjz9MKMSAwIG9iago8PAovVHlwZSAvQ2F0YWxvZwovUGFnZXMgMiAwIFIKPj4KZW5kb2JqCjIgMCBvYmoKPDwKL1R5cGUgL1BhZ2VzCi9LaWRzIFszIDAgUl0KL0NvdW50IDEKPj4KZW5kb2JqCjMgMCBvYmoKPDwKL1R5cGUgL1BhZ2UKL1BhcmVudCAyIDAgUgovTWVkaWFCb3ggWzAgMCA2MTIgNzkyXQovQ29udGVudHMgNCAwIFIKPj4KZW5kb2JqCjQgMCBvYmoKPDwKL0xlbmd0aCA0Nwo+PgpzdHJlYW0KQVQgL0YxIDEyIFRmIDUwIDcwMCBUZCAoU21hcnQgQ2FtcHVzIEFzc2lnbm1lbnQgU3VibWlzc2lvbikgVGoKRVRKCmVuZHN0cmVhbQplbmRvYmoKeHJlZgowIDUKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwMDE1IDAwMDAwIG4gCjAwMDAwMDAwNjggMDAwMDAgbiAKMDAwMDAwMDEyNSAwMDAwMCBuIAowMDAwMDAwMjE2IDAwMDAwIG4gCnRyYWlsZXIKPDwKL1NpemUgNQovUm9vdCAxIDAgUgo+PgpzdGFydHhyZWYKMzEzCiUlRU9G";

        // Seed submissions for Assignment 1 (CS101) & Assignment 3 (CS601)
        submissionRepository.save(new AssignmentSubmission(
                1L, 1L, 1L, "John Doe", "student1cse", "CSE",
                "Implemented preorder, inorder and postorder tree traversals with complete test suite.",
                "submitted", "2026-09-20 18:30",
                "Binary_Tree_Report.pdf", "application/pdf", "340 KB", samplePdfData
        ));
        submissionRepository.save(new AssignmentSubmission(
                2L, 3L, 6L, "Alex Morgan", "student6cse", "CSE",
                "Implemented Alpha-Beta pruning minimax agent for Connect-4 with evaluation heuristic benchmarks.",
                "submitted", "2026-09-21 14:15",
                "Alpha_Beta_Agent_Report.pdf", "application/pdf", "420 KB", samplePdfData
        ));

        // 9. Announcements
        announcementRepository.save(new Announcement(1L, "Welcome to Academic Year 2026-2027", "Official curriculum and 6-day timetables across all 8 semesters for CSE, ECE, MECH, CIVIL, and EEE are published.", "All campus", "important", "Campus Administration", "2 hrs ago"));
        announcementRepository.save(new Announcement(2L, "Semester 6 Capstone Project Guidelines Released", "Sixth-semester engineering students must register project proposals with their assigned department mentors.", "Students", "normal", "Academic Dean", "1 day ago"));

        // 10. Events
        eventRepository.save(new CampusEvent(1L, "Annual Engineering Expo 2026", "Showcase of capstone projects across CSE, ECE, MECH, CIVIL, and EEE with industry evaluators.", "Oct 18", "09:00 AM", "Central Convention Center", "Engineering Council", 600, 320, true));
        eventRepository.save(new CampusEvent(2L, "Campus Career & Internship Fair", "Top technology, manufacturing, and construction companies recruiting on-campus graduates.", "Nov 05", "10:00 AM", "Student Center Plaza", "Placement Cell", 1200, 890, false));

        // 11. Complaints to Administrator
        complaintRepository.save(new Complaint(
                1L, "Library 2nd Floor High-Speed Wi-Fi Drops", "IT Support",
                "John Doe", "student1cse", "student", "CSE",
                "The wireless access point in the study hall disconnects during peak study hours between 2 PM and 5 PM.",
                "high", "Today", "submitted"
        ));
        complaintRepository.save(new Complaint(
                2L, "AI Lab GPU Cluster Driver Upgrade", "IT Support",
                "Alex Morgan", "student6cse", "student", "CSE",
                "CUDA drivers in Lab 3 need update to version 12.4 for PyTorch deep learning models.",
                "medium", "Yesterday", "progress"
        ));

        // 12. Notifications
        notificationRepository.save(new Notification(1L, "System Upgrade Completed", "Smart campus portal upgraded with full 8-semester curriculum (5 subjects per semester) and dedicated professors.", false, "10 mins ago", null, null, null));
        notificationRepository.save(new Notification(2L, "Assignment Deadline Approaching", "Alpha-Beta Pruning assignment for CS601 is due soon.", false, "1 hr ago", "student", "student6cse", 6L));

        // 13. Fee records for students
        FeeInfo feeInfo1 = new FeeInfo();
        feeInfo1.setId("fee_1");
        feeInfo1.setStudentId(1L);
        feeInfo1.setTotal(15000.0);
        feeInfo1.setPaid(10000.0);
        feeInfo1.setPending(5000.0);
        feeRepository.save(feeInfo1);

        FeeInfo feeInfo6 = new FeeInfo();
        feeInfo6.setId("fee_6");
        feeInfo6.setStudentId(6L);
        feeInfo6.setTotal(45000.0);
        feeInfo6.setPaid(35000.0);
        feeInfo6.setPending(10000.0);
        feeRepository.save(feeInfo6);

        // 14. Seed Monday to Saturday 7-period Timetables for ALL 5 Departments across ALL 8 Semesters
        // Rule: Each timetable contains ONLY that semester's 5 department subjects, taught by their dedicated professor.
        seedTimetables();

        // 15. Seed Leave Requests for Students and Faculty
        seedLeaveRequests();

        log.info("Database reset & seeding completed successfully with 5 departments, 200 subjects across 8 semesters (5 per semester), 200 dedicated professors (1 per subject), 40 students, and dedicated Monday-Saturday timetables.");
    }

    private void seedLeaveRequests() {
        leaveRequestRepository.save(new LeaveRequest(
                1L, "student", "student1cse", "John Doe", "CSE", "S1001",
                "Medical Leave", "2026-10-02", "2026-10-04", 3,
                "Viral fever and physician-advised rest. Medical prescription submitted.",
                "faculty", "teacher1cse", "Dr. Alan Turing",
                null, "APPROVED", "Dr. Alan Turing",
                "Approved on medical grounds. Please catch up on CS101 lab exercises.",
                "2026-10-02 11:30", "2026-10-01 18:20"
        ));

        leaveRequestRepository.save(new LeaveRequest(
                2L, "student", "student6cse", "Alex Morgan", "CSE", "S1006",
                "Academic / Hackathon Duty", "2026-10-14", "2026-10-16", 3,
                "Representing Meridian University at the National AI & Security Hackathon 2026.",
                "faculty", "teacher26cse", "Dr. Marvin Minsky",
                null, "APPROVED", "Dr. Marvin Minsky",
                "Approved. Best of luck at the hackathon competition.",
                "2026-10-09 10:00", "2026-10-08 09:15"
        ));

        leaveRequestRepository.save(new LeaveRequest(
                3L, "faculty", "teacher1cse", "Dr. Alan Turing", "CSE", null,
                "Academic Duty / Keynote", "2026-10-21", "2026-10-24", 4,
                "Keynote address at International Symposium on Machine Intelligence.",
                "admin", "admin", "Campus Administrator",
                null, "APPROVED", "Campus Administrator",
                "Approved for official academic duty.",
                "2026-10-07 14:00", "2026-10-06 10:30"
        ));
    }

    private void seedTimetables() {
        log.info("Seeding Monday-to-Saturday 7-period engineering timetables (45 min per period) for all 5 departments across all 8 semesters...");
        List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        long idCounter = 1L;

        String[][] timings = {
                {"09:00 AM", "09:45 AM"},
                {"09:45 AM", "10:30 AM"},
                {"10:45 AM", "11:30 AM"},
                {"11:30 AM", "12:15 PM"},
                {"01:00 PM", "01:45 PM"},
                {"01:45 PM", "02:30 PM"},
                {"02:30 PM", "03:15 PM"}
        };

        List<String> departments = List.of("CSE", "ECE", "MECH", "CIVIL", "EEE");

        for (String dept : departments) {
            for (int sem = 1; sem <= 8; sem++) {
                List<Subject> semSubjects = subjectRepository.findByDepartmentAndSemester(dept, sem);
                if (semSubjects.size() < 5) continue;

                Subject s1 = semSubjects.get(0);
                Subject s2 = semSubjects.get(1);
                Subject s3 = semSubjects.get(2);
                Subject s4 = semSubjects.get(3);
                Subject s5 = semSubjects.get(4); // Typically lab or practical course

                String roomLecture = dept + "-LH-" + (sem * 100 + 1);
                String roomLab = dept + "-Lab-" + sem;

                // Dedicated weekly rotation for the 5 subjects across 6 days (7 periods each)
                // Every single period contains ONLY one of the 5 subjects of this semester!
                for (int dayIdx = 0; dayIdx < days.size(); dayIdx++) {
                    String dayName = days.get(dayIdx);
                    for (int period = 1; period <= 7; period++) {
                        Subject activeSub;
                        String sessionType = "lecture";
                        String room = roomLecture;

                        switch (dayIdx) {
                            case 0 -> { // Monday
                                switch (period) {
                                    case 1 -> activeSub = s1;
                                    case 2 -> activeSub = s2;
                                    case 3 -> activeSub = s3;
                                    case 4 -> { activeSub = s4; sessionType = "tutorial"; }
                                    case 5 -> { activeSub = s5; sessionType = "lab"; room = roomLab; }
                                    case 6 -> { activeSub = s5; sessionType = "lab"; room = roomLab; }
                                    default -> { activeSub = s1; sessionType = "tutorial"; }
                                }
                            }
                            case 1 -> { // Tuesday
                                switch (period) {
                                    case 1 -> activeSub = s2;
                                    case 2 -> activeSub = s3;
                                    case 3 -> activeSub = s4;
                                    case 4 -> activeSub = s1;
                                    case 5 -> { activeSub = s5; sessionType = "lab"; room = roomLab; }
                                    case 6 -> { activeSub = s5; sessionType = "lab"; room = roomLab; }
                                    default -> { activeSub = s2; sessionType = "tutorial"; }
                                }
                            }
                            case 2 -> { // Wednesday
                                switch (period) {
                                    case 1 -> activeSub = s3;
                                    case 2 -> activeSub = s4;
                                    case 3 -> activeSub = s1;
                                    case 4 -> activeSub = s2;
                                    case 5 -> { activeSub = s3; sessionType = "tutorial"; }
                                    case 6 -> { activeSub = s4; sessionType = "tutorial"; }
                                    default -> { activeSub = s5; sessionType = "lab"; room = roomLab; }
                                }
                            }
                            case 3 -> { // Thursday
                                switch (period) {
                                    case 1 -> activeSub = s4;
                                    case 2 -> activeSub = s1;
                                    case 3 -> activeSub = s2;
                                    case 4 -> activeSub = s3;
                                    case 5 -> { activeSub = s5; sessionType = "lab"; room = roomLab; }
                                    case 6 -> { activeSub = s5; sessionType = "lab"; room = roomLab; }
                                    default -> { activeSub = s1; sessionType = "tutorial"; }
                                }
                            }
                            case 4 -> { // Friday
                                switch (period) {
                                    case 1 -> activeSub = s1;
                                    case 2 -> activeSub = s2;
                                    case 3 -> activeSub = s3;
                                    case 4 -> activeSub = s4;
                                    case 5 -> { activeSub = s5; sessionType = "lab"; room = roomLab; }
                                    case 6 -> { activeSub = s2; sessionType = "tutorial"; }
                                    default -> { activeSub = s3; sessionType = "tutorial"; }
                                }
                            }
                            default -> { // Saturday
                                switch (period) {
                                    case 1 -> activeSub = s2;
                                    case 2 -> activeSub = s3;
                                    case 3 -> activeSub = s4;
                                    case 4 -> activeSub = s1;
                                    case 5 -> { activeSub = s4; sessionType = "tutorial"; }
                                    case 6 -> { activeSub = s5; sessionType = "lab"; room = roomLab; }
                                    default -> activeSub = s1;
                                }
                            }
                        }

                        timetableRepository.save(new TimetableEntry(
                                idCounter++, dept, sem, "A",
                                dayName, period, timings[period - 1][0], timings[period - 1][1],
                                activeSub.getCode(), activeSub.getName(),
                                activeSub.getFaculty(), activeSub.getFacultyUsername(),
                                room, sessionType
                        ));
                    }
                }
            }
        }
    }

    private List<TeacherSpec> createCseTeachers() {
        return List.of(
                // Semester 1 (Year 1) - 5 Subjects
                new TeacherSpec("teacher1cse", "Dr. Alan Turing", "CS101", "Problem Solving & Python Programming", 1, 1, 4, "Ph.D. Computer Science", "Professor & HOD", "+1 (555) 011-2301", "teacher1cse@meridian.edu"),
                new TeacherSpec("teacher2cse", "Dr. Barbara Liskov", "CS102", "Engineering Mathematics I", 1, 1, 4, "Ph.D. Applied Mathematics", "Professor", "+1 (555) 011-2302", "teacher2cse@meridian.edu"),
                new TeacherSpec("teacher3cse", "Dr. Claude Shannon", "CS103", "Digital Principles & Logic Design", 1, 1, 4, "Ph.D. Information Theory", "Professor", "+1 (555) 011-2303", "teacher3cse@meridian.edu"),
                new TeacherSpec("teacher4cse", "Dr. John von Neumann", "CS104", "Engineering Physics & Circuit Theory", 1, 1, 4, "Ph.D. Applied Physics", "Associate Professor", "+1 (555) 011-2304", "teacher4cse@meridian.edu"),
                new TeacherSpec("teacher5cse", "Dr. Grace Hopper", "CS105", "Python & Computing Laboratory", 1, 1, 2, "Ph.D. Software Systems", "Assistant Professor", "+1 (555) 011-2305", "teacher5cse@meridian.edu"),

                // Semester 2 (Year 1) - 5 Subjects
                new TeacherSpec("teacher6cse", "Dr. Edgar Codd", "CS201", "Data Structures & Algorithms", 2, 1, 4, "Ph.D. Relational Systems", "Professor", "+1 (555) 011-2306", "teacher6cse@meridian.edu"),
                new TeacherSpec("teacher7cse", "Dr. Donald Knuth", "CS202", "Engineering Mathematics II", 2, 1, 4, "Ph.D. Mathematics", "Professor", "+1 (555) 011-2307", "teacher7cse@meridian.edu"),
                new TeacherSpec("teacher8cse", "Dr. Maurice Wilkes", "CS203", "Computer Organization & Architecture", 2, 1, 4, "Ph.D. Hardware Architecture", "Associate Professor", "+1 (555) 011-2308", "teacher8cse@meridian.edu"),
                new TeacherSpec("teacher9cse", "Dr. Bjarne Stroustrup", "CS204", "Object-Oriented Programming with C++", 2, 1, 4, "Ph.D. Programming Systems", "Associate Professor", "+1 (555) 011-2309", "teacher9cse@meridian.edu"),
                new TeacherSpec("teacher10cse", "Dr. Niklaus Wirth", "CS205", "Data Structures Laboratory", 2, 1, 2, "Ph.D. Compiler Science", "Assistant Professor", "+1 (555) 011-2310", "teacher10cse@meridian.edu"),

                // Semester 3 (Year 2) - 5 Subjects
                new TeacherSpec("teacher11cse", "Dr. James Gosling", "CS301", "Object Oriented Programming & Java", 3, 2, 4, "Ph.D. Programming Languages", "Professor", "+1 (555) 011-2311", "teacher11cse@meridian.edu"),
                new TeacherSpec("teacher12cse", "Dr. Paul Erdos", "CS302", "Discrete Mathematics & Graph Theory", 3, 2, 4, "Ph.D. Discrete Mathematics", "Professor", "+1 (555) 011-2312", "teacher12cse@meridian.edu"),
                new TeacherSpec("teacher13cse", "Dr. Ken Thompson", "CS303", "Systems Programming & Shell Scripting", 3, 2, 4, "M.S. Operating Systems", "Associate Professor", "+1 (555) 011-2313", "teacher13cse@meridian.edu"),
                new TeacherSpec("teacher14cse", "Dr. Michael Stonebraker", "CS304", "Database Management Systems", 3, 2, 4, "Ph.D. Database Systems", "Associate Professor", "+1 (555) 011-2314", "teacher14cse@meridian.edu"),
                new TeacherSpec("teacher15cse", "Dr. Dennis Ritchie", "CS305", "Java & RDBMS Laboratory", 3, 2, 2, "Ph.D. Systems Software", "Assistant Professor", "+1 (555) 011-2315", "teacher15cse@meridian.edu"),

                // Semester 4 (Year 2) - 5 Subjects
                new TeacherSpec("teacher16cse", "Dr. Linus Torvalds", "CS401", "Operating Systems & Kernel Internals", 4, 2, 4, "M.S. Systems Architecture", "Professor", "+1 (555) 011-2316", "teacher16cse@meridian.edu"),
                new TeacherSpec("teacher17cse", "Dr. Robert Tarjan", "CS402", "Design & Analysis of Algorithms", 4, 2, 4, "Ph.D. Computer Algorithms", "Professor", "+1 (555) 011-2317", "teacher17cse@meridian.edu"),
                new TeacherSpec("teacher18cse", "Dr. Frederick Brooks", "CS403", "Software Engineering Principles", 4, 2, 4, "Ph.D. Software Architecture", "Associate Professor", "+1 (555) 011-2318", "teacher18cse@meridian.edu"),
                new TeacherSpec("teacher19cse", "Dr. Michael Rabin", "CS404", "Theory of Computation & Automata", 4, 2, 4, "Ph.D. Theoretical CS", "Associate Professor", "+1 (555) 011-2319", "teacher19cse@meridian.edu"),
                new TeacherSpec("teacher20cse", "Dr. Richard Stallman", "CS405", "Operating Systems & Shell Lab", 4, 2, 2, "Ph.D. Systems Architecture", "Assistant Professor", "+1 (555) 011-2320", "teacher20cse@meridian.edu"),

                // Semester 5 (Year 3) - 5 Subjects
                new TeacherSpec("teacher21cse", "Dr. Tim Berners-Lee", "CS501", "Computer Networks & Protocols", 5, 3, 4, "Ph.D. Network Communications", "Professor", "+1 (555) 011-2321", "teacher21cse@meridian.edu"),
                new TeacherSpec("teacher22cse", "Dr. Sophie Wilson", "CS502", "Microprocessors & Embedded Microcontrollers", 5, 3, 4, "M.Tech Processor Design", "Associate Professor", "+1 (555) 011-2322", "teacher22cse@meridian.edu"),
                new TeacherSpec("teacher23cse", "Dr. Alfred Aho", "CS503", "Formal Languages & Compiler Construction", 5, 3, 4, "Ph.D. Compiler Design", "Associate Professor", "+1 (555) 011-2323", "teacher23cse@meridian.edu"),
                new TeacherSpec("teacher24cse", "Dr. Brendan Eich", "CS504", "Web Technologies & Modern Frameworks", 5, 3, 4, "M.S. Web Technologies", "Associate Professor", "+1 (555) 011-2324", "teacher24cse@meridian.edu"),
                new TeacherSpec("teacher25cse", "Dr. Vint Cerf", "CS505", "Networking & Protocols Laboratory", 5, 3, 2, "Ph.D. Internet Architecture", "Assistant Professor", "+1 (555) 011-2325", "teacher25cse@meridian.edu"),

                // Semester 6 (Year 3) - 5 Subjects (Crucial: Sixth Semester Student Curriculum)
                new TeacherSpec("teacher26cse", "Dr. Marvin Minsky", "CS601", "Artificial Intelligence & Expert Systems", 6, 3, 4, "Ph.D. Artificial Intelligence", "Professor", "+1 (555) 011-2326", "teacher26cse@meridian.edu"),
                new TeacherSpec("teacher27cse", "Dr. Ronald Rivest", "CS602", "Cryptography & Network Security", 6, 3, 4, "Ph.D. Cryptography", "Professor", "+1 (555) 011-2327", "teacher27cse@meridian.edu"),
                new TeacherSpec("teacher28cse", "Dr. Andy Rubin", "CS603", "Mobile Application Development", 6, 3, 4, "Ph.D. Mobile Computing", "Associate Professor", "+1 (555) 011-2328", "teacher28cse@meridian.edu"),
                new TeacherSpec("teacher29cse", "Dr. Werner Vogels", "CS604", "Cloud Computing & Virtualization", 6, 3, 4, "Ph.D. Distributed Systems", "Associate Professor", "+1 (555) 011-2329", "teacher29cse@meridian.edu"),
                new TeacherSpec("teacher30cse", "Dr. Yann LeCun", "CS605", "AI & Security Implementation Lab", 6, 3, 2, "Ph.D. Computational Intelligence", "Assistant Professor", "+1 (555) 011-2330", "teacher30cse@meridian.edu"),

                // Semester 7 (Year 4) - 5 Subjects
                new TeacherSpec("teacher31cse", "Dr. Geoffrey Hinton", "CS701", "Machine Learning & Deep Neural Networks", 7, 4, 4, "Ph.D. Cognitive Systems", "Professor", "+1 (555) 011-2331", "teacher31cse@meridian.edu"),
                new TeacherSpec("teacher32cse", "Dr. Jeffrey Dean", "CS702", "Big Data Analytics & Distributed Engines", 7, 4, 4, "Ph.D. Distributed Systems", "Professor", "+1 (555) 011-2332", "teacher32cse@meridian.edu"),
                new TeacherSpec("teacher33cse", "Dr. Gene Spafford", "CS703", "Cyber Defense & Information Assurance", 7, 4, 4, "Ph.D. Cyber Security", "Associate Professor", "+1 (555) 011-2333", "teacher33cse@meridian.edu"),
                new TeacherSpec("teacher34cse", "Dr. Kevin Ashton", "CS704", "Internet of Things & Edge Computing", 7, 4, 4, "Ph.D. IoT Architectures", "Associate Professor", "+1 (555) 011-2334", "teacher34cse@meridian.edu"),
                new TeacherSpec("teacher35cse", "Dr. Yoshua Bengio", "CS705", "Capstone Phase I Project & AI Lab", 7, 4, 2, "Ph.D. Neural Computing", "Assistant Professor", "+1 (555) 011-2335", "teacher35cse@meridian.edu"),

                // Semester 8 (Year 4) - 5 Subjects
                new TeacherSpec("teacher36cse", "Dr. Seymour Cray", "CS801", "High Performance Computing & Clusters", 8, 4, 4, "Ph.D. Supercomputing", "Professor", "+1 (555) 011-2336", "teacher36cse@meridian.edu"),
                new TeacherSpec("teacher37cse", "Dr. Leslie Lamport", "CS802", "Blockchain & Distributed Ledgers", 8, 4, 4, "Ph.D. Distributed Concurrency", "Professor", "+1 (555) 011-2337", "teacher37cse@meridian.edu"),
                new TeacherSpec("teacher38cse", "Dr. David Deutsch", "CS803", "Quantum Information Processing", 8, 4, 4, "Ph.D. Quantum Computation", "Associate Professor", "+1 (555) 011-2338", "teacher38cse@meridian.edu"),
                new TeacherSpec("teacher39cse", "Dr. Ada Lovelace", "CS804", "Professional Engineering Ethics & Law", 8, 4, 3, "Ph.D. Engineering Ethics", "Associate Professor", "+1 (555) 011-2339", "teacher39cse@meridian.edu"),
                new TeacherSpec("teacher40cse", "Dr. John Hopcroft", "CS805", "Major Project & Technical Dissertation", 8, 4, 6, "Ph.D. Computer Science", "Assistant Professor", "+1 (555) 011-2340", "teacher40cse@meridian.edu")
        );
    }

    private List<TeacherSpec> createEceTeachers() {
        return List.of(
                // Sem 1
                new TeacherSpec("teacher1ece", "Dr. Charles Proteus", "EC101", "Circuit Theory & Electric Analysis", 1, 1, 4, "Ph.D. Information Theory", "Professor & HOD", "+1 (555) 012-2301", "teacher1ece@meridian.edu"),
                new TeacherSpec("teacher2ece", "Dr. Oliver Heaviside", "EC102", "Engineering Mathematics I", 1, 1, 4, "Ph.D. Solid State Physics", "Professor", "+1 (555) 012-2302", "teacher2ece@meridian.edu"),
                new TeacherSpec("teacher3ece", "Dr. William Shockley", "EC103", "Semiconductor Physics & Devices", 1, 1, 4, "Ph.D. Semiconductor Physics", "Associate Professor", "+1 (555) 012-2303", "teacher3ece@meridian.edu"),
                new TeacherSpec("teacher4ece", "Dr. Sadi Carnot", "EC104", "Basic Mechanical & Civil Sciences", 1, 1, 4, "M.Tech General Engineering", "Associate Professor", "+1 (555) 012-2304", "teacher4ece@meridian.edu"),
                new TeacherSpec("teacher5ece", "Dr. Walter Brattain", "EC105", "Electric Circuits & Simulation Lab", 1, 1, 2, "Ph.D. Electronics Lab", "Assistant Professor", "+1 (555) 012-2305", "teacher5ece@meridian.edu"),

                // Sem 2
                new TeacherSpec("teacher6ece", "Dr. John Bardeen", "EC201", "Electronic Devices & Analog Circuits", 2, 1, 4, "Ph.D. Microelectronics", "Professor", "+1 (555) 012-2306", "teacher6ece@meridian.edu"),
                new TeacherSpec("teacher7ece", "Dr. Pierre Laplace", "EC202", "Engineering Mathematics II", 2, 1, 4, "Ph.D. Mathematics", "Professor", "+1 (555) 012-2307", "teacher7ece@meridian.edu"),
                new TeacherSpec("teacher8ece", "Dr. George Boole", "EC203", "Digital Logic & Switching Theory", 2, 1, 4, "Ph.D. Switching Logic", "Associate Professor", "+1 (555) 012-2308", "teacher8ece@meridian.edu"),
                new TeacherSpec("teacher9ece", "Dr. James Maxwell", "EC204", "Electromagnetic Fields & Waves", 2, 1, 4, "Ph.D. Electromagnetics", "Associate Professor", "+1 (555) 012-2309", "teacher9ece@meridian.edu"),
                new TeacherSpec("teacher10ece", "Dr. Robert Noyce", "EC205", "Analog Electronics Laboratory", 2, 1, 2, "M.Tech VLSI Circuits", "Assistant Professor", "+1 (555) 012-2310", "teacher10ece@meridian.edu"),

                // Sem 3
                new TeacherSpec("teacher11ece", "Dr. Jack Kilby", "EC301", "Digital Electronics & Microprocessors", 3, 2, 4, "Ph.D. Integrated Circuits", "Professor", "+1 (555) 012-2311", "teacher11ece@meridian.edu"),
                new TeacherSpec("teacher12ece", "Dr. Harry Nyquist", "EC302", "Signals & Linear Systems", 3, 2, 4, "Ph.D. Communication Networks", "Professor", "+1 (555) 012-2312", "teacher12ece@meridian.edu"),
                new TeacherSpec("teacher13ece", "Dr. Hendrik Bode", "EC303", "Network Analysis & Synthesis", 3, 2, 4, "Ph.D. Control Theory", "Associate Professor", "+1 (555) 012-2313", "teacher13ece@meridian.edu"),
                new TeacherSpec("teacher14ece", "Dr. Edwin Armstrong", "EC304", "Analog Communication Systems", 3, 2, 4, "Ph.D. RF Modulation", "Associate Professor", "+1 (555) 012-2314", "teacher14ece@meridian.edu"),
                new TeacherSpec("teacher15ece", "Dr. Gordon Moore", "EC305", "Digital Circuits & Microprocessor Lab", 3, 2, 2, "M.Tech Microprocessors", "Assistant Professor", "+1 (555) 012-2315", "teacher15ece@meridian.edu"),

                // Sem 4
                new TeacherSpec("teacher16ece", "Dr. Robert Widlar", "EC401", "Linear Integrated Circuits & Op-Amps", 4, 2, 4, "Ph.D. Analog ICs", "Professor", "+1 (555) 012-2316", "teacher16ece@meridian.edu"),
                new TeacherSpec("teacher17ece", "Dr. Guglielmo Marconi", "EC402", "Transmission Lines & Waveguides", 4, 2, 4, "Ph.D. Radio Transmission", "Professor", "+1 (555) 012-2317", "teacher17ece@meridian.edu"),
                new TeacherSpec("teacher18ece", "Dr. Harold Black", "EC403", "Control Engineering Systems", 4, 2, 4, "Ph.D. Feedback Systems", "Associate Professor", "+1 (555) 012-2318", "teacher18ece@meridian.edu"),
                new TeacherSpec("teacher19ece", "Dr. Alan Oppenheim", "EC404", "Digital Signal Processing Architecture", 4, 2, 4, "Ph.D. DSP Engineering", "Associate Professor", "+1 (555) 012-2319", "teacher19ece@meridian.edu"),
                new TeacherSpec("teacher20ece", "Dr. Federico Faggin", "EC405", "Integrated Circuits & Signal Lab", 4, 2, 2, "M.Tech Silicon Tech", "Assistant Professor", "+1 (555) 012-2320", "teacher20ece@meridian.edu"),

                // Sem 5
                new TeacherSpec("teacher21ece", "Dr. Claude Shannon", "EC501", "Digital Communication & Information Theory", 5, 3, 4, "Ph.D. Digital Communications", "Professor", "+1 (555) 012-2321", "teacher21ece@meridian.edu"),
                new TeacherSpec("teacher22ece", "Dr. Carver Mead", "EC502", "VLSI System Design & Verilog HDL", 5, 3, 4, "Ph.D. VLSI Microelectronics", "Professor", "+1 (555) 012-2322", "teacher22ece@meridian.edu"),
                new TeacherSpec("teacher23ece", "Dr. Lynn Conway", "EC503", "Microcontrollers & Embedded Systems", 5, 3, 4, "M.Tech Embedded Architecture", "Associate Professor", "+1 (555) 012-2323", "teacher23ece@meridian.edu"),
                new TeacherSpec("teacher24ece", "Dr. John Kraus", "EC504", "Antenna & Wave Propagation", 5, 3, 4, "Ph.D. Antenna Design", "Associate Professor", "+1 (555) 012-2324", "teacher24ece@meridian.edu"),
                new TeacherSpec("teacher25ece", "Dr. Irwin Jacobs", "EC505", "VLSI & Embedded Systems Lab", 5, 3, 2, "Ph.D. Communications", "Assistant Professor", "+1 (555) 012-2325", "teacher25ece@meridian.edu"),

                // Sem 6 (Year 3 - ECE Semester 6)
                new TeacherSpec("teacher26ece", "Dr. Heinrich Hertz", "EC601", "Wireless & Cellular Communications", 6, 3, 4, "Ph.D. Electromagnetics", "Professor", "+1 (555) 012-2326", "teacher26ece@meridian.edu"),
                new TeacherSpec("teacher27ece", "Dr. Robert Watson-Watt", "EC602", "Microwave Engineering & Radar", 6, 3, 4, "Ph.D. Radar Tech", "Professor", "+1 (555) 012-2327", "teacher27ece@meridian.edu"),
                new TeacherSpec("teacher28ece", "Dr. Neil Gershenfeld", "EC603", "Embedded IoT Systems & Sensors", 6, 3, 4, "Ph.D. Physics & Media", "Associate Professor", "+1 (555) 012-2328", "teacher28ece@meridian.edu"),
                new TeacherSpec("teacher29ece", "Dr. Charles Kao", "EC604", "Optical Fiber Communication Networks", 6, 3, 4, "Ph.D. Photonics", "Associate Professor", "+1 (555) 012-2329", "teacher29ece@meridian.edu"),
                new TeacherSpec("teacher30ece", "Dr. J. C. Bose", "EC605", "Wireless & Microwave Communication Lab", 6, 3, 2, "Ph.D. Millimeter Waves", "Assistant Professor", "+1 (555) 012-2330", "teacher30ece@meridian.edu"),

                // Sem 7
                new TeacherSpec("teacher31ece", "Dr. Behzad Razavi", "EC701", "RF Circuit Design & Transceivers", 7, 4, 4, "Ph.D. RF Microelectronics", "Professor", "+1 (555) 012-2331", "teacher31ece@meridian.edu"),
                new TeacherSpec("teacher32ece", "Dr. Arthur C. Clarke", "EC702", "Satellite & Space Communication", 7, 4, 4, "Ph.D. Geostationary Systems", "Professor", "+1 (555) 012-2332", "teacher32ece@meridian.edu"),
                new TeacherSpec("teacher33ece", "Dr. Willem Einthoven", "EC703", "Biomedical Instrumentation & Telemetry", 7, 4, 4, "Ph.D. Bio-Electronics", "Associate Professor", "+1 (555) 012-2333", "teacher33ece@meridian.edu"),
                new TeacherSpec("teacher34ece", "Dr. Rafael Gonzalez", "EC704", "Digital Image & Video Processing", 7, 4, 4, "Ph.D. Image Systems", "Associate Professor", "+1 (555) 012-2334", "teacher34ece@meridian.edu"),
                new TeacherSpec("teacher35ece", "Dr. Thomas Kailath", "EC705", "Optical & DSP Implementation Lab", 7, 4, 2, "Ph.D. Information Systems", "Assistant Professor", "+1 (555) 012-2335", "teacher35ece@meridian.edu"),

                // Sem 8
                new TeacherSpec("teacher36ece", "Dr. Andrea Goldsmith", "EC801", "5G/6G Mobile Network Architecture", 8, 4, 4, "Ph.D. Wireless Systems", "Professor", "+1 (555) 012-2336", "teacher36ece@meridian.edu"),
                new TeacherSpec("teacher37ece", "Dr. Richard Feynman", "EC802", "Nano-Electronics & MEMS Transducers", 8, 4, 4, "Ph.D. Quantum Devices", "Professor", "+1 (555) 012-2337", "teacher37ece@meridian.edu"),
                new TeacherSpec("teacher38ece", "Dr. Merrill Skolnik", "EC803", "Radar Signal Processing & Sonar", 8, 4, 4, "Ph.D. Radar Tech", "Associate Professor", "+1 (555) 012-2338", "teacher38ece@meridian.edu"),
                new TeacherSpec("teacher39ece", "Dr. Martin Cooper", "EC804", "Telecom Management & Regulatory Standards", 8, 4, 3, "Ph.D. Cellular Standards", "Associate Professor", "+1 (555) 012-2339", "teacher39ece@meridian.edu"),
                new TeacherSpec("teacher40ece", "Dr. Andrew Viterbi", "EC805", "Major Engineering Capstone & Project", 8, 4, 6, "Ph.D. Coding Theory", "Assistant Professor", "+1 (555) 012-2340", "teacher40ece@meridian.edu")
        );
    }

    private List<TeacherSpec> createMechTeachers() {
        return List.of(
                // Sem 1
                new TeacherSpec("teacher1mech", "Dr. Rudolf Diesel", "ME101", "Engineering Mechanics & Statics", 1, 1, 4, "Ph.D. Thermal Power", "Professor & HOD", "+1 (555) 013-2301", "teacher1mech@meridian.edu"),
                new TeacherSpec("teacher2mech", "Dr. Leonhard Euler", "ME102", "Engineering Mathematics I", 1, 1, 4, "Ph.D. Analytical Mechanics", "Professor", "+1 (555) 013-2302", "teacher2mech@meridian.edu"),
                new TeacherSpec("teacher3mech", "Dr. Robert Hooke", "ME103", "Engineering Physics & Materials Science", 1, 1, 4, "Ph.D. Elasticity", "Associate Professor", "+1 (555) 013-2303", "teacher3mech@meridian.edu"),
                new TeacherSpec("teacher4mech", "Dr. Gaspard Monge", "ME104", "Engineering Drawing & Computer Graphics", 1, 1, 4, "M.Tech Descriptive Geometry", "Associate Professor", "+1 (555) 013-2304", "teacher4mech@meridian.edu"),
                new TeacherSpec("teacher5mech", "Dr. Eli Whitney", "ME105", "Workshop Practice & Fitting Lab", 1, 1, 2, "M.Tech Manufacturing Lab", "Assistant Professor", "+1 (555) 013-2305", "teacher5mech@meridian.edu"),

                // Sem 2
                new TeacherSpec("teacher6mech", "Dr. Nicolas Carnot", "ME201", "Engineering Thermodynamics", 2, 1, 4, "Ph.D. Thermodynamics", "Professor", "+1 (555) 013-2306", "teacher6mech@meridian.edu"),
                new TeacherSpec("teacher7mech", "Dr. Joseph Lagrange", "ME202", "Engineering Mathematics II", 2, 1, 4, "Ph.D. Classical Dynamics", "Professor", "+1 (555) 013-2307", "teacher7mech@meridian.edu"),
                new TeacherSpec("teacher8mech", "Dr. Stephen Timoshenko", "ME203", "Mechanics of Solids & Elasticity", 2, 1, 4, "Ph.D. Solid Mechanics", "Associate Professor", "+1 (555) 013-2308", "teacher8mech@meridian.edu"),
                new TeacherSpec("teacher9mech", "Dr. Henry Sorby", "ME204", "Materials Science & Metallurgy", 2, 1, 4, "Ph.D. Metallography", "Associate Professor", "+1 (555) 013-2309", "teacher9mech@meridian.edu"),
                new TeacherSpec("teacher10mech", "Dr. Henry Maudslay", "ME205", "Computer Aided Machine Drafting Lab", 2, 1, 2, "M.Tech Machine Design", "Assistant Professor", "+1 (555) 013-2310", "teacher10mech@meridian.edu"),

                // Sem 3
                new TeacherSpec("teacher11mech", "Dr. Daniel Bernoulli", "ME301", "Fluid Mechanics & Machinery", 3, 2, 4, "Ph.D. Hydrodynamics", "Professor", "+1 (555) 013-2311", "teacher11mech@meridian.edu"),
                new TeacherSpec("teacher12mech", "Dr. Henry Ford", "ME302", "Manufacturing Technology & Casting", 3, 2, 4, "M.Tech Industrial Engineering", "Professor", "+1 (555) 013-2312", "teacher12mech@meridian.edu"),
                new TeacherSpec("teacher13mech", "Dr. William Rankine", "ME303", "Applied Thermodynamics & Steam Power", 3, 2, 4, "Ph.D. Thermal Systems", "Associate Professor", "+1 (555) 013-2313", "teacher13mech@meridian.edu"),
                new TeacherSpec("teacher14mech", "Dr. Franz Reuleaux", "ME304", "Kinematics of Machinery", 3, 2, 4, "Ph.D. Mechanisms", "Associate Professor", "+1 (555) 013-2314", "teacher14mech@meridian.edu"),
                new TeacherSpec("teacher15mech", "Dr. Osborne Reynolds", "ME305", "Fluid Mechanics & Machinery Lab", 3, 2, 2, "M.Tech Hydraulics", "Assistant Professor", "+1 (555) 013-2315", "teacher15mech@meridian.edu"),

                // Sem 4
                new TeacherSpec("teacher16mech", "Dr. James Watt", "ME401", "Dynamics of Machinery & Vibrations", 4, 2, 4, "Ph.D. Mechanical Dynamics", "Professor", "+1 (555) 013-2316", "teacher16mech@meridian.edu"),
                new TeacherSpec("teacher17mech", "Dr. Frederick Taylor", "ME402", "Metal Cutting & Machine Tools", 4, 2, 4, "Ph.D. Production Engineering", "Professor", "+1 (555) 013-2317", "teacher17mech@meridian.edu"),
                new TeacherSpec("teacher18mech", "Dr. Aurel Stodola", "ME403", "Thermal Engineering & Gas Turbines", 4, 2, 4, "Ph.D. Turbomachinery", "Associate Professor", "+1 (555) 013-2318", "teacher18mech@meridian.edu"),
                new TeacherSpec("teacher19mech", "Dr. Thomas Young", "ME404", "Strength of Materials Testing", 4, 2, 4, "Ph.D. Elastic Modulus", "Associate Professor", "+1 (555) 013-2319", "teacher19mech@meridian.edu"),
                new TeacherSpec("teacher20mech", "Dr. Joseph Whitworth", "ME405", "Manufacturing Technology Lab", 4, 2, 2, "M.Tech Production Lab", "Assistant Professor", "+1 (555) 013-2320", "teacher20mech@meridian.edu"),

                // Sem 5
                new TeacherSpec("teacher21mech", "Dr. Joseph Shigley", "ME501", "Design of Machine Elements", 5, 3, 4, "Ph.D. Mechanical Design", "Professor", "+1 (555) 013-2321", "teacher21mech@meridian.edu"),
                new TeacherSpec("teacher22mech", "Dr. Jean Fourier", "ME502", "Heat & Mass Transfer Principles", 5, 3, 4, "Ph.D. Conduction Theory", "Professor", "+1 (555) 013-2322", "teacher22mech@meridian.edu"),
                new TeacherSpec("teacher23mech", "Dr. Walter Shewhart", "ME503", "Metrology & Quality Inspection", 5, 3, 4, "Ph.D. Statistical Quality", "Associate Professor", "+1 (555) 013-2323", "teacher23mech@meridian.edu"),
                new TeacherSpec("teacher24mech", "Dr. Ernst Mach", "ME504", "Turbo Machines & Gas Dynamics", 5, 3, 4, "Ph.D. Shockwaves & Aero", "Associate Professor", "+1 (555) 013-2324", "teacher24mech@meridian.edu"),
                new TeacherSpec("teacher25mech", "Dr. Wilhelm Nusselt", "ME505", "Heat Transfer & Thermal Lab", 5, 3, 2, "M.Tech Thermal Lab", "Assistant Professor", "+1 (555) 013-2325", "teacher25mech@meridian.edu"),

                // Sem 6 (Year 3 - MECH Semester 6)
                new TeacherSpec("teacher26mech", "Dr. Patrick Hanratty", "ME601", "CAD/CAM & CNC Manufacturing", 6, 3, 4, "Ph.D. CAD/CAM Tech", "Professor", "+1 (555) 013-2326", "teacher26mech@meridian.edu"),
                new TeacherSpec("teacher27mech", "Dr. Robert Stirling", "ME602", "Automobile Engineering & Hybrid Tech", 6, 3, 4, "M.Tech Automotive Propulsion", "Professor", "+1 (555) 013-2327", "teacher27mech@meridian.edu"),
                new TeacherSpec("teacher28mech", "Dr. Olgierd Zienkiewicz", "ME603", "Finite Element Analysis & Simulation", 6, 3, 4, "Ph.D. FEA Computational", "Associate Professor", "+1 (555) 013-2328", "teacher28mech@meridian.edu"),
                new TeacherSpec("teacher29mech", "Dr. Carl Benz", "ME604", "Design of Transmission Systems", 6, 3, 4, "Ph.D. Automotive Design", "Associate Professor", "+1 (555) 013-2329", "teacher29mech@meridian.edu"),
                new TeacherSpec("teacher30mech", "Dr. Ferdinand Porsche", "ME605", "CAD/CAM Simulation & FEA Lab", 6, 3, 2, "M.Tech Numerical Methods", "Assistant Professor", "+1 (555) 013-2330", "teacher30mech@meridian.edu"),

                // Sem 7
                new TeacherSpec("teacher31mech", "Dr. George Devol", "ME701", "Robotics & Industrial Automation", 7, 4, 4, "Ph.D. Mechatronics", "Professor", "+1 (555) 013-2331", "teacher31mech@meridian.edu"),
                new TeacherSpec("teacher32mech", "Dr. Charles Parsons", "ME702", "Power Plant Engineering & Renewables", 7, 4, 4, "Ph.D. Energy Systems", "Professor", "+1 (555) 013-2332", "teacher32mech@meridian.edu"),
                new TeacherSpec("teacher33mech", "Dr. Tetsuro Mori", "ME703", "Mechatronics & Electro-Hydraulics", 7, 4, 4, "Ph.D. Automation Control", "Associate Professor", "+1 (555) 013-2333", "teacher33mech@meridian.edu"),
                new TeacherSpec("teacher34mech", "Dr. Willis Carrier", "ME704", "Refrigeration & Air Conditioning", 7, 4, 4, "Ph.D. HVAC Engineering", "Associate Professor", "+1 (555) 013-2334", "teacher34mech@meridian.edu"),
                new TeacherSpec("teacher35mech", "Dr. Victor Scheinman", "ME705", "Mechatronics & Robotics Lab", 7, 4, 2, "M.Tech Robotics Lab", "Assistant Professor", "+1 (555) 013-2335", "teacher35mech@meridian.edu"),

                // Sem 8
                new TeacherSpec("teacher36mech", "Dr. George Dantzig", "ME801", "Operations Research & Supply Chain", 8, 4, 4, "Ph.D. Optimization Theory", "Professor", "+1 (555) 013-2336", "teacher36mech@meridian.edu"),
                new TeacherSpec("teacher37mech", "Dr. Chuck Hull", "ME802", "Additive Manufacturing & 3D Printing", 8, 4, 4, "Ph.D. Rapid Prototyping", "Professor", "+1 (555) 013-2337", "teacher37mech@meridian.edu"),
                new TeacherSpec("teacher38mech", "Dr. Suhas Patankar", "ME803", "Computational Fluid Dynamics (CFD)", 8, 4, 4, "Ph.D. Computational Physics", "Associate Professor", "+1 (555) 013-2338", "teacher38mech@meridian.edu"),
                new TeacherSpec("teacher39mech", "Dr. Frank Gilbreth", "ME804", "Industrial Safety & Maintenance", 8, 4, 3, "Ph.D. Ergonomics", "Associate Professor", "+1 (555) 013-2339", "teacher39mech@meridian.edu"),
                new TeacherSpec("teacher40mech", "Dr. Soichiro Honda", "ME805", "Final Year Mechanical Capstone", 8, 4, 6, "Ph.D. Mechanical Systems", "Assistant Professor", "+1 (555) 013-2340", "teacher40mech@meridian.edu")
        );
    }

    private List<TeacherSpec> createCivilTeachers() {
        return List.of(
                // Sem 1
                new TeacherSpec("teacher1civil", "Dr. Arthur Casagrande", "CE101", "Engineering Geology & Rock Mechanics", 1, 1, 4, "Ph.D. Geotechnical Structures", "Professor & HOD", "+1 (555) 014-2301", "teacher1civil@meridian.edu"),
                new TeacherSpec("teacher2civil", "Dr. Augustin Cauchy", "CE102", "Engineering Mathematics I", 1, 1, 4, "Ph.D. Continuum Mechanics", "Professor", "+1 (555) 014-2302", "teacher2civil@meridian.edu"),
                new TeacherSpec("teacher3civil", "Dr. William Rankine", "CE103", "Engineering Mechanics for Structures", 1, 1, 4, "Ph.D. Structural Theory", "Associate Professor", "+1 (555) 014-2303", "teacher3civil@meridian.edu"),
                new TeacherSpec("teacher4civil", "Dr. Rachel Carson", "CE104", "Environmental Chemistry & Ecology", 1, 1, 4, "Ph.D. Environmental Sciences", "Associate Professor", "+1 (555) 014-2304", "teacher4civil@meridian.edu"),
                new TeacherSpec("teacher5civil", "Dr. Thomas Telford", "CE105", "Field Surveying & Levelling Lab", 1, 1, 2, "M.Tech Geoinformatics", "Assistant Professor", "+1 (555) 014-2305", "teacher5civil@meridian.edu"),

                // Sem 2
                new TeacherSpec("teacher6civil", "Dr. Robert Hooke", "CE201", "Mechanics of Solids & Beams", 2, 1, 4, "Ph.D. Elastic Beams", "Professor", "+1 (555) 014-2306", "teacher6civil@meridian.edu"),
                new TeacherSpec("teacher7civil", "Dr. Carl Gauss", "CE202", "Engineering Mathematics II", 2, 1, 4, "Ph.D. Geodesy & Math", "Professor", "+1 (555) 014-2307", "teacher7civil@meridian.edu"),
                new TeacherSpec("teacher8civil", "Dr. Joseph Aspdin", "CE203", "Construction Materials & Concrete Tech", 2, 1, 4, "Ph.D. Portland Cement", "Associate Professor", "+1 (555) 014-2308", "teacher8civil@meridian.edu"),
                new TeacherSpec("teacher9civil", "Dr. George Everest", "CE204", "Advanced Surveying & Geomatics", 2, 1, 4, "Ph.D. Trigonometrical Survey", "Associate Professor", "+1 (555) 014-2309", "teacher9civil@meridian.edu"),
                new TeacherSpec("teacher10civil", "Dr. Louis Vicat", "CE205", "Material Testing & Concrete Lab", 2, 1, 2, "M.Tech Material Testing", "Assistant Professor", "+1 (555) 014-2310", "teacher10civil@meridian.edu"),

                // Sem 3
                new TeacherSpec("teacher11civil", "Dr. Henry Darcy", "CE301", "Fluid Mechanics & Hydraulics", 3, 2, 4, "Ph.D. Porous Media & Flow", "Professor", "+1 (555) 014-2311", "teacher11civil@meridian.edu"),
                new TeacherSpec("teacher12civil", "Dr. Alberto Castigliano", "CE302", "Structural Analysis I (Determinate)", 3, 2, 4, "Ph.D. Strain Energy", "Professor", "+1 (555) 014-2312", "teacher12civil@meridian.edu"),
                new TeacherSpec("teacher13civil", "Dr. Karl Terzaghi", "CE303", "Soil Mechanics & Geotechnical Engg", 3, 2, 4, "Ph.D. Foundation Engineering", "Associate Professor", "+1 (555) 014-2313", "teacher13civil@meridian.edu"),
                new TeacherSpec("teacher14civil", "Dr. Vitruvius Pollio", "CE304", "Building Planning & Drawing", 3, 2, 4, "Ph.D. Architectural Systems", "Associate Professor", "+1 (555) 014-2314", "teacher14civil@meridian.edu"),
                new TeacherSpec("teacher15civil", "Dr. Antoine de Chezy", "CE305", "Fluid Mechanics & Hydraulic Lab", 3, 2, 2, "M.Tech Hydraulics Lab", "Assistant Professor", "+1 (555) 014-2315", "teacher15civil@meridian.edu"),

                // Sem 4
                new TeacherSpec("teacher16civil", "Dr. Hardy Cross", "CE401", "Structural Analysis II (Indeterminate)", 4, 2, 4, "Ph.D. Moment Distribution", "Professor", "+1 (555) 014-2316", "teacher16civil@meridian.edu"),
                new TeacherSpec("teacher17civil", "Dr. Robert Hazen", "CE402", "Water Supply Engineering & Quality", 4, 2, 4, "Ph.D. Hydraulic Filtration", "Professor", "+1 (555) 014-2317", "teacher17civil@meridian.edu"),
                new TeacherSpec("teacher18civil", "Dr. Ralph Peck", "CE403", "Foundation Engineering & Retaining Walls", 4, 2, 4, "Ph.D. Earth Retaining Systems", "Associate Professor", "+1 (555) 014-2318", "teacher18civil@meridian.edu"),
                new TeacherSpec("teacher19civil", "Dr. John McAdam", "CE404", "Highway Engineering & Traffic Design", 4, 2, 4, "M.Tech Highway Engineering", "Associate Professor", "+1 (555) 014-2319", "teacher19civil@meridian.edu"),
                new TeacherSpec("teacher20civil", "Dr. Donald Burmister", "CE405", "Geotechnical Engineering Lab", 4, 2, 2, "M.Tech Soil Mechanics", "Assistant Professor", "+1 (555) 014-2320", "teacher20civil@meridian.edu"),

                // Sem 5
                new TeacherSpec("teacher21civil", "Dr. Francois Hennebique", "CE501", "Design of Reinforced Concrete Structures", 5, 3, 4, "Ph.D. RCC Systems", "Professor", "+1 (555) 014-2321", "teacher21civil@meridian.edu"),
                new TeacherSpec("teacher22civil", "Dr. Karl Imhoff", "CE502", "Wastewater Engineering & Treatment", 5, 3, 4, "Ph.D. Sanitary Engineering", "Professor", "+1 (555) 014-2322", "teacher22civil@meridian.edu"),
                new TeacherSpec("teacher23civil", "Dr. Robert Horton", "CE503", "Hydrology & Water Resources Engg", 5, 3, 4, "Ph.D. Watershed Infiltration", "Associate Professor", "+1 (555) 014-2323", "teacher23civil@meridian.edu"),
                new TeacherSpec("teacher24civil", "Dr. Thomas MacDonald", "CE504", "Transportation Planning & Pavement", 5, 3, 4, "Ph.D. Pavement Design", "Associate Professor", "+1 (555) 014-2324", "teacher24civil@meridian.edu"),
                new TeacherSpec("teacher25civil", "Dr. Allen Hazen", "CE505", "Environmental Engineering Lab", 5, 3, 2, "M.Tech Environmental Lab", "Assistant Professor", "+1 (555) 014-2325", "teacher25civil@meridian.edu"),

                // Sem 6 (Year 3 - CIVIL Semester 6)
                new TeacherSpec("teacher26civil", "Dr. Gustave Eiffel", "CE601", "Design of Steel Structures & Bridges", 6, 3, 4, "Ph.D. Metallic Structures", "Professor", "+1 (555) 014-2326", "teacher26civil@meridian.edu"),
                new TeacherSpec("teacher27civil", "Dr. Arthur Cotton", "CE602", "Irrigation Engineering & Hydraulic Design", 6, 3, 4, "Ph.D. Barrages & Canals", "Professor", "+1 (555) 014-2327", "teacher27civil@meridian.edu"),
                new TeacherSpec("teacher28civil", "Dr. Nathan Newmark", "CE603", "Geotechnical Earthquake Engineering", 6, 3, 4, "Ph.D. Seismic Dynamics", "Associate Professor", "+1 (555) 014-2328", "teacher28civil@meridian.edu"),
                new TeacherSpec("teacher29civil", "Dr. Henry Gantt", "CE604", "Construction Management & BIM Software", 6, 3, 4, "Ph.D. Project Scheduling", "Associate Professor", "+1 (555) 014-2329", "teacher29civil@meridian.edu"),
                new TeacherSpec("teacher30civil", "Dr. John Roebling", "CE605", "Structural Design & Drafting Lab", 6, 3, 2, "M.Tech Structural CAD", "Assistant Professor", "+1 (555) 014-2330", "teacher30civil@meridian.edu"),

                // Sem 7
                new TeacherSpec("teacher31civil", "Dr. Eugene Freyssinet", "CE701", "Prestressed Concrete Structures", 7, 4, 4, "Ph.D. Prestressed Girders", "Professor", "+1 (555) 014-2331", "teacher31civil@meridian.edu"),
                new TeacherSpec("teacher32civil", "Dr. George Stephenson", "CE702", "Railways, Airports & Harbours", 7, 4, 4, "Ph.D. Track Geometrics", "Professor", "+1 (555) 014-2332", "teacher32civil@meridian.edu"),
                new TeacherSpec("teacher33civil", "Dr. Frank B. Gilbreth", "CE703", "Estimation, Costing & Valuation", 7, 4, 4, "Ph.D. Cost Estimations", "Associate Professor", "+1 (555) 014-2333", "teacher33civil@meridian.edu"),
                new TeacherSpec("teacher34civil", "Dr. Roger Tomlinson", "CE704", "Remote Sensing & GIS in Civil Engg", 7, 4, 4, "Ph.D. Geographic Systems", "Associate Professor", "+1 (555) 014-2334", "teacher34civil@meridian.edu"),
                new TeacherSpec("teacher35civil", "Dr. Edward Wilson", "CE705", "Computer Aided Design (STAAD/ETABS) Lab", 7, 4, 2, "M.Tech Computational Structures", "Assistant Professor", "+1 (555) 014-2335", "teacher35civil@meridian.edu"),

                // Sem 8
                new TeacherSpec("teacher36civil", "Dr. Fazlur Rahman Khan", "CE801", "Tall Buildings & Earthquake Resistant Design", 8, 4, 4, "Ph.D. Tube Structures", "Professor", "+1 (555) 014-2336", "teacher36civil@meridian.edu"),
                new TeacherSpec("teacher37civil", "Dr. Ian McHarg", "CE802", "Smart Cities & Green Building Rating", 8, 4, 4, "Ph.D. Sustainable Cities", "Professor", "+1 (555) 014-2337", "teacher37civil@meridian.edu"),
                new TeacherSpec("teacher38civil", "Dr. Charles Richter", "CE803", "Disaster Management & Hazard Mitigation", 8, 4, 4, "Ph.D. Seismology", "Associate Professor", "+1 (555) 014-2338", "teacher38civil@meridian.edu"),
                new TeacherSpec("teacher39civil", "Dr. Isambard Brunel", "CE804", "Professional Practice & Construction Law", 8, 4, 3, "Ph.D. Construction Contracts", "Associate Professor", "+1 (555) 014-2339", "teacher39civil@meridian.edu"),
                new TeacherSpec("teacher40civil", "Dr. Othmar Ammann", "CE805", "Civil Engineering Capstone Project", 8, 4, 6, "Ph.D. Bridge Engineering", "Assistant Professor", "+1 (555) 014-2340", "teacher40civil@meridian.edu")
        );
    }

    private List<TeacherSpec> createEeeTeachers() {
        return List.of(
                // Sem 1
                new TeacherSpec("teacher1eee", "Dr. Michael Faraday", "EE101", "Electric Circuit Theory & Analysis", 1, 1, 4, "Ph.D. Electrodynamics", "Professor & HOD", "+1 (555) 015-2301", "teacher1eee@meridian.edu"),
                new TeacherSpec("teacher2eee", "Dr. Gottfried Leibniz", "EE102", "Engineering Mathematics I", 1, 1, 4, "Ph.D. Calculus & Physics", "Professor", "+1 (555) 015-2302", "teacher2eee@meridian.edu"),
                new TeacherSpec("teacher3eee", "Dr. Charles Steinmetz", "EE103", "Electromagnetic Theory & Fields", 1, 1, 4, "Ph.D. Alternating Currents", "Associate Professor", "+1 (555) 015-2303", "teacher3eee@meridian.edu"),
                new TeacherSpec("teacher4eee", "Dr. Alessandro Volta", "EE104", "Engineering Chemistry & Energy Storage", 1, 1, 4, "Ph.D. Electrochemistry", "Associate Professor", "+1 (555) 015-2304", "teacher4eee@meridian.edu"),
                new TeacherSpec("teacher5eee", "Dr. Andre Ampere", "EE105", "Electric Circuits & Network Lab", 1, 1, 2, "M.Tech Circuits Lab", "Assistant Professor", "+1 (555) 015-2305", "teacher5eee@meridian.edu"),

                // Sem 2
                new TeacherSpec("teacher6eee", "Dr. Lord Kelvin", "EE201", "Electrical Measurements & Instruments", 2, 1, 4, "Ph.D. Thermodynamics & Meters", "Professor", "+1 (555) 015-2306", "teacher6eee@meridian.edu"),
                new TeacherSpec("teacher7eee", "Dr. Joseph Fourier", "EE202", "Engineering Mathematics II", 2, 1, 4, "Ph.D. Harmonic Analysis", "Professor", "+1 (555) 015-2307", "teacher7eee@meridian.edu"),
                new TeacherSpec("teacher8eee", "Dr. Lee de Forest", "EE203", "Electronic Devices & Analog Circuits", 2, 1, 4, "Ph.D. Vacuum & Solid Devices", "Associate Professor", "+1 (555) 015-2308", "teacher8eee@meridian.edu"),
                new TeacherSpec("teacher9eee", "Dr. George Westinghouse", "EE204", "Generation & Transmission of Power", 2, 1, 4, "Ph.D. AC Grid Engineering", "Associate Professor", "+1 (555) 015-2309", "teacher9eee@meridian.edu"),
                new TeacherSpec("teacher10eee", "Dr. Edward Weston", "EE205", "Electrical Measurements & Circuits Lab", 2, 1, 2, "M.Tech Metrology Lab", "Assistant Professor", "+1 (555) 015-2310", "teacher10eee@meridian.edu"),

                // Sem 3
                new TeacherSpec("teacher11eee", "Dr. Thomas Edison", "EE301", "DC Machines & Transformers", 3, 2, 4, "M.Tech Power Systems", "Professor", "+1 (555) 015-2311", "teacher11eee@meridian.edu"),
                new TeacherSpec("teacher12eee", "Dr. John von Neumann", "EE302", "Digital Logic & Microprocessor Architecture", 3, 2, 4, "Ph.D. Computer Systems", "Professor", "+1 (555) 015-2312", "teacher12eee@meridian.edu"),
                new TeacherSpec("teacher13eee", "Dr. Ernst Guillemin", "EE303", "Network Analysis & Synthesis", 3, 2, 4, "Ph.D. Circuit Synthesis", "Associate Professor", "+1 (555) 015-2313", "teacher13eee@meridian.edu"),
                new TeacherSpec("teacher14eee", "Dr. Paul Gray", "EE304", "Analog Integrated Circuits", 3, 2, 4, "Ph.D. Analog Drives", "Associate Professor", "+1 (555) 015-2314", "teacher14eee@meridian.edu"),
                new TeacherSpec("teacher15eee", "Dr. Nikola Tesla", "EE305", "DC Electrical Machines Lab", 3, 2, 2, "Ph.D. Electrical Machinery", "Assistant Professor", "+1 (555) 015-2315", "teacher15eee@meridian.edu"),

                // Sem 4
                new TeacherSpec("teacher16eee", "Dr. Joseph Henry", "EE401", "AC Rotating Machines & Alternators", 4, 2, 4, "Ph.D. Electrical Machinery", "Professor", "+1 (555) 015-2316", "teacher16eee@meridian.edu"),
                new TeacherSpec("teacher17eee", "Dr. Oliver Heaviside", "EE402", "Transmission & Grid Distribution", 4, 2, 4, "Ph.D. Power Grids", "Professor", "+1 (555) 015-2317", "teacher17eee@meridian.edu"),
                new TeacherSpec("teacher18eee", "Dr. Norbert Wiener", "EE403", "Linear Control Systems Engineering", 4, 2, 4, "Ph.D. Cybernetics & Control", "Associate Professor", "+1 (555) 015-2318", "teacher18eee@meridian.edu"),
                new TeacherSpec("teacher19eee", "Dr. Richard Hamming", "EE404", "Digital Signal Processing", 4, 2, 4, "Ph.D. Numerical Filters", "Associate Professor", "+1 (555) 015-2319", "teacher19eee@meridian.edu"),
                new TeacherSpec("teacher20eee", "Dr. M. Dolivo-Dobrovolsky", "EE405", "AC Electrical Machines Lab", 4, 2, 2, "M.Tech Polyphase Machinery", "Assistant Professor", "+1 (555) 015-2320", "teacher20eee@meridian.edu"),

                // Sem 5
                new TeacherSpec("teacher21eee", "Dr. John Bardeen", "EE501", "Power Electronics & Solid State Drives", 5, 3, 4, "Ph.D. Semiconductor Drives", "Professor", "+1 (555) 015-2321", "teacher21eee@meridian.edu"),
                new TeacherSpec("teacher22eee", "Dr. William Stevenson", "EE502", "Power System Analysis & Load Flow", 5, 3, 4, "Ph.D. Transmission Grids", "Professor", "+1 (555) 015-2322", "teacher22eee@meridian.edu"),
                new TeacherSpec("teacher23eee", "Dr. Gordon Bell", "EE503", "Microcontrollers & Embedded Controllers", 5, 3, 4, "Ph.D. Embedded Systems", "Associate Professor", "+1 (555) 015-2323", "teacher23eee@meridian.edu"),
                new TeacherSpec("teacher24eee", "Dr. Frank Peek", "EE504", "High Voltage Engineering & Insulation", 5, 3, 4, "Ph.D. Corona & Insulation", "Associate Professor", "+1 (555) 015-2324", "teacher24eee@meridian.edu"),
                new TeacherSpec("teacher25eee", "Dr. Bimal K. Bose", "EE505", "Power Electronics & Drives Lab", 5, 3, 2, "M.Tech Power Electronics", "Assistant Professor", "+1 (555) 015-2325", "teacher25eee@meridian.edu"),

                // Sem 6 (Year 3 - EEE Semester 6)
                new TeacherSpec("teacher26eee", "Dr. Charles Fritts", "EE601", "Renewable Energy Systems & Solar/Wind", 6, 3, 4, "Ph.D. Photovoltaic Systems", "Professor", "+1 (555) 015-2326", "teacher26eee@meridian.edu"),
                new TeacherSpec("teacher27eee", "Dr. C. Russell Mason", "EE602", "Power System Protection & Switchgear", 6, 3, 4, "Ph.D. Protective Relays", "Professor", "+1 (555) 015-2327", "teacher27eee@meridian.edu"),
                new TeacherSpec("teacher28eee", "Dr. Rudolf Kalman", "EE603", "Modern Control Systems & State Space", 6, 3, 4, "Ph.D. Filtering & Control", "Associate Professor", "+1 (555) 015-2328", "teacher28eee@meridian.edu"),
                new TeacherSpec("teacher29eee", "Dr. Frank Sprague", "EE604", "Electric Drives & Traction Systems", 6, 3, 4, "Ph.D. Electric Traction", "Associate Professor", "+1 (555) 015-2329", "teacher29eee@meridian.edu"),
                new TeacherSpec("teacher30eee", "Dr. Edith Clarke", "EE605", "Power System Simulation & Protection Lab", 6, 3, 2, "Ph.D. Circuit Calculations", "Assistant Professor", "+1 (555) 015-2330", "teacher30eee@meridian.edu"),

                // Sem 7
                new TeacherSpec("teacher31eee", "Dr. Massoud Amin", "EE701", "Smart Grid Technology & IoT Meters", 7, 4, 4, "Ph.D. Grid Security", "Professor", "+1 (555) 015-2331", "teacher31eee@meridian.edu"),
                new TeacherSpec("teacher32eee", "Dr. Ferdinand Porsche", "EE702", "Electric Vehicle Powertrains & Charging", 7, 4, 4, "Ph.D. Electric Propulsion", "Professor", "+1 (555) 015-2332", "teacher32eee@meridian.edu"),
                new TeacherSpec("teacher33eee", "Dr. Narain Hingorani", "EE703", "FACTS & HVDC Power Transmission", 7, 4, 4, "Ph.D. HVDC Converters", "Associate Professor", "+1 (555) 015-2333", "teacher33eee@meridian.edu"),
                new TeacherSpec("teacher34eee", "Dr. Richard Morley", "EE704", "Industrial Automation & PLC/SCADA", 7, 4, 4, "Ph.D. Ladder Logic", "Associate Professor", "+1 (555) 015-2334", "teacher34eee@meridian.edu"),
                new TeacherSpec("teacher35eee", "Dr. Hermann Helmholtz", "EE705", "Renewable Energy & Smart Grid Lab", 7, 4, 2, "M.Tech Clean Energy Lab", "Assistant Professor", "+1 (555) 015-2335", "teacher35eee@meridian.edu"),

                // Sem 8
                new TeacherSpec("teacher36eee", "Dr. Dugan McGranaghan", "EE801", "Power Quality & Harmonics Mitigation", 8, 4, 4, "Ph.D. Harmonic Distortion", "Professor", "+1 (555) 015-2336", "teacher36eee@meridian.edu"),
                new TeacherSpec("teacher37eee", "Dr. John Goodenough", "EE802", "Energy Storage & Battery Management", 8, 4, 4, "Ph.D. Battery Electrochemistry", "Professor", "+1 (555) 015-2337", "teacher37eee@meridian.edu"),
                new TeacherSpec("teacher38eee", "Dr. Fred Schweppe", "EE803", "Restructured Power Systems & Deregulation", 8, 4, 4, "Ph.D. Energy Economics", "Associate Professor", "+1 (555) 015-2338", "teacher38eee@meridian.edu"),
                new TeacherSpec("teacher39eee", "Dr. Benjamin Franklin", "EE804", "Professional Engineering Ethics & Safety", 8, 4, 3, "Ph.D. Industrial Safety", "Associate Professor", "+1 (555) 015-2339", "teacher39eee@meridian.edu"),
                new TeacherSpec("teacher40eee", "Dr. Werner von Siemens", "EE805", "Major Electrical Capstone Project", 8, 4, 6, "Ph.D. Power Systems", "Assistant Professor", "+1 (555) 015-2340", "teacher40eee@meridian.edu")
        );
    }
}
