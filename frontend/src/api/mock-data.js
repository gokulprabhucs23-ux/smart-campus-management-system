export function getMockRole() {
  if (typeof window !== "undefined") {
    return localStorage.getItem("mockRole") || "student";
  }
  return "student";
}

export function handleMockRequest(method, url) {
  const role = getMockRole();
  const path = url.split("?")[0];

  if (!path.startsWith("/api")) {
    return { handled: false };
  }

  // Handle specific endpoints
  if (path === "/api/me") {
    return {
      handled: true,
      data: {
        id: 1,
        name: "Mock User",
        email: `mock@${role}.meridian.edu`,
        role: role,
      },
    };
  }

  if (path === "/api/dashboard/analytics") {
    return {
      handled: true,
      data: {
        departmentStudents: [
          { label: "Computer Science", value: 450 },
          { label: "Engineering", value: 320 },
          { label: "Business", value: 200 },
        ],
        complaints: [
          { label: "Resolved", value: 120, color: "#10b981" },
          { label: "Pending", value: 45, color: "#f59e0b" },
        ],
        attendanceTrend: [
          { label: "W1", value: 90 },
          { label: "W2", value: 92 },
          { label: "W3", value: 88 },
          { label: "W4", value: 95 },
        ],
      },
    };
  }

  if (path === "/api/dashboard") {
    if (role === "admin") {
      return {
        handled: true,
        data: {
          role: "admin",
          greeting: "Good morning, Admin.",
          stats: [
            { label: "Total Students", value: 1250 },
            { label: "Active Faculty", value: 84 },
            { label: "Open Complaints", value: 12, tone: "critical" },
            { label: "System Health", value: "99.9%", tone: "positive" },
          ],
          activities: [
            {
              title: "New Faculty Registered",
              detail: "Dr. Alan Turing joined CS Dept",
              time: "1 hr ago",
            },
            {
              title: "System Backup",
              detail: "Automated backup completed successfully",
              time: "3 hrs ago",
            },
          ],
        },
      };
    }
    if (role === "faculty") {
      return {
        handled: true,
        data: {
          role: "faculty",
          greeting: "Good morning, Professor.",
          stats: [
            { label: "My Students", value: 120 },
            { label: "Classes Today", value: 3 },
            { label: "Pending Assignments", value: 45, tone: "warning" },
            { label: "Avg Attendance", value: "92%", tone: "positive" },
          ],
          activities: [
            {
              title: "Assignment Submitted",
              detail: "John Doe submitted Advanced Algorithms",
              time: "10 mins ago",
            },
            {
              title: "Leave Request",
              detail: "Alice Smith requested medical leave",
              time: "1 hr ago",
            },
          ],
        },
      };
    }
    return {
      handled: true,
      data: {
        role: "student",
        greeting: "Good morning.",
        studentName: "John Doe",
        department: "CSE",
        year: 1,
        semester: 1,
        cgpa: 8.75,
        arrearCount: 0,
        currentArrears: [],
        arrearsHistory: [
          {
            subjectCode: "CS101-P",
            subjectName: "Problem Solving Lab",
            semester: 1,
            clearedSemester: 1,
            grade: "B+",
            credits: 2,
            status: "Cleared",
            clearedDate: "May 2026",
          },
        ],
        semesterPercentages: [
          {
            semester: 1,
            year: 1,
            percentage: 88.5,
            sgpa: 8.8,
            creditsEarned: 22,
            status: "Ongoing",
          },
        ],
        stats: [
          { label: "Overall Attendance", value: "95%", tone: "positive" },
          { label: "Cumulative CGPA", value: "8.75" },
          { label: "Current Arrears", value: 0, tone: "positive" },
          { label: "Current Semester", value: "Sem 1 (Yr 1)" },
        ],
        activities: [
          {
            title: "Grade Posted",
            detail: "Dr. Alan Turing posted Midterm marks for CS101",
            time: "1 hr ago",
          },
          {
            title: "Academic Advisory",
            detail: "Semester 1 lab schedules and mentor hours finalized",
            time: "3 hrs ago",
          },
        ],
      },
    };
  }

  if (path === "/api/academic-insights") {
    return {
      handled: true,
      data: {
        score: 85,
        level: "Strong standing",
        recommendations: [
          "Review database normalization for upcoming quiz",
          "Submit CS401 project draft",
        ],
        risks: [],
      },
    };
  }

  if (path === "/api/students") {
    let allStudents = [
      {
        id: 1,
        studentId: "S1001",
        name: "John Doe",
        email: "john@meridian.edu",
        department: "Computer Science",
        year: 2,
        section: "A",
        status: "active",
        attendance: 95,
        mentorId: 1,
      },
      {
        id: 2,
        studentId: "S1002",
        name: "Emma Wilson",
        email: "emma@meridian.edu",
        department: "Computer Science",
        year: 2,
        section: "A",
        status: "active",
        attendance: 82,
        mentorId: 1,
      },
      {
        id: 3,
        studentId: "S1003",
        name: "Lucas Brown",
        email: "lucas@meridian.edu",
        department: "Computer Science",
        year: 2,
        section: "B",
        status: "active",
        attendance: 98,
        mentorId: 1,
      },
      {
        id: 4,
        studentId: "S1004",
        name: "Mia Davis",
        email: "mia@meridian.edu",
        department: "Computer Science",
        year: 2,
        section: "B",
        status: "active",
        attendance: 89,
        mentorId: 1,
      },
      {
        id: 5,
        studentId: "S1005",
        name: "Ethan Miller",
        email: "ethan@meridian.edu",
        department: "Computer Science",
        year: 2,
        section: "C",
        status: "active",
        attendance: 91,
        mentorId: 1,
      },
      {
        id: 6,
        studentId: "S1006",
        name: "Sophia Smith",
        email: "sophia@meridian.edu",
        department: "Engineering",
        year: 3,
        section: "A",
        status: "active",
        attendance: 75,
        mentorId: 2,
      },
    ];
    if (role === "faculty") {
      allStudents = allStudents.filter((s) => s.mentorId === 1);
    }
    return { handled: true, data: allStudents };
  }

  const studentDetailsMatch = path.match(/^\/api\/students\/(\d+)\/details$/);
  if (studentDetailsMatch) {
    const studentId = Number(studentDetailsMatch[1]);
    const mockDetails = {
      1: {
        cgpa: 3.8,
        sgpa: [
          { sem: 1, gpa: 3.7 },
          { sem: 2, gpa: 3.9 },
        ],
        arrearCount: 0,
        arrearsHistory: [],
        marks: [
          { subject: "CS101", grade: "A" },
          { subject: "MTH101", grade: "B+" },
        ],
        risks: [],
        mentor: { name: "Dr. Alan Turing", phone: "+1 555-0100" },
        fees: { pending: 0, paid: 15000, total: 15000 },
      },
      2: {
        cgpa: 3.1,
        sgpa: [
          { sem: 1, gpa: 3.4 },
          { sem: 2, gpa: 2.8 },
        ],
        arrearCount: 1,
        arrearsHistory: [{ subject: "CS101", sem: 2 }],
        marks: [
          { subject: "CS101", grade: "C" },
          { subject: "MTH101", grade: "B" },
        ],
        risks: ["Low attendance in CS101"],
        mentor: { name: "Dr. Alan Turing", phone: "+1 555-0100" },
        fees: { pending: 2000, paid: 13000, total: 15000 },
      },
      3: {
        cgpa: 3.9,
        sgpa: [
          { sem: 1, gpa: 3.8 },
          { sem: 2, gpa: 4.0 },
        ],
        arrearCount: 0,
        arrearsHistory: [],
        marks: [
          { subject: "CS101", grade: "A" },
          { subject: "MTH101", grade: "A" },
        ],
        risks: [],
        mentor: { name: "Dr. Alan Turing", phone: "+1 555-0100" },
        fees: { pending: 0, paid: 15000, total: 15000 },
      },
      4: {
        cgpa: 3.5,
        sgpa: [
          { sem: 1, gpa: 3.5 },
          { sem: 2, gpa: 3.5 },
        ],
        arrearCount: 0,
        arrearsHistory: [],
        marks: [
          { subject: "CS101", grade: "B" },
          { subject: "MTH101", grade: "B+" },
        ],
        risks: [],
        mentor: { name: "Dr. Alan Turing", phone: "+1 555-0100" },
        fees: { pending: 1000, paid: 14000, total: 15000 },
      },
      5: {
        cgpa: 3.4,
        sgpa: [
          { sem: 1, gpa: 3.2 },
          { sem: 2, gpa: 3.6 },
        ],
        arrearCount: 0,
        arrearsHistory: [],
        marks: [
          { subject: "CS101", grade: "B+" },
          { subject: "MTH101", grade: "B" },
        ],
        risks: [],
        mentor: { name: "Dr. Alan Turing", phone: "+1 555-0100" },
        fees: { pending: 5000, paid: 10000, total: 15000 },
      },
      6: {
        cgpa: 2.9,
        sgpa: [
          { sem: 1, gpa: 3.0 },
          { sem: 2, gpa: 2.8 },
        ],
        arrearCount: 2,
        arrearsHistory: [
          { subject: "MTH101", sem: 1 },
          { subject: "PHY101", sem: 2 },
        ],
        marks: [
          { subject: "CS101", grade: "C" },
          { subject: "MTH101", grade: "D" },
        ],
        risks: ["Low attendance", "Multiple arrears"],
        mentor: { name: "Dr. Ada Lovelace", phone: "+1 555-0200" },
        fees: { pending: 0, paid: 15000, total: 15000 },
      },
    };
    return { handled: true, data: mockDetails[studentId] || mockDetails[1] };
  }

  if (path === "/api/fees") {
    return {
      handled: true,
      data: {
        total: 60000,
        paid: 45000,
        pending: 15000,
        history: [
          {
            id: 1,
            date: "2023-08-15",
            amount: 15000,
            method: "Online Net Banking",
            status: "Success",
            academicYear: "Year 1",
          },
          {
            id: 2,
            date: "2024-08-10",
            amount: 15000,
            method: "Debit Card Payment",
            status: "Success",
            academicYear: "Year 2",
          },
          {
            id: 3,
            date: "2025-08-05",
            amount: 15000,
            method: "Wire Transfer",
            status: "Success",
            academicYear: "Year 3",
          },
        ],
        yearlyBreakdown: [
          {
            year: 1,
            label: "Year 1 (Semesters 1 & 2)",
            tuition: 12000,
            lab: 1500,
            exam: 1000,
            library: 500,
            total: 15000,
            paid: 15000,
            pending: 0,
            status: "Paid",
            receiptId: "REC-2023-Y1",
            paidDate: "15 Aug 2023",
          },
          {
            year: 2,
            label: "Year 2 (Semesters 3 & 4)",
            tuition: 12000,
            lab: 1500,
            exam: 1000,
            library: 500,
            total: 15000,
            paid: 15000,
            pending: 0,
            status: "Paid",
            receiptId: "REC-2024-Y2",
            paidDate: "10 Aug 2024",
          },
          {
            year: 3,
            label: "Year 3 (Semesters 5 & 6)",
            tuition: 12000,
            lab: 1500,
            exam: 1000,
            library: 500,
            total: 15000,
            paid: 15000,
            pending: 0,
            status: "Paid",
            receiptId: "REC-2025-Y3",
            paidDate: "05 Aug 2025",
          },
          {
            year: 4,
            label: "Year 4 (Semesters 7 & 8)",
            tuition: 12000,
            lab: 1500,
            exam: 1000,
            library: 500,
            total: 15000,
            paid: 0,
            pending: 15000,
            status: "Due",
            receiptId: "-",
            dueDate: "30 Sep 2026",
          },
        ],
      },
    };
  }

  if (path === "/api/faculty") {
    return {
      handled: true,
      data: [
        {
          id: 1,
          facultyId: "F1001",
          name: "Dr. Alan Turing",
          email: "alan@meridian.edu",
          department: "Computer Science",
          designation: "Professor",
          qualification: "PhD",
          status: "active",
          subjects: ["CS101", "CS401"],
        },
        {
          id: 2,
          facultyId: "F1002",
          name: "Marie Curie",
          email: "marie@meridian.edu",
          department: "Physics",
          designation: "Associate Professor",
          qualification: "PhD",
          status: "active",
          subjects: ["PHY201"],
        },
      ],
    };
  }

  if (path === "/api/departments") {
    return {
      handled: true,
      data: [
        {
          id: 1,
          name: "Computer Science",
          code: "CS",
          hod: "Dr. Alan Turing",
          description: "Computing, software, and systems.",
          students: 450,
          faculty: 24,
        },
        {
          id: 2,
          name: "Physics",
          code: "PHY",
          hod: "Marie Curie",
          description: "Study of matter and energy.",
          students: 320,
          faculty: 18,
        },
      ],
    };
  }

  if (path.startsWith("/api/subjects")) {
    return {
      handled: true,
      data: [
        {
          id: 1,
          code: "CS101",
          name: "Problem Solving & Python Programming",
          department: "CSE",
          semester: 1,
          year: 1,
          credits: 4,
          faculty: "Dr. Alan Turing",
          facultyUsername: "teacher1cse",
          facultyEmail: "teacher1cse@meridian.edu",
          facultyPhone: "+1 (555) 011-2301",
          facultyDesignation: "Professor & HOD",
        },
        {
          id: 2,
          code: "CS102",
          name: "Data Structures & Algorithms",
          department: "CSE",
          semester: 2,
          year: 1,
          credits: 4,
          faculty: "Dr. Barbara Liskov",
          facultyUsername: "teacher2cse",
          facultyEmail: "teacher2cse@meridian.edu",
          facultyPhone: "+1 (555) 011-2302",
          facultyDesignation: "Professor",
        },
        {
          id: 3,
          code: "CS201",
          name: "Object Oriented Programming & Java",
          department: "CSE",
          semester: 3,
          year: 2,
          credits: 4,
          faculty: "Dr. James Gosling",
          facultyUsername: "teacher3cse",
          facultyEmail: "teacher3cse@meridian.edu",
          facultyPhone: "+1 (555) 011-2303",
          facultyDesignation: "Professor",
        },
        {
          id: 4,
          code: "CS202",
          name: "Database Management Systems",
          department: "CSE",
          semester: 4,
          year: 2,
          credits: 4,
          faculty: "Dr. Edgar Codd",
          facultyUsername: "teacher4cse",
          facultyEmail: "teacher4cse@meridian.edu",
          facultyPhone: "+1 (555) 011-2304",
          facultyDesignation: "Associate Professor",
        },
      ],
    };
  }

  if (path === "/api/attendance") {
    if (role === "faculty") {
      return {
        handled: true,
        data: {
          overall: 96,
          label: "Present this semester",
          lastUpdated: "Today",
          status: "good",
        },
      };
    }
    return {
      handled: true,
      data: [
        {
          subjectId: 1,
          subject: "Introduction to Programming",
          present: 38,
          total: 40,
          status: "good",
          percentage: 95,
          lastUpdated: "Today",
        },
        {
          subjectId: 2,
          subject: "Data Structures",
          present: 30,
          total: 40,
          status: "warning",
          percentage: 75,
          lastUpdated: "Yesterday",
        },
      ],
    };
  }

  if (path === "/api/marks") {
    if (role === "faculty") {
      return {
        handled: true,
        data: {
          subject: "Introduction to Programming (CS101)",
          average: 84.5,
          students: [
            {
              id: 1,
              name: "John Doe",
              score: 88,
              maxScore: 100,
              grade: "A",
              passed: true,
            },
            {
              id: 2,
              name: "Emma Wilson",
              score: 65,
              maxScore: 100,
              grade: "C",
              passed: true,
            },
            {
              id: 3,
              name: "Lucas Brown",
              score: 92,
              maxScore: 100,
              grade: "A",
              passed: true,
            },
            {
              id: 4,
              name: "Mia Davis",
              score: 85,
              maxScore: 100,
              grade: "B+",
              passed: true,
            },
            {
              id: 5,
              name: "Ethan Miller",
              score: 78,
              maxScore: 100,
              grade: "B",
              passed: true,
            },
            {
              id: 6,
              name: "Sophia Smith",
              score: 99,
              maxScore: 100,
              grade: "A+",
              passed: true,
            },
          ],
        },
      };
    }
    return {
      handled: true,
      data: [
        {
          id: 1,
          studentId: 1,
          subjectId: 1,
          subject: "Introduction to Programming",
          assessment: "Midterm",
          score: 88,
          maxScore: 100,
          grade: "A",
          passed: true,
        },
        {
          id: 2,
          studentId: 1,
          subjectId: 2,
          subject: "Data Structures",
          assessment: "Midterm",
          score: 65,
          maxScore: 100,
          grade: "C",
          passed: true,
        },
      ],
    };
  }

  if (path === "/api/assignments") {
    if (role === "faculty") {
      return {
        handled: true,
        data: [
          {
            id: 1,
            subjectId: 1,
            subject: "Introduction to Programming (CS101)",
            title: "Binary Tree Traversal",
            description: "Implement standard traversals in C++.",
            dueDate: "2024-10-15",
            maxMarks: 50,
            submissions: 24,
            totalStudents: 30,
            completionStatus: {
              completed: ["John Doe", "Lucas Brown", "Mia Davis"],
              incomplete: ["Emma Wilson", "Ethan Miller"],
            },
          },
          {
            id: 2,
            subjectId: 1,
            subject: "Introduction to Programming (CS101)",
            title: "Sorting Algorithms",
            description: "Implement Quick Sort and Merge Sort.",
            dueDate: "2024-09-20",
            maxMarks: 100,
            submissions: 30,
            totalStudents: 30,
            completionStatus: {
              completed: [
                "John Doe",
                "Emma Wilson",
                "Lucas Brown",
                "Mia Davis",
                "Ethan Miller",
              ],
              incomplete: [],
            },
          },
        ],
      };
    }
    return {
      handled: true,
      data: [
        {
          id: 1,
          subjectId: 1,
          subject: "Introduction to Programming",
          title: "Binary Tree Traversal",
          description: "Implement standard traversals in C++.",
          dueDate: "2024-10-15",
          maxMarks: 50,
          status: "due",
          submissions: 24,
          submitted: false,
        },
        {
          id: 2,
          subjectId: 2,
          subject: "Data Structures",
          title: "Graph Algorithms",
          description: "Dijkstra and A* implementations.",
          dueDate: "2024-09-20",
          maxMarks: 100,
          status: "submitted",
          submissions: 40,
          submitted: true,
        },
      ],
    };
  }

  if (path === "/api/announcements") {
    return {
      handled: true,
      data: [
        {
          id: 1,
          title: "Campus Maintenance",
          content:
            "Main library will be closed this weekend for system upgrades.",
          audience: "All campus",
          priority: "important",
          author: "Facilities",
          publishedAt: "2 hrs ago",
        },
        {
          id: 2,
          title: "Hackathon Registration Open",
          content: "Annual coding competition starts next month. Register now.",
          audience: "Students",
          priority: "normal",
          author: "CS Dept",
          publishedAt: "1 day ago",
        },
      ],
    };
  }

  if (path === "/api/events") {
    return {
      handled: true,
      data: [
        {
          id: 1,
          title: "Tech Symposium 2024",
          description: "Annual gathering of industry experts and academia.",
          date: "Oct 15",
          time: "09:00 AM",
          venue: "Main Auditorium",
          organizer: "Engineering Dept",
          capacity: 500,
          registered: 340,
          registeredByMe: true,
        },
        {
          id: 2,
          title: "Career Fair",
          description: "Meet with top employers looking for fresh graduates.",
          date: "Nov 02",
          time: "10:00 AM",
          venue: "Student Center",
          organizer: "Career Services",
          capacity: 1000,
          registered: 850,
          registeredByMe: false,
        },
      ],
    };
  }

  if (path === "/api/complaints") {
    return {
      handled: true,
      data: [
        {
          id: 1,
          title: "Wi-Fi issues in Library",
          category: "IT Support",
          student: "John Doe",
          description: "Connection keeps dropping on the second floor.",
          priority: "high",
          createdAt: "Today",
          status: "progress",
        },
        {
          id: 2,
          title: "Broken chair in Room 302",
          category: "Facilities",
          student: "Jane Smith",
          description: "The front row has a broken seat.",
          priority: "low",
          createdAt: "Yesterday",
          status: "submitted",
        },
      ],
    };
  }

  if (path === "/api/notifications") {
    return {
      handled: true,
      data: [
        {
          id: 1,
          title: "New Grade Posted",
          message: "Professor Turing posted grades for CS101.",
          read: false,
          createdAt: "10 mins ago",
        },
        {
          id: 2,
          title: "Assignment Due",
          message: "Binary Tree Traversal is due tomorrow.",
          read: true,
          createdAt: "Yesterday",
        },
      ],
    };
  }

  if (path === "/api/leaves" || path.startsWith("/api/leaves/")) {
    const mockLeaves = [
      {
        id: 1,
        applicantType: "student",
        applicantUsername: "student1cse",
        applicantName: "Alex Rivera",
        department: "CSE",
        studentId: "STU-2024-001",
        leaveType: "Medical Leave",
        startDate: "2026-10-02",
        endDate: "2026-10-04",
        totalDays: 3,
        reason: "High viral fever and physician-advised rest. Medical prescription submitted to department.",
        assignedApproverRole: "faculty",
        assignedApproverUsername: "teacher1cse",
        assignedApproverName: "Dr. Alan Turing",
        status: "APPROVED",
        reviewedBy: "Dr. Alan Turing",
        reviewerRemarks: "Approved on medical grounds. Please catch up on CS101 lab exercises.",
        reviewedAt: "2026-10-02 11:30",
        createdAt: "2026-10-01 18:20",
      },
      {
        id: 2,
        applicantType: "student",
        applicantUsername: "student1cse",
        applicantName: "Alex Rivera",
        department: "CSE",
        studentId: "STU-2024-001",
        leaveType: "Academic / On-Duty",
        startDate: "2026-10-14",
        endDate: "2026-10-16",
        totalDays: 3,
        reason: "Representing Meridian University at the National Inter-College Hackathon 2026.",
        assignedApproverRole: "faculty",
        assignedApproverUsername: "teacher1cse",
        assignedApproverName: "Dr. Alan Turing",
        status: "PENDING",
        reviewedBy: null,
        reviewerRemarks: null,
        reviewedAt: null,
        createdAt: "2026-10-08 09:15",
      },
      {
        id: 3,
        applicantType: "student",
        applicantUsername: "student2cse",
        applicantName: "Maya Patel",
        department: "CSE",
        studentId: "STU-2024-002",
        leaveType: "Personal / Family",
        startDate: "2026-10-18",
        endDate: "2026-10-19",
        totalDays: 2,
        reason: "Attending elder sister's wedding ceremony in home town.",
        assignedApproverRole: "faculty",
        assignedApproverUsername: "teacher2cse",
        assignedApproverName: "Dr. Barbara Liskov",
        status: "PENDING",
        reviewedBy: null,
        reviewerRemarks: null,
        reviewedAt: null,
        createdAt: "2026-10-08 14:00",
      },
      {
        id: 4,
        applicantType: "faculty",
        applicantUsername: "teacher1cse",
        applicantName: "Dr. Alan Turing",
        department: "CSE",
        studentId: null,
        leaveType: "Academic Duty / Conference",
        startDate: "2026-10-21",
        endDate: "2026-10-24",
        totalDays: 4,
        reason: "Keynote speaker at International Symposium on Machine Intelligence & Distributed Systems.",
        assignedApproverRole: "admin",
        assignedApproverUsername: "admin",
        assignedApproverName: "Campus Administrator",
        substituteTeacher: "Dr. Barbara Liskov will handle CS101 lectures and lab sessions.",
        status: "PENDING",
        reviewedBy: null,
        reviewerRemarks: null,
        reviewedAt: null,
        createdAt: "2026-10-07 10:30",
      },
      {
        id: 5,
        applicantType: "faculty",
        applicantUsername: "teacher2cse",
        applicantName: "Dr. Barbara Liskov",
        department: "CSE",
        studentId: null,
        leaveType: "Casual Leave",
        startDate: "2026-09-28",
        endDate: "2026-09-29",
        totalDays: 2,
        reason: "Family engagement and domestic commitments.",
        assignedApproverRole: "admin",
        assignedApproverUsername: "admin",
        assignedApproverName: "Campus Administrator",
        substituteTeacher: "Dr. James Gosling handled CS102 lectures.",
        status: "APPROVED",
        reviewedBy: "Campus Administrator",
        reviewerRemarks: "Approved. Lecture coverage by Dr. Gosling noted.",
        reviewedAt: "2026-09-26 16:45",
        createdAt: "2026-09-25 11:00",
      },
    ];

    if (method === "GET") {
      return { handled: true, data: mockLeaves };
    }
    return { handled: true, data: { success: true, ...mockLeaves[0] } };
  }

  // Fallback for all other /api routes
  if (method === "GET") {
    return { handled: true, data: [] };
  }
  return { handled: true, data: { success: true } };
}

