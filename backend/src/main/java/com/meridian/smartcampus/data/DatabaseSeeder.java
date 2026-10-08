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
                          TimetableRepository timetableRepository) {
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
    }

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

        // 2. Administrator Users
        userRepository.save(new User(1L, "admin", "123", "Campus Administrator", "admin@meridian.edu", "admin", "Administration"));
        userRepository.save(new User(2L, "admin1", "123", "Head Administrator", "admin1@meridian.edu", "admin", "Administration"));

        // 3. 5 Engineering Departments (4-Year B.Tech / B.E. Program, 8 Semesters)
        departmentRepository.save(new Department(1L, "Computer Science & Engineering", "CSE", "Dr. Alan Turing", "Computing systems, software architecture, algorithms, and artificial intelligence.", 150, 8));
        departmentRepository.save(new Department(2L, "Electronics & Communication Engineering", "ECE", "Dr. Claude Shannon", "Circuits, microcontrollers, VLSI, signal processing, and communication networks.", 130, 8));
        departmentRepository.save(new Department(3L, "Mechanical Engineering", "MECH", "Dr. Rudolf Diesel", "Thermodynamics, robotics, fluid mechanics, CAD/CAM, and automotive design.", 115, 8));
        departmentRepository.save(new Department(4L, "Civil Engineering", "CIVIL", "Dr. Arthur Casagrande", "Structural analysis, geotechnical surveying, environmental engineering, and BIM.", 95, 8));
        departmentRepository.save(new Department(5L, "Electrical & Electronics Engineering", "EEE", "Dr. Michael Faraday", "Power systems, renewable grids, electrical machines, and control engineering.", 100, 8));

        // 4. Department Spec: 5 Departments * 8 Semesters = 40 Subjects with dedicated faculty
        record TeacherSpec(
                String username, String name, String code, String subjectName,
                int semester, int year, int credits, String qualification,
                String designation, String phone, String email) {}

        Map<String, List<TeacherSpec>> deptMap = new LinkedHashMap<>();

        deptMap.put("CSE", List.of(
                new TeacherSpec("teacher1cse", "Dr. Alan Turing", "CS101", "Problem Solving & Python Programming", 1, 1, 4, "Ph.D. Computer Science", "Professor & HOD", "+1 (555) 011-2301", "teacher1cse@meridian.edu"),
                new TeacherSpec("teacher2cse", "Dr. Barbara Liskov", "CS102", "Data Structures & Algorithms", 2, 1, 4, "Ph.D. Software Systems", "Professor", "+1 (555) 011-2302", "teacher2cse@meridian.edu"),
                new TeacherSpec("teacher3cse", "Dr. James Gosling", "CS201", "Object Oriented Programming & Java", 3, 2, 4, "Ph.D. Programming Languages", "Professor", "+1 (555) 011-2303", "teacher3cse@meridian.edu"),
                new TeacherSpec("teacher4cse", "Dr. Edgar Codd", "CS202", "Database Management Systems", 4, 2, 4, "Ph.D. Relational Systems", "Associate Professor", "+1 (555) 011-2304", "teacher4cse@meridian.edu"),
                new TeacherSpec("teacher5cse", "Dr. Linus Torvalds", "CS301", "Operating Systems & Kernels", 5, 3, 4, "M.S. Systems Architecture", "Associate Professor", "+1 (555) 011-2305", "teacher5cse@meridian.edu"),
                new TeacherSpec("teacher6cse", "Dr. Tim Berners-Lee", "CS302", "Computer Networks & Protocols", 6, 3, 3, "Ph.D. Network Communications", "Associate Professor", "+1 (555) 011-2306", "teacher6cse@meridian.edu"),
                new TeacherSpec("teacher7cse", "Dr. Geoffrey Hinton", "CS401", "Artificial Intelligence & Deep Learning", 7, 4, 4, "Ph.D. Cognitive Systems", "Professor", "+1 (555) 011-2307", "teacher7cse@meridian.edu"),
                new TeacherSpec("teacher8cse", "Dr. Werner Vogels", "CS402", "Cloud Computing & Distributed Systems", 8, 4, 3, "Ph.D. Distributed Systems", "Assistant Professor", "+1 (555) 011-2308", "teacher8cse@meridian.edu")
        ));

        deptMap.put("ECE", List.of(
                new TeacherSpec("teacher1ece", "Dr. Claude Shannon", "EC101", "Circuit Theory & Analysis", 1, 1, 4, "Ph.D. Information Theory", "Professor & HOD", "+1 (555) 012-2301", "teacher1ece@meridian.edu"),
                new TeacherSpec("teacher2ece", "Dr. William Shockley", "EC102", "Electronic Devices & Amplifiers", 2, 1, 4, "Ph.D. Solid State Physics", "Professor", "+1 (555) 012-2302", "teacher2ece@meridian.edu"),
                new TeacherSpec("teacher3ece", "Dr. Jack Kilby", "EC201", "Digital Logic & Microprocessors", 3, 2, 4, "M.Tech Integrated Circuits", "Professor", "+1 (555) 012-2303", "teacher3ece@meridian.edu"),
                new TeacherSpec("teacher4ece", "Dr. Harry Nyquist", "EC202", "Signals & Linear Systems", 4, 2, 4, "Ph.D. Communications", "Associate Professor", "+1 (555) 012-2304", "teacher4ece@meridian.edu"),
                new TeacherSpec("teacher5ece", "Dr. Carver Mead", "EC301", "VLSI System Design & Verilog", 5, 3, 4, "Ph.D. Microelectronics", "Associate Professor", "+1 (555) 012-2305", "teacher5ece@meridian.edu"),
                new TeacherSpec("teacher6ece", "Dr. Alan Oppenheim", "EC302", "Digital Signal Processing", 6, 3, 3, "Ph.D. Digital Signal Processing", "Associate Professor", "+1 (555) 012-2306", "teacher6ece@meridian.edu"),
                new TeacherSpec("teacher7ece", "Dr. Heinrich Hertz", "EC401", "Wireless & 5G Communications", 7, 4, 4, "Ph.D. Electromagnetics", "Professor", "+1 (555) 012-2307", "teacher7ece@meridian.edu"),
                new TeacherSpec("teacher8ece", "Dr. Lynn Conway", "EC402", "Embedded IoT Systems Architecture", 8, 4, 3, "M.Tech Embedded Architecture", "Assistant Professor", "+1 (555) 012-2308", "teacher8ece@meridian.edu")
        ));

        deptMap.put("MECH", List.of(
                new TeacherSpec("teacher1mech", "Dr. Rudolf Diesel", "ME101", "Engineering Mechanics & Statics", 1, 1, 4, "Ph.D. Thermal Power", "Professor & HOD", "+1 (555) 013-2301", "teacher1mech@meridian.edu"),
                new TeacherSpec("teacher2mech", "Dr. Nicolas Carnot", "ME102", "Engineering Thermodynamics", 2, 1, 4, "Ph.D. Thermodynamics", "Professor", "+1 (555) 013-2302", "teacher2mech@meridian.edu"),
                new TeacherSpec("teacher3mech", "Dr. Daniel Bernoulli", "ME201", "Fluid Mechanics & Turbo Machinery", 3, 2, 4, "Ph.D. Hydrodynamics", "Professor", "+1 (555) 013-2303", "teacher3mech@meridian.edu"),
                new TeacherSpec("teacher4mech", "Dr. Henry Ford", "ME202", "Manufacturing Technology & Casting", 4, 2, 3, "M.Tech Industrial Engineering", "Associate Professor", "+1 (555) 013-2304", "teacher4mech@meridian.edu"),
                new TeacherSpec("teacher5mech", "Dr. James Watt", "ME301", "Kinematics & Dynamics of Machines", 5, 3, 4, "Ph.D. Mechanical Dynamics", "Associate Professor", "+1 (555) 013-2305", "teacher5mech@meridian.edu"),
                new TeacherSpec("teacher6mech", "Dr. Jean Fourier", "ME302", "Heat & Mass Transfer Systems", 6, 3, 4, "Ph.D. Thermal Conduction", "Associate Professor", "+1 (555) 013-2306", "teacher6mech@meridian.edu"),
                new TeacherSpec("teacher7mech", "Dr. Nikola Tesla", "ME401", "CAD/CAM & Industrial Robotics", 7, 4, 4, "Ph.D. Mechatronics", "Professor", "+1 (555) 013-2307", "teacher7mech@meridian.edu"),
                new TeacherSpec("teacher8mech", "Dr. Robert Stirling", "ME402", "Automobile Engineering & EV Tech", 8, 4, 3, "M.Tech Automotive Propulsion", "Assistant Professor", "+1 (555) 013-2308", "teacher8mech@meridian.edu")
        ));

        deptMap.put("CIVIL", List.of(
                new TeacherSpec("teacher1civil", "Dr. Arthur Casagrande", "CE101", "Engineering Geology & Surveying I", 1, 1, 3, "Ph.D. Structures", "Professor & HOD", "+1 (555) 014-2301", "teacher1civil@meridian.edu"),
                new TeacherSpec("teacher2civil", "Dr. Thomas Telford", "CE102", "Mechanics of Solids & Materials", 2, 1, 4, "M.Tech Geoinformatics", "Associate Professor", "+1 (555) 014-2302", "teacher2civil@meridian.edu"),
                new TeacherSpec("teacher3civil", "Dr. Henry Darcy", "CE201", "Fluid Mechanics & Open Channel Flow", 3, 2, 4, "Ph.D. Hydrology", "Professor", "+1 (555) 014-2303", "teacher3civil@meridian.edu"),
                new TeacherSpec("teacher4civil", "Dr. William Rankine", "CE202", "Advanced Geomatics & Remote Sensing", 4, 2, 3, "M.Tech Geomatics", "Associate Professor", "+1 (555) 014-2304", "teacher4civil@meridian.edu"),
                new TeacherSpec("teacher5civil", "Dr. Karl Terzaghi", "CE301", "Structural Analysis & RCC Design", 5, 3, 4, "Ph.D. Foundation Engineering", "Professor", "+1 (555) 014-2305", "teacher5civil@meridian.edu"),
                new TeacherSpec("teacher6civil", "Dr. John McAdam", "CE302", "Geotechnical & Highway Engineering", 6, 3, 4, "M.Tech Highway Engineering", "Associate Professor", "+1 (555) 014-2306", "teacher6civil@meridian.edu"),
                new TeacherSpec("teacher7civil", "Dr. Robert Hazen", "CE401", "Environmental & Water Resources Engg", 7, 4, 4, "Ph.D. Hydrology", "Assistant Professor", "+1 (555) 014-2307", "teacher7civil@meridian.edu"),
                new TeacherSpec("teacher8civil", "Dr. Gustave Eiffel", "CE402", "Construction Project Management & BIM", 8, 4, 3, "M.Tech Structural Architecture", "Assistant Professor", "+1 (555) 014-2308", "teacher8civil@meridian.edu")
        ));

        deptMap.put("EEE", List.of(
                new TeacherSpec("teacher1eee", "Dr. Michael Faraday", "EE101", "Electric Circuit Theory & Networks", 1, 1, 4, "Ph.D. Electrodynamics", "Professor & HOD", "+1 (555) 015-2301", "teacher1eee@meridian.edu"),
                new TeacherSpec("teacher2eee", "Dr. Charles Steinmetz", "EE102", "Electromagnetic Fields & Waves", 2, 1, 4, "Ph.D. Alternating Currents", "Professor", "+1 (555) 015-2302", "teacher2eee@meridian.edu"),
                new TeacherSpec("teacher3eee", "Dr. Thomas Edison", "EE201", "DC Machines & Transformers", 3, 2, 4, "M.Tech Power Systems", "Associate Professor", "+1 (555) 015-2303", "teacher3eee@meridian.edu"),
                new TeacherSpec("teacher4eee", "Dr. Joseph Henry", "EE202", "AC Rotating Machines & Alternators", 4, 2, 4, "Ph.D. Electrical Machinery", "Associate Professor", "+1 (555) 015-2304", "teacher4eee@meridian.edu"),
                new TeacherSpec("teacher5eee", "Dr. Oliver Heaviside", "EE301", "Power Transmission & Grid Distribution", 5, 3, 4, "Ph.D. Power Grids", "Associate Professor", "+1 (555) 015-2305", "teacher5eee@meridian.edu"),
                new TeacherSpec("teacher6eee", "Dr. Norbert Wiener", "EE302", "Modern Control Systems & Stability", 6, 3, 3, "Ph.D. Cybernetics & Control", "Associate Professor", "+1 (555) 015-2306", "teacher6eee@meridian.edu"),
                new TeacherSpec("teacher7eee", "Dr. John Bardeen", "EE401", "Power Electronics & Motor Drives", 7, 4, 4, "Ph.D. Semiconductor Drives", "Assistant Professor", "+1 (555) 015-2307", "teacher7eee@meridian.edu"),
                new TeacherSpec("teacher8eee", "Dr. George Westinghouse", "EE402", "Renewable Energy & Smart Grids", 8, 4, 3, "M.Tech Energy Engineering", "Assistant Professor", "+1 (555) 015-2308", "teacher8eee@meridian.edu")
        ));

        long teacherUserNumericId = 10L;
        long facultyIdCounter = 1L;
        long subjectIdCounter = 1L;

        for (Map.Entry<String, List<TeacherSpec>> entry : deptMap.entrySet()) {
            String deptCode = entry.getKey();
            List<TeacherSpec> teachers = entry.getValue();

            for (TeacherSpec ts : teachers) {
                // Save Teacher login credentials (password 123)
                userRepository.save(new User(teacherUserNumericId, ts.username(), "123", ts.name(), ts.email(), "faculty", deptCode));

                // Save Faculty entity with phone number
                Faculty facultyEntity = new Faculty(facultyIdCounter, "FAC" + (1000 + facultyIdCounter), ts.name(), ts.email(), deptCode, ts.designation(), ts.qualification(), "active", List.of(ts.code()));
                facultyEntity.setPhone(ts.phone());
                facultyRepository.save(facultyEntity);

                // Save Subject entity with semester (1-8), year (1-4), and faculty phone/email
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

        // 5. Students across all 4 years: 5 students per department (25 students total)
        // Engineering 4 years = Year 1 (Sem 1 & 2), Year 2 (Sem 3 & 4), Year 3 (Sem 5 & 6), Year 4 (Sem 7 & 8)
        record StudentSpec(
                String studentId, String username, String alias, String name,
                String dept, int year, int semester, String section, int attendance) {}

        List<StudentSpec> studentList = List.of(
                // CSE Students (Distributed across all 4 years)
                new StudentSpec("S1001", "student1cse", "user1cse", "John Doe", "CSE", 1, 1, "A", 95),
                new StudentSpec("S1002", "student2cse", "user2cse", "Emma Wilson", "CSE", 1, 2, "A", 82),
                new StudentSpec("S1003", "student3cse", "user3cse", "Lucas Brown", "CSE", 2, 3, "B", 98),
                new StudentSpec("S1004", "student4cse", "user4cse", "Mia Davis", "CSE", 3, 5, "B", 89),
                new StudentSpec("S1005", "student5cse", "user5cse", "Ethan Miller", "CSE", 4, 7, "C", 91),

                // ECE Students (Distributed across all 4 years)
                new StudentSpec("S2001", "student1ece", "user1ece", "Alex Rivera", "ECE", 1, 1, "A", 94),
                new StudentSpec("S2002", "student2ece", "user2ece", "Chloe Bennett", "ECE", 1, 2, "A", 88),
                new StudentSpec("S2003", "student3ece", "user3ece", "Daniel Kim", "ECE", 2, 3, "B", 92),
                new StudentSpec("S2004", "student4ece", "user4ece", "Grace Hopper", "ECE", 3, 5, "B", 96),
                new StudentSpec("S2005", "student5ece", "user5ece", "Liam Johnson", "ECE", 4, 7, "C", 78),

                // MECH Students (Distributed across all 4 years)
                new StudentSpec("S3001", "student1mech", "user1mech", "Marcus Vance", "MECH", 1, 1, "A", 90),
                new StudentSpec("S3002", "student2mech", "user2mech", "Olivia Taylor", "MECH", 1, 2, "A", 85),
                new StudentSpec("S3003", "student3mech", "user3mech", "Ryan Patel", "MECH", 2, 3, "B", 93),
                new StudentSpec("S3004", "student4mech", "user4mech", "Sarah Connor", "MECH", 3, 5, "B", 87),
                new StudentSpec("S3005", "student5mech", "user5mech", "Tyler Durden", "MECH", 4, 7, "C", 76),

                // CIVIL Students (Distributed across all 4 years)
                new StudentSpec("S4001", "student1civil", "user1civil", "Nathan Drake", "CIVIL", 1, 1, "A", 92),
                new StudentSpec("S4002", "student2civil", "user2civil", "Sophia Martinez", "CIVIL", 1, 2, "A", 89),
                new StudentSpec("S4003", "student3civil", "user3civil", "Victor Vance", "CIVIL", 2, 3, "B", 95),
                new StudentSpec("S4004", "student4civil", "user4civil", "Wendy Wu", "CIVIL", 3, 5, "B", 91),
                new StudentSpec("S4005", "student5civil", "user5civil", "Zachary Bell", "CIVIL", 4, 7, "C", 84),

                // EEE Students (Distributed across all 4 years)
                new StudentSpec("S5001", "student1eee", "user1eee", "Benjamin Franklin", "EEE", 1, 1, "A", 96),
                new StudentSpec("S5002", "student2eee", "user2eee", "Diana Prince", "EEE", 1, 2, "A", 92),
                new StudentSpec("S5003", "student3eee", "user3eee", "Edward Norton", "EEE", 2, 3, "B", 85),
                new StudentSpec("S5004", "student4eee", "user4eee", "Fiona Gallagher", "EEE", 3, 5, "B", 89),
                new StudentSpec("S5005", "student5eee", "user5eee", "George Clark", "EEE", 4, 7, "C", 79)
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
            for (int s = 1; s <= st.semester(); s++) {
                int yr = (s + 1) / 2;
                double pct = 82.0 + ((s * 3.7 + studentEntityIdCounter * 2.1) % 11.5);
                double sgpa = Math.round((pct / 10.0) * 10.0) / 10.0;
                String status = (s == st.semester()) ? "Ongoing" : "Passed";
                semesterPercentages.add(Map.of(
                        "semester", s,
                        "year", yr,
                        "percentage", Math.round(pct * 10.0) / 10.0,
                        "sgpa", sgpa,
                        "creditsEarned", 22,
                        "status", status
                ));
                sgpaList.add(Map.of("sem", s, "gpa", sgpa));
            }

            // Arrears history (sample cleared arrears)
            List<Map<String, Object>> arrearsHistory = new ArrayList<>();
            if (st.year() >= 2) {
                arrearsHistory.add(Map.of(
                        "subjectCode", st.dept() + "101",
                        "subjectName", "Introductory Engineering Sciences",
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
                // Student 3 in CSE has 1 active backlog for testing
                currentArrears.add(Map.of(
                        "subjectCode", "CS201",
                        "subjectName", "Object Oriented Programming & Java",
                        "semester", 3,
                        "credits", 4,
                        "status", "Active Backlog",
                        "internalMarks", 18,
                        "examDate", "Nov 2026"
                ));
            }

            double cgpa = Math.round((8.5 + (studentEntityIdCounter % 8) * 0.1) * 100.0) / 100.0;

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

        // 6. Seed Marks for student 1 (John Doe - student1cse, Year 1, Semester 1)
        // Strictly Semester 1 courses only - NO premature marks for subsequent semesters
        markRepository.save(new Mark(1L, 1L, 1L, "CS101", "Problem Solving & Python Programming (CS101)", 1, 1, 4, "Midterm Exam", 92.0, 100.0, "A+", true));
        markRepository.save(new Mark(2L, 1L, 1L, "CS101", "Problem Solving & Python Programming (CS101)", 1, 1, 4, "Continuous Assessment 1", 48.0, 50.0, "A+", true));
        markRepository.save(new Mark(3L, 1L, 1L, "CS101", "Problem Solving & Python Programming (CS101)", 1, 1, 4, "Final Practical Exam", 96.0, 100.0, "O", true));
        markRepository.save(new Mark(4L, 1L, 3L, "EE101", "Basic Electrical Engineering (EE101)", 1, 1, 4, "Midterm Exam", 88.0, 100.0, "A", true));
        markRepository.save(new Mark(5L, 1L, 3L, "EE101", "Basic Electrical Engineering (EE101)", 1, 1, 4, "Continuous Assessment 1", 45.0, 50.0, "A", true));
        markRepository.save(new Mark(6L, 1L, 4L, "MA101", "Engineering Mathematics I (MA101)", 1, 1, 4, "Midterm Exam", 90.0, 100.0, "A+", true));
        markRepository.save(new Mark(7L, 1L, 4L, "MA101", "Engineering Mathematics I (MA101)", 1, 1, 4, "Continuous Assessment 1", 47.0, 50.0, "A+", true));
        markRepository.save(new Mark(8L, 1L, 5L, "CS101-L", "Computer Practice Laboratory (CS101-LAB)", 1, 1, 2, "Practical Viva & Execution", 98.0, 100.0, "O", true));

        // 7. Seed Attendance for student 1 (John Doe - student1cse) in current semester (Semester 1)
        attendanceRepository.save(new AttendanceItem(1L, 1L, 1L, "CS101", "Problem Solving & Python Programming (CS101)", 1, 38, 40, "good", 95, "Today"));
        attendanceRepository.save(new AttendanceItem(2L, 1L, 2L, "CS102", "Data Structures & Algorithms (CS102)", 1, 36, 40, "good", 90, "Yesterday"));
        attendanceRepository.save(new AttendanceItem(3L, 1L, 3L, "EE101", "Basic Electrical Engineering (EE101)", 1, 37, 40, "good", 92, "Today"));
        attendanceRepository.save(new AttendanceItem(4L, 1L, 4L, "MA101", "Engineering Mathematics I (MA101)", 1, 35, 40, "good", 88, "2 days ago"));
        attendanceRepository.save(new AttendanceItem(5L, 1L, 5L, "CS101-L", "Computer Practice Laboratory (CS101-LAB)", 1, 20, 20, "good", 100, "Today"));

        // 8. Assignments
        // Assignment 1 in CS101 (Taught by teacher1cse - Dr. Alan Turing)
        assignmentRepository.save(new Assignment(
                1L, 1L, "Problem Solving & Python Programming (CS101)",
                "Binary Tree Traversal & Balancing Algorithms",
                "Implement standard preorder, inorder, and postorder traversals with AVL auto-balancing benchmarks in Python or C++. Attach solution document (PDF, Word, or Image file).",
                "2026-10-15", 100, "due", 2, 5, false
        ));

        // Assignment 2 in CS102 (Taught by teacher2cse - Dr. Barbara Liskov)
        assignmentRepository.save(new Assignment(
                2L, 2L, "Data Structures & Algorithms (CS102)",
                "E-R Diagram & Normalization Case Study",
                "Construct complete 3NF entity-relationship schema for a distributed hospital database with indexing strategies. Upload PDF or Word document.",
                "2026-10-20", 100, "due", 1, 5, false
        ));

        // Assignment 3 in EC101 (Taught by teacher1ece - Dr. Claude Shannon)
        assignmentRepository.save(new Assignment(
                3L, 9L, "Circuit Theory & Analysis (EC101)",
                "AC Phasor & Transient Response Analysis",
                "Simulate RLC resonant frequency and filter characteristics using SPICE. Upload simulation report.",
                "2026-10-22", 100, "due", 0, 5, false
        ));

        // Assignment 4 in ME101 (Taught by teacher1mech - Dr. Rudolf Diesel)
        assignmentRepository.save(new Assignment(
                4L, 17L, "Engineering Mechanics & Statics (ME101)",
                "3D Truss Equilibrium & Force Polygon",
                "Calculate reaction forces and pin joint tensions for space truss structure.",
                "2026-10-25", 100, "due", 0, 5, false
        ));

        // Assignment 5 in CE101 (Taught by teacher1civil - Dr. Arthur Casagrande)
        assignmentRepository.save(new Assignment(
                5L, 25L, "Engineering Geology & Surveying I (CE101)",
                "Contour Map Surveying & Theodolite Traverse",
                "Prepare closed traverse adjustments and compute area using Simpson's Rule.",
                "2026-10-28", 100, "due", 0, 5, false
        ));

        // Assignment 6 in EE101 (Taught by teacher1eee - Dr. Michael Faraday)
        assignmentRepository.save(new Assignment(
                6L, 33L, "Electric Circuit Theory & Networks (EE101)",
                "Three-Phase AC Balanced Load Analysis",
                "Determine active, reactive, and apparent power in delta-wye configurations with vector phasor diagrams.",
                "2026-10-30", 100, "due", 0, 5, false
        ));

        // Sample PDF data URL for initial testing
        String samplePdfData = "data:application/pdf;base64,JVBERi0xLjQKJeLjz9MKMSAwIG9iago8PAovVHlwZSAvQ2F0YWxvZwovUGFnZXMgMiAwIFIKPj4KZW5kb2JqCjIgMCBvYmoKPDwKL1R5cGUgL1BhZ2VzCi9LaWRzIFszIDAgUl0KL0NvdW50IDEKPj4KZW5kb2JqCjMgMCBvYmoKPDwKL1R5cGUgL1BhZ2UKL1BhcmVudCAyIDAgUgovTWVkaWFCb3ggWzAgMCA2MTIgNzkyXQovQ29udGVudHMgNCAwIFIKPj4KZW5kb2JqCjQgMCBvYmoKPDwKL0xlbmd0aCA0Nwo+PgpzdHJlYW0KQVQgL0YxIDEyIFRmIDUwIDcwMCBUZCAoU21hcnQgQ2FtcHVzIEFzc2lnbm1lbnQgU3VibWlzc2lvbikgVGoKRVRKCmVuZHN0cmVhbQplbmRvYmoKeHJlZgowIDUKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwMDE1IDAwMDAwIG4gCjAwMDAwMDAwNjggMDAwMDAgbiAKMDAwMDAwMDEyNSAwMDAwMCBuIAowMDAwMDAwMjE2IDAwMDAwIG4gCnRyYWlsZXIKPDwKL1NpemUgNQovUm9vdCAxIDAgUgo+PgpzdGFydHhyZWYKMzEzCiUlRU9G";

        // Sample PNG image data URL
        String sampleImageData = "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='400' height='300' viewBox='0 0 400 300'><rect width='100%' height='100%' fill='%231e293b'/><circle cx='200' cy='120' r='60' fill='%2338bdf8'/><text x='200' y='220' font-size='18' text-anchor='middle' fill='%23ffffff' font-family='sans-serif'>AVL Tree Balancing Diagram</text><text x='200' y='250' font-size='12' text-anchor='middle' fill='%2394a3b8' font-family='sans-serif'>Submitted by Lucas Brown</text></svg>";

        // Seed submissions for Assignment 1 (CS101)
        submissionRepository.save(new AssignmentSubmission(
                1L, 1L, 1L, "John Doe", "student1cse", "CSE",
                "Implemented preorder, inorder and postorder tree traversals with full test suite and Big-O runtime analysis.",
                "submitted", "2026-09-20 18:30",
                "Binary_Tree_Traversal_Report.pdf", "application/pdf", "340 KB", samplePdfData
        ));

        submissionRepository.save(new AssignmentSubmission(
                2L, 1L, 3L, "Lucas Brown", "student3cse", "CSE",
                "Attached benchmark diagrams and execution profiling across 10,000 random nodes.",
                "submitted", "2026-09-20 20:15",
                "AVL_Balancing_Diagram.png", "image/png", "180 KB", sampleImageData
        ));

        // 9. Announcements
        announcementRepository.save(new Announcement(1L, "Welcome to Academic Year 2026-2027", "Orientation for all 5 Engineering departments (CSE, ECE, MECH, CIVIL, EEE) commences next Monday. Lab schedules are published.", "All campus", "important", "Campus Administration", "2 hrs ago"));
        announcementRepository.save(new Announcement(2L, "Inter-Department Technical Symposium Registration Open", "Submit project proposals and hackathon registrations across all 4 years in CSE, ECE, MECH, CIVIL, and EEE.", "Students", "normal", "Academic Dean", "1 day ago"));

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
                2L, "Projector Lamp Failure in Mechanical CAD Lab 102", "Facilities",
                "Dr. Rudolf Diesel", "teacher1mech", "faculty", "MECH",
                "The high-definition projector in Lab 102 flickers and shuts down after 15 minutes of lecture.",
                "medium", "Yesterday", "progress"
        ));

        complaintRepository.save(new Complaint(
                3L, "Air Conditioning Maintenance in Electrical Machines Lab", "Facilities",
                "Dr. Michael Faraday", "teacher1eee", "faculty", "EEE",
                "Ambient temperature during machine load testing exceeds safety recommendations.",
                "medium", "2 days ago", "resolved"
        ));

        // 12. Notifications
        notificationRepository.save(new Notification(1L, "System Upgrade Completed", "Smart campus portal upgraded with 4-year engineering curriculum, semester marks, and teacher contact cards.", false, "10 mins ago", null, null, null));
        notificationRepository.save(new Notification(2L, "Assignment Deadline Approaching", "Binary Tree Traversal & Balancing assignment for CS101 is due soon.", false, "1 hr ago", "student", "student2cse", 2L));

        // 13. Fee records: Year 1 Engineering Program records
        FeeInfo feeInfo1 = new FeeInfo();
        feeInfo1.setId("fee_1");
        feeInfo1.setStudentId(1L);
        feeInfo1.setTotal(15000.0);
        feeInfo1.setPaid(10000.0);
        feeInfo1.setPending(5000.0);
        feeInfo1.setHistory(List.of(
                Map.of("id", 1, "date", "2026-08-15", "amount", 10000, "method", "Online Net Banking", "status", "Success", "academicYear", "Year 1 / Semester 1", "receiptNo", "REC-2026-S1")
        ));
        List<Map<String, Object>> breakdown = new ArrayList<>();

        Map<String, Object> y1 = new LinkedHashMap<>();
        y1.put("year", 1);
        y1.put("label", "Year 1 (Semesters 1 & 2)");
        y1.put("tuition", 12000);
        y1.put("lab", 1500);
        y1.put("exam", 1000);
        y1.put("library", 500);
        y1.put("total", 15000);
        y1.put("paid", 10000);
        y1.put("pending", 5000);
        y1.put("status", "Partial Paid");
        y1.put("receiptId", "REC-2026-S1");
        y1.put("paidDate", "15 Aug 2026");
        y1.put("dueDate", "30 Nov 2026");
        breakdown.add(y1);

        Map<String, Object> y2 = new LinkedHashMap<>();
        y2.put("year", 2);
        y2.put("label", "Year 2 (Semesters 3 & 4)");
        y2.put("tuition", 12000);
        y2.put("lab", 1500);
        y2.put("exam", 1000);
        y2.put("library", 500);
        y2.put("total", 15000);
        y2.put("paid", 0);
        y2.put("pending", 15000);
        y2.put("status", "Upcoming Academic Year");
        y2.put("receiptId", "-");
        y2.put("dueDate", "15 Aug 2027");
        breakdown.add(y2);

        Map<String, Object> y3 = new LinkedHashMap<>();
        y3.put("year", 3);
        y3.put("label", "Year 3 (Semesters 5 & 6)");
        y3.put("tuition", 12000);
        y3.put("lab", 1500);
        y3.put("exam", 1000);
        y3.put("library", 500);
        y3.put("total", 15000);
        y3.put("paid", 0);
        y3.put("pending", 15000);
        y3.put("status", "Upcoming Academic Year");
        y3.put("receiptId", "-");
        y3.put("dueDate", "15 Aug 2028");
        breakdown.add(y3);

        Map<String, Object> y4 = new LinkedHashMap<>();
        y4.put("year", 4);
        y4.put("label", "Year 4 (Semesters 7 & 8)");
        y4.put("tuition", 12000);
        y4.put("lab", 1500);
        y4.put("exam", 1000);
        y4.put("library", 500);
        y4.put("total", 15000);
        y4.put("paid", 0);
        y4.put("pending", 15000);
        y4.put("status", "Upcoming Academic Year");
        y4.put("receiptId", "-");
        y4.put("dueDate", "15 Aug 2029");
        breakdown.add(y4);

        feeInfo1.setYearlyBreakdown(breakdown);
        feeRepository.save(feeInfo1);

        // 14. Seed Monday to Saturday 7-period Timetables (45 min each)
        seedTimetables();

        log.info("Database reset & seeding completed successfully with 5 departments, 40 subjects across 8 semesters, 40 teachers, 25 students, and Monday-Saturday 7-period timetables.");
    }

    private void seedTimetables() {
        log.info("Seeding Monday-to-Saturday 7-period engineering timetables (45 min per period)...");
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

        record SlotSpec(String code, String name, String teacherName, String teacherUser, String room, String type) {}

        Map<String, List<SlotSpec>> cseSem1Schedule = Map.of(
                "Monday", List.of(
                        new SlotSpec("CS101", "Problem Solving & Python Programming", "Dr. Alan Turing", "teacher1cse", "LH-101", "lecture"),
                        new SlotSpec("MA101", "Engineering Mathematics I", "Prof. David Hilbert", "teacher_math", "LH-101", "lecture"),
                        new SlotSpec("EE101", "Basic Electrical Engineering", "Dr. Michael Faraday", "teacher1eee", "LH-101", "lecture"),
                        new SlotSpec("CS101", "Python Problem Solving Tutorial", "Dr. Alan Turing", "teacher1cse", "LH-101", "tutorial"),
                        new SlotSpec("CS101-L", "Computer Practice Laboratory", "Dr. Alan Turing", "teacher1cse", "CS-Lab 1", "lab"),
                        new SlotSpec("CS101-L", "Computer Practice Laboratory", "Dr. Alan Turing", "teacher1cse", "CS-Lab 1", "lab"),
                        new SlotSpec("LIB", "Digital Library & Research", "Librarian In-Charge", "staff_lib", "Central Library", "activity")
                ),
                "Tuesday", List.of(
                        new SlotSpec("MA101", "Engineering Mathematics I", "Prof. David Hilbert", "teacher_math", "LH-101", "lecture"),
                        new SlotSpec("EE101", "Basic Electrical Engineering", "Dr. Michael Faraday", "teacher1eee", "LH-101", "lecture"),
                        new SlotSpec("CS101", "Problem Solving & Python Programming", "Dr. Alan Turing", "teacher1cse", "LH-101", "lecture"),
                        new SlotSpec("EE101-L", "Electrical Circuits Lab", "Dr. Michael Faraday", "teacher1eee", "EE-Lab 1", "lab"),
                        new SlotSpec("EE101-L", "Electrical Circuits Lab", "Dr. Michael Faraday", "teacher1eee", "EE-Lab 1", "lab"),
                        new SlotSpec("CS101", "Python Coding Workshop", "Dr. Alan Turing", "teacher1cse", "CS-Lab 2", "tutorial"),
                        new SlotSpec("MENTOR", "Faculty Mentorship & Guidance", "Dr. Alan Turing", "teacher1cse", "LH-101", "activity")
                ),
                "Wednesday", List.of(
                        new SlotSpec("EE101", "Basic Electrical Engineering", "Dr. Michael Faraday", "teacher1eee", "LH-101", "lecture"),
                        new SlotSpec("CS101", "Problem Solving & Python Programming", "Dr. Alan Turing", "teacher1cse", "LH-101", "lecture"),
                        new SlotSpec("MA101", "Engineering Mathematics I", "Prof. David Hilbert", "teacher_math", "LH-101", "lecture"),
                        new SlotSpec("HUM101", "Engineering Ethics & Professional Skills", "Dr. Ada Lovelace", "teacher_hum", "LH-101", "lecture"),
                        new SlotSpec("CS101-L", "Python Algorithms Lab", "Dr. Alan Turing", "teacher1cse", "CS-Lab 1", "lab"),
                        new SlotSpec("CS101-L", "Python Algorithms Lab", "Dr. Alan Turing", "teacher1cse", "CS-Lab 1", "lab"),
                        new SlotSpec("SPORTS", "Campus Sports & Fitness Activity", "Sports Director", "staff_sports", "Campus Sports Arena", "activity")
                ),
                "Thursday", List.of(
                        new SlotSpec("CS101", "Problem Solving & Python Programming", "Dr. Alan Turing", "teacher1cse", "LH-101", "lecture"),
                        new SlotSpec("EE101", "Basic Electrical Engineering", "Dr. Michael Faraday", "teacher1eee", "LH-101", "lecture"),
                        new SlotSpec("MA101", "Engineering Mathematics I", "Prof. David Hilbert", "teacher_math", "LH-101", "lecture"),
                        new SlotSpec("CS101", "Algorithm Design & Flowcharts", "Dr. Alan Turing", "teacher1cse", "LH-101", "tutorial"),
                        new SlotSpec("EE101", "Circuit Analysis Practice", "Dr. Michael Faraday", "teacher1eee", "LH-101", "tutorial"),
                        new SlotSpec("MA101", "Calculus Problem Solving Session", "Prof. David Hilbert", "teacher_math", "LH-101", "tutorial"),
                        new SlotSpec("STUDY", "Peer Study Group & Doubts", "Academic Tutors", "staff_tutors", "LH-101", "activity")
                ),
                "Friday", List.of(
                        new SlotSpec("MA101", "Engineering Mathematics I", "Prof. David Hilbert", "teacher_math", "LH-101", "lecture"),
                        new SlotSpec("CS101", "Problem Solving & Python Programming", "Dr. Alan Turing", "teacher1cse", "LH-101", "lecture"),
                        new SlotSpec("EE101", "Basic Electrical Engineering", "Dr. Michael Faraday", "teacher1eee", "LH-101", "lecture"),
                        new SlotSpec("CS101-L", "Open Source & Git Lab", "Dr. Alan Turing", "teacher1cse", "CS-Lab 1", "lab"),
                        new SlotSpec("CS101-L", "Open Source & Git Lab", "Dr. Alan Turing", "teacher1cse", "CS-Lab 1", "lab"),
                        new SlotSpec("TEST", "Continuous Assessment Weekly Test", "Exam Cell", "staff_exam", "LH-101", "assessment"),
                        new SlotSpec("CLUB", "Technical Club & Hackathon Prep", "Club Coordinator", "staff_club", "Auditorium 2", "activity")
                ),
                "Saturday", List.of(
                        new SlotSpec("CS101", "Advanced Python Topics & Libraries", "Dr. Alan Turing", "teacher1cse", "LH-101", "lecture"),
                        new SlotSpec("EE101", "Power Grid Overview & Safety", "Dr. Michael Faraday", "teacher1eee", "LH-101", "lecture"),
                        new SlotSpec("MA101", "Linear Algebra Applications", "Prof. David Hilbert", "teacher_math", "LH-101", "lecture"),
                        new SlotSpec("CS101", "Project Consultation & Doubts", "Dr. Alan Turing", "teacher1cse", "LH-101", "tutorial"),
                        new SlotSpec("PROJ", "Mini-Project Work Session", "Project Mentors", "teacher1cse", "CS-Lab 2", "lab"),
                        new SlotSpec("PROJ", "Mini-Project Work Session", "Project Mentors", "teacher1cse", "CS-Lab 2", "lab"),
                        new SlotSpec("WRAP", "Weekly Academic Review & Wrap-up", "Dr. Alan Turing", "teacher1cse", "LH-101", "activity")
                )
        );

        for (Map.Entry<String, List<SlotSpec>> entry : cseSem1Schedule.entrySet()) {
            String day = entry.getKey();
            List<SlotSpec> slots = entry.getValue();
            for (int p = 1; p <= 7; p++) {
                SlotSpec spec = slots.get(p - 1);
                timetableRepository.save(new TimetableEntry(
                        idCounter++, "CSE", 1, "A",
                        day, p, timings[p - 1][0], timings[p - 1][1],
                        spec.code(), spec.name(), spec.teacherName(), spec.teacherUser(),
                        spec.room(), spec.type()
                ));
            }
        }

        // Other engineering departments Semester 1
        String[] otherDepts = {"ECE", "MECH", "CIVIL", "EEE"};
        String[] leadTeachers = {"teacher1ece", "teacher1mech", "teacher1civil", "teacher1eee"};
        String[] leadNames = {"Dr. Claude Shannon", "Dr. Rudolf Diesel", "Dr. Arthur Casagrande", "Dr. Michael Faraday"};
        String[] deptCodes = {"EC101", "ME101", "CE101", "EE101"};
        String[] deptNames = {
                "Semiconductor Physics & Devices",
                "Engineering Mechanics & Statics",
                "Geodetic Surveying & Field Work",
                "Electric Circuit Theory & Networks"
        };

        for (int d = 0; d < otherDepts.length; d++) {
            String dCode = otherDepts[d];
            String tUser = leadTeachers[d];
            String tName = leadNames[d];
            String subCode = deptCodes[d];
            String subName = deptNames[d];
            String room = dCode + "-LH1";

            for (String day : days) {
                for (int p = 1; p <= 7; p++) {
                    String sCode = (p % 2 == 1) ? subCode : "MA101";
                    String sName = (p % 2 == 1) ? subName : "Engineering Mathematics I";
                    String prof = (p % 2 == 1) ? tName : "Prof. David Hilbert";
                    String pUser = (p % 2 == 1) ? tUser : "teacher_math";
                    String sType = (p == 5 || p == 6) ? "lab" : "lecture";

                    timetableRepository.save(new TimetableEntry(
                            idCounter++, dCode, 1, "A",
                            day, p, timings[p - 1][0], timings[p - 1][1],
                            sCode, sName, prof, pUser,
                            room, sType
                    ));
                }
            }
        }
    }
}
