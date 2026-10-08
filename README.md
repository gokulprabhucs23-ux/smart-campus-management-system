# AI-Enabled Smart Campus Management System

A comprehensive, production-ready Full Stack Smart Campus Management System for Academic and Administrative Services.

Built with a **Spring Boot 3 + MongoDB** backend and a high-performance **React + Tailwind CSS + Vite** frontend.

---

## 🏛️ System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     React Frontend                      │
│      (Vite Dev Server :5173 / Production Build)         │
│   • Role Switcher (Student / Faculty / Admin / Staff)   │
│   • Dashboard & Analytics Widgets                       │
│   • Students, Faculty, Attendance, & Grading Systems    │
│   • AI Campus Assistant & Academic Insights             │
└────────────────────────────┬────────────────────────────┘
                             │  HTTP / REST (/api/*)
                             ▼
┌─────────────────────────────────────────────────────────┐
│                Spring Boot 3.2 Backend                  │
│               (Embedded Tomcat on :8080)                │
│   • 16 REST Controllers covering Academic & Admin APIs  │
│   • Real-Time Role & Security Header Handling           │
│   • Automatic Database Seeder with Realistic Mock Data  │
└────────────────────────────┬────────────────────────────┘
                             │  Spring Data MongoDB
                             ▼
┌─────────────────────────────────────────────────────────┐
│                     MongoDB Server                      │
│                   (Port 27017)                          │
│   • Collections: users, students, faculty, departments, │
│     subjects, attendance, marks, assignments,           │
│     announcements, events, complaints, notifications    │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites
1. **Java 17+** (OpenJDK 27 or 21)
2. **Maven 3.9+**
3. **Node.js 18+** & **npm**
4. **MongoDB Community Server** (running locally on port 27017 or remote URI via `MONGODB_URI`)

---

### Running the Services

#### 1. Start MongoDB (if not already running)
```bash
brew services start mongodb-community
# or run manually:
mongod --config /opt/homebrew/etc/mongod.conf
```

#### 2. Start the Spring Boot Backend
```bash
cd backend
export PATH="/opt/homebrew/opt/openjdk/bin:/opt/homebrew/bin:$PATH"
mvn spring-boot:run
```
*The backend automatically seeds initial data if MongoDB is empty.*
*Backend runs at:* `http://localhost:8080`
*Health check:* `http://localhost:8080/api/healthz`

#### 3. Start the React Frontend
```bash
cd frontend
npm run dev
```
*Frontend runs at:* `http://localhost:5173` (proxies `/api` to `http://localhost:8080`)

---

## 👥 User Roles & Features

| Role | Key Capabilities |
| :--- | :--- |
| **Admin** | Full system analytics, faculty & student directory CRUD, department stats, open complaints monitor, system health, announcements. |
| **Faculty** | Class schedule, student attendance marking, entering & updating student marks, assignments creation & submission reviews, department communications. |
| **Student** | Personal GPA & attendance tracking, subject syllabus, assignment submissions, fee ledger & payment history, campus event registration, service desk complaints, AI assistant. |
| **Staff / Admin**| Campus infrastructure tickets, event logistics, fee approvals, announcements, and administrative resolution workflows. |

---

## 🔌 API Endpoints Summary

### Core & Diagnostics
- `GET /api/healthz` - Health probe (database & service status)
- `GET /api/me` - Current session user profile based on `X-User-Role`

### Dashboards & Analytics
- `GET /api/dashboard` - Role-tailored metrics, stats, and real-time activity feeds
- `GET /api/dashboard/analytics` - Department distribution, complaint resolution, attendance trends

### Academic Services
- `GET /api/students` - Filterable student roster (`?search=`)
- `POST /api/students` - Register student
- `GET /api/students/{id}` - Get student profile
- `PUT /api/students/{id}` - Update student profile
- `DELETE /api/students/{id}` - Delete student
- `GET /api/students/{id}/details` - Detailed CGPA, arrears history, and fees
- `GET /api/faculty` - Faculty roster & subjects taught
- `POST /api/faculty` - Register faculty member
- `GET /api/departments` - Department metrics and faculty/student ratios
- `POST /api/departments` - Add department
- `GET /api/subjects` - Subject list with credits & assigned professor
- `POST /api/subjects` - Add subject

### Performance & Submissions
- `GET /api/attendance` - Student subject-wise attendance logs or faculty class summary
- `POST /api/attendance` - Record attendance check
- `GET /api/marks` - Grade breakdown & pass status
- `POST /api/marks` - Record/update examination scores
- `GET /api/assignments` - Active assignments & submission statuses
- `POST /api/assignments` - Post new assignment
- `POST /api/assignments/{id}/submit` - Submit student assignment work

### Campus Services & Administration
- `GET /api/announcements` - Priority campus bulletins
- `POST /api/announcements` - Publish announcement
- `GET /api/events` - Upcoming campus symposiums & events
- `POST /api/events` - Create event
- `POST /api/events/{id}/register` - Student registration for event
- `GET /api/complaints` - Grievance & facility tickets
- `POST /api/complaints` - Raise support ticket
- `PATCH /api/complaints/{id}/status` - Update ticket status (`submitted`, `progress`, `resolved`)
- `GET /api/notifications` - Notifications list
- `PATCH /api/notifications/{id}/read` - Mark notification read
- `GET /api/fees` - Tuition fee ledger and payment history

### AI Assistant & Academic Insights
- `POST /api/chat` - Intelligent assistant querying campus database
- `GET /api/academic-insights` - AI-driven academic recommendations & risk detection
