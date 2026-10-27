import { useState, useEffect, useRef } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import {
  QueryClient,
  QueryClientProvider,
  useQueryClient,
  useQuery,
} from "@tanstack/react-query";
import { Link, Route, Switch, useLocation } from "wouter";
import {
  Activity,
  AlertCircle,
  ArrowRight,
  Award,
  Bell,
  BookOpen,
  Bot,
  CalendarDays,
  Check,
  CheckCheck,
  CheckCircle2,
  ChevronRight,
  ClipboardCheck,
  Clock3,
  Command,
  Compass,
  Copy,
  Cpu,
  CreditCard,
  Download,
  ExternalLink,
  Eye,
  FileText,
  GraduationCap,
  Key,
  LayoutDashboard,
  LifeBuoy,
  Loader2,
  LogOut,
  Menu,
  MoreHorizontal,
  Paperclip,
  Phone,
  Mail,
  Plus,
  RefreshCw,
  Search,
  Send,
  CalendarCheck,
  CalendarClock,
  CheckSquare,
  Settings2,
  ShieldCheck,
  Sliders,
  Sparkles,
  TrendingUp,
  BarChart3,
  Layers,
  Trash2,
  Upload,
  UserRound,
  Users,
  X,
} from "lucide-react";
import {
  useGetMe,
  useGetDashboard,
  useGetDashboardAnalytics,
  useListDepartments,
  useCreateDepartment,
  useListStudents,
  useCreateStudent,
  useUpdateStudent,
  useDeleteStudent,
  useListFaculty,
  useCreateFaculty,
  useListSubjects,
  useListAttendance,
  useSaveAttendance,
  useListMarks,
  useSaveMarks,
  useListAssignments,
  useCreateAssignment,
  useSubmitAssignment,
  useListAnnouncements,
  useCreateAnnouncement,
  useListEvents,
  useCreateEvent,
  useRegisterForEvent,
  useListComplaints,
  useCreateComplaint,
  useUpdateComplaintStatus,
  useListNotifications,
  useMarkNotificationRead,
  useChatWithAssistant,
  useGetAcademicInsights,
  getListDepartmentsQueryKey,
  getListStudentsQueryKey,
  getListFacultyQueryKey,
  getListAttendanceQueryKey,
  getListAssignmentsQueryKey,
  getListAnnouncementsQueryKey,
  getListEventsQueryKey,
  getListComplaintsQueryKey,
  getListNotificationsQueryKey,
} from "@/api";
import { ErrorBoundary } from "@/components/error-boundary";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import NotFound from "@/pages/not-found";
import "@/index.css";
import { customFetch } from "@/api/custom-fetch";
import { StudentLeaveRequests } from "@/components/leaves/StudentLeaveRequests";
import { TeacherLeaveRequests } from "@/components/leaves/TeacherLeaveRequests";
import { LeaveApprovals } from "@/components/leaves/LeaveApprovals";

function StudentProfileModal({ student, onClose }) {
  const { data: details, isLoading } = useQuery({
    queryKey: ["studentDetails", student.id],
    queryFn: () => customFetch(`/api/students/${student.id}/details`),
  });

  return (
    <Modal title="Student Profile" onClose={onClose}>
      <div className="space-y-6 max-h-[75vh] overflow-y-auto pr-2">
        <div className="flex items-start gap-4">
          <div className="grid size-12 place-items-center rounded-full bg-primary/20 text-lg font-bold text-primary">
            {student.name
              .split(" ")
              .map((x) => x[0])
              .join("")
              .slice(0, 2)}
          </div>
          <div>
            <h3 className="display text-xl font-semibold">{student.name}</h3>
            <p className="text-sm text-muted-foreground">
              {student.studentId} · {student.email}
            </p>
            <p className="text-sm text-muted-foreground">
              {student.department} · Year {student.year} · Section{" "}
              {student.section}
            </p>
          </div>
        </div>

        {isLoading ? (
          <SkeletonRows count={5} />
        ) : (
          <>
            {details?.mentor && (
              <div className="rounded-lg bg-accent/20 p-4 border border-accent/30">
                <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Mentorship
                </div>
                <div className="mt-2 text-sm font-medium">
                  {details.mentor.name}
                </div>
                <div className="text-xs text-muted-foreground">
                  {details.mentor.phone}
                </div>
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <div className="rounded-lg bg-muted p-4">
                <div className="text-xs text-muted-foreground">CGPA</div>
                <div className="mt-1 text-2xl font-semibold">
                  {details?.cgpa || student.average || "N/A"}
                </div>
              </div>
              <div className="rounded-lg bg-muted p-4">
                <div className="text-xs text-muted-foreground">Attendance</div>
                <div className="mt-1 text-2xl font-semibold">
                  {student.attendance}%
                </div>
              </div>
            </div>

            {(details?.sgpa || []).length > 0 && (
              <div>
                <h4 className="mb-3 font-semibold">Semester GPA (SGPA)</h4>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                  {details.sgpa.map((s, i) => (
                    <div
                      key={i}
                      className="rounded border border-border p-3 text-center"
                    >
                      <div className="text-xs text-muted-foreground">
                        Sem {s.sem}
                      </div>
                      <div className="font-semibold">{s.gpa}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div>
              <h4 className="mb-3 font-semibold flex items-center gap-2">
                Arrears History
                <span className="rounded-full bg-muted px-2 py-0.5 text-xs">
                  {details?.arrearCount || 0} active
                </span>
              </h4>
              {(details?.arrearsHistory || []).length === 0 ? (
                <div className="text-sm text-muted-foreground">
                  No arrears history.
                </div>
              ) : (
                <div className="space-y-2">
                  {details.arrearsHistory.map((a, i) => (
                    <div
                      key={i}
                      className="flex justify-between rounded-lg border border-border p-3 text-sm"
                    >
                      <span>{a.subject}</span>
                      <span className="text-muted-foreground">Sem {a.sem}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {details?.fees && (
              <div>
                <h4 className="mb-3 font-semibold">Fee Status</h4>
                <div className="flex gap-4">
                  <div className="flex-1 rounded border border-border p-3">
                    <div className="text-xs text-muted-foreground">Paid</div>
                    <div className="font-semibold text-emerald-600">
                      ${details.fees.paid}
                    </div>
                  </div>
                  <div className="flex-1 rounded border border-border p-3">
                    <div className="text-xs text-muted-foreground">Pending</div>
                    <div className="font-semibold text-rose-600">
                      ${details.fees.pending}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </Modal>
  );
}

const queryClient = new QueryClient();
const getNavGroups = (role) => {
  const allGroups = [
    {
      label: "Workspace",
      items: [
        ["Dashboard", "/dashboard", LayoutDashboard],
        ...(role === "admin" || role === "faculty"
          ? [["Students", "/students", Users]]
          : []),
        ...(role === "admin"
          ? [
              ["Faculty", "/faculty", GraduationCap],
              ["Departments", "/departments", Compass],
            ]
          : []),
        ...(role === "admin" || role === "faculty"
          ? [["Subjects", "/subjects", BookOpen]]
          : []),
        ...(role === "admin"
          ? [
              ["Timetable", "/timetable", Clock3],
              ["Leave Approvals", "/leave-approvals", CheckSquare],
            ]
          : []),
      ],
    },
    ...(role === "faculty" || role === "student"
      ? [
          {
            label: "Academic",
            items: [
              ...(role === "student" ? [["Subjects", "/subjects", BookOpen]] : []),
              ["Timetable", "/timetable", Clock3],
              ["Attendance", "/attendance", ClipboardCheck],
              ["Marks", "/marks", Award],
              ["Assignments", "/assignments", FileText],
              ...(role === "student"
                ? [
                    ["Leave Requests", "/leave-requests", CalendarCheck],
                    ["Fees", "/fees", CreditCard],
                  ]
                : []),
            ],
          },
        ]
      : []),
    ...(role === "faculty"
      ? [
          {
            label: "Leave & Approvals",
            items: [
              ["My Leave Requests", "/my-leave-requests", CalendarClock],
              ["Student Leave Approvals", "/leave-approvals", CheckSquare],
            ],
          },
        ]
      : []),
    {
      label: "Campus life",
      items: [
        ["Announcements", "/announcements", Bell],
        ["Events", "/events", CalendarDays],
        ["Complaints", "/complaints", LifeBuoy],
      ],
    },
  ];
  return allGroups.filter((g) => g.items.length > 0);
};
const getUtilityNav = (role) => [
  ["Notifications", "/notifications", Bell],
  ["AI assistant", "/ai-assistant", Bot],
  ...(role === "faculty" || role === "student"
    ? [["Academic insights", "/academic-insights", Sparkles]]
    : []),
];

function Toast({ message, onClose }) {
  return (
    <div
      className="fixed bottom-5 right-5 z-50 flex items-center gap-3 rounded-xl border border-primary/20 bg-card px-4 py-3 text-sm shadow-xl rise"
      data-testid="status-toast"
    >
      <Check className="size-4 text-emerald-600" />
      <span>{message}</span>
      <button
        onClick={onClose}
        className="text-muted-foreground"
        data-testid="button-close-toast"
      >
        <X className="size-4" />
      </button>
    </div>
  );
}

function Shell({ children }) {
  const [location] = useLocation();
  const [open, setOpen] = useState(false);
  const [toast, setToast] = useState("");
  const { signOut, switchRole } = useAuth();
  const me = useGetMe();
  const name = me.data?.name ?? "Campus colleague";
  const initials = name
    .split(" ")
    .map((x) => x[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
  const role = me.data?.role ?? "student";
  const currentNavGroups = getNavGroups(role);
  const showToast = (message) => setToast(message);
  return (
    <div className="app-shell grain">
      <aside
        className={`fixed inset-y-0 left-0 z-30 flex w-[254px] flex-col border-r border-sidebar-border bg-sidebar px-4 py-5 text-sidebar-foreground transition-transform md:translate-x-0 ${open ? "translate-x-0" : "-translate-x-full"}`}
      >
        <div className="mb-8 flex items-center gap-3 px-2">
          <div className="grid size-9 place-items-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
            <Command className="size-5" />
          </div>
          <div>
            <div className="display text-lg font-semibold tracking-tight">
              meridian
            </div>
            <div className="text-[10px] uppercase tracking-[.2em] text-sidebar-foreground/50">
              campus operations
            </div>
          </div>
        </div>
        <nav className="flex-1 space-y-6 overflow-y-auto">
          {currentNavGroups.map((group) => (
            <div key={group.label}>
              <div className="mb-2 px-3 text-[10px] font-bold uppercase tracking-[.16em] text-sidebar-foreground/40">
                {group.label}
              </div>
              <div className="space-y-1">
                {group.items.map(([label, href, Icon]) => (
                  <NavItem
                    key={href}
                    label={label}
                    href={href}
                    Icon={Icon}
                    active={location === href}
                  />
                ))}
              </div>
            </div>
          ))}
          <div className="space-y-1">
            {getUtilityNav(role).map(([label, href, Icon]) => (
              <NavItem
                key={href}
                label={label}
                href={href}
                Icon={Icon}
                active={location === href}
              />
            ))}
          </div>
        </nav>
        <div className="mt-5 border-t border-sidebar-border pt-4">
          <NavItem
            label="Profile"
            href="/profile"
            Icon={UserRound}
            active={location === "/profile"}
          />
          <NavItem
            label="Settings"
            href="/settings"
            Icon={Settings2}
            active={location === "/settings"}
          />
        </div>
        <div className="mt-4 rounded-xl bg-sidebar-accent p-3">
          <div className="flex items-center gap-3">
            <div className="grid size-9 place-items-center rounded-full bg-sidebar-primary text-xs font-bold text-sidebar-primary-foreground">
              {initials}
            </div>
            <div className="min-w-0 flex-1">
              <div className="truncate text-sm font-medium">{name}</div>
              <div className="flex items-center gap-1.5 text-[11px] text-sidebar-foreground/60">
                <span className="font-mono text-primary">@{me.data?.username || "user"}</span>
                {me.data?.department && (
                  <span className="rounded bg-primary/15 px-1 font-semibold text-[10px] text-primary">
                    {me.data.department}
                  </span>
                )}
              </div>
            </div>
            <button
              onClick={signOut}
              title="Sign Out"
              className="text-sidebar-foreground/50 hover:text-destructive p-1 rounded transition-colors"
              data-testid="button-sidebar-logout"
            >
              <LogOut className="size-4" />
            </button>
          </div>
        </div>
      </aside>
      {open && (
        <button
          className="fixed inset-0 z-20 bg-foreground/30 md:hidden"
          onClick={() => setOpen(false)}
          aria-label="Close menu"
          data-testid="button-close-menu"
        />
      )}
      <main className="md:pl-[254px]">
        <header className="sticky top-0 z-10 flex h-[68px] items-center justify-between glass-panel px-5 md:px-9">
          <div className="flex items-center gap-3">
            <button
              className="md:hidden"
              onClick={() => setOpen(true)}
              data-testid="button-open-menu"
            >
              <Menu className="size-5" />
            </button>
            <div className="hidden items-center gap-2 text-xs text-muted-foreground md:flex">
              <span className="font-medium text-foreground">{name}</span>
              <span className="size-1 rounded-full bg-accent" />
              <span className="font-mono text-primary">@{me.data?.username || "user"}</span>
              <span className="size-1 rounded-full bg-accent" />
              <span className="capitalize font-semibold rounded bg-muted px-2 py-0.5 text-[11px]">
                {role}
              </span>
              {me.data?.department && (
                <span className="rounded bg-primary/10 text-primary px-2 py-0.5 text-[11px] font-semibold">
                  {me.data.department}
                </span>
              )}
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Link
              href="/ai-assistant"
              className="flex items-center gap-1.5 rounded-lg bg-gradient-to-r from-primary/15 via-primary/20 to-accent/30 border border-primary/30 px-3 py-1.5 text-xs font-semibold text-primary hover:from-primary/25 hover:to-accent/40 transition-all shadow-xs"
              data-testid="link-header-ask-ai"
              title="Launch Meridian AI Campus Assistant"
            >
              <Sparkles className="size-3.5 text-primary animate-pulse" />
              <span className="hidden sm:inline">Ask AI</span>
            </Link>
            <div className="hidden sm:block">
              <select
                value={role}
                onChange={(e) => switchRole(e.target.value)}
                className="h-8.5 rounded-lg border border-border bg-card px-2.5 text-xs font-medium text-foreground outline-none hover:border-primary cursor-pointer transition-colors"
                title="Switch role for demo and testing"
              >
                <option value="student">🎓 Student View</option>
                <option value="faculty">👨‍🏫 Faculty View</option>
                <option value="admin">👑 Admin View</option>
              </select>
            </div>
            <Link
              href="/notifications"
              className="relative grid size-9 place-items-center rounded-lg text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
              data-testid="link-header-notifications"
            >
              <Bell className="size-[18px]" />
              <span className="absolute right-1.5 top-1.5 size-1.5 rounded-full bg-accent" />
            </Link>
            <Link
              href="/profile"
              className="grid size-9 place-items-center rounded-full bg-primary/10 text-xs font-bold text-primary hover:bg-primary/20 transition-colors"
              data-testid="link-header-profile"
            >
              {initials}
            </Link>
            <button
              onClick={signOut}
              title="Sign Out"
              className="flex items-center gap-1.5 text-xs text-muted-foreground hover:text-destructive px-2.5 py-1.5 rounded-lg border border-border transition-colors ml-1"
              data-testid="button-header-logout"
            >
              <LogOut className="size-3.5" />
              <span className="hidden sm:inline">Logout</span>
            </button>
          </div>
        </header>
        <div className="mx-auto max-w-[1440px] px-5 py-7 md:px-9 lg:py-9">
          {children}
        </div>
      </main>
      {toast && <Toast message={toast} onClose={() => setToast("")} />}
    </div>
  );
}
function NavItem({ label, href, Icon, active }) {
  return (
    <Link
      href={href}
      className={`nav-item flex items-center gap-3 rounded-lg px-3 py-2.5 text-[13px] ${active ? "bg-sidebar-primary font-semibold text-sidebar-primary-foreground" : "text-sidebar-foreground/68 hover:bg-sidebar-accent hover:text-sidebar-foreground"}`}
      data-testid={`link-nav-${label.toLowerCase().replaceAll(" ", "-")}`}
    >
      <Icon className="size-[17px]" />
      <span>{label}</span>
      {label === "Notifications" && (
        <span className="ml-auto size-1.5 rounded-full bg-sidebar-primary" />
      )}
    </Link>
  );
}

function Landing() {
  return (
    <div className="min-h-[100dvh] bg-background text-foreground">
      <header className="mx-auto flex max-w-7xl items-center justify-between px-6 py-6 md:px-10">
        <div className="flex items-center gap-3">
          <div className="grid size-9 place-items-center rounded-lg bg-primary text-primary-foreground shadow-lg">
            <Command className="size-5" />
          </div>
          <span className="display text-xl font-semibold">meridian</span>
        </div>
        <Link
          href="/sign-in"
          className="rounded-lg border border-primary/20 px-4 py-2 text-sm font-semibold transition-colors hover:bg-primary hover:text-primary-foreground"
          data-testid="link-landing-sign-in"
        >
          Sign in <ArrowRight className="ml-2 inline size-4" />
        </Link>
      </header>
      <section className="mx-auto grid max-w-7xl items-center gap-12 px-6 pb-20 pt-14 md:grid-cols-[1.05fr_.95fr] md:px-10 md:pb-28 md:pt-20">
        <div className="rise">
          <div className="mb-6 flex items-center gap-2 text-xs font-bold uppercase tracking-[.18em] text-primary">
            <span className="size-2 rounded-full bg-accent" /> One dependable
            daily workspace
          </div>
          <h1 className="display max-w-3xl text-5xl font-semibold leading-[.98] md:text-7xl">
            Campus work,
            <br />
            <span className="text-primary drop-shadow-sm">in rhythm.</span>
          </h1>
          <p className="mt-7 max-w-xl text-lg leading-8 text-foreground/65">
            Meridian gives administrators, faculty, staff, and students one
            clear view of the work that keeps a campus moving.
          </p>
          <div className="mt-9 flex flex-wrap gap-3">
            <Link
              href="/dashboard"
              className="rounded-lg bg-primary px-5 py-3.5 text-sm font-semibold text-primary-foreground transition-transform hover:-translate-y-0.5 hover:shadow-lg hover:shadow-primary/20"
              data-testid="link-landing-dashboard"
            >
              Open the workspace <ArrowRight className="ml-2 inline size-4" />
            </Link>
            <a
              href="#how-it-works"
              className="rounded-lg px-5 py-3.5 text-sm font-semibold text-foreground/70 transition-colors hover:bg-muted/50 hover:text-primary"
              data-testid="link-landing-learn"
            >
              See how it works
            </a>
          </div>
        </div>
        <div className="relative rise delay-2">
          <div className="absolute -inset-5 rounded-[2rem] bg-accent/20 blur-2xl" />
          <div className="relative glass-card rounded-2xl p-4">
            <div className="rounded-xl bg-card/50 p-5 backdrop-blur-sm">
              <div className="mb-7 flex items-center justify-between">
                <div>
                  <div className="text-[10px] uppercase tracking-[.16em] text-muted-foreground">
                    Good morning, Elena
                  </div>
                  <div className="display mt-1 text-2xl font-semibold">
                    A day in view.
                  </div>
                </div>
                <div className="grid size-9 place-items-center rounded-full bg-primary/20 text-xs font-bold text-primary">
                  EC
                </div>
              </div>
              <div className="grid grid-cols-3 gap-2">
                <div className="rounded-lg bg-emerald-500/10 p-3">
                  <div className="text-[10px] text-muted-foreground">
                    Attendance
                  </div>
                  <div className="mt-2 text-xl font-semibold">92.4%</div>
                </div>
                <div className="rounded-lg bg-amber-500/10 p-3">
                  <div className="text-[10px] text-muted-foreground">
                    Open tasks
                  </div>
                  <div className="mt-2 text-xl font-semibold">18</div>
                </div>
                <div className="rounded-lg bg-rose-500/10 p-3">
                  <div className="text-[10px] text-muted-foreground">
                    Notices
                  </div>
                  <div className="mt-2 text-xl font-semibold">06</div>
                </div>
              </div>
              <div className="mt-6 space-y-3">
                <div className="flex items-center gap-3 border-b border-border/50 pb-3">
                  <span className="size-2 rounded-full bg-emerald-500" />
                  <span className="flex-1 text-sm">
                    Faculty review window closes
                  </span>
                  <span className="text-xs text-muted-foreground">Today</span>
                </div>
                <div className="flex items-center gap-3 border-b border-border/50 pb-3">
                  <span className="size-2 rounded-full bg-amber-500" />
                  <span className="flex-1 text-sm">Engineering open house</span>
                  <span className="text-xs text-muted-foreground">Thu</span>
                </div>
                <div className="flex items-center gap-3">
                  <span className="size-2 rounded-full bg-rose-500" />
                  <span className="flex-1 text-sm">
                    3 attendance risks need a look
                  </span>
                  <span className="text-xs text-muted-foreground">New</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
      <section
        id="how-it-works"
        className="border-t border-border/50 bg-primary/5 px-6 py-20 md:px-10 md:py-28"
      >
        <div className="mx-auto max-w-7xl">
          <div className="max-w-2xl">
            <div className="text-xs font-bold uppercase tracking-[.18em] text-primary">
              Built for the whole campus
            </div>
            <h2 className="display mt-4 text-4xl font-semibold md:text-6xl">
              Less chasing.
              <br />
              More moving.
            </h2>
          </div>
          <div className="mt-16 grid gap-10 md:grid-cols-3">
            {[
              [
                "01",
                "See the signal",
                "Dashboards turn the day’s activity into a calm, useful starting point.",
              ],
              [
                "02",
                "Do the work",
                "Attendance, marks, assignments, and requests live where action happens.",
              ],
              [
                "03",
                "Stay in step",
                "Announcements and events reach the right people without adding noise.",
              ],
            ].map(([n, t, d], i) => (
              <div
                key={n}
                className={`rise delay-${i + 1} border-t border-border/50 pt-5`}
              >
                <div className="text-sm font-bold text-primary">{n}</div>
                <h3 className="display mt-12 text-2xl font-semibold">{t}</h3>
                <p className="mt-3 max-w-xs leading-7 text-muted-foreground">
                  {d}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>
      <footer className="mx-auto flex max-w-7xl items-center justify-between px-6 py-8 text-xs text-muted-foreground md:px-10">
        <span>Meridian · campus operations</span>
        <span>Quietly keeping campus moving.</span>
      </footer>
    </div>
  );
}

function PageHead({ eyebrow, title, description, action }) {
  return (
    <div className="mb-7 flex flex-col justify-between gap-5 md:flex-row md:items-end">
      <div>
        <div className="mb-2 text-[11px] font-bold uppercase tracking-[.16em] text-primary">
          {eyebrow}
        </div>
        <h1
          className="display text-4xl font-semibold tracking-tight md:text-[2.8rem]"
          data-testid={`text-page-title-${title.toLowerCase().replaceAll(" ", "-")}`}
        >
          {title}
        </h1>
        {description && (
          <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
            {description}
          </p>
        )}
      </div>
      {action}
    </div>
  );
}
function Card({ children, className = "", ...rest }) {
  return (
    <section className={`glass-card rounded-xl ${className}`} {...rest}>
      {children}
    </section>
  );
}
function SkeletonRows({ count = 4 }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: count }, (_, i) => (
        <div key={i} className="skeleton h-12 rounded-lg" />
      ))}
    </div>
  );
}
function ErrorState({ retry }) {
  return (
    <div className="rounded-xl border border-destructive/20 bg-destructive/5 p-8 text-center">
      <AlertCircle className="mx-auto size-8 text-destructive" />
      <p className="mt-3 font-semibold">This view could not load.</p>
      <p className="mt-1 text-sm text-muted-foreground">
        Check your connection and try again.
      </p>
      {retry && (
        <button
          onClick={retry}
          className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm text-primary-foreground"
          data-testid="button-retry"
        >
          Try again
        </button>
      )}
    </div>
  );
}
function Empty({ label }) {
  return (
    <div className="border border-dashed border-border p-12 text-center">
      <div className="mx-auto grid size-11 place-items-center rounded-full bg-muted">
        <Compass className="size-5 text-muted-foreground" />
      </div>
      <p className="mt-3 font-medium">No {label} yet</p>
      <p className="mt-1 text-sm text-muted-foreground">
        When something arrives, it will appear here.
      </p>
    </div>
  );
}
function SearchBox({ value, onChange, placeholder = "Search" }) {
  return (
    <label className="flex h-10 min-w-[220px] items-center gap-2 rounded-lg border border-input bg-background px-3 text-muted-foreground">
      <Search className="size-4" />
      <input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="w-full bg-transparent text-sm outline-none placeholder:text-muted-foreground/70"
        data-testid="input-search"
      />
    </label>
  );
}
function Status({ children, tone = "neutral" }) {
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-semibold capitalize ${tone === "positive" ? "bg-emerald-100 text-emerald-800" : tone === "warning" ? "bg-amber-100 text-amber-800" : tone === "critical" ? "bg-rose-100 text-rose-800" : "bg-muted text-muted-foreground"}`}
    >
      {children}
    </span>
  );
}
function Modal({ title, subtitle, onClose, maxWidth = "max-w-lg", children }) {
  return (
    <div className="fixed inset-0 z-40 grid place-items-center bg-foreground/30 p-4 backdrop-blur-sm overflow-y-auto">
      <div className={`w-full ${maxWidth} max-h-[92vh] flex flex-col rounded-2xl border border-border bg-card p-6 shadow-2xl rise my-auto`}>
        <div className="mb-4 flex items-start justify-between shrink-0">
          <div>
            <h2 className="display text-2xl font-semibold">{title}</h2>
            {subtitle ? (
              <p className="mt-1 text-xs text-muted-foreground">{subtitle}</p>
            ) : (
              <p className="mt-1 text-xs text-muted-foreground">
                Add the details below to keep the record complete.
              </p>
            )}
          </div>
          <button
            onClick={onClose}
            className="rounded-lg p-1 text-muted-foreground hover:bg-muted"
            data-testid="button-close-modal"
          >
            <X className="size-5" />
          </button>
        </div>
        <div className="overflow-y-auto flex-1 pr-1">
          {children}
        </div>
      </div>
    </div>
  );
}
function Field({
  label,
  name,
  type = "text",
  required = true,
  defaultValue = "",
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1.5 block text-xs font-semibold text-muted-foreground">
        {label}
      </span>
      <input
        name={name}
        type={type}
        defaultValue={defaultValue}
        required={required}
        className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm outline-none transition-colors focus:border-primary focus:ring-2 focus:ring-primary/10"
        data-testid={`input-${name}`}
      />
    </label>
  );
}
function StudentDashboard({ d, analytics }) {
  return (
    <>
      <PageHead
        eyebrow={`${d?.role ?? "Campus"} workspace`}
        title={d?.greeting ?? "Good morning."}
        description="A clear starting point for everything happening across your campus."
        action={
          <div className="flex items-center gap-2">
            <Link
              href="/leave-requests"
              className="rounded-lg border border-primary/20 bg-primary/10 px-3.5 py-2 text-xs font-semibold text-primary hover:bg-primary/20 transition-colors"
            >
              Apply for Leave
            </Link>
            <Link
              href="/announcements"
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
              data-testid="link-dashboard-announcements"
            >
              View announcements <ArrowRight className="ml-2 inline size-4" />
            </Link>
          </div>
        }
      />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {(d?.stats ?? []).map((s, i) => (
          <Card key={s.label} className={`rise delay-${i + 1} p-5`}>
            <div className="flex items-start justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {s.label}
              </span>
              <Activity className="size-4 text-primary/60" />
            </div>
            <div
              className="mt-4 display text-3xl font-semibold"
              data-testid={`text-stat-${i}`}
            >
              {s.value}
            </div>
            <div
              className={`mt-2 text-xs ${s.tone === "critical" ? "text-rose-700" : "text-emerald-700"}`}
            >
              {s.change}
            </div>
          </Card>
        ))}
      </div>
      <div className="mt-6 grid gap-6 xl:grid-cols-[1.3fr_.7fr]">
        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="display text-xl font-semibold">
                Current activity
              </h2>
              <p className="mt-1 text-xs text-muted-foreground">
                The latest signals from around campus
              </p>
            </div>
            <Link
              href="/notifications"
              className="text-xs font-semibold text-primary"
              data-testid="link-dashboard-activity"
            >
              See all <ChevronRight className="inline size-3" />
            </Link>
          </div>
          <div className="mt-5 divide-y divide-border">
            {(d?.activities ?? []).map((a, i) => (
              <div
                key={i}
                className="flex gap-4 py-4 first:pt-0 last:pb-0 rise"
                style={{ animationDelay: `${i * 60}ms` }}
              >
                <div className="mt-1 size-2 shrink-0 rounded-full bg-accent" />
                <div className="min-w-0 flex-1">
                  <div className="text-sm font-medium">{a.title}</div>
                  <div className="mt-1 text-xs text-muted-foreground">
                    {a.detail}
                  </div>
                </div>
                <time className="shrink-0 text-xs text-muted-foreground">
                  {a.time}
                </time>
              </div>
            ))}
          </div>
        </Card>
        <div className="space-y-6">
          <Card className="overflow-hidden p-0">
            <div className="bg-primary p-6 text-primary-foreground h-full">
              <div className="flex items-start justify-between">
                <div>
                  <div className="text-[10px] font-bold uppercase tracking-[.16em] text-primary-foreground/60">
                    Campus pulse
                  </div>
                  <h2 className="display mt-2 text-2xl font-semibold">
                    Make the next move.
                  </h2>
                </div>
                <Sparkles className="size-5 text-accent" />
              </div>
              <p className="mt-4 text-sm leading-6 text-primary-foreground/65">
                Check today’s open requests and help the right work move
                forward.
              </p>
              <Link
                href="/complaints"
                className="mt-5 inline-flex items-center text-sm font-semibold text-accent"
                data-testid="link-dashboard-complaints"
              >
                Open service requests <ArrowRight className="ml-2 size-4" />
              </Link>
            </div>
          </Card>
          <Card className="p-6">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="display text-xl font-semibold">
                Attendance trend
              </h2>
              <span className="text-xs text-muted-foreground">This term</span>
            </div>
            <div className="flex h-28 items-end gap-2">
              {(analytics.data?.attendanceTrend ?? []).map((x, i) => (
                <div
                  key={x.label}
                  className="group flex flex-1 flex-col items-center gap-2"
                >
                  <div
                    className="w-full rounded-t bg-primary/75 transition-all group-hover:bg-accent"
                    style={{ height: `${Math.max(12, x.value)}%` }}
                  />
                  <span className="text-[10px] text-muted-foreground">
                    {x.label}
                  </span>
                </div>
              ))}
            </div>
          </Card>
        </div>
      </div>
      {/* Semester-Wise Percentages & Academic Journey */}
      <div className="mt-6 space-y-6">
        <Card className="p-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border pb-4">
            <div>
              <div className="flex items-center gap-2">
                <span className="rounded-md bg-primary/10 px-2.5 py-1 text-xs font-semibold text-primary">
                  {d?.department || "Engineering"} · Year {d?.year || 1}
                </span>
                <span className="rounded-md bg-accent/30 px-2.5 py-1 text-xs font-semibold text-primary">
                  Semester {d?.semester || 1}
                </span>
              </div>
              <h2 className="display mt-2 text-xl font-semibold">
                Semester-Wise Academic Percentages & SGPA
              </h2>
              <p className="mt-1 text-xs text-muted-foreground">
                Track progression across all 4 years and 8 semesters of the engineering program.
              </p>
            </div>
            <div className="flex items-center gap-4 text-right">
              <div>
                <div className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                  Cumulative CGPA
                </div>
                <div className="text-2xl font-bold text-primary">
                  {d?.cgpa || "8.75"}
                </div>
              </div>
              <div>
                <div className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                  Total Cleared
                </div>
                <div className="text-2xl font-bold text-emerald-600">
                  {(d?.semesterPercentages || []).filter(p => p.status === "Passed").length} Sems
                </div>
              </div>
            </div>
          </div>

          <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {(d?.semesterPercentages && d.semesterPercentages.length > 0
              ? d.semesterPercentages
              : [
                  { semester: 1, year: 1, percentage: 88.5, sgpa: 8.8, creditsEarned: 22, status: "Ongoing" },
                ]
            ).map((sp) => (
              <div
                key={sp.semester}
                className="rounded-xl border border-border bg-card p-4 transition-all hover:border-primary/40 hover:shadow-sm"
              >
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-sm">
                    Semester {sp.semester}
                  </span>
                  <Status tone={sp.status === "Passed" ? "positive" : "warning"}>
                    {sp.status}
                  </Status>
                </div>
                <div className="mt-3 flex items-baseline justify-between">
                  <span className="display text-3xl font-bold text-foreground">
                    {sp.percentage}%
                  </span>
                  <span className="text-sm font-semibold text-primary">
                    {sp.sgpa} SGPA
                  </span>
                </div>
                <div className="mt-3 flex items-center justify-between text-xs text-muted-foreground">
                  <span>Year {sp.year || Math.ceil(sp.semester / 2)}</span>
                  <span>{sp.creditsEarned || 22} Credits Earned</span>
                </div>
                <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-muted/60">
                  <div
                    className="h-full rounded-full bg-primary"
                    style={{ width: `${Math.min(100, sp.percentage)}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </Card>

        {/* Arrears & Backlog Management */}
        <div className="grid gap-6 lg:grid-cols-2">
          {/* Current Arrears */}
          <Card className="p-6">
            <div className="flex items-center justify-between border-b border-border pb-4">
              <div>
                <h2 className="display text-lg font-semibold flex items-center gap-2">
                  <ShieldCheck className="size-5 text-primary" /> Current Arrears
                </h2>
                <p className="mt-0.5 text-xs text-muted-foreground">
                  Active backlogs requiring examination clearance
                </p>
              </div>
              <span className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${
                (d?.currentArrears || []).length > 0
                  ? "bg-rose-100 text-rose-800"
                  : "bg-emerald-100 text-emerald-800"
              }`}>
                {(d?.currentArrears || []).length} Active
              </span>
            </div>

            <div className="mt-4">
              {(d?.currentArrears || []).length === 0 ? (
                <div className="rounded-xl border border-dashed border-emerald-500/30 bg-emerald-500/5 p-6 text-center">
                  <CheckCircle2 className="mx-auto size-8 text-emerald-600" />
                  <div className="mt-2 font-semibold text-sm text-emerald-800 dark:text-emerald-300">
                    Clean Academic Standing
                  </div>
                  <p className="mt-1 text-xs text-muted-foreground">
                    You have 0 active arrears. All enrolled courses are cleared on schedule.
                  </p>
                </div>
              ) : (
                <div className="space-y-3">
                  {(d?.currentArrears || []).map((ca, idx) => (
                    <div
                      key={idx}
                      className="rounded-xl border border-rose-200 bg-rose-50/40 p-4 dark:border-rose-950 dark:bg-rose-950/20"
                    >
                      <div className="flex items-start justify-between">
                        <div>
                          <div className="font-semibold text-sm text-rose-900 dark:text-rose-200">
                            {ca.subjectCode}: {ca.subjectName}
                          </div>
                          <div className="mt-1 text-xs text-muted-foreground">
                            Semester {ca.semester} · {ca.credits || 4} Credits
                          </div>
                        </div>
                        <Status tone="critical">{ca.status || "Backlog"}</Status>
                      </div>
                      <div className="mt-3 flex items-center justify-between text-xs text-muted-foreground border-t border-rose-200/60 pt-2 dark:border-rose-900/40">
                        <span>Internal Marks: {ca.internalMarks || "18"}/30</span>
                        <span>Next Exam: {ca.examDate || "Nov 2026"}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </Card>

          {/* Arrears History */}
          <Card className="p-6">
            <div className="flex items-center justify-between border-b border-border pb-4">
              <div>
                <h2 className="display text-lg font-semibold flex items-center gap-2">
                  <Award className="size-5 text-primary" /> History of Cleared Arrears
                </h2>
                <p className="mt-0.5 text-xs text-muted-foreground">
                  Record of previously cleared backlogs with clearance grade
                </p>
              </div>
              <span className="rounded-full bg-muted px-2.5 py-0.5 text-xs font-semibold text-muted-foreground">
                {(d?.arrearsHistory || []).length} Cleared
              </span>
            </div>

            <div className="mt-4">
              {(d?.arrearsHistory || []).length === 0 ? (
                <div className="rounded-xl border border-dashed border-border p-6 text-center text-xs text-muted-foreground">
                  No historical arrears on academic transcript. All courses cleared in original semester.
                </div>
              ) : (
                <div className="divide-y divide-border">
                  {(d?.arrearsHistory || []).map((ah, idx) => (
                    <div key={idx} className="flex items-center justify-between py-3.5 first:pt-0 last:pb-0">
                      <div>
                        <div className="font-semibold text-sm">
                          {ah.subjectCode}: {ah.subjectName}
                        </div>
                        <div className="mt-0.5 text-xs text-muted-foreground">
                          Original: Semester {ah.semester} · Cleared in Semester {ah.clearedSemester || ah.semester} ({ah.clearedDate || "2024"})
                        </div>
                      </div>
                      <div className="text-right">
                        <span className="inline-flex items-center gap-1 rounded bg-emerald-100 px-2 py-0.5 text-xs font-bold text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300">
                          Grade {ah.grade || "B+"}
                        </span>
                        <div className="mt-0.5 text-[11px] text-muted-foreground">
                          {ah.credits || 4} credits
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </Card>
        </div>
      </div>
    </>
  );
}
function AdminDashboard({ d, analytics }) {
  return (
    <>
      <PageHead
        eyebrow="Administration"
        title={d?.greeting ?? "Good morning, Admin."}
        description="Campus operations at a glance."
        action={
          <div className="flex items-center gap-2">
            <Link
              href="/leave-approvals"
              className="rounded-lg border border-primary/20 bg-primary/10 px-3.5 py-2 text-xs font-semibold text-primary hover:bg-primary/20 transition-colors"
            >
              Faculty Leave Approvals
            </Link>
            <Link
              href="/departments"
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
            >
              Manage Campus <ArrowRight className="ml-2 inline size-4" />
            </Link>
          </div>
        }
      />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {(d?.stats ?? []).map((s, i) => (
          <Card key={s.label} className={`rise delay-${i + 1} p-5`}>
            <div className="flex items-start justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {s.label}
              </span>
              <Activity className="size-4 text-primary/60" />
            </div>
            <div className="mt-4 display text-3xl font-semibold">{s.value}</div>
            <div
              className={`mt-2 text-xs ${s.tone === "critical" ? "text-rose-700" : "text-emerald-700"}`}
            >
              {s.change}
            </div>
          </Card>
        ))}
      </div>
      <div className="mt-6 grid gap-6 xl:grid-cols-[1.3fr_.7fr]">
        <Card className="p-6">
          <h2 className="display text-xl font-semibold mb-4">
            Department Student Distribution
          </h2>
          <div className="space-y-4">
            {(analytics.data?.departmentStudents ?? []).map((x) => (
              <div key={x.label} className="flex justify-between items-center">
                <span className="text-sm font-medium">{x.label}</span>
                <span className="text-sm text-muted-foreground">
                  {x.value} students
                </span>
              </div>
            ))}
          </div>
        </Card>
        <Card className="p-6">
          <h2 className="display text-xl font-semibold mb-4">
            Complaint Metrics
          </h2>
          <div className="space-y-4">
            {(analytics.data?.complaints ?? []).map((x) => (
              <div key={x.label} className="flex justify-between items-center">
                <span className="text-sm font-medium flex items-center gap-2">
                  <span
                    className="size-2 rounded-full"
                    style={{ backgroundColor: x.color }}
                  />{" "}
                  {x.label}
                </span>
                <span className="text-sm text-muted-foreground">{x.value}</span>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </>
  );
}
function FacultyDashboard({ d, analytics }) {
  return (
    <>
      <PageHead
        eyebrow="Faculty Workspace"
        title={d?.greeting ?? "Good morning."}
        description="Your classes and department insights."
        action={
          <div className="flex items-center gap-2">
            <Link
              href="/leave-approvals"
              className="rounded-lg bg-emerald-600 px-3.5 py-2 text-xs font-semibold text-white shadow-sm hover:bg-emerald-700 transition-colors"
            >
              Student Leave Approvals
            </Link>
            <Link
              href="/students"
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
            >
              View Students <ArrowRight className="ml-2 inline size-4" />
            </Link>
          </div>
        }
      />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {(d?.stats ?? []).map((s, i) => (
          <Card key={s.label} className={`rise delay-${i + 1} p-5`}>
            <div className="flex items-start justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {s.label}
              </span>
              <Activity className="size-4 text-primary/60" />
            </div>
            <div className="mt-4 display text-3xl font-semibold">{s.value}</div>
            <div
              className={`mt-2 text-xs ${s.tone === "critical" ? "text-rose-700" : "text-emerald-700"}`}
            >
              {s.change}
            </div>
          </Card>
        ))}
      </div>
      <div className="mt-6 grid gap-6 xl:grid-cols-[1.3fr_.7fr]">
        <Card className="p-6">
          <h2 className="display text-xl font-semibold mb-4">
            Recent Activities
          </h2>
          <div className="mt-5 divide-y divide-border">
            {(d?.activities ?? []).map((a, i) => (
              <div
                key={i}
                className="flex gap-4 py-4 first:pt-0 last:pb-0 rise"
                style={{ animationDelay: `${i * 60}ms` }}
              >
                <div className="mt-1 size-2 shrink-0 rounded-full bg-accent" />
                <div className="min-w-0 flex-1">
                  <div className="text-sm font-medium">{a.title}</div>
                  <div className="mt-1 text-xs text-muted-foreground">
                    {a.detail}
                  </div>
                </div>
                <time className="shrink-0 text-xs text-muted-foreground">
                  {a.time}
                </time>
              </div>
            ))}
          </div>
        </Card>
        <Card className="p-6">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="display text-xl font-semibold">Attendance trend</h2>
            <span className="text-xs text-muted-foreground">This term</span>
          </div>
          <div className="flex h-28 items-end gap-2">
            {(analytics.data?.attendanceTrend ?? []).map((x, i) => (
              <div
                key={x.label}
                className="group flex flex-1 flex-col items-center gap-2"
              >
                <div
                  className="w-full rounded-t bg-primary/75 transition-all group-hover:bg-accent"
                  style={{ height: `${Math.max(12, x.value)}%` }}
                />
                <span className="text-[10px] text-muted-foreground">
                  {x.label}
                </span>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </>
  );
}
function DashboardRouter() {
  const q = useGetDashboard();
  const analytics = useGetDashboardAnalytics();
  if (q.isLoading)
    return (
      <>
        <PageHead eyebrow="Overview" title="Your day, in view." />
        <SkeletonRows count={5} />
      </>
    );
  if (q.isError) return <ErrorState retry={() => q.refetch()} />;
  const d = q.data;
  if (d?.role === "admin" || d?.role === "staff")
    return <AdminDashboard d={d} analytics={analytics} />;
  if (d?.role === "faculty")
    return <FacultyDashboard d={d} analytics={analytics} />;
  return <StudentDashboard d={d} analytics={analytics} />;
}

function Students() {
  const me = useGetMe();
  const isAdmin = me.data?.role === "admin";
  const [search, setSearch] = useState("");
  const [modal, setModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [viewing, setViewing] = useState(null);
  const q = useListStudents(search ? { search } : undefined);
  const qc = useQueryClient();
  const create = useCreateStudent();
  const update = useUpdateStudent();
  const remove = useDeleteStudent();
  const list = q.data ?? [];
  const submit = (e) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const base = {
      studentId: String(fd.get("studentId")),
      name: String(fd.get("name")),
      email: String(fd.get("email")),
      phone: String(fd.get("phone") ?? ""),
      department: String(fd.get("department")),
      course: String(fd.get("course")),
      year: Number(fd.get("year")),
      section: String(fd.get("section")),
    };
    if (editing)
      update.mutate(
        {
          id: editing.id,
          data: {
            name: base.name,
            email: base.email,
            phone: base.phone,
            department: base.department,
            year: base.year,
            section: base.section,
          },
        },
        {
          onSuccess: () => {
            qc.invalidateQueries({
              queryKey: getListStudentsQueryKey(
                search ? { search } : undefined,
              ),
            });
            setModal(false);
          },
        },
      );
    else
      create.mutate(
        { data: base },
        {
          onSuccess: () => {
            qc.invalidateQueries({
              queryKey: getListStudentsQueryKey(
                search ? { search } : undefined,
              ),
            });
            setModal(false);
          },
        },
      );
  };
  return (
    <>
      <PageHead
        eyebrow="Directory"
        title="Students"
        description="Search, review, and maintain the student register."
        action={
          isAdmin && (
            <button
              onClick={() => {
                setEditing(null);
                setModal(true);
              }}
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
              data-testid="button-create-student"
            >
              <Plus className="mr-2 inline size-4" /> Add student
            </button>
          )
        }
      />
      <Card>
        <div className="flex flex-col justify-between gap-3 border-b border-border p-4 sm:flex-row sm:items-center">
          <div className="text-sm font-semibold">
            {list.length} registered students
          </div>
          <SearchBox
            value={search}
            onChange={setSearch}
            placeholder="Search by name or ID"
          />
        </div>
        {q.isLoading ? (
          <div className="p-4">
            <SkeletonRows />
          </div>
        ) : q.isError ? (
          <div className="p-4">
            <ErrorState retry={() => q.refetch()} />
          </div>
        ) : list.length === 0 ? (
          <Empty label="students" />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-muted/50 text-[11px] uppercase tracking-wider text-muted-foreground">
                <tr>
                  <th className="px-5 py-3">Student</th>
                  <th className="px-5 py-3">Department</th>
                  <th className="px-5 py-3">Year</th>
                  <th className="px-5 py-3">Attendance</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {list.map((s) => (
                  <tr
                    key={s.id}
                    className="transition-colors hover:bg-muted/30"
                    data-testid={`row-student-${s.id}`}
                  >
                    <td className="px-5 py-4">
                      <div className="font-semibold">{s.name}</div>
                      <div className="mt-0.5 text-xs text-muted-foreground">
                        {s.studentId} · {s.email}
                      </div>
                    </td>
                    <td className="px-5 py-4">{s.department}</td>
                    <td className="px-5 py-4">
                      {s.year} · {s.section}
                    </td>
                    <td className="px-5 py-4">
                      <div className="w-24">
                        <div className="mb-1 text-xs font-semibold">
                          {s.attendance}%
                        </div>
                        <div className="metric-bar">
                          <span style={{ width: `${s.attendance}%` }} />
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <Status
                        tone={s.status === "active" ? "positive" : "neutral"}
                      >
                        {s.status}
                      </Status>
                    </td>
                    <td className="px-5 py-4 text-right">
                      <button
                        onClick={() => setViewing(s)}
                        className="mr-3 text-xs font-semibold text-primary"
                        data-testid={`button-view-student-${s.id}`}
                      >
                        View Details
                      </button>
                      {isAdmin && (
                        <>
                          <button
                            onClick={() => {
                              setEditing(s);
                              setModal(true);
                            }}
                            className="mr-3 text-xs font-semibold text-primary"
                            data-testid={`button-edit-student-${s.id}`}
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => {
                              if (confirm(`Delete ${s.name}?`))
                                remove.mutate(
                                  { id: s.id },
                                  {
                                    onSuccess: () =>
                                      qc.invalidateQueries({
                                        queryKey: getListStudentsQueryKey(),
                                      }),
                                  },
                                );
                            }}
                            className="text-xs font-semibold text-destructive"
                            data-testid={`button-delete-student-${s.id}`}
                          >
                            Delete
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
      {modal && (
        <Modal
          title={editing ? "Edit student" : "Add student"}
          onClose={() => setModal(false)}
        >
          <form onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
            <Field
              label="Student ID"
              name="studentId"
              defaultValue={editing?.studentId ?? ""}
            />
            <Field
              label="Full name"
              name="name"
              defaultValue={editing?.name ?? ""}
            />
            <Field
              label="Email"
              name="email"
              type="email"
              defaultValue={editing?.email ?? ""}
            />
            <Field
              label="Phone"
              name="phone"
              required={false}
              defaultValue={editing?.phone ?? ""}
            />
            <Field
              label="Department"
              name="department"
              defaultValue={editing?.department ?? ""}
            />
            <Field
              label="Course"
              name="course"
              defaultValue={editing?.course ?? ""}
            />
            <Field
              label="Year"
              name="year"
              type="number"
              defaultValue={editing?.year ?? 1}
            />
            <Field
              label="Section"
              name="section"
              defaultValue={editing?.section ?? ""}
            />
            <button
              type="submit"
              disabled={create.isPending || update.isPending}
              className="mt-2 rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground sm:col-span-2"
              data-testid="button-save-student"
            >
              {create.isPending || update.isPending
                ? "Saving…"
                : editing
                  ? "Save changes"
                  : "Create student"}
            </button>
          </form>
        </Modal>
      )}
      {viewing && (
        <StudentProfileModal
          student={viewing}
          onClose={() => setViewing(null)}
        />
      )}
    </>
  );
}

function Faculty() {
  const me = useGetMe();
  const isAdmin = me.data?.role === "admin";
  const [search, setSearch] = useState("");
  const [modal, setModal] = useState(false);
  const q = useListFaculty(search ? { search } : undefined);
  const create = useCreateFaculty();
  const qc = useQueryClient();
  const list = q.data ?? [];
  const submit = (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    create.mutate(
      {
        data: {
          facultyId: String(f.get("facultyId")),
          name: String(f.get("name")),
          email: String(f.get("email")),
          department: String(f.get("department")),
          designation: String(f.get("designation")),
          qualification: String(f.get("qualification") ?? ""),
        },
      },
      {
        onSuccess: () => {
          qc.invalidateQueries({ queryKey: getListFacultyQueryKey() });
          setModal(false);
        },
      },
    );
  };
  return (
    <>
      <PageHead
        eyebrow="Directory"
        title="Faculty"
        description="The people, disciplines, and expertise powering every class."
        action={
          isAdmin && (
            <button
              onClick={() => setModal(true)}
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
              data-testid="button-create-faculty"
            >
              <Plus className="mr-2 inline size-4" /> Add faculty
            </button>
          )
        }
      />
      <Card>
        <div className="flex flex-col justify-between gap-3 border-b border-border p-4 sm:flex-row sm:items-center">
          <div className="text-sm font-semibold">
            {list.length} faculty members
          </div>
          <SearchBox
            value={search}
            onChange={setSearch}
            placeholder="Search faculty"
          />
        </div>
        {q.isLoading ? (
          <div className="p-4">
            <SkeletonRows />
          </div>
        ) : q.isError ? (
          <div className="p-4">
            <ErrorState retry={() => q.refetch()} />
          </div>
        ) : list.length === 0 ? (
          <Empty label="faculty records" />
        ) : (
          <div className="grid gap-px bg-border sm:grid-cols-2 lg:grid-cols-3">
            {list.map((f, i) => (
              <div
                key={f.id}
                className={`bg-card p-5 rise delay-${(i % 4) + 1}`}
                data-testid={`card-faculty-${f.id}`}
              >
                <div className="flex items-start justify-between">
                  <div className="grid size-11 place-items-center rounded-xl bg-accent/30 text-sm font-bold text-primary">
                    {f.name
                      .split(" ")
                      .map((x) => x[0])
                      .join("")
                      .slice(0, 2)}
                  </div>
                  <Status tone={f.status === "active" ? "positive" : "neutral"}>
                    {f.status}
                  </Status>
                </div>
                <h3 className="mt-5 font-semibold">{f.name}</h3>
                <p className="mt-1 text-xs text-muted-foreground">
                  {f.designation} · {f.department}
                </p>
                <div className="mt-5 border-t border-border pt-4 text-xs text-muted-foreground">
                  {f.email}
                  <div className="mt-2 flex flex-wrap gap-1.5">
                    {f.subjects?.slice(0, 3).map((x) => (
                      <span key={x} className="rounded bg-muted px-2 py-1">
                        {x}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>
      {modal && (
        <Modal title="Add faculty" onClose={() => setModal(false)}>
          <form onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
            <Field label="Faculty ID" name="facultyId" />
            <Field label="Full name" name="name" />
            <Field label="Email" name="email" type="email" />
            <Field label="Department" name="department" />
            <Field label="Designation" name="designation" />
            <Field
              label="Qualification"
              name="qualification"
              required={false}
            />
            <button
              className="mt-2 rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground sm:col-span-2"
              disabled={create.isPending}
              data-testid="button-save-faculty"
            >
              {create.isPending ? "Saving…" : "Create faculty"}
            </button>
          </form>
        </Modal>
      )}
    </>
  );
}

function Departments() {
  const me = useGetMe();
  const isAdmin = me.data?.role === "admin";
  const [modal, setModal] = useState(false);
  const q = useListDepartments();
  const create = useCreateDepartment();
  const qc = useQueryClient();
  const list = q.data ?? [];
  const submit = (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    create.mutate(
      {
        data: {
          name: String(f.get("name")),
          code: String(f.get("code")),
          hod: String(f.get("hod") ?? ""),
          description: String(f.get("description") ?? ""),
        },
      },
      {
        onSuccess: () => {
          qc.invalidateQueries({ queryKey: getListDepartmentsQueryKey() });
          setModal(false);
        },
      },
    );
  };
  return (
    <>
      <PageHead
        eyebrow="Structure"
        title="Departments"
        description="Organize people and programs around the disciplines they belong to."
        action={
          isAdmin && (
            <button
              onClick={() => setModal(true)}
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
              data-testid="button-create-department"
            >
              <Plus className="mr-2 inline size-4" /> Add department
            </button>
          )
        }
      />
      {q.isLoading ? (
        <SkeletonRows count={6} />
      ) : q.isError ? (
        <ErrorState retry={() => q.refetch()} />
      ) : list.length === 0 ? (
        <Card>
          <Empty label="departments" />
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {list.map((d, i) => (
            <Card
              key={d.id}
              className={`relative overflow-hidden p-6 rise delay-${(i % 4) + 1}`}
            >
              <div className="absolute right-0 top-0 h-20 w-20 rounded-bl-full bg-accent/20" />
              <div className="flex items-center justify-between">
                <span className="rounded bg-primary px-2 py-1 text-[11px] font-bold text-primary-foreground">
                  {d.code}
                </span>
                <ChevronRight className="size-4 text-muted-foreground" />
              </div>
              <h2 className="display mt-6 text-2xl font-semibold">{d.name}</h2>
              <p className="mt-2 min-h-12 text-sm leading-6 text-muted-foreground">
                {d.description ||
                  "Department overview and academic operations."}
              </p>
              <div className="mt-5 grid grid-cols-2 border-t border-border pt-4">
                <div>
                  <div className="text-xl font-semibold">{d.students}</div>
                  <div className="text-xs text-muted-foreground">students</div>
                </div>
                <div>
                  <div className="text-xl font-semibold">{d.faculty}</div>
                  <div className="text-xs text-muted-foreground">faculty</div>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
      {modal && (
        <Modal title="Add department" onClose={() => setModal(false)}>
          <form onSubmit={submit} className="grid gap-4">
            <Field label="Department name" name="name" />
            <Field label="Short code" name="code" />
            <Field label="Head of department" name="hod" required={false} />
            <label className="text-sm">
              <span className="mb-1.5 block text-xs font-semibold text-muted-foreground">
                Description
              </span>
              <textarea
                name="description"
                className="min-h-24 w-full rounded-lg border border-input bg-background p-3 text-sm outline-none"
                data-testid="input-description"
              />
            </label>
            <button
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
              data-testid="button-save-department"
            >
              Create department
            </button>
          </form>
        </Modal>
      )}
    </>
  );
}

function Subjects() {
  const me = useGetMe();
  const role = me.data?.role || "student";
  const userDept = me.data?.department || "CSE";
  const userSem = me.data?.semester || 1;
  const [selectedSemester, setSelectedSemester] = useState(role === "student" ? String(userSem) : "all");
  const [selectedDept, setSelectedDept] = useState(role === "student" ? userDept : "all");
  const [search, setSearch] = useState("");

  useEffect(() => {
    if (role === "student") {
      if (me.data?.department) setSelectedDept(me.data.department);
      if (me.data?.semester) setSelectedSemester(String(me.data.semester));
    }
  }, [me.data?.department, me.data?.semester, role]);

  const q = useListSubjects();
  const rawList = q.data ?? [];

  const filtered = rawList.filter((s) => {
    // If faculty/teacher, strictly show ONLY their assigned subjects and hide unrelated faculty details
    if (role === "faculty") {
      const myUsername = (me.data?.username || "").toLowerCase().trim();
      const myName = (me.data?.name || "").toLowerCase().trim();
      const sTeacherUser = (s.facultyUsername || "").toLowerCase().trim();
      const sTeacherName = (s.faculty || "").toLowerCase().trim();
      const isAssigned =
        (sTeacherUser && sTeacherUser === myUsername) ||
        (sTeacherName && myName && (sTeacherName.includes(myName) || myName.includes(sTeacherName)));
      if (!isAssigned) return false;
    }

    if (selectedDept !== "all" && s.department && s.department.toUpperCase() !== selectedDept.toUpperCase()) {
      return false;
    }
    if (selectedSemester !== "all" && s.semester !== Number(selectedSemester)) {
      return false;
    }
    if (search.trim()) {
      const qLower = search.toLowerCase();
      const matchCode = s.code?.toLowerCase().includes(qLower);
      const matchName = s.name?.toLowerCase().includes(qLower);
      const matchTeacher = (s.faculty || "").toLowerCase().includes(qLower);
      if (!matchCode && !matchName && !matchTeacher) return false;
    }
    return true;
  });

  return (
    <>
      <PageHead
        eyebrow={role === "faculty" ? "Faculty Academic Portfolio" : role === "student" ? "Academic Portal" : "Curriculum Directory"}
        title={role === "faculty" ? "My Assigned Teaching Subjects" : role === "student" ? "My Enrolled Subjects & Faculty" : "Engineering Subjects & Faculty"}
        description={
          role === "faculty"
            ? "Review the specific subjects and laboratory courses assigned to your teaching schedule."
            : "View 4-year engineering curriculum across all 8 semesters with dedicated subject teachers and course details."
        }
      />

      {/* Controls & Semester Navigator */}
      <Card className="mb-6 p-5">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          {/* Search */}
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
            <input
              type="text"
              placeholder={role === "faculty" ? "Search your assigned subjects…" : "Search by subject code, title, or teacher name…"}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full rounded-lg border border-border bg-background py-2 pl-10 pr-4 text-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
            />
          </div>

          {/* Department Filter (Admin only) */}
          {role === "admin" && (
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold text-muted-foreground uppercase">Dept:</span>
              <select
                value={selectedDept}
                onChange={(e) => setSelectedDept(e.target.value)}
                className="rounded-lg border border-border bg-background px-3 py-2 text-sm font-medium focus:border-primary focus:outline-none"
              >
                <option value="all">All Departments</option>
                <option value="CSE">CSE</option>
                <option value="ECE">ECE</option>
                <option value="MECH">MECH</option>
                <option value="CIVIL">CIVIL</option>
                <option value="EEE">EEE</option>
              </select>
            </div>
          )}
        </div>

        {/* Semester Filter Tabs (1 to 8) */}
        <div className="mt-4 border-t border-border pt-4">
          <div className="flex items-center justify-between gap-2 mb-2">
            <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Engineering Semester (4-Year B.Tech Program):
            </div>
            {role === "student" && me.data?.semester && (
              <span className="text-xs font-medium text-emerald-600 dark:text-emerald-400">
                You are enrolled in Semester {me.data.semester}
              </span>
            )}
          </div>
          <div className="flex flex-wrap gap-1.5">
            <button
              onClick={() => setSelectedSemester("all")}
              className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition-colors ${
                selectedSemester === "all"
                  ? "bg-primary text-primary-foreground"
                  : "bg-muted/60 text-muted-foreground hover:bg-muted"
              }`}
            >
              All Semesters (1-8)
            </button>
            {[1, 2, 3, 4, 5, 6, 7, 8].map((sem) => {
              const yr = Math.ceil(sem / 2);
              const isMySem = role === "student" && me.data?.semester === sem;
              return (
                <button
                  key={sem}
                  onClick={() => setSelectedSemester(String(sem))}
                  className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition-colors ${
                    selectedSemester === String(sem)
                      ? "bg-primary text-primary-foreground shadow-sm"
                      : "bg-muted/60 text-muted-foreground hover:bg-muted"
                  }`}
                >
                  Sem {sem} (Yr {yr}) {isMySem ? "★" : ""}
                </button>
              );
            })}
          </div>
        </div>
      </Card>

      {/* Curriculum Context Ribbon */}
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-primary/20 bg-primary/5 p-4">
        <div className="flex items-center gap-3">
          <div className="grid size-10 place-items-center rounded-xl bg-primary text-primary-foreground font-bold">
            <BookOpen className="size-5" />
          </div>
          <div>
            <div className="font-bold text-sm text-foreground flex items-center gap-2">
              <span>{selectedDept} · {selectedSemester === "all" ? "Full 8-Semester Curriculum" : `Semester ${selectedSemester} Courses`}</span>
              {selectedSemester !== "all" && (
                <span className="rounded bg-primary/10 px-2 py-0.5 text-xs font-semibold text-primary">
                  5 Core Subjects
                </span>
              )}
              {role === "student" && me.data?.semester && String(me.data.semester) === selectedSemester && (
                <span className="rounded bg-emerald-500/15 px-2 py-0.5 text-xs font-bold text-emerald-600 dark:text-emerald-400">
                  ★ Your Active Semester
                </span>
              )}
            </div>
            <p className="text-xs text-muted-foreground mt-0.5">
              {role === "faculty"
                ? "Showing strictly the single course assigned to you as dedicated professor."
                : selectedSemester === "all"
                ? "Every semester comprises strictly 5 department subjects, each taught by a dedicated professor."
                : `Semester ${selectedSemester} comprises 5 specialized ${selectedDept} subjects, each instructed by its dedicated professor.`}
            </p>
          </div>
        </div>
      </div>

      {/* Subjects Grid */}
      {q.isLoading ? (
        <Card className="p-6">
          <SkeletonRows count={4} />
        </Card>
      ) : q.isError ? (
        <Card className="p-6">
          <ErrorState retry={() => q.refetch()} />
        </Card>
      ) : filtered.length === 0 ? (
        <Card className="p-8 text-center">
          <BookOpen className="mx-auto size-10 text-muted-foreground/50 mb-3" />
          <div className="font-semibold text-base">
            {role === "faculty" ? "No assigned subjects found" : "No subjects found"}
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            {role === "faculty"
              ? "You do not have any teaching allocations matching this filter."
              : "Try adjusting your semester or department filter."}
          </p>
        </Card>
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
          {filtered.map((s, idx) => {
            const yr = s.year || Math.ceil((s.semester || 1) / 2);
            const phone = s.facultyPhone || "+1 (555) 011-2301";
            const email = s.facultyEmail || (s.facultyUsername ? `${s.facultyUsername}@meridian.edu` : "teacher@meridian.edu");
            const teacherName = s.faculty || "Faculty Assigned";
            const designation = s.facultyDesignation || "Professor";

            return (
              <Card
                key={s.id || idx}
                className="overflow-hidden border border-border p-0 transition-all hover:border-primary/40 hover:shadow-md"
                data-testid={`card-subject-${s.id}`}
              >
                {/* Header Banner */}
                <div className="border-b border-border bg-muted/40 p-4">
                  <div className="flex items-start justify-between gap-3">
                    <span className="grid size-11 shrink-0 place-items-center rounded-xl bg-primary font-mono text-xs font-bold text-primary-foreground shadow-sm">
                      {s.code}
                    </span>
                    <div className="flex flex-wrap items-center gap-1.5 justify-end">
                      <span className="rounded bg-accent/30 px-2 py-0.5 text-[11px] font-bold text-primary">
                        Sem {s.semester} · Yr {yr}
                      </span>
                      <span className="rounded bg-primary/10 px-2 py-0.5 text-[11px] font-bold text-primary">
                        {s.credits} Credits
                      </span>
                      <span className="rounded bg-muted px-2 py-0.5 text-[11px] font-bold text-muted-foreground uppercase">
                        {s.department}
                      </span>
                    </div>
                  </div>
                  <h3 className="mt-3 font-semibold text-base leading-snug">
                    {s.name}
                  </h3>
                </div>

                {/* Faculty Card Content */}
                {role === "faculty" ? (
                  /* Teacher View: No phone numbers or other faculty info, strictly course allocation details */
                  <div className="p-4 space-y-3">
                    <div className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">
                      Course Teaching Allocation
                    </div>
                    <div className="rounded-lg bg-muted/40 p-3 space-y-2 text-xs border border-border/50">
                      <div className="flex items-center justify-between">
                        <span className="text-muted-foreground">Department & Semester:</span>
                        <span className="font-semibold text-foreground">{s.department} · Semester {s.semester}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-muted-foreground">Curriculum Credits:</span>
                        <span className="font-semibold text-foreground">{s.credits} Credits ({s.hoursPerWeek || 4} hrs/week)</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-muted-foreground">Course Type:</span>
                        <span className="font-semibold capitalize text-foreground">{s.type || (s.code?.includes("-L") ? "Laboratory Practical" : "Theory Lecture")}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-muted-foreground">Teaching Status:</span>
                        <span className="font-medium text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
                          <CheckCircle2 className="size-3" /> Assigned Instructor
                        </span>
                      </div>
                    </div>
                    <div className="flex gap-2 pt-1">
                      <Link
                        to="/timetable"
                        className="flex-1 rounded-lg border border-primary/20 bg-primary/10 py-2 text-center text-xs font-semibold text-primary transition-colors hover:bg-primary/20 flex items-center justify-center gap-1.5"
                      >
                        <Clock3 className="size-3" /> View In My Timetable
                      </Link>
                    </div>
                  </div>
                ) : (
                  /* Student / Admin View: Teacher In Charge Card */
                  <div className="p-4 space-y-3">
                    <div className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">
                      Subject Faculty in Charge
                    </div>

                    <div className="flex items-center gap-3">
                      <div className="grid size-10 place-items-center rounded-full bg-gradient-to-br from-primary/20 to-accent/30 text-primary font-bold text-sm">
                        {teacherName.replace("Dr. ", "").charAt(0)}
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="font-semibold text-sm truncate text-foreground">
                          {teacherName}
                        </div>
                        <div className="text-xs text-muted-foreground truncate">
                          {designation}
                        </div>
                      </div>
                    </div>

                    {/* Teacher Contact Details */}
                    <div className="rounded-lg bg-muted/40 p-3 space-y-2 text-xs border border-border/50">
                      <div className="flex items-center justify-between gap-2">
                        <span className="text-muted-foreground flex items-center gap-1.5 shrink-0">
                          <Phone className="size-3.5 text-emerald-600 dark:text-emerald-400" /> Phone:
                        </span>
                        <a
                          href={`tel:${phone}`}
                          className="font-medium text-foreground hover:text-primary transition-colors hover:underline truncate"
                          title="Click to call teacher"
                        >
                          {phone}
                        </a>
                      </div>

                      <div className="flex items-center justify-between gap-2">
                        <span className="text-muted-foreground flex items-center gap-1.5 shrink-0">
                          <Mail className="size-3.5 text-blue-600 dark:text-blue-400" /> Email:
                        </span>
                        <a
                          href={`mailto:${email}`}
                          className="font-medium text-foreground hover:text-primary transition-colors hover:underline truncate"
                          title="Click to send email"
                        >
                          {email}
                        </a>
                      </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex gap-2 pt-1">
                      <a
                        href={`mailto:${email}`}
                        className="flex-1 rounded-lg border border-primary/20 bg-primary/10 py-2 text-center text-xs font-semibold text-primary transition-colors hover:bg-primary/20 flex items-center justify-center gap-1.5"
                      >
                        <Mail className="size-3" /> Email Professor
                      </a>
                      <a
                        href={`tel:${phone}`}
                        className="flex-1 rounded-lg border border-border bg-card py-2 text-center text-xs font-semibold text-foreground transition-colors hover:bg-muted flex items-center justify-center gap-1.5"
                      >
                        <Phone className="size-3" /> Call Faculty
                      </a>
                    </div>
                  </div>
                )}
              </Card>
            );
          })}
        </div>
      )}
    </>
  );
}

const TIMETABLE_PERIODS = [
  { period: 1, startTime: "09:00 AM", endTime: "09:45 AM", label: "P1" },
  { period: 2, startTime: "09:45 AM", endTime: "10:30 AM", label: "P2" },
  { period: 3, startTime: "10:45 AM", endTime: "11:30 AM", label: "P3" },
  { period: 4, startTime: "11:30 AM", endTime: "12:15 PM", label: "P4" },
  { period: 5, startTime: "01:00 PM", endTime: "01:45 PM", label: "P5" },
  { period: 6, startTime: "01:45 PM", endTime: "02:30 PM", label: "P6" },
  { period: 7, startTime: "02:30 PM", endTime: "03:15 PM", label: "P7" },
];

const TIMETABLE_DAYS = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];

function Timetable() {
  const me = useGetMe();
  const qc = useQueryClient();
  const role = me.data?.role || "student";
  const userDept = me.data?.department || "CSE";
  const userSem = me.data?.semester || 1;

  const [selectedDept, setSelectedDept] = useState(userDept);
  const [selectedSem, setSelectedSem] = useState(userSem);
  const [viewMode, setViewMode] = useState(role === "faculty" ? "teacher" : "department");
  const [targetTeacher, setTargetTeacher] = useState(
    role === "faculty" ? (me.data?.username || "teacher1cse") : "teacher1cse"
  );

  useEffect(() => {
    if (role === "student") {
      if (me.data?.department) setSelectedDept(me.data.department);
      if (me.data?.semester) setSelectedSem(me.data.semester);
    } else if (role === "faculty") {
      if (me.data?.username) setTargetTeacher(me.data.username);
      if (me.data?.department) setSelectedDept(me.data.department);
    }
  }, [me.data, role]);

  // Edit / Add Slot Modal State for Admin
  const [modalOpen, setModalOpen] = useState(false);
  const [slotData, setSlotData] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [toastMsg, setToastMsg] = useState("");

  const subjectsQuery = useListSubjects();
  const facultyQuery = useListFaculty();

  // Distinct subjects strictly enrolled for the selected department & semester (5 subjects)
  const currentSemesterSubjects = (subjectsQuery.data || []).filter(
    (s) => (s.department || "").toUpperCase() === (selectedDept || "").toUpperCase() && s.semester === Number(selectedSem)
  );

  // Student / Department timetable query
  const deptQuery = useQuery({
    queryKey: ["timetable", selectedDept, selectedSem],
    queryFn: () => customFetch(`/api/timetable?department=${selectedDept}&semester=${selectedSem}`),
    enabled: viewMode === "department" || role === "admin" || role === "student",
  });

  // Teacher timetable query
  const teacherQuery = useQuery({
    queryKey: ["timetable-teacher", targetTeacher],
    queryFn: () => customFetch(`/api/timetable/teacher?username=${targetTeacher}`),
    enabled: viewMode === "teacher" || role === "faculty",
  });

  // Map student/dept slots into day -> period dictionary for O(1) cell lookup
  const deptSlotMap = {};
  (deptQuery.data || []).forEach((slot) => {
    const key = `${slot.day}-${slot.period}`;
    deptSlotMap[key] = slot;
  });

  // Open modal to assign or edit slot
  const handleOpenSlotModal = (day, period, currentSlot = null) => {
    if (role !== "admin") return;
    const pMeta = TIMETABLE_PERIODS.find((p) => p.period === period);
    setSlotData(
      currentSlot || {
        department: selectedDept,
        semester: Number(selectedSem),
        section: "A",
        day,
        period,
        startTime: pMeta?.startTime || "09:00 AM",
        endTime: pMeta?.endTime || "09:45 AM",
        subjectCode: "",
        subjectName: "",
        facultyName: "",
        facultyUsername: "",
        room: "LH-101",
        type: "lecture",
      }
    );
    setModalOpen(true);
  };

  const handleSaveSlot = async (e) => {
    e.preventDefault();
    if (!slotData) return;
    setActionLoading(true);
    try {
      await customFetch("/api/timetable/slot", {
        method: "POST",
        body: JSON.stringify(slotData),
      });
      await qc.invalidateQueries({ queryKey: ["timetable"] });
      await qc.invalidateQueries({ queryKey: ["timetable-teacher"] });
      setToastMsg("Timetable slot saved successfully!");
      setModalOpen(false);
      setTimeout(() => setToastMsg(""), 3500);
    } catch (err) {
      alert(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  const handleDeleteSlot = async () => {
    if (!slotData) return;
    if (!confirm(`Are you sure you want to clear this ${slotData.day} Period ${slotData.period} slot?`)) return;
    setActionLoading(true);
    try {
      await customFetch(
        `/api/timetable/slot?department=${encodeURIComponent(slotData.department)}&semester=${slotData.semester}&day=${encodeURIComponent(slotData.day)}&period=${slotData.period}`,
        { method: "DELETE" }
      );
      await qc.invalidateQueries({ queryKey: ["timetable"] });
      await qc.invalidateQueries({ queryKey: ["timetable-teacher"] });
      setToastMsg("Timetable slot cleared!");
      setModalOpen(false);
      setTimeout(() => setToastMsg(""), 3500);
    } catch (err) {
      alert(err.message);
    } finally {
      setActionLoading(false);
    }
  };

  // Helper for slot type styling
  const getTypeBadge = (type) => {
    switch ((type || "").toLowerCase()) {
      case "lab":
        return "bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-500/30";
      case "tutorial":
        return "bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/30";
      case "activity":
        return "bg-purple-500/15 text-purple-600 dark:text-purple-400 border-purple-500/30";
      case "assessment":
        return "bg-rose-500/15 text-rose-600 dark:text-rose-400 border-rose-500/30";
      default:
        return "bg-primary/10 text-primary border-primary/20";
    }
  };

  // Determine current active day & period based on live time
  const currentDayName = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"][new Date().getDay()];

  return (
    <>
      {toastMsg && (
        <div className="fixed bottom-6 right-6 z-50 flex items-center gap-2 rounded-xl border border-emerald-500/30 bg-card p-4 shadow-xl text-emerald-600 dark:text-emerald-400 text-sm font-semibold">
          <Check className="size-4" /> {toastMsg}
        </div>
      )}

      <PageHead
        eyebrow="Academic Schedule"
        title={
          role === "faculty"
            ? "Faculty Teaching Schedule"
            : role === "student"
            ? "Weekly Class Timetable"
            : "Campus Timetable Management"
        }
        description="Official Monday to Saturday academic timetable (7 periods · 45 minutes each) with dedicated breaks."
      />

      {/* Control Toolbar */}
      <Card className="mb-6 p-5">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div className="flex flex-wrap items-center gap-3">
            {/* Admin View Mode Switcher */}
            {role === "admin" && (
              <div className="flex rounded-lg bg-muted p-1 text-xs font-semibold">
                <button
                  onClick={() => setViewMode("department")}
                  className={`rounded-md px-3 py-1.5 transition-colors ${
                    viewMode === "department" ? "bg-card text-foreground shadow-sm" : "text-muted-foreground hover:text-foreground"
                  }`}
                >
                  Department Class Grid
                </button>
                <button
                  onClick={() => setViewMode("teacher")}
                  className={`rounded-md px-3 py-1.5 transition-colors ${
                    viewMode === "teacher" ? "bg-card text-foreground shadow-sm" : "text-muted-foreground hover:text-foreground"
                  }`}
                >
                  Faculty Schedule Inspector
                </button>
              </div>
            )}

            {/* Department & Semester Selectors */}
            {viewMode === "department" && (
              <>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold text-muted-foreground uppercase">Dept:</span>
                  <select
                    value={selectedDept}
                    onChange={(e) => setSelectedDept(e.target.value)}
                    className="rounded-lg border border-border bg-background px-3 py-1.5 text-xs font-medium focus:border-primary focus:outline-none"
                  >
                    <option value="CSE">CSE (Computer Science)</option>
                    <option value="ECE">ECE (Electronics)</option>
                    <option value="MECH">MECH (Mechanical)</option>
                    <option value="CIVIL">CIVIL (Civil)</option>
                    <option value="EEE">EEE (Electrical)</option>
                  </select>
                </div>

                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold text-muted-foreground uppercase">Semester:</span>
                  <select
                    value={selectedSem}
                    onChange={(e) => setSelectedSem(Number(e.target.value))}
                    className="rounded-lg border border-border bg-background px-3 py-1.5 text-xs font-medium focus:border-primary focus:outline-none"
                  >
                    {[1, 2, 3, 4, 5, 6, 7, 8].map((sem) => (
                      <option key={sem} value={sem}>
                        Semester {sem} (Year {Math.ceil(sem / 2)}) {role === "student" && me.data?.semester === sem ? "★ Your Active Semester" : ""}
                      </option>
                    ))}
                  </select>
                </div>
              </>
            )}

            {/* Teacher Selector for Admin */}
            {viewMode === "teacher" && role === "admin" && (
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold text-muted-foreground uppercase">Faculty:</span>
                <select
                  value={targetTeacher}
                  onChange={(e) => setTargetTeacher(e.target.value)}
                  className="rounded-lg border border-border bg-background px-3 py-1.5 text-xs font-medium focus:border-primary focus:outline-none"
                >
                  {(facultyQuery.data || [
                    { username: "teacher1cse", name: "Dr. Alan Turing" },
                    { username: "teacher26cse", name: "Dr. Marvin Minsky" },
                    { username: "teacher1eee", name: "Dr. Michael Faraday" },
                  ]).map((f) => (
                    <option key={f.username || f.id} value={f.username || f.facultyId}>
                      {f.name} ({f.username || f.department})
                    </option>
                  ))}
                </select>
              </div>
            )}
          </div>

          {/* Quick Info / Print */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => window.print()}
              className="flex items-center gap-1.5 rounded-lg border border-border bg-card px-3 py-1.5 text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
            >
              <Download className="size-3.5" /> Print Timetable
            </button>
            {role === "admin" && viewMode === "department" && (
              <button
                onClick={() => handleOpenSlotModal("Monday", 1)}
                className="flex items-center gap-1.5 rounded-lg bg-primary px-3 py-1.5 text-xs font-semibold text-primary-foreground hover:opacity-90 transition-opacity"
              >
                <Plus className="size-3.5" /> Quick Assign Slot
              </button>
            )}
          </div>
        </div>

        {/* Schedule Timing Summary Bar */}
        <div className="mt-4 flex flex-wrap items-center justify-between border-t border-border pt-4 text-xs text-muted-foreground gap-2">
          <div className="flex items-center gap-4">
            <span className="font-semibold text-foreground">
              {viewMode === "department"
                ? `${selectedDept} Department · Semester ${selectedSem} · Section A`
                : `Faculty Teaching Schedule: ${teacherQuery.data?.teacherUsername || targetTeacher}`}
            </span>
            <span>•</span>
            <span>6 Days (Mon–Sat)</span>
            <span>•</span>
            <span>7 Periods/Day (45 min each)</span>
            {viewMode === "department" && (
              <>
                <span>•</span>
                <span className="font-semibold text-primary">Strictly {currentSemesterSubjects.length || 5} Enrolled Subjects</span>
              </>
            )}
          </div>
          <div className="flex items-center gap-3">
            <span className="rounded bg-amber-500/10 px-2 py-0.5 font-medium text-amber-600 dark:text-amber-400">
              Break: 10:30 – 10:45 AM
            </span>
            <span className="rounded bg-emerald-500/10 px-2 py-0.5 font-medium text-emerald-600 dark:text-emerald-400">
              Lunch: 12:15 – 01:00 PM
            </span>
          </div>
        </div>
      </Card>

      {/* 5 Enrolled Subjects Pill Ribbon */}
      {viewMode === "department" && currentSemesterSubjects.length > 0 && (
        <Card className="mb-6 p-4 border border-border bg-card">
          <div className="flex items-center justify-between gap-2 mb-2.5">
            <div className="text-xs font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-2">
              <BookOpen className="size-3.5 text-primary" />
              <span>{selectedDept} Semester {selectedSem} Curriculum ({currentSemesterSubjects.length} Subjects):</span>
            </div>
            <span className="text-[11px] text-muted-foreground">
              This timetable consists strictly of these {currentSemesterSubjects.length} subjects &amp; dedicated professors
            </span>
          </div>
          <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-5">
            {currentSemesterSubjects.map((sub) => (
              <div key={sub.code} className="rounded-lg border border-border/70 bg-muted/30 p-2.5 text-xs transition-all hover:border-primary/40 hover:bg-muted/50">
                <div className="flex items-center justify-between gap-1 mb-1">
                  <span className="font-mono font-bold text-primary">{sub.code}</span>
                  <span className="text-[10px] text-muted-foreground">{sub.credits} Credits</span>
                </div>
                <div className="font-semibold text-foreground truncate" title={sub.name}>{sub.name}</div>
                <div className="text-[11px] text-muted-foreground mt-1 flex items-center gap-1 truncate" title={sub.faculty}>
                  <span>👨‍🏫 {sub.faculty}</span>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* TEACHER TIMETABLE VIEW (Dedicated empty space for unallocated slots) */}
      {viewMode === "teacher" && (
        <div className="space-y-6">
          {/* Teacher Summary Metrics */}
          {teacherQuery.data && (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <Card className="p-4 flex items-center gap-3">
                <div className="grid size-10 place-items-center rounded-xl bg-primary/10 text-primary">
                  <Award className="size-5" />
                </div>
                <div>
                  <div className="text-2xl font-bold">{teacherQuery.data.allocatedSlots?.length || 0}</div>
                  <div className="text-xs text-muted-foreground">Allocated Teaching Periods / Week</div>
                </div>
              </Card>

              <Card className="p-4 flex items-center gap-3">
                <div className="grid size-10 place-items-center rounded-xl bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
                  <Clock3 className="size-5" />
                </div>
                <div>
                  <div className="text-2xl font-bold">
                    {Math.round(((teacherQuery.data.allocatedSlots?.length || 0) * 45) / 60 * 10) / 10} hrs
                  </div>
                  <div className="text-xs text-muted-foreground">Weekly Direct Student Contact Time</div>
                </div>
              </Card>

              <Card className="p-4 flex items-center gap-3">
                <div className="grid size-10 place-items-center rounded-xl bg-muted text-muted-foreground">
                  <BookOpen className="size-5" />
                </div>
                <div>
                  <div className="text-2xl font-bold">
                    {42 - (teacherQuery.data.allocatedSlots?.length || 0)}
                  </div>
                  <div className="text-xs text-muted-foreground">Free Periods / Office Hours (Open Space)</div>
                </div>
              </Card>
            </div>
          )}

          {/* Teacher Weekly 6x7 Grid */}
          <div className="overflow-x-auto rounded-2xl border border-border bg-card shadow-sm">
            <table className="w-full border-collapse text-left text-xs min-w-[950px]">
              <thead>
                <tr className="border-b border-border bg-muted/60 text-muted-foreground font-semibold">
                  <th className="p-3.5 w-28 text-center uppercase tracking-wider">Day / Period</th>
                  {TIMETABLE_PERIODS.map((p) => (
                    <th key={p.period} className="p-3 text-center border-l border-border/60">
                      <div className="font-bold text-foreground text-sm">{p.label}</div>
                      <div className="text-[10px] text-muted-foreground font-normal mt-0.5">
                        {p.startTime} – {p.endTime}
                      </div>
                      <div className="text-[9px] text-primary font-mono mt-0.5">45 min</div>
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {TIMETABLE_DAYS.map((day) => {
                  const daySlots = teacherQuery.data?.grid?.[day] || [];
                  const isToday = day === currentDayName;

                  return (
                    <tr key={day} className={`transition-colors ${isToday ? "bg-primary/5" : "hover:bg-muted/20"}`}>
                      {/* Day Label */}
                      <td className="p-3 text-center font-bold text-sm bg-muted/30 border-r border-border">
                        <div className="text-foreground">{day}</div>
                        {isToday && (
                          <span className="mt-1 inline-block rounded-full bg-primary/20 px-2 py-0.5 text-[9px] font-bold text-primary">
                            Today
                          </span>
                        )}
                      </td>

                      {/* 7 Period Columns */}
                      {TIMETABLE_PERIODS.map((p) => {
                        const cell = daySlots[p.period - 1];
                        const isAllocated = cell?.allocated && cell?.slot;
                        const slot = cell?.slot;

                        return (
                          <td
                            key={p.period}
                            className="p-2.5 border-l border-border/60 align-top h-28 w-[13.5%]"
                          >
                            {isAllocated ? (
                              /* ALLOCATED CLASS CARD */
                              <div className="h-full rounded-xl border border-primary/25 bg-card p-2.5 shadow-sm transition-all hover:border-primary hover:shadow-md flex flex-col justify-between">
                                <div>
                                  <div className="flex items-center justify-between gap-1 mb-1">
                                    <span className="font-mono font-bold text-[11px] text-primary">
                                      {slot.subjectCode}
                                    </span>
                                    <span
                                      className={`rounded border px-1.5 py-0.2 text-[9px] font-bold uppercase ${getTypeBadge(
                                        slot.type
                                      )}`}
                                    >
                                      {slot.type || "lecture"}
                                    </span>
                                  </div>
                                  <div className="font-semibold text-[11px] text-foreground line-clamp-2 leading-tight">
                                    {slot.subjectName}
                                  </div>
                                </div>

                                <div className="mt-2 pt-2 border-t border-border/60 flex items-center justify-between text-[10px] text-muted-foreground">
                                  <span className="font-medium text-foreground">
                                    {slot.department} Sem {slot.semester}
                                  </span>
                                  <span className="rounded bg-muted px-1.5 py-0.5 font-mono text-[9px]">
                                    {slot.room}
                                  </span>
                                </div>
                              </div>
                            ) : (
                              /* UNALLOCATED: EMPTY SPACE OCCUPIED AS REQUESTED */
                              <div className="h-full rounded-xl border border-dashed border-border/80 bg-muted/20 p-2 flex flex-col items-center justify-center text-center transition-colors hover:bg-muted/30">
                                <span className="text-[10px] text-muted-foreground/60 font-medium">
                                  — Free Period —
                                </span>
                                <span className="text-[9px] text-muted-foreground/40 mt-0.5">
                                  No class allocated
                                </span>
                              </div>
                            )}
                          </td>
                        );
                      })}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* DEPARTMENT / STUDENT TIMETABLE VIEW */}
      {viewMode === "department" && (
        <div className="overflow-x-auto rounded-2xl border border-border bg-card shadow-sm">
          <table className="w-full border-collapse text-left text-xs min-w-[950px]">
            <thead>
              <tr className="border-b border-border bg-muted/60 text-muted-foreground font-semibold">
                <th className="p-3.5 w-28 text-center uppercase tracking-wider">Day / Period</th>
                {TIMETABLE_PERIODS.map((p) => (
                  <th key={p.period} className="p-3 text-center border-l border-border/60">
                    <div className="font-bold text-foreground text-sm">{p.label}</div>
                    <div className="text-[10px] text-muted-foreground font-normal mt-0.5">
                      {p.startTime} – {p.endTime}
                    </div>
                    <div className="text-[9px] text-primary font-mono mt-0.5">45 min</div>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-border/60">
              {TIMETABLE_DAYS.map((day) => {
                const isToday = day === currentDayName;

                return (
                  <tr key={day} className={`transition-colors ${isToday ? "bg-primary/5" : "hover:bg-muted/20"}`}>
                    {/* Day Label */}
                    <td className="p-3 text-center font-bold text-sm bg-muted/30 border-r border-border">
                      <div className="text-foreground">{day}</div>
                      {isToday && (
                        <span className="mt-1 inline-block rounded-full bg-primary/20 px-2 py-0.5 text-[9px] font-bold text-primary">
                          Today
                        </span>
                      )}
                    </td>

                    {/* 7 Period Columns */}
                    {TIMETABLE_PERIODS.map((p) => {
                      const slotKey = `${day}-${p.period}`;
                      const slot = deptSlotMap[slotKey];

                      return (
                        <td
                          key={p.period}
                          className="p-2.5 border-l border-border/60 align-top h-28 w-[13.5%] group relative"
                        >
                          {slot ? (
                            /* Occupied Subject Slot */
                            <div
                              onClick={() => role === "admin" && handleOpenSlotModal(day, p.period, slot)}
                              className={`h-full rounded-xl border border-border bg-card p-2.5 shadow-sm transition-all flex flex-col justify-between ${
                                role === "admin"
                                  ? "cursor-pointer hover:border-primary hover:shadow-md"
                                  : "hover:border-primary/40"
                              }`}
                            >
                              <div>
                                <div className="flex items-center justify-between gap-1 mb-1">
                                  <span className="font-mono font-bold text-[11px] text-primary">
                                    {slot.subjectCode}
                                  </span>
                                  <span
                                    className={`rounded border px-1.5 py-0.2 text-[9px] font-bold uppercase ${getTypeBadge(
                                      slot.type
                                    )}`}
                                  >
                                    {slot.type || "lecture"}
                                  </span>
                                </div>
                                <div className="font-semibold text-[11px] text-foreground line-clamp-2 leading-tight">
                                  {slot.subjectName}
                                </div>
                              </div>

                              <div className="mt-2 pt-2 border-t border-border/60">
                                <div className="text-[10px] text-muted-foreground truncate font-medium">
                                  {slot.facultyName || "Faculty Assigned"}
                                </div>
                                <div className="flex items-center justify-between mt-1 text-[9px]">
                                  <span className="font-mono text-muted-foreground">{slot.room}</span>
                                  {role === "admin" && (
                                    <span className="text-primary font-bold opacity-0 group-hover:opacity-100 transition-opacity">
                                      Edit ✎
                                    </span>
                                  )}
                                </div>
                              </div>
                            </div>
                          ) : (
                            /* Empty Slot */
                            <div
                              onClick={() => role === "admin" && handleOpenSlotModal(day, p.period)}
                              className={`h-full rounded-xl border border-dashed border-border bg-muted/10 p-2 flex flex-col items-center justify-center text-center transition-colors ${
                                role === "admin"
                                  ? "cursor-pointer hover:border-primary hover:bg-primary/5"
                                  : ""
                              }`}
                            >
                              {role === "admin" ? (
                                <>
                                  <Plus className="size-4 text-muted-foreground group-hover:text-primary transition-colors" />
                                  <span className="text-[10px] text-muted-foreground group-hover:text-primary mt-1 font-medium">
                                    + Assign Slot
                                  </span>
                                </>
                              ) : (
                                <span className="text-[10px] text-muted-foreground/50">— Free Period —</span>
                              )}
                            </div>
                          )}
                        </td>
                      );
                    })}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* ADMIN EDIT / ASSIGN SLOT MODAL */}
      {modalOpen && slotData && (
        <Modal
          title={slotData.id ? "Edit Timetable Slot" : "Assign Timetable Slot"}
          subtitle={`${slotData.department} Semester ${slotData.semester} · ${slotData.day} Period ${slotData.period} (${slotData.startTime} - ${slotData.endTime})`}
          onClose={() => setModalOpen(false)}
        >
          <form onSubmit={handleSaveSlot} className="space-y-4 pt-2">
            {/* Quick Pick from Existing Curriculum Subjects */}
            <div>
              <label className="block text-xs font-semibold uppercase text-muted-foreground mb-1">
                Select Curriculum Subject:
              </label>
              <select
                onChange={(e) => {
                  const sCode = e.target.value;
                  const found = (subjectsQuery.data || []).find((s) => s.code === sCode);
                  if (found) {
                    setSlotData((prev) => ({
                      ...prev,
                      subjectCode: found.code,
                      subjectName: found.name,
                      facultyName: found.faculty || prev.facultyName,
                      facultyUsername: found.facultyUsername || prev.facultyUsername,
                      type: found.code.includes("-L") ? "lab" : "lecture",
                    }));
                  }
                }}
                className="w-full rounded-lg border border-border bg-background px-3 py-2 text-sm focus:border-primary focus:outline-none"
              >
                <option value="">-- Choose Subject from Department Curriculum --</option>
                {(subjectsQuery.data || [])
                  .filter((s) => s.department === slotData.department)
                  .map((s) => (
                    <option key={s.id || s.code} value={s.code}>
                      [{s.code}] {s.name} (Sem {s.semester})
                    </option>
                  ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-muted-foreground mb-1">
                  Subject Code *
                </label>
                <input
                  type="text"
                  required
                  value={slotData.subjectCode}
                  onChange={(e) => setSlotData({ ...slotData, subjectCode: e.target.value })}
                  placeholder="e.g. CS101"
                  className="w-full rounded-lg border border-border bg-background px-3 py-2 text-sm focus:border-primary focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-muted-foreground mb-1">
                  Subject Title *
                </label>
                <input
                  type="text"
                  required
                  value={slotData.subjectName}
                  onChange={(e) => setSlotData({ ...slotData, subjectName: e.target.value })}
                  placeholder="e.g. Problem Solving & Python"
                  className="w-full rounded-lg border border-border bg-background px-3 py-2 text-sm focus:border-primary focus:outline-none"
                />
              </div>
            </div>

            {/* Quick Pick Faculty */}
            <div>
              <label className="block text-xs font-semibold uppercase text-muted-foreground mb-1">
                Assign Subject Faculty:
              </label>
              <select
                onChange={(e) => {
                  const fUser = e.target.value;
                  const found = (facultyQuery.data || [
                    { username: "teacher1cse", name: "Dr. Alan Turing" },
                    { username: "teacher1eee", name: "Dr. Michael Faraday" },
                  ]).find((f) => (f.username || f.facultyId) === fUser);
                  if (found) {
                    setSlotData((prev) => ({
                      ...prev,
                      facultyUsername: found.username || found.facultyId,
                      facultyName: found.name,
                    }));
                  }
                }}
                className="w-full rounded-lg border border-border bg-background px-3 py-2 text-sm focus:border-primary focus:outline-none"
              >
                <option value="">-- Choose Faculty Instructor --</option>
                {(facultyQuery.data || [
                  { username: "teacher1cse", name: "Dr. Alan Turing", department: "CSE" },
                  { username: "teacher1eee", name: "Dr. Michael Faraday", department: "EEE" },
                ]).map((f) => (
                  <option key={f.username || f.facultyId} value={f.username || f.facultyId}>
                    {f.name} ({f.department || "Faculty"}) - @{f.username || f.facultyId}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-muted-foreground mb-1">
                  Faculty Name *
                </label>
                <input
                  type="text"
                  required
                  value={slotData.facultyName}
                  onChange={(e) => setSlotData({ ...slotData, facultyName: e.target.value })}
                  placeholder="e.g. Dr. Alan Turing"
                  className="w-full rounded-lg border border-border bg-background px-3 py-2 text-sm focus:border-primary focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-muted-foreground mb-1">
                  Faculty Username *
                </label>
                <input
                  type="text"
                  required
                  value={slotData.facultyUsername}
                  onChange={(e) => setSlotData({ ...slotData, facultyUsername: e.target.value })}
                  placeholder="e.g. teacher1cse"
                  className="w-full rounded-lg border border-border bg-background px-3 py-2 text-sm focus:border-primary focus:outline-none"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-muted-foreground mb-1">
                  Classroom / Lab *
                </label>
                <input
                  type="text"
                  required
                  value={slotData.room}
                  onChange={(e) => setSlotData({ ...slotData, room: e.target.value })}
                  placeholder="e.g. LH-101 or CS-Lab 1"
                  className="w-full rounded-lg border border-border bg-background px-3 py-2 text-sm focus:border-primary focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-muted-foreground mb-1">
                  Session Type *
                </label>
                <select
                  value={slotData.type}
                  onChange={(e) => setSlotData({ ...slotData, type: e.target.value })}
                  className="w-full rounded-lg border border-border bg-background px-3 py-2 text-sm focus:border-primary focus:outline-none"
                >
                  <option value="lecture">Theory Lecture</option>
                  <option value="lab">Laboratory Practical</option>
                  <option value="tutorial">Tutorial / Problem Solving</option>
                  <option value="activity">Academic Activity / Mentorship</option>
                  <option value="assessment">Continuous Assessment / Test</option>
                </select>
              </div>
            </div>

            {/* Actions */}
            <div className="flex items-center justify-between pt-4 border-t border-border mt-4">
              {slotData.id ? (
                <button
                  type="button"
                  onClick={handleDeleteSlot}
                  disabled={actionLoading}
                  className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-4 py-2 text-xs font-semibold text-rose-600 hover:bg-rose-500/20 transition-colors flex items-center gap-1.5"
                >
                  <Trash2 className="size-3.5" /> Clear Slot
                </button>
              ) : (
                <div />
              )}

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="rounded-lg border border-border px-4 py-2 text-xs font-semibold text-muted-foreground hover:bg-muted"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={actionLoading}
                  className="rounded-lg bg-primary px-5 py-2 text-xs font-semibold text-primary-foreground hover:opacity-90 flex items-center gap-1.5 shadow-sm"
                >
                  {actionLoading ? <Loader2 className="size-3.5 animate-spin" /> : <Check className="size-3.5" />}
                  Save Timetable Slot
                </button>
              </div>
            </div>
          </form>
        </Modal>
      )}
    </>
  );
}

function Attendance() {
  const me = useGetMe();
  const isFaculty = me.data?.role === "faculty";
  const q = useListAttendance();
  const save = useSaveAttendance();
  const qc = useQueryClient();
  const data = q.data;

  const submit = () => {
    if (Array.isArray(data) && data[0]) {
      save.mutate(
        {
          data: {
            subjectId: data[0].subjectId,
            date: new Date().toISOString().slice(0, 10),
            records: [],
          },
        },
        {
          onSuccess: () =>
            qc.invalidateQueries({ queryKey: getListAttendanceQueryKey() }),
        },
      );
    }
  };

  if (q.isLoading)
    return (
      <>
        <PageHead eyebrow="Academic" title="Attendance" />
        <SkeletonRows count={6} />
      </>
    );
  if (q.isError)
    return (
      <>
        <PageHead eyebrow="Academic" title="Attendance" />
        <ErrorState retry={() => q.refetch()} />
      </>
    );

  if (isFaculty && !Array.isArray(data) && data) {
    const d = data;
    return (
      <>
        <PageHead
          eyebrow="Academic"
          title="My Attendance"
          description="Your overall presence and compliance."
        />
        <Card className="max-w-md p-8 text-center rise mx-auto mt-10">
          <div className="mx-auto mb-6 grid size-16 place-items-center rounded-full bg-primary/10 text-primary">
            <ClipboardCheck className="size-7" />
          </div>
          <div className="text-sm font-semibold uppercase tracking-wider text-muted-foreground">
            {d.label}
          </div>
          <div className="mt-4 display text-6xl font-semibold">
            {d.overall}%
          </div>
          <div className="mt-8 mb-2 flex items-center justify-between text-xs text-muted-foreground">
            <span>
              Status:{" "}
              <Status tone={d.status === "good" ? "positive" : "warning"}>
                {d.status}
              </Status>
            </span>
            <span>Updated {d.lastUpdated}</span>
          </div>
          <div className="metric-bar bg-muted/50">
            <span style={{ width: `${d.overall}%` }} className="bg-primary" />
          </div>
        </Card>
      </>
    );
  }

  const list = Array.isArray(data) ? data : [];
  const totalPresent = list.reduce((sum, a) => sum + (a.present || 0), 0);
  const totalSessions = list.reduce((sum, a) => sum + (a.total || 0), 0);
  const avgAttendance = totalSessions > 0 ? Math.round((totalPresent / totalSessions) * 100) : 94;
  const isEligible = avgAttendance >= 75;

  return (
    <>
      <PageHead
        eyebrow="Academic Portal"
        title="Attendance & Eligibility"
        description="Attendance percentage for all enrolled subjects in the current semester with final examination clearance."
        action={
          isFaculty ? (
            <button
              onClick={submit}
              disabled={save.isPending || !list.length}
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
              data-testid="button-save-attendance"
            >
              <ClipboardCheck className="mr-2 inline size-4" />{" "}
              {save.isPending ? "Saving…" : "Save attendance"}
            </button>
          ) : null
        }
      />

      {/* Current Semester Summary Card for Students */}
      <Card className="mb-6 p-6 border-l-4 border-l-primary">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="rounded bg-primary/10 px-2.5 py-0.5 text-xs font-bold text-primary">
                Current Semester
              </span>
              <span className="text-xs text-muted-foreground">
                Academic Session 2026-2027
              </span>
            </div>
            <h2 className="display mt-1 text-2xl font-semibold">
              Current Semester Subject Attendance
            </h2>
            <p className="mt-1 text-xs text-muted-foreground">
              Total sessions attended: <strong>{totalPresent}</strong> of <strong>{totalSessions}</strong> lectures conducted across all subjects.
            </p>
          </div>

          <div className="flex items-center gap-6">
            <div className="text-right">
              <div className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                Cumulative Average
              </div>
              <div className="mt-0.5 text-3xl font-bold text-primary">
                {avgAttendance}%
              </div>
            </div>

            <div className="border-l border-border pl-6 text-right">
              <div className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
                Exam Eligibility
              </div>
              <div className="mt-1">
                <Status tone={isEligible ? "positive" : "critical"}>
                  {isEligible ? "Eligible (>=75%)" : "Condonation Required (<75%)"}
                </Status>
              </div>
            </div>
          </div>
        </div>
      </Card>

      {/* Subject Wise Attendance Cards */}
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {list.length === 0 ? (
          <Card className="sm:col-span-2 xl:col-span-3">
            <Empty label="attendance summaries" />
          </Card>
        ) : (
          list.map((a, i) => {
            const pct = a.percentage != null ? a.percentage : (a.total > 0 ? Math.round((a.present / a.total) * 100) : 90);
            const isGood = pct >= 75;

            return (
              <Card key={a.subjectId || i} className={`p-5 rise delay-${(i % 4) + 1} border hover:border-primary/40 transition-all`}>
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <div className="flex items-center gap-2">
                      {a.subjectCode && (
                        <span className="rounded bg-muted px-1.5 py-0.5 font-mono text-[10px] font-bold text-muted-foreground">
                          {a.subjectCode}
                        </span>
                      )}
                      {a.semester && (
                        <span className="text-[10px] text-muted-foreground">
                          Sem {a.semester}
                        </span>
                      )}
                    </div>
                    <h3 className="font-semibold text-base mt-1.5 leading-snug">
                      {a.subject}
                    </h3>
                    <div className="mt-1 text-xs text-muted-foreground">
                      <strong>{a.present}</strong> present of <strong>{a.total}</strong> sessions
                    </div>
                  </div>
                  <Status
                    tone={
                      isGood ? "positive" : pct >= 65 ? "warning" : "critical"
                    }
                  >
                    {isGood ? "On Track" : "Shortage"}
                  </Status>
                </div>

                <div className="mt-6 flex items-end justify-between">
                  <span className={`display text-4xl font-bold ${isGood ? "text-foreground" : "text-rose-600"}`}>
                    {pct}%
                  </span>
                  <span className="text-xs text-muted-foreground">
                    {a.lastUpdated ? `Updated ${a.lastUpdated}` : "Current semester"}
                  </span>
                </div>

                <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-muted/60">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ${isGood ? "bg-primary" : "bg-rose-500"}`}
                    style={{ width: `${pct}%` }}
                  />
                </div>
              </Card>
            );
          })
        )}
      </div>
    </>
  );
}

function Marks() {
  const me = useGetMe();
  const isFaculty = me.data?.role === "faculty";
  const activeSem = Number(me.data?.semester || 1);
  const activeDept = me.data?.department || "CSE";

  const [selectedSemester, setSelectedSemester] = useState("all");
  const [hoveredSem, setHoveredSem] = useState(activeSem);

  const q = useListMarks();
  const save = useSaveMarks();
  const subjects = useListSubjects();
  const data = q.data;

  const summaryQuery = useQuery({
    queryKey: ["marks-summary", me.data?.username],
    queryFn: () => customFetch("/api/marks/summary"),
    enabled: !isFaculty,
  });

  if (q.isLoading)
    return (
      <>
        <PageHead eyebrow="Academic" title="Marks" />
        <SkeletonRows count={6} />
      </>
    );
  if (q.isError)
    return (
      <>
        <PageHead eyebrow="Academic" title="Marks" />
        <ErrorState retry={() => q.refetch()} />
      </>
    );

  if (isFaculty && !Array.isArray(data) && data) {
    const d = data;
    return (
      <>
        <PageHead
          eyebrow="Academic"
          title="Subject Performance"
          description="Track and manage student grades for your designated subject."
        />
        <Card className="mb-6 p-6 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Assigned Subject
            </div>
            <h2 className="display mt-1 text-2xl font-semibold">{d.subject}</h2>
          </div>
          <div className="text-right">
            <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Class Average
            </div>
            <div className="mt-1 text-3xl font-semibold text-primary">
              {d.average}%
            </div>
          </div>
        </Card>
        <Card>
          <div className="border-b border-border p-4 font-semibold">
            Student Roster ({d.students?.length || 0} enrolled)
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-muted/50 text-[11px] uppercase tracking-wider text-muted-foreground">
                <tr>
                  <th className="px-5 py-3">Student Name</th>
                  <th className="px-5 py-3">Score</th>
                  <th className="px-5 py-3">Grade</th>
                  <th className="px-5 py-3 text-right">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {d.students?.map((s) => (
                  <tr key={s.id} className="hover:bg-muted/30">
                    <td className="px-5 py-4 font-semibold">{s.name}</td>
                    <td className="px-5 py-4">
                      {s.score}{" "}
                      <span className="text-muted-foreground text-xs">
                        / {s.maxScore}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <span className="grid size-8 place-items-center rounded bg-accent/30 text-xs font-bold">
                        {s.grade}
                      </span>
                    </td>
                    <td className="px-5 py-4 text-right">
                      <Status tone={s.passed ? "positive" : "critical"}>
                        {s.passed ? "Passed" : "Needs review"}
                      </Status>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </>
    );
  }

  const rawList = Array.isArray(data) ? data : [];
  // Restrict marks strictly to current and past semesters (never future)
  const list = rawList.filter((m) => m.semester == null || m.semester <= activeSem);

  // Compute allowed semesters: 1 up to activeSem (only current and history)
  const historySemesters = Array.from({ length: Math.max(0, activeSem - 1) }, (_, i) => i + 1);
  const allowedSemesters = Array.from({ length: activeSem }, (_, i) => i + 1);

  // Process semester chart data
  const summarySemesters = summaryQuery.data?.semesters || [];
  const chartData = allowedSemesters.map((semNum) => {
    const existing = summarySemesters.find((s) => s.semester === semNum);
    const semMarks = list.filter((m) => m.semester === semNum);
    const totalScore = semMarks.reduce((sum, m) => sum + (m.score || 0), 0);
    const totalMax = semMarks.reduce((sum, m) => sum + (m.maxScore || 100), 0);
    const computedPct = totalMax > 0 ? Math.round((totalScore / totalMax) * 1000) / 10 : 88.0;
    const computedSgpa = Math.round((computedPct / 10) * 10) / 10;

    const isCurrent = semNum === activeSem;
    return {
      semester: semNum,
      year: Math.ceil(semNum / 2),
      sgpa: existing?.sgpa ?? computedSgpa,
      cgpa: existing?.cgpa ?? computedSgpa,
      percentage: existing?.percentage ?? computedPct,
      credits: existing?.creditsEarned ?? 22,
      type: isCurrent ? "current" : "history",
      status: isCurrent ? "Ongoing" : "Passed",
    };
  });

  const activeHoveredData = chartData.find((c) => c.semester === hoveredSem) || chartData[chartData.length - 1] || {
    semester: activeSem,
    year: Math.ceil(activeSem / 2),
    sgpa: 8.8,
    cgpa: 8.75,
    percentage: 88.0,
    credits: 22,
    type: "current",
    status: "Ongoing",
  };

  const currentCumulativeCgpa = chartData.length > 0 ? chartData[chartData.length - 1].cgpa : 8.75;
  const totalCompletedCredits = historySemesters.length * 22;

  // Filter marks based on selected tab
  const displayedMarks = list.filter((m) => {
    if (selectedSemester === "all") return true;
    if (selectedSemester === "current") return m.semester === activeSem;
    if (selectedSemester === "history") return m.semester != null && m.semester < activeSem;
    return m.semester != null && String(m.semester) === selectedSemester;
  });

  const currentTermMarks = list.filter((m) => m.semester === activeSem);
  const historyMarks = list.filter((m) => m.semester != null && m.semester < activeSem);

  const totalScores = displayedMarks.reduce((sum, m) => sum + (m.score || 0), 0);
  const totalMax = displayedMarks.reduce((sum, m) => sum + (m.maxScore || 100), 0);
  const avgPct = totalMax > 0 ? Math.round((totalScores / totalMax) * 100) : 88;
  const currentSgpa = Math.round((avgPct / 10) * 10) / 10;
  const passedCount = displayedMarks.filter((m) => m.passed !== false).length;
  const arrearsCount = displayedMarks.filter((m) => m.passed === false).length;

  const submit = () => {
    const first = list[0];
    const subject = subjects.data?.[0];
    if (first && subject)
      save.mutate({
        data: {
          studentId: first.studentId ?? 0,
          subjectId: first.subjectId,
          assessment: first.assessment,
          score: first.score,
          maxScore: first.maxScore,
        },
      });
  };

  // Helper SVG coordinates calculation for the graph
  const svgW = 680;
  const svgH = 220;
  const padL = 50;
  const padR = 40;
  const padT = 30;
  const padB = 40;
  const plotW = svgW - padL - padR;
  const plotH = svgH - padT - padB;
  const minGpa = 6.0;
  const maxGpa = 10.0;

  const getY = (val) => {
    const clamped = Math.max(minGpa, Math.min(maxGpa, val));
    return padT + plotH - ((clamped - minGpa) / (maxGpa - minGpa)) * plotH;
  };

  const getX = (index) => {
    if (chartData.length <= 1) return padL + plotW / 2;
    return padL + (index / (chartData.length - 1)) * plotW;
  };

  // Construct smooth SVG path for Cumulative CGPA line
  const cgpaPoints = chartData.map((d, i) => ({ x: getX(i), y: getY(d.cgpa) }));
  let cgpaPath = "";
  if (cgpaPoints.length === 1) {
    cgpaPath = `M ${cgpaPoints[0].x - 30} ${cgpaPoints[0].y} L ${cgpaPoints[0].x + 30} ${cgpaPoints[0].y}`;
  } else if (cgpaPoints.length > 1) {
    cgpaPath = `M ${cgpaPoints[0].x} ${cgpaPoints[0].y}`;
    for (let i = 0; i < cgpaPoints.length - 1; i++) {
      const p0 = cgpaPoints[i];
      const p1 = cgpaPoints[i + 1];
      const cp1x = p0.x + (p1.x - p0.x) / 2;
      const cp1y = p0.y;
      const cp2x = p0.x + (p1.x - p0.x) / 2;
      const cp2y = p1.y;
      cgpaPath += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p1.x} ${p1.y}`;
    }
  }

  return (
    <>
      <PageHead
        eyebrow="Academic Records"
        title="Student Marks & Academic History"
        description="Semester-wise classification of current academic assessments, verified university history, and cumulative CGPA progression."
        action={
          isFaculty ? (
            <button
              onClick={submit}
              disabled={save.isPending || !list.length}
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
              data-testid="button-save-marks"
            >
              <Award className="mr-2 inline size-4" />{" "}
              {save.isPending ? "Saving…" : "Save marks"}
            </button>
          ) : null
        }
      />

      {/* Top Academic Standing & Performance Metrics */}
      <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card className="p-5 border-l-4 border-l-primary flex items-center justify-between">
          <div>
            <div className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
              <GraduationCap className="size-4 text-primary" /> Cumulative CGPA
            </div>
            <div className="mt-1.5 text-3xl font-extrabold text-foreground tracking-tight">
              {currentCumulativeCgpa}
              <span className="text-sm font-normal text-muted-foreground ml-1">/ 10.0</span>
            </div>
            <div className="mt-1 text-xs text-primary font-semibold flex items-center gap-1">
              <Sparkles className="size-3" /> First Class with Distinction
            </div>
          </div>
          <div className="rounded-full bg-primary/10 p-3 text-primary">
            <TrendingUp className="size-6" />
          </div>
        </Card>

        <Card className="p-5 border-l-4 border-l-emerald-500 flex items-center justify-between">
          <div>
            <div className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
              <Clock3 className="size-4 text-emerald-600" /> Current Term (Sem {activeSem})
            </div>
            <div className="mt-1.5 text-3xl font-extrabold text-emerald-600 tracking-tight">
              {chartData.find((c) => c.semester === activeSem)?.sgpa || 8.8}
              <span className="text-sm font-normal text-muted-foreground ml-1">SGPA</span>
            </div>
            <div className="mt-1 text-xs text-muted-foreground font-medium">
              5 Core {activeDept} Subjects · Ongoing
            </div>
          </div>
          <div className="rounded-full bg-emerald-500/10 p-3 text-emerald-600">
            <Award className="size-6" />
          </div>
        </Card>

        <Card className="p-5 border-l-4 border-l-blue-500 flex items-center justify-between">
          <div>
            <div className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
              <CheckCircle2 className="size-4 text-blue-600" /> Completed Credits
            </div>
            <div className="mt-1.5 text-3xl font-extrabold text-blue-600 tracking-tight">
              {totalCompletedCredits}
              <span className="text-sm font-normal text-muted-foreground ml-1">Credits</span>
            </div>
            <div className="mt-1 text-xs text-muted-foreground font-medium">
              {historySemesters.length} Completed Semesters Cleared
            </div>
          </div>
          <div className="rounded-full bg-blue-500/10 p-3 text-blue-600">
            <BookOpen className="size-6" />
          </div>
        </Card>

        <Card className="p-5 border-l-4 border-l-amber-500 flex items-center justify-between">
          <div>
            <div className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
              <ShieldCheck className="size-4 text-amber-600" /> University Standing
            </div>
            <div className="mt-1.5 text-2xl font-bold text-foreground tracking-tight">
              Zero Backlogs
            </div>
            <div className="mt-1 text-xs font-semibold text-emerald-600 flex items-center gap-1">
              <Check className="size-3.5" /> 100% Courses Cleared
            </div>
          </div>
          <div className="rounded-full bg-amber-500/10 p-3 text-amber-600">
            <Layers className="size-6" />
          </div>
        </Card>
      </div>

      {/* Interactive Semester-Wise CGPA & SGPA Graph */}
      <Card className="mb-6 p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border pb-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="rounded-md bg-primary/10 px-2.5 py-1 text-xs font-bold text-primary flex items-center gap-1.5">
                <BarChart3 className="size-3.5" /> Performance Analytics
              </span>
              <span className="rounded-md bg-muted px-2 py-0.5 text-xs font-semibold text-muted-foreground">
                Semesters 1 to {activeSem}
              </span>
            </div>
            <h2 className="display mt-1.5 text-xl font-bold">
              Semester-Wise CGPA & SGPA Progression Graph
            </h2>
            <p className="mt-1 text-xs text-muted-foreground">
              Visualizes term grade point averages (SGPA bars) alongside cumulative progression (CGPA curve). Hover over bars to inspect details.
            </p>
          </div>

          {/* Interactive Tooltip Card for Active / Hovered Semester */}
          <div className="rounded-xl border border-primary/20 bg-primary/5 p-3 sm:text-right min-w-[210px]">
            <div className="flex items-center justify-between sm:justify-end gap-2 text-xs font-bold text-primary">
              <span>Semester {activeHoveredData.semester} (Yr {activeHoveredData.year})</span>
              <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                activeHoveredData.type === "current"
                  ? "bg-emerald-500 text-white"
                  : "bg-primary text-primary-foreground"
              }`}>
                {activeHoveredData.type === "current" ? "Current Term" : "Passed"}
              </span>
            </div>
            <div className="mt-1 flex items-baseline justify-between sm:justify-end gap-3">
              <span className="text-[11px] text-muted-foreground">Term SGPA: <b className="text-foreground">{activeHoveredData.sgpa}</b></span>
              <span className="text-[11px] text-muted-foreground">Cumulative CGPA: <b className="text-primary font-bold">{activeHoveredData.cgpa}</b></span>
            </div>
            <div className="mt-0.5 text-[10px] text-muted-foreground">
              Marks Avg: <b>{activeHoveredData.percentage}%</b> · Credits: <b>{activeHoveredData.credits}</b>
            </div>
          </div>
        </div>

        {/* SVG Visualization Canvas */}
        <div className="mt-6 w-full overflow-x-auto">
          <div className="min-w-[620px]">
            <svg viewBox={`0 0 ${svgW} ${svgH}`} className="w-full h-56 select-none">
              <defs>
                <linearGradient id="historyBarGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#6366f1" stopOpacity="0.85" />
                  <stop offset="100%" stopColor="#4338ca" stopOpacity="0.4" />
                </linearGradient>
                <linearGradient id="currentBarGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#10b981" stopOpacity="0.9" />
                  <stop offset="100%" stopColor="#059669" stopOpacity="0.45" />
                </linearGradient>
                <linearGradient id="cgpaLineGrad" x1="0" y1="0" x2="1" y2="0">
                  <stop offset="0%" stopColor="#38bdf8" />
                  <stop offset="100%" stopColor="#ec4899" />
                </linearGradient>
                <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                  <feGaussianBlur stdDeviation="3" result="blur" />
                  <feComposite in="SourceGraphic" in2="blur" operator="over" />
                </filter>
              </defs>

              {/* Horizontal Reference Grid Lines */}
              {[6.0, 7.0, 8.0, 9.0, 10.0].map((val) => {
                const y = getY(val);
                const isDistinction = val === 8.0;
                return (
                  <g key={val}>
                    <line
                      x1={padL}
                      y1={y}
                      x2={svgW - padR}
                      y2={y}
                      stroke={isDistinction ? "#f59e0b" : "currentColor"}
                      strokeOpacity={isDistinction ? 0.45 : 0.08}
                      strokeDasharray={isDistinction ? "4,4" : undefined}
                      strokeWidth={isDistinction ? 1.5 : 1}
                    />
                    <text
                      x={padL - 10}
                      y={y + 4}
                      textAnchor="end"
                      className="fill-muted-foreground text-[10px] font-mono"
                    >
                      {val.toFixed(1)}
                    </text>
                    {isDistinction && (
                      <text
                        x={svgW - padR - 5}
                        y={y - 5}
                        textAnchor="end"
                        className="fill-amber-600 text-[9px] font-bold uppercase tracking-wider"
                      >
                        ★ 8.0 Distinction Line
                      </text>
                    )}
                  </g>
                );
              })}

              {/* SGPA Vertical Bars */}
              {chartData.map((d, i) => {
                const cx = getX(i);
                const barWidth = 34;
                const barY = getY(d.sgpa);
                const barHeight = Math.max(12, padT + plotH - barY);
                const isHovered = hoveredSem === d.semester;
                const isCurrent = d.type === "current";

                return (
                  <g
                    key={d.semester}
                    className="cursor-pointer transition-opacity"
                    onMouseEnter={() => setHoveredSem(d.semester)}
                    onClick={() => setSelectedSemester(String(d.semester))}
                  >
                    {/* Bar Pillar */}
                    <rect
                      x={cx - barWidth / 2}
                      y={barY}
                      width={barWidth}
                      height={barHeight}
                      rx={6}
                      fill={isCurrent ? "url(#currentBarGrad)" : "url(#historyBarGrad)"}
                      stroke={isHovered ? (isCurrent ? "#10b981" : "#6366f1") : "transparent"}
                      strokeWidth={2}
                      className="transition-all duration-200"
                      opacity={isHovered ? 1 : 0.85}
                    />
                    {/* SGPA Value on Top of Bar */}
                    <text
                      x={cx}
                      y={barY - 7}
                      textAnchor="middle"
                      className={`text-[11px] font-bold font-mono ${
                        isCurrent ? "fill-emerald-600" : "fill-foreground"
                      }`}
                    >
                      {d.sgpa.toFixed(1)}
                    </text>
                    {/* Semester Axis Label */}
                    <text
                      x={cx}
                      y={padT + plotH + 18}
                      textAnchor="middle"
                      className={`text-[11px] font-semibold ${
                        isHovered || isCurrent ? "fill-primary font-bold" : "fill-muted-foreground"
                      }`}
                    >
                      Sem {d.semester}
                    </text>
                    <text
                      x={cx}
                      y={padT + plotH + 30}
                      textAnchor="middle"
                      className="fill-muted-foreground/60 text-[9px]"
                    >
                      {isCurrent ? "(Ongoing)" : "(Passed)"}
                    </text>
                  </g>
                );
              })}

              {/* Cumulative CGPA Spline Curve */}
              {cgpaPath && (
                <path
                  d={cgpaPath}
                  fill="none"
                  stroke="url(#cgpaLineGrad)"
                  strokeWidth={3}
                  filter="url(#glow)"
                  strokeLinecap="round"
                />
              )}

              {/* Data Nodes for CGPA */}
              {cgpaPoints.map((pt, i) => {
                const d = chartData[i];
                const isHovered = hoveredSem === d.semester;
                return (
                  <g
                    key={i}
                    className="cursor-pointer"
                    onMouseEnter={() => setHoveredSem(d.semester)}
                    onClick={() => setSelectedSemester(String(d.semester))}
                  >
                    <circle
                      cx={pt.x}
                      cy={pt.y}
                      r={isHovered ? 7 : 5}
                      fill="#ffffff"
                      stroke="#0284c7"
                      strokeWidth={3}
                      className="transition-all duration-200"
                    />
                  </g>
                );
              })}
            </svg>
          </div>
        </div>

        {/* Legend & Guide */}
        <div className="mt-4 flex flex-wrap items-center justify-between gap-4 border-t border-border pt-4 text-xs text-muted-foreground">
          <div className="flex flex-wrap items-center gap-5">
            <div className="flex items-center gap-2">
              <span className="size-3.5 rounded bg-indigo-500/80" />
              <span><b>Past SGPA:</b> Completed Semesters 1–{Math.max(1, activeSem - 1)}</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="size-3.5 rounded bg-emerald-500" />
              <span><b>Current SGPA:</b> Active Term (Sem {activeSem})</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="h-1 w-6 rounded bg-gradient-to-r from-sky-400 to-pink-500" />
              <span><b>CGPA Curve:</b> Cumulative Grade Trajectory</span>
            </div>
          </div>
          <div className="text-[11px] text-muted-foreground italic">
            💡 Click on any semester bar to filter that semester's subjects below.
          </div>
        </div>
      </Card>

      {/* Semester Classification Filter Navigation */}
      <Card className="mb-6 p-4">
        <div className="flex flex-col gap-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <div className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
                Classify Marks By Term:
              </div>
              <div className="text-xs text-muted-foreground mt-0.5">
                Restricted to your current active semester and completed academic history.
              </div>
            </div>

            {/* Top Classification Tabs */}
            <div className="flex flex-wrap items-center gap-2">
              <button
                type="button"
                onClick={() => setSelectedSemester("all")}
                className={`rounded-lg px-3.5 py-2 text-xs font-semibold transition-all ${
                  selectedSemester === "all"
                    ? "bg-primary text-primary-foreground shadow-sm"
                    : "bg-muted text-muted-foreground hover:bg-muted/80"
                }`}
              >
                📋 All Records (Sem 1–{activeSem})
              </button>
              <button
                type="button"
                onClick={() => setSelectedSemester("current")}
                className={`rounded-lg px-3.5 py-2 text-xs font-semibold transition-all flex items-center gap-1.5 ${
                  selectedSemester === "current" || selectedSemester === String(activeSem)
                    ? "bg-emerald-600 text-white shadow-sm ring-2 ring-emerald-500/30"
                    : "bg-emerald-500/10 text-emerald-700 hover:bg-emerald-500/20"
                }`}
              >
                <Clock3 className="size-3.5" /> ⚡ Current Term (Sem {activeSem} · Active)
              </button>
              {historySemesters.length > 0 && (
                <button
                  type="button"
                  onClick={() => setSelectedSemester("history")}
                  className={`rounded-lg px-3.5 py-2 text-xs font-semibold transition-all flex items-center gap-1.5 ${
                    selectedSemester === "history"
                      ? "bg-indigo-600 text-white shadow-sm"
                      : "bg-indigo-500/10 text-indigo-700 hover:bg-indigo-500/20"
                  }`}
                >
                  <BookOpen className="size-3.5" /> 📜 Academic History ({historySemesters.length} Completed Sems)
                </button>
              )}
            </div>
          </div>

          {/* Individual Semester Quick Switcher (Strictly 1 to activeSem, NO future) */}
          <div className="flex flex-wrap items-center gap-1.5 pt-3 border-t border-border">
            <span className="text-[11px] font-bold text-muted-foreground uppercase mr-1">
              Jump To Semester:
            </span>
            {allowedSemesters.map((sem) => {
              const isCurrent = sem === activeSem;
              const isSelected = selectedSemester === String(sem);
              return (
                <button
                  key={sem}
                  type="button"
                  onClick={() => setSelectedSemester(String(sem))}
                  className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition-all flex items-center gap-1 font-mono ${
                    isSelected
                      ? "bg-primary text-primary-foreground shadow-sm"
                      : isCurrent
                      ? "bg-emerald-500/15 text-emerald-800 hover:bg-emerald-500/25 font-bold"
                      : "bg-muted text-muted-foreground hover:bg-muted/80"
                  }`}
                >
                  {isCurrent ? "⚡ " : "✓ "}Sem {sem}
                  {isCurrent && <span className="ml-0.5 text-[10px] font-normal uppercase">(Active)</span>}
                </button>
              );
            })}
          </div>
        </div>

        {/* Selected Context Summary Strip */}
        <div className="mt-4 grid gap-4 border-t border-border pt-4 sm:grid-cols-4">
          <div>
            <div className="text-[10px] font-bold uppercase text-muted-foreground">
              {selectedSemester === "all"
                ? `Overall Program Average (Sem 1–${activeSem})`
                : selectedSemester === "current"
                ? `Current Term Average (Sem ${activeSem})`
                : selectedSemester === "history"
                ? `Completed History Average (Sem 1–${activeSem - 1})`
                : `Semester ${selectedSemester} Average`}
            </div>
            <div className="mt-1 text-2xl font-bold text-foreground">
              {avgPct}%
            </div>
          </div>
          <div>
            <div className="text-[10px] font-bold uppercase text-muted-foreground">
              {selectedSemester === "all" ? "Cumulative CGPA" : "Term SGPA"}
            </div>
            <div className="mt-1 text-2xl font-bold text-primary">
              {selectedSemester === "all" ? currentCumulativeCgpa : currentSgpa}
            </div>
          </div>
          <div>
            <div className="text-[10px] font-bold uppercase text-muted-foreground">
              Evaluated Assessments
            </div>
            <div className="mt-1 text-2xl font-bold text-emerald-600">
              {passedCount} Passed
            </div>
          </div>
          <div>
            <div className="text-[10px] font-bold uppercase text-muted-foreground">
              Standing / Backlogs
            </div>
            <div className="mt-1 text-2xl font-bold text-foreground flex items-center gap-1.5">
              {arrearsCount === 0 ? (
                <span className="text-emerald-600 flex items-center gap-1">
                  <CheckCircle2 className="size-5" /> All Cleared
                </span>
              ) : (
                <span className="text-rose-600 font-bold">{arrearsCount} Backlogs</span>
              )}
            </div>
          </div>
        </div>
      </Card>

      {/* CLASSIFIED MARKS SECTION: CURRENT AND HISTORY */}
      {displayedMarks.length === 0 ? (
        <Card className="p-10 text-center">
          <Award className="mx-auto size-12 text-muted-foreground/40 mb-3" />
          <h3 className="font-semibold text-base">No Marks Recorded for this Filter</h3>
          <p className="text-xs text-muted-foreground mt-1 max-w-md mx-auto">
            Select another semester tab above to inspect your continuous assessments or university results.
          </p>
        </Card>
      ) : (
        <div className="space-y-6">
          {/* SECTION 1: CURRENT SEMESTER (Shown when viewing "all", "current", or specific active sem) */}
          {(selectedSemester === "all" || selectedSemester === "current" || selectedSemester === String(activeSem)) && (
            <Card className="border-2 border-emerald-500/30 overflow-hidden shadow-sm">
              <div className="bg-emerald-500/10 border-b border-emerald-500/20 px-6 py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  <div className="rounded-lg bg-emerald-600 p-2 text-white shadow-sm">
                    <Clock3 className="size-5" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold uppercase tracking-wider text-emerald-700 dark:text-emerald-400">
                        Current Academic Term Evaluation
                      </span>
                      <span className="rounded-full bg-emerald-600 text-white px-2 py-0.5 text-[10px] font-bold uppercase">
                        Ongoing
                      </span>
                    </div>
                    <h3 className="text-lg font-bold text-foreground">
                      Semester {activeSem} Core Curriculum ({activeDept} Department · 5 Subjects)
                    </h3>
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-xs text-muted-foreground font-semibold">Continuous Assessment Average</div>
                  <div className="text-xl font-extrabold text-emerald-600">
                    {Math.round(
                      (currentTermMarks.reduce((s, m) => s + (m.score || 0), 0) /
                        Math.max(1, currentTermMarks.reduce((s, m) => s + (m.maxScore || 100), 0))) *
                        100
                    )}%
                  </div>
                </div>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-muted/50 text-[11px] uppercase tracking-wider text-muted-foreground">
                    <tr>
                      <th className="px-5 py-3">Course Code & Name</th>
                      <th className="px-5 py-3">Assessment Type</th>
                      <th className="px-5 py-3">Credits</th>
                      <th className="px-5 py-3">Score & Performance</th>
                      <th className="px-5 py-3">Grade</th>
                      <th className="px-5 py-3 text-right">Term Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border">
                    {currentTermMarks.map((m) => {
                      const pct = Math.round(((m.score || 0) / (m.maxScore || 100)) * 100);
                      return (
                        <tr key={m.id} className="hover:bg-muted/30">
                          <td className="px-5 py-4">
                            <div className="font-semibold text-foreground">{m.subject}</div>
                            {m.subjectCode && (
                              <span className="inline-block mt-0.5 font-mono text-xs px-2 py-0.5 rounded bg-primary/10 text-primary font-bold">
                                {m.subjectCode}
                              </span>
                            )}
                          </td>
                          <td className="px-5 py-4 text-muted-foreground font-medium">
                            {m.assessment}
                          </td>
                          <td className="px-5 py-4 text-xs font-semibold text-muted-foreground">
                            {m.credits || 4} Credits
                          </td>
                          <td className="px-5 py-4">
                            <div className="flex items-center gap-3">
                              <span className="font-bold text-foreground">
                                {m.score}{" "}
                                <span className="font-normal text-muted-foreground text-xs">
                                  / {m.maxScore}
                                </span>
                              </span>
                              <div className="h-1.5 w-20 rounded-full bg-muted overflow-hidden">
                                <div
                                  className="h-full bg-emerald-500 rounded-full"
                                  style={{ width: `${Math.min(100, pct)}%` }}
                                />
                              </div>
                            </div>
                          </td>
                          <td className="px-5 py-4">
                            <span className="grid size-8 place-items-center rounded-lg bg-emerald-500/15 text-emerald-700 font-extrabold text-xs">
                              {m.grade}
                            </span>
                          </td>
                          <td className="px-5 py-4 text-right">
                            <span className="inline-flex items-center gap-1 rounded-full bg-emerald-500/10 px-2.5 py-1 text-xs font-semibold text-emerald-600">
                              <CheckCircle2 className="size-3.5" /> In Progress
                            </span>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </Card>
          )}

          {/* SECTION 2: ACADEMIC HISTORY (Shown when viewing "all", "history", or specific past sem) */}
          {(selectedSemester === "all" || selectedSemester === "history" || (Number(selectedSemester) < activeSem && selectedSemester !== "current")) && (
            <Card className="border border-border overflow-hidden">
              <div className="bg-muted/40 border-b border-border px-6 py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  <div className="rounded-lg bg-indigo-600 p-2 text-white shadow-sm">
                    <BookOpen className="size-5" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold uppercase tracking-wider text-indigo-600 dark:text-indigo-400">
                        Academic History & University Transcripts
                      </span>
                      <span className="rounded-full bg-indigo-100 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-300 px-2 py-0.5 text-[10px] font-bold uppercase">
                        Verified
                      </span>
                    </div>
                    <h3 className="text-lg font-bold text-foreground">
                      Completed Semesters 1 to {Math.max(1, activeSem - 1)} Official Records
                    </h3>
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-xs text-muted-foreground font-semibold">Total Cleared Courses</div>
                  <div className="text-xl font-bold text-foreground">
                    {historyMarks.filter((m) => m.passed !== false).length} Evaluations Passed
                  </div>
                </div>
              </div>

              {historySemesters.length === 0 ? (
                <div className="p-8 text-center text-muted-foreground text-sm">
                  First semester student — no prior completed semesters in academic history.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-muted/50 text-[11px] uppercase tracking-wider text-muted-foreground">
                      <tr>
                        <th className="px-5 py-3">Course Code & Name</th>
                        <th className="px-5 py-3">Completed Term</th>
                        <th className="px-5 py-3">Assessment Category</th>
                        <th className="px-5 py-3">Credits</th>
                        <th className="px-5 py-3">University Score</th>
                        <th className="px-5 py-3">Final Grade</th>
                        <th className="px-5 py-3 text-right">Result Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-border">
                      {historyMarks
                        .filter((m) => selectedSemester === "all" || selectedSemester === "history" || String(m.semester) === selectedSemester)
                        .map((m) => {
                          const pct = Math.round(((m.score || 0) / (m.maxScore || 100)) * 100);
                          return (
                            <tr key={m.id} className="hover:bg-muted/30">
                              <td className="px-5 py-4">
                                <div className="font-semibold text-foreground">{m.subject}</div>
                                {m.subjectCode && (
                                  <span className="inline-block mt-0.5 font-mono text-xs px-2 py-0.5 rounded bg-muted text-muted-foreground font-semibold">
                                    {m.subjectCode}
                                  </span>
                                )}
                              </td>
                              <td className="px-5 py-4">
                                <span className="rounded-md bg-indigo-500/10 px-2.5 py-1 text-xs font-bold text-indigo-700 dark:text-indigo-300 font-mono">
                                  Sem {m.semester}
                                </span>
                              </td>
                              <td className="px-5 py-4 text-muted-foreground font-medium">
                                {m.assessment}
                              </td>
                              <td className="px-5 py-4 text-xs font-semibold text-muted-foreground">
                                {m.credits || 4} Credits
                              </td>
                              <td className="px-5 py-4">
                                <div className="flex items-center gap-3">
                                  <span className="font-bold text-foreground">
                                    {m.score}{" "}
                                    <span className="font-normal text-muted-foreground text-xs">
                                      / {m.maxScore}
                                    </span>
                                  </span>
                                  <div className="h-1.5 w-16 rounded-full bg-muted overflow-hidden">
                                    <div
                                      className="h-full bg-indigo-600 rounded-full"
                                      style={{ width: `${Math.min(100, pct)}%` }}
                                    />
                                  </div>
                                </div>
                              </td>
                              <td className="px-5 py-4">
                                <span className="grid size-8 place-items-center rounded-lg bg-accent/40 text-foreground font-extrabold text-xs">
                                  {m.grade}
                                </span>
                              </td>
                              <td className="px-5 py-4 text-right">
                                <Status tone={m.passed ? "positive" : "critical"}>
                                  {m.passed ? "Passed" : "Arrear"}
                                </Status>
                              </td>
                            </tr>
                          );
                        })}
                    </tbody>
                  </table>
                </div>
              )}
            </Card>
          )}
        </div>
      )}
    </>
  );
}

function Assignments() {
  const me = useGetMe();
  const auth = useAuth();

  // Multi-fallback role detection so it never fails even during query caching/hydration
  let currentRole = me.data?.role;
  if (!currentRole && typeof window !== "undefined") {
    try {
      const u = JSON.parse(localStorage.getItem("campusUser") || "{}");
      currentRole = u?.role;
    } catch (_) {}
    if (!currentRole) currentRole = localStorage.getItem("mockRole");
  }
  const isFaculty = currentRole === "faculty" || currentRole === "admin" || auth.role === "faculty" || auth.role === "admin";

  const [modal, setModal] = useState(false);
  const [viewingAssignment, setViewingAssignment] = useState(null); // Dedicated for Teacher Status Modal
  const [submittingAssignment, setSubmittingAssignment] = useState(null); // Dedicated for Student Submit Modal
  const [studentViewingAssignment, setStudentViewingAssignment] = useState(null); // Dedicated for Student View Details & Solution File Modal
  const [previewImageModal, setPreviewImageModal] = useState(null); // In-app image preview modal
  const [selectedFile, setSelectedFile] = useState(null);
  const [filePreview, setFilePreview] = useState(null);
  const [submissionNote, setSubmissionNote] = useState("");
  const [isSubmittingWork, setIsSubmittingWork] = useState(false);
  const [fileError, setFileError] = useState(null);

  // Faculty status view filters & actions
  const [statusTab, setStatusTab] = useState("all"); // 'all' | 'completed' | 'pending'
  const [deptFilter, setDeptFilter] = useState("all");
  const [searchQuery, setSearchQuery] = useState("");
  const [alertedUsers, setAlertedUsers] = useState({});
  const [alertingUser, setAlertingUser] = useState(null);
  const [toast, setToast] = useState(null);

  const q = useListAssignments();
  const subjects = useListSubjects();
  const create = useCreateAssignment();
  const qc = useQueryClient();
  const list = q.data ?? [];

  // Query student completion status whenever teacher opens an assignment
  const statusQuery = useQuery({
    queryKey: ["assignment-status", viewingAssignment?.id],
    queryFn: () => customFetch(`/api/assignments/${viewingAssignment.id}/students-status`),
    enabled: Boolean(viewingAssignment?.id),
  });

  // Query personal submission whenever student opens an assignment
  const studentSubQuery = useQuery({
    queryKey: ["student-my-submission", studentViewingAssignment?.id],
    queryFn: () => customFetch(`/api/assignments/${studentViewingAssignment.id}/my-submission`),
    enabled: Boolean(studentViewingAssignment?.id && !isFaculty),
  });
  const activeStudentSub = studentSubQuery.data || studentViewingAssignment?.mySubmission;

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const downloadOrOpenFile = (fileData, fileName = "assignment_file") => {
    if (!fileData) {
      showToast("No file attached to this submission.", "error");
      return;
    }
    try {
      if (fileData.startsWith("data:")) {
        const parts = fileData.split(";base64,");
        if (parts.length === 2) {
          const contentType = parts[0].split(":")[1] || "application/octet-stream";
          const byteCharacters = atob(parts[1]);
          const byteNumbers = new Array(byteCharacters.length);
          for (let i = 0; i < byteCharacters.length; i++) {
            byteNumbers[i] = byteCharacters.charCodeAt(i);
          }
          const byteArray = new Uint8Array(byteNumbers);
          const blob = new Blob([byteArray], { type: contentType });
          const blobUrl = URL.createObjectURL(blob);
          const link = document.createElement("a");
          link.href = blobUrl;
          link.download = fileName;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          setTimeout(() => URL.revokeObjectURL(blobUrl), 3000);
          return;
        }
      }
      const link = document.createElement("a");
      link.href = fileData;
      link.download = fileName;
      link.target = "_blank";
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      console.error("Error opening file:", err);
      window.open(fileData, "_blank");
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    setFileError(null);
    if (!file) return;

    if (file.size > 10 * 1024 * 1024) {
      setFileError("File size exceeds 10MB limit. Please choose a smaller file.");
      return;
    }

    const reader = new FileReader();
    reader.onload = (event) => {
      setSelectedFile(file);
      setFilePreview({
        name: file.name,
        size: `${(file.size / 1024).toFixed(1)} KB`,
        type: file.type || "application/octet-stream",
        dataUrl: event.target.result,
      });
    };
    reader.onerror = () => {
      setFileError("Failed to read the file. Please try another file.");
    };
    reader.readAsDataURL(file);
  };

  const handleStudentSubmit = async (e) => {
    e.preventDefault();
    if (!submittingAssignment) return;

    setIsSubmittingWork(true);
    setFileError(null);
    try {
      const payload = {
        note: submissionNote || "Uploaded assignment work",
        fileName: filePreview?.name || null,
        fileType: filePreview?.type || null,
        fileSize: filePreview?.size || null,
        fileData: filePreview?.dataUrl || null,
        studentUsername: me.data?.username || auth.user?.username,
        studentName: me.data?.name || auth.user?.name,
        department: me.data?.department || auth.user?.department,
      };

      await customFetch(`/api/assignments/${submittingAssignment.id}/submit`, {
        method: "POST",
        body: JSON.stringify(payload),
      });

      qc.invalidateQueries({ queryKey: getListAssignmentsQueryKey() });
      qc.invalidateQueries({ queryKey: ["assignment-status", submittingAssignment.id] });
      showToast(`Work submitted successfully for ${submittingAssignment.title}!`);
      setSubmittingAssignment(null);
      setSelectedFile(null);
      setFilePreview(null);
      setSubmissionNote("");
    } catch (err) {
      setFileError("Error submitting assignment: " + (err.message || "Failed to submit."));
    } finally {
      setIsSubmittingWork(false);
    }
  };

  const handleSendAlert = async (student) => {
    if (!viewingAssignment || !student) return;
    setAlertingUser(student.username);
    try {
      await customFetch(`/api/assignments/${viewingAssignment.id}/alert`, {
        method: "POST",
        body: JSON.stringify({
          studentId: student.id,
          studentUsername: student.username,
          studentName: student.name,
        }),
      });

      setAlertedUsers((prev) => ({ ...prev, [student.username]: true }));
      showToast(`Deadline alert dispatched to ${student.name} (@${student.username})!`);
    } catch (err) {
      showToast(`Failed to dispatch alert: ${err.message || "Unknown error"}`, "error");
    } finally {
      setAlertingUser(null);
    }
  };

  const createIt = (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    create.mutate(
      {
        data: {
          subjectId: Number(f.get("subjectId")),
          title: String(f.get("title")),
          description: String(f.get("description")),
          dueDate: String(f.get("dueDate")),
          maxMarks: Number(f.get("maxMarks")),
        },
      },
      {
        onSuccess: () => {
          qc.invalidateQueries({ queryKey: getListAssignmentsQueryKey() });
          setModal(false);
          showToast("New assignment created successfully!");
        },
      },
    );
  };

  // Filter students for teacher modal
  const rawStatusData = statusQuery.data;
  const completedList = rawStatusData?.completed ?? [];
  const pendingList = rawStatusData?.incomplete ?? [];

  const filterStudent = (s) => {
    if (deptFilter !== "all" && s.department !== deptFilter) return false;
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const matchName = s.name?.toLowerCase().includes(q);
      const matchUser = s.username?.toLowerCase().includes(q);
      const matchId = s.studentId?.toLowerCase().includes(q);
      return matchName || matchUser || matchId;
    }
    return true;
  };

  const visibleCompleted = completedList.filter(filterStudent);
  const visiblePending = pendingList.filter(filterStudent);

  return (
    <>
      <PageHead
        eyebrow="Academic"
        title="Assignments"
        description="Manage briefs, student file submissions (PDF, Word, Images), completion tracking, and student alerts."
        action={
          isFaculty && (
            <button
              onClick={() => setModal(true)}
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground shadow-sm hover:bg-primary/90 transition-colors"
              data-testid="button-create-assignment"
            >
              <Plus className="mr-2 inline size-4" /> New assignment
            </button>
          )
        }
      />

      {toast && (
        <div
          className={`mb-4 flex items-center justify-between rounded-xl border p-4 text-sm shadow-sm rise ${
            toast.type === "error"
              ? "border-destructive/30 bg-destructive/10 text-destructive"
              : "border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-400"
          }`}
        >
          <div className="flex items-center gap-2">
            <CheckCircle2 className="size-4" />
            <span className="font-medium">{toast.message}</span>
          </div>
          <button onClick={() => setToast(null)} className="opacity-60 hover:opacity-100">
            <X className="size-4" />
          </button>
        </div>
      )}

      <div className="space-y-3">
        {q.isLoading ? (
          <SkeletonRows count={5} />
        ) : q.isError ? (
          <ErrorState retry={() => q.refetch()} />
        ) : list.length === 0 ? (
          <Card>
            <Empty label="assignments" />
          </Card>
        ) : (
          list.map((a, i) => (
            <Card
              key={a.id}
              onClick={() => {
                if (isFaculty) {
                  setViewingAssignment(a);
                } else {
                  setStudentViewingAssignment(a);
                }
              }}
              className={`p-5 rise delay-${(i % 4) + 1} cursor-pointer hover:border-primary/50 transition-all hover:shadow-md`}
            >
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
                <div className="grid size-11 shrink-0 place-items-center rounded-xl bg-accent/30 text-primary">
                  <FileText className="size-5" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 className="font-semibold text-base">{a.title}</h2>
                    {!isFaculty && (
                      <Status tone={a.submitted ? "positive" : a.status === "due" ? "warning" : "neutral"}>
                        {a.submitted ? "Submitted" : a.status || "Pending"}
                      </Status>
                    )}
                  </div>
                  <p className="mt-1 text-sm text-muted-foreground">
                    <span className="font-medium text-foreground/80">{a.subject}</span> · Due {a.dueDate} · Max Marks: {a.maxMarks || 100}
                  </p>
                  {a.description && (
                    <p className="mt-1 text-xs text-muted-foreground line-clamp-1">
                      {a.description}
                    </p>
                  )}
                </div>

                <div className="flex items-center gap-3 text-xs text-muted-foreground flex-wrap sm:flex-nowrap">
                  <div className="flex items-center gap-1.5 bg-muted/50 px-3 py-1.5 rounded-lg border border-border/50 shrink-0">
                    <Users className="size-3.5" />
                    <span className="font-medium">
                      {a.submissions || 0} {isFaculty && a.totalStudents ? `/ ${a.totalStudents}` : ""} submissions
                    </span>
                  </div>

                  {!isFaculty ? (
                    <div className="flex items-center gap-2 shrink-0">
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setStudentViewingAssignment(a);
                        }}
                        className="inline-flex items-center gap-1.5 rounded-lg border border-border bg-card hover:bg-muted px-3 py-1.5 font-semibold text-foreground transition-all shadow-sm"
                        data-testid={`button-view-details-${a.id}`}
                        title="View assignment brief, instructions, and your submission file"
                      >
                        <Eye className="size-3.5 text-primary" />
                        <span>View Details</span>
                      </button>

                      {a.submitted ? (
                        <span className="inline-flex items-center gap-1 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 px-3 py-1.5 font-semibold text-xs border border-emerald-500/20">
                          <Check className="size-3.5" /> Submitted
                        </span>
                      ) : (
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            setSubmittingAssignment(a);
                            setSelectedFile(null);
                            setFilePreview(null);
                            setSubmissionNote("");
                            setFileError(null);
                          }}
                          className="inline-flex items-center gap-1.5 rounded-lg bg-primary px-3.5 py-1.5 font-semibold text-primary-foreground shadow-sm hover:bg-primary/90 transition-all hover:scale-[1.02]"
                          data-testid={`button-submit-assignment-${a.id}`}
                        >
                          <Upload className="size-3.5" /> Submit work
                        </button>
                      )}
                    </div>
                  ) : (
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        setViewingAssignment(a);
                      }}
                      className="inline-flex items-center gap-1.5 rounded-lg bg-primary/10 hover:bg-primary text-primary hover:text-primary-foreground px-3.5 py-2 font-semibold text-xs transition-all shadow-sm shrink-0"
                      data-testid={`button-view-assignment-${a.id}`}
                    >
                      <span>View details & students</span>
                      <ChevronRight className="size-3.5" />
                    </button>
                  )}
                </div>
              </div>
            </Card>
          ))
        )}
      </div>

      {/* Student: Submit Work Modal with File Picker */}
      {submittingAssignment && (
        <Modal
          title="Submit Assignment Work"
          subtitle={`Upload your solution file (PDF, Word, Image) for ${submittingAssignment.title}`}
          maxWidth="max-w-xl"
          onClose={() => {
            setSubmittingAssignment(null);
            setSelectedFile(null);
            setFilePreview(null);
          }}
        >
          <form onSubmit={handleStudentSubmit} className="space-y-4">
            <div className="rounded-xl border border-border bg-muted/30 p-3.5 text-xs space-y-1">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Subject:</span>
                <span className="font-semibold text-foreground">{submittingAssignment.subject}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Due Date:</span>
                <span className="font-medium text-amber-600 dark:text-amber-400">{submittingAssignment.dueDate}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Max Marks:</span>
                <span className="font-medium">{submittingAssignment.maxMarks || 100}</span>
              </div>
              {submittingAssignment.description && (
                <p className="mt-2 text-muted-foreground pt-1 border-t border-border">
                  {submittingAssignment.description}
                </p>
              )}
            </div>

            {/* File Upload Section */}
            <div>
              <label className="block text-xs font-semibold text-muted-foreground mb-1.5">
                Upload Solution File (PDF, Word, or Image) *
              </label>

              {!filePreview ? (
                <label className="flex flex-col items-center justify-center border-2 border-dashed border-border hover:border-primary/70 rounded-xl p-6 cursor-pointer bg-background hover:bg-muted/20 transition-all text-center group">
                  <div className="grid size-12 place-items-center rounded-xl bg-primary/10 text-primary group-hover:scale-110 transition-transform mb-2">
                    <Upload className="size-6" />
                  </div>
                  <span className="text-sm font-semibold text-foreground">
                    Click to select or drag & drop file
                  </span>
                  <span className="text-xs text-muted-foreground mt-1">
                    Accepts PDF (.pdf), Word (.doc, .docx), or Image (.png, .jpg, .jpeg) up to 10MB
                  </span>
                  <input
                    type="file"
                    required
                    accept=".pdf,.doc,.docx,.png,.jpg,.jpeg,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,image/*"
                    onChange={handleFileChange}
                    className="hidden"
                    data-testid="input-assignment-file"
                  />
                </label>
              ) : (
                <div className="rounded-xl border border-primary/30 bg-primary/5 p-4 flex items-center justify-between">
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="grid size-10 place-items-center rounded-lg bg-primary/20 text-primary shrink-0">
                      {filePreview.type?.startsWith("image/") ? (
                        <Paperclip className="size-5" />
                      ) : (
                        <FileText className="size-5" />
                      )}
                    </div>
                    <div className="min-w-0">
                      <p className="text-sm font-semibold text-foreground truncate">
                        {filePreview.name}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {filePreview.size} · {filePreview.type || "Document"}
                      </p>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      setSelectedFile(null);
                      setFilePreview(null);
                    }}
                    className="rounded-lg p-1.5 text-muted-foreground hover:bg-muted hover:text-destructive transition-colors ml-2"
                    title="Remove file"
                  >
                    <X className="size-4" />
                  </button>
                </div>
              )}

              {fileError && (
                <p className="mt-1.5 text-xs text-destructive flex items-center gap-1">
                  <AlertCircle className="size-3.5" /> {fileError}
                </p>
              )}
            </div>

            {/* Notes / Comments */}
            <div>
              <label className="block text-xs font-semibold text-muted-foreground mb-1.5">
                Submission Notes or Comments (Optional)
              </label>
              <textarea
                value={submissionNote}
                onChange={(e) => setSubmissionNote(e.target.value)}
                placeholder="Include any notes, repository links, or explanations for your professor..."
                className="min-h-20 w-full rounded-xl border border-input bg-background p-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
                data-testid="input-submission-note"
              />
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={() => {
                  setSubmittingAssignment(null);
                  setSelectedFile(null);
                  setFilePreview(null);
                }}
                className="rounded-lg border border-border px-4 py-2 text-sm font-medium hover:bg-muted transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isSubmittingWork || !filePreview}
                className="rounded-lg bg-primary px-5 py-2 text-sm font-semibold text-primary-foreground shadow-sm hover:bg-primary/90 disabled:opacity-50 transition-all"
                data-testid="button-confirm-submit-work"
              >
                {isSubmittingWork ? "Uploading & Submitting..." : "Submit Assignment"}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Student: Full Assignment Details, Submission Status & Solution File Modal */}
      {studentViewingAssignment && (
        <Modal
          title={studentViewingAssignment.title}
          subtitle={`${studentViewingAssignment.subject} · Max Marks: ${studentViewingAssignment.maxMarks || 100}`}
          maxWidth="max-w-2xl"
          onClose={() => setStudentViewingAssignment(null)}
        >
          <div className="space-y-4">
            {/* Header Metrics */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5 text-xs">
              <div className="rounded-xl border border-border bg-muted/30 p-3">
                <span className="text-muted-foreground block mb-0.5">Subject</span>
                <span className="font-semibold text-foreground truncate block">{studentViewingAssignment.subject}</span>
              </div>
              <div className="rounded-xl border border-border bg-muted/30 p-3">
                <span className="text-muted-foreground block mb-0.5">Due Date</span>
                <span className="font-semibold text-amber-600 dark:text-amber-400 block">{studentViewingAssignment.dueDate}</span>
              </div>
              <div className="col-span-2 sm:col-span-1 rounded-xl border border-border bg-muted/30 p-3">
                <span className="text-muted-foreground block mb-0.5">Your Submission Status</span>
                <Status tone={activeStudentSub || studentViewingAssignment.submitted ? "positive" : "warning"}>
                  {activeStudentSub || studentViewingAssignment.submitted ? "Submitted" : "Pending Submission"}
                </Status>
              </div>
            </div>

            {/* Assignment Brief & Description */}
            <div className="rounded-xl border border-border bg-card p-4">
              <h4 className="text-xs font-bold uppercase tracking-wider text-muted-foreground mb-2 flex items-center gap-1.5">
                <FileText className="size-3.5 text-primary" /> Assignment Brief & Instructions
              </h4>
              <p className="text-sm text-foreground leading-relaxed whitespace-pre-line">
                {studentViewingAssignment.description || "Complete the solution and upload your document (PDF, Word, or Image) before the deadline."}
              </p>
            </div>

            {/* Submission Details & Attached File Section */}
            {activeStudentSub ? (
              <div className="rounded-xl border border-emerald-500/30 bg-emerald-500/5 p-4 space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="size-5 text-emerald-600 dark:text-emerald-400" />
                    <div>
                      <h4 className="font-bold text-sm text-emerald-800 dark:text-emerald-300">
                        Work Submitted Successfully
                      </h4>
                      <p className="text-[11px] text-muted-foreground">
                        Recorded on {activeStudentSub.submittedAt || "Recently"}
                      </p>
                    </div>
                  </div>
                  <span className="rounded-full bg-emerald-500/20 px-2.5 py-0.5 text-xs font-semibold text-emerald-700 dark:text-emerald-300">
                    Received
                  </span>
                </div>

                {activeStudentSub.note && (
                  <div className="rounded-lg bg-card p-3 border border-border text-xs">
                    <span className="text-muted-foreground font-semibold block mb-0.5">Your Submission Note:</span>
                    <p className="text-foreground italic">"{activeStudentSub.note}"</p>
                  </div>
                )}

                {/* Uploaded File View & Download */}
                {activeStudentSub.fileData ? (
                  <div className="rounded-xl border border-emerald-500/20 bg-card p-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-sm">
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="grid size-10 place-items-center rounded-lg bg-emerald-500/10 text-emerald-600 shrink-0">
                        {activeStudentSub.fileType?.startsWith("image/") ? (
                          <Paperclip className="size-5" />
                        ) : (
                          <FileText className="size-5" />
                        )}
                      </div>
                      <div className="min-w-0">
                        <p className="text-sm font-semibold text-foreground truncate">
                          {activeStudentSub.fileName || "Solution_File"}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {activeStudentSub.fileSize || "Attachment"} · {activeStudentSub.fileType || "Document"}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 shrink-0">
                      {activeStudentSub.fileType?.startsWith("image/") && (
                        <button
                          type="button"
                          onClick={() => setPreviewImageModal({
                            url: activeStudentSub.fileData,
                            title: activeStudentSub.fileName || "Submitted Diagram"
                          })}
                          className="inline-flex items-center gap-1.5 rounded-lg border border-border bg-muted/50 hover:bg-muted px-3 py-1.5 text-xs font-semibold text-foreground transition-all"
                        >
                          <Eye className="size-3.5 text-primary" /> Preview
                        </button>
                      )}
                      <button
                        type="button"
                        onClick={() => downloadOrOpenFile(activeStudentSub.fileData, activeStudentSub.fileName || "submission_solution")}
                        className="inline-flex items-center gap-1.5 rounded-lg bg-primary hover:bg-primary/90 text-primary-foreground px-3.5 py-1.5 text-xs font-semibold shadow-sm transition-all hover:scale-[1.02]"
                        title="Download or view file"
                      >
                        <Download className="size-3.5" /> Download File
                      </button>
                    </div>
                  </div>
                ) : (
                  <p className="text-xs text-muted-foreground italic">Text-only submission (no file attached).</p>
                )}

                <div className="flex justify-end pt-1">
                  <button
                    type="button"
                    onClick={() => {
                      const assign = studentViewingAssignment;
                      setStudentViewingAssignment(null);
                      setSubmittingAssignment(assign);
                      setSelectedFile(null);
                      setFilePreview(null);
                      setSubmissionNote(activeStudentSub.note || "");
                      setFileError(null);
                    }}
                    className="text-xs font-semibold text-primary hover:underline inline-flex items-center gap-1"
                  >
                    <Upload className="size-3.5" /> Resubmit / Upload New Solution File
                  </button>
                </div>
              </div>
            ) : (
              <div className="rounded-xl border border-amber-500/30 bg-amber-500/5 p-4 space-y-3">
                <div className="flex items-start gap-3">
                  <Clock3 className="size-5 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
                  <div>
                    <h4 className="font-bold text-sm text-amber-800 dark:text-amber-300">
                      Solution Submission Pending
                    </h4>
                    <p className="text-xs text-muted-foreground mt-0.5">
                      You have not uploaded a solution for this assignment yet. Attach your PDF, Word, or Image file before the due date ({studentViewingAssignment.dueDate}).
                    </p>
                  </div>
                </div>

                <div className="pt-2 flex justify-end">
                  <button
                    type="button"
                    onClick={() => {
                      const assign = studentViewingAssignment;
                      setStudentViewingAssignment(null);
                      setSubmittingAssignment(assign);
                      setSelectedFile(null);
                      setFilePreview(null);
                      setSubmissionNote("");
                      setFileError(null);
                    }}
                    className="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground shadow-sm hover:bg-primary/90 transition-all hover:scale-[1.02]"
                    data-testid="button-open-submit-from-details"
                  >
                    <Upload className="size-4" /> Submit Solution Work Now
                  </button>
                </div>
              </div>
            )}

            <div className="flex justify-end pt-2 border-t border-border">
              <button
                type="button"
                onClick={() => setStudentViewingAssignment(null)}
                className="rounded-lg border border-border px-4 py-2 text-sm font-semibold text-foreground hover:bg-muted transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* In-App Image / Diagram Preview Modal */}
      {previewImageModal && (
        <Modal
          title={previewImageModal.title || "File Preview"}
          maxWidth="max-w-3xl"
          onClose={() => setPreviewImageModal(null)}
        >
          <div className="space-y-3 text-center">
            <div className="max-h-[70vh] overflow-auto rounded-xl border border-border bg-muted/20 p-2 flex items-center justify-center">
              <img
                src={previewImageModal.url}
                alt="Submission preview"
                className="max-h-[65vh] w-auto object-contain rounded-lg shadow-sm"
              />
            </div>
            <div className="flex justify-end gap-2 pt-2 border-t border-border">
              <button
                type="button"
                onClick={() => downloadOrOpenFile(previewImageModal.url, previewImageModal.title || "diagram")}
                className="inline-flex items-center gap-1.5 rounded-lg bg-primary px-4 py-2 text-xs font-semibold text-primary-foreground shadow-sm hover:bg-primary/90"
              >
                <Download className="size-3.5" /> Download Image
              </button>
              <button
                type="button"
                onClick={() => setPreviewImageModal(null)}
                className="rounded-lg border border-border px-4 py-2 text-xs font-semibold hover:bg-muted"
              >
                Close Preview
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Teacher: Assignment Status, Submissions, File Downloads, and Student Alerts */}
      {viewingAssignment && (
        <Modal
          title="Assignment Completion & Student Submissions"
          subtitle={`${viewingAssignment.title} · ${viewingAssignment.subject}`}
          maxWidth="max-w-4xl"
          onClose={() => setViewingAssignment(null)}
        >
          <div className="space-y-4">
            {/* Assignment Details Header Brief */}
            <div className="rounded-xl border border-border bg-muted/20 p-4 space-y-2">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <span className="font-semibold text-base text-foreground">
                  {viewingAssignment.title}
                </span>
                <span className="text-xs font-semibold text-primary bg-primary/10 px-2.5 py-1 rounded-md">
                  {viewingAssignment.subject}
                </span>
              </div>
              <div className="flex flex-wrap gap-4 text-xs text-muted-foreground">
                <div>Due Date: <span className="font-medium text-foreground">{viewingAssignment.dueDate}</span></div>
                <div>Max Marks: <span className="font-medium text-foreground">{viewingAssignment.maxMarks || 100}</span></div>
              </div>
              {viewingAssignment.description && (
                <p className="text-xs text-muted-foreground pt-1.5 border-t border-border leading-relaxed whitespace-pre-line">
                  {viewingAssignment.description}
                </p>
              )}
            </div>

            {/* Top Metric Cards */}
            <div className="grid grid-cols-3 gap-3">
              <div className="rounded-xl border border-border bg-card p-4 text-center">
                <div className="text-2xl font-bold text-foreground">
                  {rawStatusData?.totalStudents ?? 25}
                </div>
                <div className="text-xs font-medium uppercase tracking-wider text-muted-foreground mt-1">
                  Enrolled Students
                </div>
              </div>
              <div className="rounded-xl border border-emerald-500/30 bg-emerald-500/10 p-4 text-center">
                <div className="text-2xl font-bold text-emerald-600 dark:text-emerald-400">
                  {rawStatusData?.completedCount ?? completedList.length}
                </div>
                <div className="text-xs font-medium uppercase tracking-wider text-emerald-700 dark:text-emerald-400 mt-1">
                  Completed
                </div>
              </div>
              <div className="rounded-xl border border-amber-500/30 bg-amber-500/10 p-4 text-center">
                <div className="text-2xl font-bold text-amber-600 dark:text-amber-400">
                  {rawStatusData?.incompleteCount ?? pendingList.length}
                </div>
                <div className="text-xs font-medium uppercase tracking-wider text-amber-700 dark:text-amber-400 mt-1">
                  Pending Submission
                </div>
              </div>
            </div>

            {/* Filter & Search Bar */}
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 border-y border-border py-3">
              {/* Status Tabs */}
              <div className="flex items-center gap-1.5 bg-muted/50 p-1 rounded-lg border border-border">
                <button
                  onClick={() => setStatusTab("all")}
                  className={`px-3 py-1 text-xs font-semibold rounded-md transition-colors ${
                    statusTab === "all" ? "bg-card text-foreground shadow-sm" : "text-muted-foreground hover:text-foreground"
                  }`}
                >
                  All ({completedList.length + pendingList.length})
                </button>
                <button
                  onClick={() => setStatusTab("completed")}
                  className={`px-3 py-1 text-xs font-semibold rounded-md transition-colors ${
                    statusTab === "completed" ? "bg-emerald-600 text-white shadow-sm" : "text-muted-foreground hover:text-foreground"
                  }`}
                >
                  Completed ({completedList.length})
                </button>
                <button
                  onClick={() => setStatusTab("pending")}
                  className={`px-3 py-1 text-xs font-semibold rounded-md transition-colors ${
                    statusTab === "pending" ? "bg-amber-600 text-white shadow-sm" : "text-muted-foreground hover:text-foreground"
                  }`}
                >
                  Pending ({pendingList.length})
                </button>
              </div>

              {/* Department Dropdown / Filter */}
              <div className="flex items-center gap-2">
                <select
                  value={deptFilter}
                  onChange={(e) => setDeptFilter(e.target.value)}
                  className="h-8 rounded-lg border border-input bg-background px-2.5 text-xs font-medium"
                >
                  <option value="all">All Departments</option>
                  <option value="CSE">CSE</option>
                  <option value="ECE">ECE</option>
                  <option value="MECH">MECH</option>
                  <option value="CIVIL">CIVIL</option>
                  <option value="EEE">EEE</option>
                </select>

                <div className="relative">
                  <Search className="size-3.5 absolute left-2.5 top-2.5 text-muted-foreground" />
                  <input
                    type="text"
                    placeholder="Search student..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="h-8 pl-8 pr-3 text-xs rounded-lg border border-input bg-background focus:outline-none focus:ring-1 focus:ring-primary w-36 sm:w-44"
                  />
                </div>
              </div>
            </div>

            {/* Students List Area */}
            <div className="space-y-4 max-h-[50vh] overflow-y-auto pr-1">
              {statusQuery.isLoading ? (
                <div className="p-8 text-center text-muted-foreground">
                  <SkeletonRows count={4} />
                </div>
              ) : (
                <>
                  {/* Completed Students Section */}
                  {(statusTab === "all" || statusTab === "completed") && (
                    <div className="space-y-2.5">
                      <div className="flex items-center justify-between">
                        <h4 className="text-xs font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-400 flex items-center gap-1.5">
                          <CheckCircle2 className="size-4" /> Completed Submissions ({visibleCompleted.length})
                        </h4>
                      </div>

                      {visibleCompleted.length === 0 ? (
                        <div className="rounded-xl border border-dashed border-border p-4 text-center text-xs text-muted-foreground">
                          {deptFilter !== "all" || searchQuery
                            ? "No completed submissions match your filter."
                            : "No students have submitted work yet."}
                        </div>
                      ) : (
                        <div className="space-y-2">
                          {visibleCompleted.map((s) => (
                            <div
                              key={s.submissionId || s.id}
                              className="rounded-xl border border-emerald-500/20 bg-card p-3.5 shadow-sm transition-all hover:border-emerald-500/40"
                            >
                              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                                <div className="flex items-start gap-3">
                                  <div className="grid size-9 place-items-center rounded-lg bg-emerald-500/10 text-emerald-600 font-bold text-xs shrink-0">
                                    {s.name?.charAt(0) || "S"}
                                  </div>
                                  <div>
                                    <div className="flex items-center gap-2">
                                      <span className="font-semibold text-sm text-foreground">{s.name}</span>
                                      <span className="text-xs text-muted-foreground font-mono">@{s.username}</span>
                                      <span className="rounded-md bg-secondary px-1.5 py-0.5 text-[10px] font-semibold uppercase">
                                        {s.department || "ENG"}
                                      </span>
                                    </div>
                                    <p className="text-xs text-muted-foreground mt-0.5">
                                      ID: {s.studentId} · Submitted: {s.submittedAt || "Recently"}
                                    </p>
                                    {s.note && (
                                      <p className="text-xs text-foreground/90 mt-1 italic bg-muted/30 px-2 py-1 rounded">
                                        "{s.note}"
                                      </p>
                                    )}
                                  </div>
                                </div>

                                {/* Uploaded File Download / View Button */}
                                <div className="shrink-0 flex items-center gap-2">
                                  {s.fileData ? (
                                    <>
                                      {s.fileType?.startsWith("image/") && (
                                        <button
                                          type="button"
                                          onClick={() => setPreviewImageModal({
                                            url: s.fileData,
                                            title: `${s.name} - ${s.fileName || "Diagram"}`
                                          })}
                                          className="inline-flex items-center gap-1 rounded-lg border border-border bg-muted/50 hover:bg-muted px-2.5 py-1.5 text-xs font-semibold text-foreground transition-all"
                                          title="Preview student diagram or image"
                                        >
                                          <Eye className="size-3.5 text-primary" />
                                          <span>Preview</span>
                                        </button>
                                      )}
                                      <button
                                        type="button"
                                        onClick={() => downloadOrOpenFile(s.fileData, s.fileName || `${s.username}_submission`)}
                                        className="inline-flex items-center gap-1.5 rounded-lg bg-primary/10 hover:bg-primary/20 text-primary px-3 py-1.5 text-xs font-semibold border border-primary/20 transition-all hover:scale-[1.02]"
                                        title="Download or view student file"
                                      >
                                        <Download className="size-3.5" />
                                        <span>{s.fileName || "Download File"}</span>
                                        {s.fileSize && (
                                          <span className="text-[10px] opacity-75 font-mono">({s.fileSize})</span>
                                        )}
                                      </button>
                                    </>
                                  ) : (
                                    <span className="text-xs text-muted-foreground italic">
                                      Text submission only
                                    </span>
                                  )}
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}

                  {/* Pending Students Section with Alert Button */}
                  {(statusTab === "all" || statusTab === "pending") && (
                    <div className="space-y-2.5 pt-2">
                      <div className="flex items-center justify-between">
                        <h4 className="text-xs font-bold uppercase tracking-wider text-amber-600 dark:text-amber-400 flex items-center gap-1.5">
                          <Clock3 className="size-4" /> Pending / Non-Completed Students ({visiblePending.length})
                        </h4>
                      </div>

                      {visiblePending.length === 0 ? (
                        <div className="rounded-xl border border-dashed border-border p-4 text-center text-xs text-muted-foreground">
                          {deptFilter !== "all" || searchQuery
                            ? "No pending students match your filter."
                            : "All enrolled students have completed this assignment!"}
                        </div>
                      ) : (
                        <div className="space-y-2">
                          {visiblePending.map((s) => {
                            const isAlerted = alertedUsers[s.username];
                            const isAlerting = alertingUser === s.username;
                            return (
                              <div
                                key={s.id}
                                className="rounded-xl border border-border bg-card p-3.5 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:border-amber-500/40 transition-colors"
                              >
                                <div className="flex items-start gap-3">
                                  <div className="grid size-9 place-items-center rounded-lg bg-amber-500/10 text-amber-600 font-bold text-xs shrink-0">
                                    {s.name?.charAt(0) || "S"}
                                  </div>
                                  <div>
                                    <div className="flex items-center gap-2">
                                      <span className="font-semibold text-sm text-foreground">{s.name}</span>
                                      <span className="text-xs text-muted-foreground font-mono">@{s.username}</span>
                                      <span className="rounded-md bg-secondary px-1.5 py-0.5 text-[10px] font-semibold uppercase">
                                        {s.department || "ENG"}
                                      </span>
                                    </div>
                                    <p className="text-xs text-muted-foreground mt-0.5">
                                      ID: {s.studentId} · {s.email}
                                    </p>
                                  </div>
                                </div>

                                {/* Action: ALERT BUTTON */}
                                <div className="shrink-0 flex items-center gap-2">
                                  <Status tone="warning">Pending</Status>
                                  <button
                                    onClick={() => handleSendAlert(s)}
                                    disabled={isAlerted || isAlerting}
                                    className={`inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-semibold transition-all ${
                                      isAlerted
                                        ? "bg-emerald-600 text-white cursor-default"
                                        : "bg-amber-500 hover:bg-amber-600 text-white shadow-sm hover:scale-105"
                                    }`}
                                    data-testid={`button-alert-student-${s.username}`}
                                    title="Send reminder notification to this student"
                                  >
                                    {isAlerted ? (
                                      <>
                                        <Check className="size-3.5" /> Alert Sent ✔
                                      </>
                                    ) : isAlerting ? (
                                      <span>Sending…</span>
                                    ) : (
                                      <>
                                        <Bell className="size-3.5" /> Alert Student
                                      </>
                                    )}
                                  </button>
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      )}
                    </div>
                  )}
                </>
              )}
            </div>

            <div className="flex justify-end pt-2 border-t border-border">
              <button
                onClick={() => setViewingAssignment(null)}
                className="rounded-lg bg-secondary px-4 py-2 text-sm font-semibold text-secondary-foreground hover:bg-secondary/80"
              >
                Close
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Modal: Create Assignment (Faculty) */}
      {modal && (
        <Modal title="Create assignment" onClose={() => setModal(false)}>
          <form onSubmit={createIt} className="grid gap-4">
            <label className="text-sm">
              <span className="mb-1.5 block text-xs font-semibold text-muted-foreground">
                Subject
              </span>
              <select
                name="subjectId"
                required
                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm"
                data-testid="select-assignment-subject"
              >
                {(subjects.data ?? []).map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.code} · {s.name} ({s.department})
                  </option>
                ))}
              </select>
            </label>
            <Field label="Title" name="title" />
            <label className="text-sm">
              <span className="mb-1.5 block text-xs font-semibold text-muted-foreground">
                Description
              </span>
              <textarea
                name="description"
                required
                placeholder="Details of assignment, requirements, and submission format..."
                className="min-h-24 w-full rounded-lg border border-input bg-background p-3 text-sm"
                data-testid="input-assignment-description"
              />
            </label>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Due date" name="dueDate" type="date" />
              <Field
                label="Max marks"
                name="maxMarks"
                type="number"
                defaultValue={100}
              />
            </div>
            <button
              disabled={create.isPending}
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground hover:bg-primary/90 transition-colors"
              data-testid="button-save-assignment"
            >
              {create.isPending ? "Creating…" : "Create assignment"}
            </button>
          </form>
        </Modal>
      )}
    </>
  );
}

function Announcements() {
  const [modal, setModal] = useState(false);
  const q = useListAnnouncements();
  const create = useCreateAnnouncement();
  const qc = useQueryClient();
  const list = q.data ?? [];
  const submit = (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    create.mutate(
      {
        data: {
          title: String(f.get("title")),
          content: String(f.get("content")),
          audience: String(f.get("audience")),
          priority: String(f.get("priority")),
        },
      },
      {
        onSuccess: () => {
          qc.invalidateQueries({ queryKey: getListAnnouncementsQueryKey() });
          setModal(false);
        },
      },
    );
  };
  return (
    <>
      <PageHead
        eyebrow="Campus life"
        title="Announcements"
        description="Published notices for the people who make campus happen."
        action={
          <button
            onClick={() => setModal(true)}
            className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
            data-testid="button-create-announcement"
          >
            <Plus className="mr-2 inline size-4" /> Publish notice
          </button>
        }
      />
      <div className="grid gap-4 lg:grid-cols-2">
        {q.isLoading ? (
          <SkeletonRows count={4} />
        ) : q.isError ? (
          <ErrorState retry={() => q.refetch()} />
        ) : list.length === 0 ? (
          <Card className="lg:col-span-2">
            <Empty label="announcements" />
          </Card>
        ) : (
          list.map((a, i) => (
            <Card key={a.id} className={`p-6 rise delay-${(i % 4) + 1}`}>
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-center gap-2 text-[11px] font-bold uppercase tracking-wider text-primary">
                  <Bell className="size-3.5" /> {a.audience}
                </div>
                <Status
                  tone={
                    a.priority === "urgent"
                      ? "critical"
                      : a.priority === "important"
                        ? "warning"
                        : "neutral"
                  }
                >
                  {a.priority}
                </Status>
              </div>
              <h2 className="display mt-5 text-2xl font-semibold">{a.title}</h2>
              <p className="mt-3 text-sm leading-6 text-muted-foreground">
                {a.content}
              </p>
              <div className="mt-6 flex items-center justify-between border-t border-border pt-4 text-xs text-muted-foreground">
                <span>{a.author}</span>
                <span>{a.publishedAt}</span>
              </div>
            </Card>
          ))
        )}
      </div>
      {modal && (
        <Modal title="Publish notice" onClose={() => setModal(false)}>
          <form onSubmit={submit} className="grid gap-4">
            <Field label="Title" name="title" />
            <label className="text-sm">
              <span className="mb-1.5 block text-xs font-semibold text-muted-foreground">
                Message
              </span>
              <textarea
                name="content"
                required
                className="min-h-28 w-full rounded-lg border border-input bg-background p-3 text-sm"
                data-testid="input-announcement-content"
              />
            </label>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field
                label="Audience"
                name="audience"
                defaultValue="All campus"
              />
              <label className="text-sm">
                <span className="mb-1.5 block text-xs font-semibold text-muted-foreground">
                  Priority
                </span>
                <select
                  name="priority"
                  className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm"
                  data-testid="select-announcement-priority"
                >
                  <option value="normal">Normal</option>
                  <option value="important">Important</option>
                  <option value="urgent">Urgent</option>
                </select>
              </label>
            </div>
            <button
              disabled={create.isPending}
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
              data-testid="button-save-announcement"
            >
              {create.isPending ? "Publishing…" : "Publish notice"}
            </button>
          </form>
        </Modal>
      )}
    </>
  );
}

function Events() {
  const [modal, setModal] = useState(false);
  const q = useListEvents();
  const create = useCreateEvent();
  const register = useRegisterForEvent();
  const qc = useQueryClient();
  const list = q.data ?? [];
  const submit = (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    create.mutate(
      {
        data: {
          title: String(f.get("title")),
          description: String(f.get("description")),
          date: String(f.get("date")),
          time: String(f.get("time")),
          venue: String(f.get("venue")),
          organizer: String(f.get("organizer")),
          capacity: Number(f.get("capacity")),
        },
      },
      {
        onSuccess: () => {
          qc.invalidateQueries({ queryKey: getListEventsQueryKey() });
          setModal(false);
        },
      },
    );
  };
  return (
    <>
      <PageHead
        eyebrow="Campus life"
        title="Events"
        description="Make room for the lectures, gatherings, and moments between classes."
        action={
          <button
            onClick={() => setModal(true)}
            className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
            data-testid="button-create-event"
          >
            <Plus className="mr-2 inline size-4" /> Create event
          </button>
        }
      />
      <div className="grid gap-4 md:grid-cols-2">
        {q.isLoading ? (
          <SkeletonRows count={4} />
        ) : q.isError ? (
          <ErrorState retry={() => q.refetch()} />
        ) : list.length === 0 ? (
          <Card className="md:col-span-2">
            <Empty label="events" />
          </Card>
        ) : (
          list.map((e, i) => (
            <Card
              key={e.id}
              className={`overflow-hidden rise delay-${(i % 4) + 1}`}
            >
              <div className="flex gap-5 border-b border-border bg-primary/5 p-5">
                <div className="w-14 shrink-0 text-center">
                  <div className="text-xs font-bold uppercase text-primary">
                    {e.date?.slice(0, 3) || "SEP"}
                  </div>
                  <div className="display text-3xl font-semibold">
                    {e.date?.slice(-2) || "24"}
                  </div>
                </div>
                <div>
                  <h2 className="display text-2xl font-semibold">{e.title}</h2>
                  <p className="mt-1 text-xs text-muted-foreground">
                    {e.time} · {e.venue}
                  </p>
                </div>
              </div>
              <div className="p-5">
                <p className="text-sm leading-6 text-muted-foreground">
                  {e.description}
                </p>
                <div className="mt-6 flex items-center justify-between text-xs text-muted-foreground">
                  <span>
                    {e.registered} / {e.capacity} registered
                  </span>
                  <button
                    disabled={e.registeredByMe || register.isPending}
                    onClick={() =>
                      register.mutate(
                        { id: e.id },
                        {
                          onSuccess: () =>
                            qc.invalidateQueries({
                              queryKey: getListEventsQueryKey(),
                            }),
                        },
                      )
                    }
                    className={`rounded-lg px-3 py-2 font-semibold ${e.registeredByMe ? "bg-muted text-muted-foreground" : "bg-primary text-primary-foreground"}`}
                    data-testid={`button-register-event-${e.id}`}
                  >
                    {e.registeredByMe ? "Registered" : "Register"}
                  </button>
                </div>
              </div>
            </Card>
          ))
        )}
      </div>
      {modal && (
        <Modal title="Create event" onClose={() => setModal(false)}>
          <form onSubmit={submit} className="grid gap-4">
            <Field label="Event title" name="title" />
            <label className="text-sm">
              <span className="mb-1.5 block text-xs font-semibold text-muted-foreground">
                Description
              </span>
              <textarea
                name="description"
                required
                className="min-h-20 w-full rounded-lg border border-input bg-background p-3 text-sm"
                data-testid="input-event-description"
              />
            </label>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Date" name="date" type="date" />
              <Field label="Time" name="time" defaultValue="10:00 AM" />
              <Field label="Venue" name="venue" />
              <Field label="Organizer" name="organizer" />
              <Field
                label="Capacity"
                name="capacity"
                type="number"
                defaultValue={100}
              />
            </div>
            <button
              disabled={create.isPending}
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
              data-testid="button-save-event"
            >
              {create.isPending ? "Creating…" : "Create event"}
            </button>
          </form>
        </Modal>
      )}
    </>
  );
}

function Complaints() {
  const [modal, setModal] = useState(false);
  const me = useGetMe();
  const isAdmin = me.data?.role === "admin";
  const isTeacher = me.data?.role === "faculty";
  const q = useListComplaints();
  const create = useCreateComplaint();
  const update = useUpdateComplaintStatus();
  const qc = useQueryClient();
  const list = q.data ?? [];

  const submit = (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    create.mutate(
      {
        data: {
          title: String(f.get("title")),
          category: String(f.get("category")),
          description: String(f.get("description")),
          priority: String(f.get("priority")),
          student: me.data?.name || (isTeacher ? "Faculty Member" : "Student"),
          senderUsername: me.data?.username || "user",
          senderRole: isTeacher ? "faculty" : "student",
          department: me.data?.department || "General",
        },
      },
      {
        onSuccess: () => {
          qc.invalidateQueries({ queryKey: getListComplaintsQueryKey() });
          setModal(false);
        },
      },
    );
  };

  return (
    <>
      <PageHead
        eyebrow={isAdmin ? "Administrative Desk" : "Campus Support"}
        title={isAdmin ? "Grievance & Complaint Management" : "Submit Complaint to Administrator"}
        description={
          isAdmin
            ? "Manage, investigate, and update status for complaints submitted by students and faculty."
            : "Directly submit your service requests, academic issues, or facility complaints to Campus Administration."
        }
        action={
          !isAdmin && (
            <button
              onClick={() => setModal(true)}
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground hover:bg-primary/90 transition-colors flex items-center gap-2"
              data-testid="button-create-complaint"
            >
              <Plus className="size-4" /> Send Complaint to Admin
            </button>
          )
        }
      />

      <Card>
        {q.isLoading ? (
          <div className="p-4">
            <SkeletonRows />
          </div>
        ) : q.isError ? (
          <div className="p-4">
            <ErrorState retry={() => q.refetch()} />
          </div>
        ) : list.length === 0 ? (
          <Empty label="service requests" />
        ) : (
          <div className="divide-y divide-border">
            {list.map((c) => (
              <div
                key={c.id}
                className="flex flex-col gap-4 p-5 sm:flex-row sm:items-center"
                data-testid={`row-complaint-${c.id}`}
              >
                <div className="grid size-10 shrink-0 place-items-center rounded-lg bg-muted">
                  <LifeBuoy className="size-5 text-primary" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 className="font-semibold">{c.title}</h2>
                    <Status
                      tone={
                        c.priority === "high"
                          ? "critical"
                          : c.priority === "medium"
                            ? "warning"
                            : "neutral"
                      }
                    >
                      {c.priority}
                    </Status>
                    <span className="rounded bg-muted px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
                      {c.senderRole === "faculty" ? "👨‍🏫 Teacher" : "🎓 Student"}
                    </span>
                    {c.department && (
                      <span className="rounded bg-primary/10 text-primary px-1.5 py-0.5 text-[10px] font-semibold">
                        {c.department}
                      </span>
                    )}
                  </div>
                  <p className="mt-1 text-xs text-muted-foreground">
                    {c.category} · By {c.student} {c.senderUsername ? `(@${c.senderUsername})` : ""} · {c.createdAt}
                  </p>
                  <p className="mt-1.5 text-xs text-foreground/80 line-clamp-2">
                    {c.description}
                  </p>
                </div>

                {isAdmin ? (
                  <div className="flex items-center gap-2">
                    <label className="text-xs text-muted-foreground font-semibold">Status:</label>
                    <select
                      value={c.status}
                      onChange={(e) =>
                        update.mutate(
                          { id: c.id, data: { status: e.target.value } },
                          {
                            onSuccess: () =>
                              qc.invalidateQueries({
                                queryKey: getListComplaintsQueryKey(),
                              }),
                          },
                        )
                      }
                      className="h-9 rounded-lg border border-input bg-background px-2.5 text-xs font-semibold focus:border-primary focus:outline-none"
                      data-testid={`select-complaint-status-${c.id}`}
                    >
                      <option value="submitted">Submitted</option>
                      <option value="review">In review</option>
                      <option value="progress">In progress</option>
                      <option value="resolved">Resolved</option>
                      <option value="rejected">Rejected</option>
                    </select>
                  </div>
                ) : (
                  <div className="flex flex-col items-end gap-1">
                    <Status
                      tone={
                        c.status === "resolved"
                          ? "positive"
                          : c.status === "progress"
                            ? "warning"
                            : c.status === "rejected"
                              ? "critical"
                              : "neutral"
                      }
                    >
                      {c.status}
                    </Status>
                    <span className="text-[10px] text-muted-foreground">Managed by Admin</span>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </Card>

      {modal && (
        <Modal title="Send Complaint to Administrator" onClose={() => setModal(false)}>
          <form onSubmit={submit} className="grid gap-4">
            <div className="rounded-lg bg-primary/5 border border-primary/20 p-3 text-xs text-muted-foreground flex items-center gap-2">
              <Command className="size-4 text-primary shrink-0" />
              <span>Your complaint will be submitted directly to Campus Administration for review and resolution.</span>
            </div>

            <Field label="Complaint Title" name="title" required placeholder="e.g. Wi-Fi disconnection in lab 2" />

            <label className="text-sm">
              <span className="mb-1.5 block text-xs font-semibold text-muted-foreground">
                Category
              </span>
              <select
                name="category"
                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm focus:border-primary focus:outline-none"
                data-testid="select-complaint-category"
              >
                <option value="Facilities">Facilities & Infrastructure</option>
                <option value="IT Support">IT & Network Support</option>
                <option value="Academic">Academic / Examination</option>
                <option value="Hostel">Hostel & Housing</option>
                <option value="Administration">Administration & Fees</option>
                <option value="Other">Other</option>
              </select>
            </label>

            <label className="text-sm">
              <span className="mb-1.5 block text-xs font-semibold text-muted-foreground">
                Priority
              </span>
              <select
                name="priority"
                className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm focus:border-primary focus:outline-none"
                data-testid="select-complaint-priority"
              >
                <option value="low">Low - Routine notice</option>
                <option value="medium">Medium - Needs attention</option>
                <option value="high">High - Urgent disruption</option>
              </select>
            </label>

            <label className="text-sm">
              <span className="mb-1.5 block text-xs font-semibold text-muted-foreground">
                Description of Issue
              </span>
              <textarea
                name="description"
                required
                placeholder="Provide detailed description of the issue, location, and relevant context..."
                className="min-h-24 w-full rounded-lg border border-input bg-background p-3 text-sm focus:border-primary focus:outline-none"
                data-testid="input-complaint-description"
              />
            </label>

            <button
              disabled={create.isPending}
              className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground hover:bg-primary/90 transition-colors"
              data-testid="button-save-complaint"
            >
              {create.isPending ? "Submitting to Admin…" : "Send to Administrator"}
            </button>
          </form>
        </Modal>
      )}
    </>
  );
}

function Notifications() {
  const q = useListNotifications();
  const read = useMarkNotificationRead();
  const qc = useQueryClient();
  const list = q.data ?? [];
  return (
    <>
      <PageHead
        eyebrow="Inbox"
        title="Notifications"
        description="The useful stuff, with the noise kept out."
      />
      <Card>
        {q.isLoading ? (
          <div className="p-4">
            <SkeletonRows />
          </div>
        ) : q.isError ? (
          <div className="p-4">
            <ErrorState retry={() => q.refetch()} />
          </div>
        ) : list.length === 0 ? (
          <Empty label="notifications" />
        ) : (
          <div className="divide-y divide-border">
            {list.map((n) => (
              <button
                key={n.id}
                onClick={() =>
                  !n.read &&
                  read.mutate(
                    { id: n.id },
                    {
                      onSuccess: () =>
                        qc.invalidateQueries({
                          queryKey: getListNotificationsQueryKey(),
                        }),
                    },
                  )
                }
                className={`flex w-full items-start gap-4 p-5 text-left transition-colors hover:bg-muted/30 ${n.read ? "" : "bg-primary/[.035]"}`}
                data-testid={`button-notification-${n.id}`}
              >
                <div
                  className={`mt-1 grid size-9 shrink-0 place-items-center rounded-lg ${n.read ? "bg-muted text-muted-foreground" : "bg-accent/35 text-primary"}`}
                >
                  <Bell className="size-4" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <span
                      className={`text-sm ${n.read ? "font-medium" : "font-bold"}`}
                    >
                      {n.title}
                    </span>
                    {!n.read && (
                      <span className="size-1.5 rounded-full bg-accent" />
                    )}
                  </div>
                  <p className="mt-1 text-sm leading-6 text-muted-foreground">
                    {n.message}
                  </p>
                  <time className="mt-2 block text-xs text-muted-foreground">
                    {n.createdAt}
                  </time>
                </div>
                <ChevronRight className="mt-1 size-4 text-muted-foreground" />
              </button>
            ))}
          </div>
        )}
      </Card>
    </>
  );
}

function AiAssistant() {
  const { role: userRole, user } = useAuth();
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState(() => {
    if (userRole === "admin" || userRole === "staff") {
      return [
        {
          id: "init",
          from: "assistant",
          text: "👋 **Welcome, Campus Administrator.**\n\nYou have full executive retrieval clearance across the university database. You can query department-wide CGPA statistics, inspect any student's complete academic record, track attendance compliance across campus, review fee collection ledgers, or manage service tickets.",
          timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
          model: "Grounded Admin Intelligence v3.0",
          source: "campus_knowledge_engine"
        }
      ];
    } else if (userRole === "faculty") {
      return [
        {
          id: "init",
          from: "assistant",
          text: "👋 **Welcome, Professor.**\n\nYou have instructor access to review class attendance, monitor at-risk students, inspect assignment submissions, dispatch deadline alerts, and check your teaching routine.",
          timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
          model: "Grounded Faculty Intelligence v3.0",
          source: "campus_knowledge_engine"
        }
      ];
    } else {
      return [
        {
          id: "init",
          from: "assistant",
          text: "👋 **Welcome, Student.**\n\nI am your personal campus intelligence advisor. I can answer questions about your CGPA, examination marks, attendance safe-skip buffer, pending assignments, fee dues, and timetable. *(Note: Other peers' records are protected by university privacy policies).* ",
          timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
          model: "Grounded Student Advisor v3.0",
          source: "campus_knowledge_engine"
        }
      ];
    }
  });
  const [copiedId, setCopiedId] = useState(null);
  const [showConfig, setShowConfig] = useState(false);
  const [aiConfig, setAiConfig] = useState({
    provider: "gemini",
    model: "gemini-1.5-flash",
    configured: false,
    maskedKey: "",
    availableProviders: ["gemini", "openai", "grounded_campus_engine"],
    availableModels: ["gemini-1.5-flash", "gemini-2.0-flash", "gpt-4o-mini", "gpt-4o"]
  });
  const [configForm, setConfigForm] = useState({
    provider: "gemini",
    model: "gemini-1.5-flash",
    apiKey: ""
  });
  const [testState, setTestState] = useState({ testing: false, result: null });
  const [currentSuggestions, setCurrentSuggestions] = useState([]);
  const chat = useChatWithAssistant();
  const chatContainerRef = useRef(null);

  useEffect(() => {
    customFetch("/api/ai/config")
      .then((data) => {
        if (!data) return;
        setAiConfig(data);
        setConfigForm((prev) => ({
          ...prev,
          provider: data.provider || "gemini",
          model: data.model || "gemini-1.5-flash"
        }));
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
    }
  }, [messages, chat.isPending]);

  const getRoleSuggestions = () => {
    if (userRole === "faculty") {
      return [
        "Summarize attendance across my subjects",
        "Show students with attendance below 75%",
        "Broadcast assignment deadline reminder alert",
        "Show pending marks submissions",
        "What is my teaching schedule for today?"
      ];
    } else if (userRole === "admin" || userRole === "staff") {
      return [
        "Executive overview of university CGPA across all departments",
        "Which students have attendance below 75%?",
        "Campus-wide fee dues and collection summary",
        "What is John Doe's CGPA and academic record?",
        "How many open complaints need attention?"
      ];
    } else {
      return [
        "How is my attendance and how many classes can I skip?",
        "What is my latest CGPA and semester marks?",
        "Simulate what score I need for a 9.0 CGPA",
        "What assignments are due this week?",
        "What is my outstanding tuition fee balance?"
      ];
    }
  };

  const activeSuggestions = currentSuggestions.length > 0 ? currentSuggestions : getRoleSuggestions();

  const send = (text = message) => {
    const trimmed = text.trim();
    if (!trimmed || chat.isPending) return;

    const userTurn = {
      id: Date.now(),
      from: "you",
      text: trimmed,
      timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
    };

    setMessages((prev) => [...prev, userTurn]);
    setMessage("");

    const historyPayload = messages
      .filter((m) => m.id !== "init")
      .map((m) => ({ from: m.from === "you" ? "user" : "model", text: m.text }));

    chat.mutate(
      {
        data: {
          message: trimmed,
          history: historyPayload,
          role: userRole || "student",
          username: user?.username || (userRole === "admin" ? "admin" : (userRole === "faculty" ? "teacher1cse" : "student1cse")),
          model: aiConfig.model,
          provider: aiConfig.provider
        }
      },
      {
        onSuccess: (response) => {
          setMessages((prev) => [
            ...prev,
            {
              id: Date.now() + 1,
              from: "assistant",
              text: response.answer,
              model: response.model || (response.source === "llm" ? aiConfig.model : "Grounded Campus Engine v3.0"),
              source: response.source || "ai",
              actionTaken: response.actionTaken,
              timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
            }
          ]);
          if (response.suggestions && response.suggestions.length > 0) {
            setCurrentSuggestions(response.suggestions);
          }
        },
        onError: (err) => {
          setMessages((prev) => [
            ...prev,
            {
              id: Date.now() + 1,
              from: "assistant",
              text: "⚠️ **Communication error**: Could not connect to campus assistant. Please verify your connection or model settings.\n\n`" + (err?.message || "Request failed") + "`",
              timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
            }
          ]);
        }
      }
    );
  };

  const copyToClipboard = (id, text) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleSaveConfig = async () => {
    try {
      const payload = {
        provider: configForm.provider,
        model: configForm.model
      };
      if (configForm.apiKey.trim()) {
        payload.apiKey = configForm.apiKey.trim();
      }
      const data = await customFetch("/api/ai/config", {
        method: "POST",
        body: JSON.stringify(payload)
      });
      setAiConfig((prev) => ({
        ...prev,
        provider: data.provider,
        model: data.model,
        configured: data.configured,
        maskedKey: data.maskedKey
      }));
      setConfigForm((prev) => ({ ...prev, apiKey: "" }));
      setShowConfig(false);
    } catch (_) {}
  };

  const handleTestConnection = async () => {
    setTestState({ testing: true, result: null });
    try {
      if (configForm.apiKey.trim()) {
        await customFetch("/api/ai/config", {
          method: "POST",
          body: JSON.stringify({
            provider: configForm.provider,
            model: configForm.model,
            apiKey: configForm.apiKey.trim()
          })
        });
      }
      const data = await customFetch("/api/ai/test", { method: "POST" });
      setTestState({ testing: false, result: data?.connected ? "success" : "failed" });
    } catch (_) {
      setTestState({ testing: false, result: "failed" });
    }
  };

  return (
    <div className="mx-auto max-w-4xl space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <PageHead
          eyebrow="Meridian Campus Intelligence"
          title="Campus AI Assistant"
          description="Context-aware reasoning grounded in live university database records."
        />
        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowConfig(true)}
            className="flex items-center gap-1.5 rounded-lg border border-border bg-card px-3 py-1.5 text-xs font-medium text-foreground transition-colors hover:border-primary hover:text-primary"
            data-testid="button-ai-settings"
          >
            <Sliders className="size-3.5" />
            <span>AI Model Settings</span>
            {aiConfig.configured ? (
              <span className="rounded bg-emerald-500/10 px-1.5 py-0.5 text-[10px] text-emerald-600 font-semibold dark:text-emerald-400">
                LLM Active
              </span>
            ) : (
              <span className="rounded bg-blue-500/10 px-1.5 py-0.5 text-[10px] text-blue-600 font-semibold dark:text-blue-400">
                Grounded Engine
              </span>
            )}
          </button>
          <button
            onClick={() => {
              if (confirm("Clear current conversation history?")) {
                setMessages([
                  {
                    id: "init",
                    from: "assistant",
                    text: "Conversation cleared. How can I help you today?",
                    timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
                  }
                ]);
                setCurrentSuggestions([]);
              }
            }}
            className="grid size-8 place-items-center rounded-lg border border-border bg-card text-muted-foreground transition-colors hover:text-destructive"
            title="Clear conversation"
          >
            <Trash2 className="size-3.5" />
          </button>
        </div>
      </div>

      <Card className="overflow-hidden border-border/80 shadow-sm">
        {/* Header Bar */}
        <div className="flex items-center justify-between border-b border-border bg-gradient-to-r from-primary/95 to-primary p-4 text-primary-foreground">
          <div className="flex items-center gap-3">
            <div className="grid size-10 place-items-center rounded-xl bg-primary-foreground/15 text-primary-foreground backdrop-blur-sm">
              <Bot className="size-5" />
            </div>
            <div>
              <div className="flex items-center gap-2 font-semibold tracking-tight text-sm md:text-base">
                Meridian Intelligent Assistant
                <span className="inline-flex items-center gap-1 rounded-full bg-emerald-400/20 px-2 py-0.5 text-[11px] font-normal text-emerald-200">
                  <span className="size-1.5 rounded-full bg-emerald-400 animate-pulse" /> Live
                </span>
              </div>
              <div className="text-xs text-primary-foreground/75">
                Role: <span className="capitalize font-medium">{userRole}</span> • Database Grounded
              </div>
            </div>
          </div>

          <div className="text-right">
            <span className="rounded-md bg-black/20 px-2.5 py-1 text-xs text-primary-foreground/90 backdrop-blur-sm">
              {aiConfig.configured
                ? (aiConfig.provider === "gemini" ? "Google Gemini" : "OpenAI") + " (" + aiConfig.model + ")"
                : "Grounded Campus Engine v2.5"}
            </span>
          </div>
        </div>

        {/* Message Scroll View */}
        <div
          ref={chatContainerRef}
          className="h-[460px] overflow-y-auto space-y-4 bg-card/40 p-4 md:p-6"
        >
          {messages.map((m) => {
            const isUser = m.from === "you";
            return (
              <div
                key={m.id}
                className={`flex gap-3 ${isUser ? "justify-end" : "justify-start"}`}
              >
                {!isUser && (
                  <div className="grid size-8 shrink-0 place-items-center rounded-lg bg-primary/10 text-primary">
                    <Bot className="size-4" />
                  </div>
                )}
                <div
                  className={`group relative max-w-[85%] rounded-2xl p-4 text-sm leading-relaxed transition-all shadow-sm ${
                    isUser
                      ? "rounded-br-none bg-primary text-primary-foreground"
                      : "rounded-bl-none border border-border/70 bg-card text-card-foreground"
                  }`}
                >
                  {isUser ? (
                    <div className="whitespace-pre-wrap">{m.text}</div>
                  ) : (
                    <div className="markdown-content prose prose-sm dark:prose-invert max-w-none text-foreground prose-table:border-collapse prose-th:border prose-th:border-border prose-th:p-2 prose-th:bg-muted/40 prose-td:border prose-td:border-border prose-td:p-2 prose-table:my-2.5 prose-p:my-1.5 prose-ul:my-1.5 prose-ol:my-1.5 prose-li:my-0.5 prose-h3:text-sm prose-h3:font-semibold prose-h3:my-2 prose-h4:text-xs prose-h4:font-semibold prose-h4:my-1.5">
                      <ReactMarkdown remarkPlugins={[remarkGfm]}>
                        {m.text}
                      </ReactMarkdown>
                    </div>
                  )}

                  {/* Metadata and Actions footer */}
                  <div className="mt-2 flex items-center justify-between gap-4 pt-1.5 border-t border-border/20 text-[11px] opacity-75">
                    <span className="flex items-center gap-1.5">
                      {m.timestamp}
                      {m.model && !isUser && (
                        <>
                          <span>•</span>
                          <span className="text-[10px] text-muted-foreground">{m.model}</span>
                        </>
                      )}
                    </span>
                    {!isUser && (
                      <button
                        onClick={() => copyToClipboard(m.id, m.text)}
                        className="flex items-center gap-1 rounded px-1.5 py-0.5 transition-colors hover:bg-muted"
                        title="Copy answer"
                      >
                        {copiedId === m.id ? (
                          <>
                            <CheckCheck className="size-3 text-emerald-500" />
                            <span className="text-emerald-500">Copied</span>
                          </>
                        ) : (
                          <>
                            <Copy className="size-3 text-muted-foreground" />
                            <span className="text-muted-foreground">Copy</span>
                          </>
                        )}
                      </button>
                    )}
                  </div>

                  {/* Autonomous Action Execution Card */}
                  {m.actionTaken && (
                    <div className="mt-3 overflow-hidden rounded-xl border border-emerald-500/35 bg-emerald-500/10 p-3.5 text-xs text-foreground shadow-2xs">
                      <div className="flex items-center justify-between font-semibold text-emerald-700 dark:text-emerald-300">
                        <span className="flex items-center gap-1.5">
                          <CheckCircle2 className="size-4 text-emerald-600 dark:text-emerald-400" />
                          {m.actionTaken.type === "EVENT_REGISTERED" && "Event Registration Verified"}
                          {m.actionTaken.type === "COMPLAINT_FILED" && `Grievance Ticket #${m.actionTaken.ticketId} Created`}
                          {m.actionTaken.type === "STUDENT_ALERTS_SENT" && "Student Deadline Alerts Delivered"}
                        </span>
                        <span className="rounded bg-emerald-500/20 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-emerald-800 dark:text-emerald-200">
                          {m.actionTaken.status || "Verified"}
                        </span>
                      </div>
                      <div className="mt-2 space-y-1 text-xs opacity-90">
                        {m.actionTaken.title && <div><strong>Subject:</strong> {m.actionTaken.title}</div>}
                        {m.actionTaken.venue && <div><strong>Location:</strong> {m.actionTaken.venue}</div>}
                        {m.actionTaken.date && <div><strong>Scheduled:</strong> {m.actionTaken.date}</div>}
                        {m.actionTaken.category && <div><strong>Category:</strong> {m.actionTaken.category} ({m.actionTaken.priority} Priority)</div>}
                        {m.actionTaken.seatsLeft !== undefined && <div><strong>Seats Remaining:</strong> {m.actionTaken.seatsLeft}</div>}
                        {m.actionTaken.alertsCreated !== undefined && <div><strong>Notifications Dispatched:</strong> {m.actionTaken.alertsCreated} active students</div>}
                      </div>
                      <div className="mt-3 pt-2 border-t border-emerald-500/20 flex gap-3">
                        {m.actionTaken.type === "EVENT_REGISTERED" && (
                          <Link href="/events" className="inline-flex items-center gap-1 font-semibold text-emerald-700 dark:text-emerald-300 hover:underline">
                            View in Campus Events <ArrowRight className="size-3" />
                          </Link>
                        )}
                        {m.actionTaken.type === "COMPLAINT_FILED" && (
                          <Link href="/complaints" className="inline-flex items-center gap-1 font-semibold text-emerald-700 dark:text-emerald-300 hover:underline">
                            Track in Service Desk <ArrowRight className="size-3" />
                          </Link>
                        )}
                        {m.actionTaken.type === "STUDENT_ALERTS_SENT" && (
                          <Link href="/assignments" className="inline-flex items-center gap-1 font-semibold text-emerald-700 dark:text-emerald-300 hover:underline">
                            Check Assignments <ArrowRight className="size-3" />
                          </Link>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            );
          })}

          {chat.isPending && (
            <div className="flex items-center gap-3">
              <div className="grid size-8 shrink-0 place-items-center rounded-lg bg-primary/10 text-primary">
                <Bot className="size-4" />
              </div>
              <div className="flex items-center gap-2 rounded-2xl rounded-bl-none border border-border bg-card px-4 py-3 text-xs text-muted-foreground shadow-sm">
                <Loader2 className="size-3.5 animate-spin text-primary" />
                <span>Consulting live campus records & reasoning…</span>
              </div>
            </div>
          )}
        </div>

        {/* Autonomous Agent Quick Action Chips */}
        <div className="border-t border-border/80 bg-muted/30 px-4 py-2.5">
          <div className="mb-2 flex items-center justify-between text-[11px] font-medium text-muted-foreground">
            <span className="flex items-center gap-1.5 font-bold uppercase tracking-wider text-[10px] text-primary">
              <Sparkles className="size-3 text-accent animate-pulse" /> Autonomous Actions & Fast Tools
            </span>
          </div>
          <div className="flex flex-wrap gap-1.5">
            {((userRole === "admin" || userRole === "staff")
              ? [
                  { label: "📊 University CGPA Report", prompt: "Executive overview of university CGPA across all departments" },
                  { label: "⚠️ Low Attendance List (<75%)", prompt: "Which students have attendance below 75%?" },
                  { label: "💳 Campus Tuition Ledger", prompt: "Campus-wide fee dues and collection summary" },
                  { label: "🛠️ Open Service Tickets", prompt: "How many open complaints need attention?" },
                  { label: "🎓 John Doe Dossier", prompt: "What is John Doe's CGPA and academic record?" },
                  { label: "👨‍🏫 Faculty & HoDs", prompt: "Institutional faculty list and department heads" }
                ]
              : userRole === "faculty"
              ? [
                  { label: "📊 Class Attendance Matrix", prompt: "Summarize attendance across my subjects" },
                  { label: "⚠️ Students Below 75%", prompt: "Show students with attendance below 75%" },
                  { label: "📢 Broadcast Deadline Alert", prompt: "Broadcast assignment deadline reminder alert" },
                  { label: "📝 Pending Marks Submissions", prompt: "Show pending marks submissions" },
                  { label: "📅 Today's Teaching Schedule", prompt: "What is my teaching schedule for today?" }
                ]
              : [
                  { label: "🚨 Attendance Safe-Skips", prompt: "How many classes can I skip without getting detained?" },
                  { label: "🎓 My CGPA & Marks", prompt: "What is my latest CGPA and semester marks?" },
                  { label: "🎯 9.0 GPA Simulator", prompt: "Simulate what score I need for a 9.0 CGPA" },
                  { label: "📅 Today's Timetable", prompt: "What is my timetable for today?" },
                  { label: "🎪 Register for AI Symposium", prompt: "Register me for the National AI Symposium" },
                  { label: "🛠️ Raise Wi-Fi Ticket", prompt: "File a complaint: Wi-Fi disconnected in Lab 2" },
                  { label: "💳 Tuition Fee Ledger", prompt: "Show my tuition fee ledger and dues" }
                ]
            ).map((chip, idx) => (
              <button
                key={idx}
                type="button"
                onClick={() => send(chip.prompt)}
                disabled={chat.isPending}
                className="inline-flex items-center gap-1 rounded-full border border-border/90 bg-card px-2.5 py-1 text-[11px] font-medium text-foreground transition-all hover:border-primary hover:bg-primary/10 hover:text-primary active:scale-95 disabled:opacity-50 shadow-2xs"
              >
                {chip.label}
              </button>
            ))}
          </div>
        </div>

        {/* Dynamic Suggested Prompts */}
        <div className="border-t border-border/80 bg-muted/15 px-4 py-2.5">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-[11px] font-medium uppercase tracking-wider text-muted-foreground">
              Conversational prompts
            </span>
            {currentSuggestions.length > 0 && (
              <button
                onClick={() => setCurrentSuggestions([])}
                className="text-[11px] text-primary hover:underline"
              >
                Reset to role defaults
              </button>
            )}
          </div>
          <div className="flex flex-wrap gap-2">
            {activeSuggestions.map((s, idx) => (
              <button
                key={idx}
                onClick={() => send(s)}
                disabled={chat.isPending}
                className="rounded-full border border-border/80 bg-card px-3 py-1.5 text-xs text-muted-foreground transition-all hover:border-primary hover:bg-primary/5 hover:text-primary disabled:opacity-50"
                data-testid={`button-suggestion-${idx}`}
              >
                {s}
              </button>
            ))}
          </div>
        </div>

        {/* Input Bar */}
        <div className="border-t border-border p-4 bg-card">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              send();
            }}
            className="flex items-center gap-2"
          >
            <input
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder={
                userRole === "admin" || userRole === "staff"
                  ? "Search any student's CGPA, low attendance lists, department analytics, fee ledger, complaints…"
                  : userRole === "faculty"
                  ? "Query class attendance, taught subjects, student submissions, dispatch reminders, schedule…"
                  : "Ask about your attendance, CGPA, exam marks, safe-skips, assignments, fees, timetable…"
              }
              className="h-11 flex-1 rounded-xl border border-input bg-background px-4 text-sm outline-none transition-colors focus:border-primary focus:ring-1 focus:ring-primary"
              disabled={chat.isPending}
              data-testid="input-chat-message"
            />
            <button
              disabled={!message.trim() || chat.isPending}
              className="grid size-11 place-items-center rounded-xl bg-primary text-primary-foreground transition-transform active:scale-95 disabled:opacity-50"
              data-testid="button-send-chat"
              title="Send message"
            >
              <Send className="size-4" />
            </button>
          </form>
          <div className="mt-2 flex items-center justify-between text-[11px] text-muted-foreground">
            <span>Press Enter ↵ to send</span>
            <span>Grounding live database records for {userRole}</span>
          </div>
        </div>
      </Card>

      {/* AI Settings Modal */}
      {showConfig && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
          <Card className="w-full max-w-lg overflow-hidden border-border bg-card p-6 shadow-2xl rise">
            <div className="flex items-center justify-between border-b border-border pb-4">
              <div className="flex items-center gap-2.5">
                <div className="grid size-8 place-items-center rounded-lg bg-primary/10 text-primary">
                  <Cpu className="size-4" />
                </div>
                <div>
                  <h3 className="font-semibold text-foreground">AI Model & Grounding Settings</h3>
                  <p className="text-xs text-muted-foreground">
                    Configure live LLM generation or Grounded Campus Engine
                  </p>
                </div>
              </div>
              <button
                onClick={() => setShowConfig(false)}
                className="grid size-8 place-items-center rounded-lg text-muted-foreground hover:bg-muted"
              >
                <X className="size-4" />
              </button>
            </div>

            <div className="space-y-4 py-4 text-sm">
              {/* Provider Selection */}
              <div>
                <label className="mb-1.5 block text-xs font-medium text-foreground">
                  AI Provider Engine
                </label>
                <select
                  value={configForm.provider}
                  onChange={(e) =>
                    setConfigForm((prev) => ({
                      ...prev,
                      provider: e.target.value,
                      model: e.target.value === "openai" ? "gpt-4o-mini" : "gemini-1.5-flash"
                    }))
                  }
                  className="w-full rounded-lg border border-input bg-background p-2.5 text-sm outline-none focus:border-primary"
                >
                  <option value="gemini">Google Gemini (Recommended)</option>
                  <option value="openai">OpenAI (GPT-4o Mini / GPT-4o)</option>
                  <option value="grounded_campus_engine">
                    Meridian Grounded Campus Engine (No API Key Required)
                  </option>
                </select>
              </div>

              {/* Model Selection */}
              {configForm.provider !== "grounded_campus_engine" && (
                <div>
                  <label className="mb-1.5 block text-xs font-medium text-foreground">
                    Model Selection
                  </label>
                  <select
                    value={configForm.model}
                    onChange={(e) => setConfigForm((prev) => ({ ...prev, model: e.target.value }))}
                    className="w-full rounded-lg border border-input bg-background p-2.5 text-sm outline-none focus:border-primary"
                  >
                    {configForm.provider === "gemini" ? (
                      <>
                        <option value="gemini-1.5-flash">gemini-1.5-flash (Fast & Accurate)</option>
                        <option value="gemini-2.0-flash">gemini-2.0-flash</option>
                      </>
                    ) : (
                      <>
                        <option value="gpt-4o-mini">gpt-4o-mini (Fast & Efficient)</option>
                        <option value="gpt-4o">gpt-4o (High Reasoning)</option>
                      </>
                    )}
                  </select>
                </div>
              )}

              {/* API Key Input */}
              {configForm.provider !== "grounded_campus_engine" && (
                <div>
                  <div className="mb-1.5 flex items-center justify-between">
                    <label className="text-xs font-medium text-foreground">
                      {configForm.provider === "gemini" ? "Google Gemini API Key" : "OpenAI API Key"}
                    </label>
                    {aiConfig.configured && (
                      <span className="text-[11px] text-emerald-600 font-medium">
                        Current: {aiConfig.maskedKey}
                      </span>
                    )}
                  </div>
                  <div className="relative">
                    <input
                      type="password"
                      placeholder={
                        aiConfig.configured
                          ? "Enter new key to replace existing key"
                          : "Paste API key (AIzaSy... / sk-...)"
                      }
                      value={configForm.apiKey}
                      onChange={(e) => setConfigForm((prev) => ({ ...prev, apiKey: e.target.value }))}
                      className="w-full rounded-lg border border-input bg-background p-2.5 pr-10 text-sm outline-none focus:border-primary font-mono"
                    />
                    <Key className="absolute right-3 top-3 size-4 text-muted-foreground pointer-events-none" />
                  </div>
                  <p className="mt-1 text-[11px] text-muted-foreground">
                    Can also be provided via <code>GEMINI_API_KEY</code> or <code>OPENAI_API_KEY</code> environment variables.
                  </p>
                </div>
              )}

              {/* Grounded Engine notice */}
              {configForm.provider === "grounded_campus_engine" && (
                <div className="rounded-lg border border-blue-500/20 bg-blue-500/10 p-3 text-xs text-blue-700 dark:text-blue-300">
                  🏛️ <strong>Autonomous Campus Engine Active</strong>: Queries MongoDB directly for real student, course, attendance, grade, and event statistics without any external API calls.
                </div>
              )}

              {/* Test status banner */}
              {testState.result && (
                <div
                  className={`rounded-lg p-3 text-xs flex items-center gap-2 ${
                    testState.result === "success"
                      ? "border border-emerald-500/20 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
                      : "border border-destructive/20 bg-destructive/10 text-destructive"
                  }`}
                >
                  {testState.result === "success" ? (
                    <>
                      <CheckCircle2 className="size-4" />
                      <span>Connection Successful! LLM is active and ready.</span>
                    </>
                  ) : (
                    <>
                      <AlertCircle className="size-4" />
                      <span>Connection failed. Please verify the API key and internet connectivity.</span>
                    </>
                  )}
                </div>
              )}
            </div>

            <div className="flex items-center justify-between border-t border-border pt-4">
              {configForm.provider !== "grounded_campus_engine" ? (
                <button
                  type="button"
                  onClick={handleTestConnection}
                  disabled={testState.testing}
                  className="flex items-center gap-1.5 rounded-lg border border-border px-3 py-2 text-xs font-medium text-foreground hover:bg-muted disabled:opacity-50"
                >
                  {testState.testing && <Loader2 className="size-3.5 animate-spin" />}
                  <span>Test Connection</span>
                </button>
              ) : <div />}

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setShowConfig(false)}
                  className="rounded-lg border border-border px-3.5 py-2 text-xs font-medium text-foreground hover:bg-muted"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleSaveConfig}
                  className="rounded-lg bg-primary px-4 py-2 text-xs font-medium text-primary-foreground hover:bg-primary/90"
                >
                  Save & Apply
                </button>
              </div>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}

function Insights() {
  const q = useGetAcademicInsights();
  const analytics = useGetDashboardAnalytics();
  const [, setLocation] = useLocation();
  const [targetCgpa, setTargetCgpa] = useState(9.0);

  if (q.isLoading)
    return (
      <>
        <PageHead eyebrow="Academic intelligence" title="Academic insights" />
        <SkeletonRows count={5} />
      </>
    );
  if (q.isError) return <ErrorState retry={() => q.refetch()} />;
  const d = q.data;

  const currentCgpa = d?.cgpa || 8.75;
  const currentSem = d?.semester || 2;
  const neededSgpa = Math.max(
    0,
    Math.min(
      10.0,
      Math.round(((targetCgpa * currentSem) - (currentCgpa * (currentSem - 1))) * 100) / 100
    )
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <PageHead
          eyebrow="Academic Intelligence Engine"
          title="AI Academic Insights & Trajectory"
          description="Real-time predictive analytics, attendance clearance buffers, and personalized GPA roadmaps."
        />
        <Link
          href="/ai-assistant"
          className="flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-semibold text-primary-foreground hover:bg-primary/90 transition-all shadow-sm"
        >
          <Bot className="size-4" />
          <span>Launch AI Campus Advisor</span>
        </Link>
      </div>

      {/* Top Metric Cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card className="p-5 border-l-4 border-l-primary">
          <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Cumulative CGPA</div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="display text-3xl font-bold">{currentCgpa}</span>
            <span className="text-xs text-muted-foreground">/ 10.0 scale</span>
          </div>
          <div className="mt-1 text-xs text-emerald-600 dark:text-emerald-400 font-medium flex items-center gap-1">
            <Check className="size-3" /> Semester {currentSem} Standing
          </div>
        </Card>

        <Card className="p-5 border-l-4 border-l-accent">
          <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Campus Wellbeing</div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="display text-3xl font-bold">{d?.score ?? 85}</span>
            <span className="text-xs text-muted-foreground">/ 100</span>
          </div>
          <div className="mt-1 text-xs font-semibold text-accent capitalize">
            {d?.level ?? "Strong standing"}
          </div>
        </Card>

        <Card className="p-5 border-l-4 border-l-blue-500">
          <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Academic Backlogs</div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="display text-3xl font-bold">{d?.arrearCount ?? 0}</span>
            <span className="text-xs text-muted-foreground">active arrears</span>
          </div>
          <div className="mt-1 text-xs text-muted-foreground">
            {d?.arrearCount ? "Remedial registration recommended" : "Clear backlog record"}
          </div>
        </Card>

        <Card className="p-5 border-l-4 border-l-purple-500">
          <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Pending Assignments</div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="display text-3xl font-bold">{d?.openAssignments ?? 3}</span>
            <span className="text-xs text-muted-foreground">active deadlines</span>
          </div>
          <div className="mt-1 text-xs text-muted-foreground">Due this semester cycle</div>
        </Card>
      </div>

      {/* Main Grid: Wellbeing + Target GPA Simulator */}
      <div className="grid gap-6 lg:grid-cols-[1fr_1.3fr]">
        {/* Wellbeing Overview */}
        <Card className="overflow-hidden p-0 flex flex-col justify-between shadow-sm">
          <div className="bg-primary p-7 text-primary-foreground">
            <div className="flex items-center justify-between">
              <div className="text-[10px] font-bold uppercase tracking-[.18em] text-primary-foreground/60">
                Composite Index
              </div>
              <span className="rounded-full bg-accent/20 px-2.5 py-0.5 text-[11px] font-semibold text-accent">
                {d?.department} • Year {d?.year}
              </span>
            </div>
            <div className="mt-6 flex items-end gap-3">
              <span className="display text-6xl font-bold">{d?.score ?? 85}</span>
              <span className="mb-2 text-sm text-primary-foreground/60">/ 100 rating</span>
            </div>
            <div className="mt-2 text-sm font-semibold capitalize text-accent">
              {d?.level ?? "Strong standing"}
            </div>
            <div className="mt-6 metric-bar bg-primary-foreground/15">
              <span className="bg-accent" style={{ width: `${d?.score ?? 85}%` }} />
            </div>
            <p className="mt-4 text-xs leading-5 text-primary-foreground/75">
              Derived from live lecture attendance records, internal assessment scores, assignment completion rates, and tuition clearance flags.
            </p>
          </div>

          {/* Assigned Mentor Quick Card */}
          <div className="p-6 bg-card border-t border-border">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                Assigned Faculty Mentor
              </span>
              <span className="rounded bg-primary/10 text-primary text-[10px] px-2 py-0.5 font-bold">Cabin 304</span>
            </div>
            <div className="mt-3 flex items-center gap-3">
              <div className="grid size-10 place-items-center rounded-full bg-primary/10 text-primary font-bold text-sm">
                AT
              </div>
              <div>
                <div className="font-semibold text-sm">{d?.mentor?.name || "Dr. Alan Turing"}</div>
                <div className="text-xs text-muted-foreground">{d?.mentor?.phone || "+1 (555) 011-2301"} • Office Hours: Tue/Thu 3:30 PM</div>
              </div>
            </div>
          </div>
        </Card>

        {/* Interactive GPA Target Simulator */}
        <Card className="p-6 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between border-b border-border pb-3">
              <div>
                <h2 className="display text-lg font-bold">🎯 Target GPA Simulator</h2>
                <p className="text-xs text-muted-foreground">
                  Simulate your required semester performance to hit your dream graduation milestone.
                </p>
              </div>
              <Sparkles className="size-5 text-accent animate-pulse" />
            </div>

            <div className="mt-5 space-y-4">
              <div>
                <div className="flex items-center justify-between text-xs font-semibold mb-1.5">
                  <span>Target Cumulative CGPA</span>
                  <span className="rounded bg-primary px-2.5 py-0.5 text-primary-foreground font-mono font-bold text-sm">
                    {targetCgpa.toFixed(1)} / 10.0
                  </span>
                </div>
                <input
                  type="range"
                  min="7.5"
                  max="9.8"
                  step="0.1"
                  value={targetCgpa}
                  onChange={(e) => setTargetCgpa(parseFloat(e.target.value))}
                  className="w-full accent-primary cursor-pointer"
                />
                <div className="flex justify-between text-[10px] text-muted-foreground mt-1">
                  <span>7.5 (Good)</span>
                  <span>8.5 (Very Good)</span>
                  <span>9.0 (Distinction)</span>
                  <span>9.8 (Dean's Honor)</span>
                </div>
              </div>

              {/* Simulation Result Box */}
              <div className="rounded-xl border border-primary/20 bg-primary/5 p-4">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-medium text-muted-foreground">Required Semester {currentSem} SGPA:</span>
                  <span className={`text-xl font-bold font-mono ${neededSgpa > 9.5 ? "text-destructive" : neededSgpa > 8.5 ? "text-amber-600 dark:text-amber-400" : "text-emerald-600 dark:text-emerald-400"}`}>
                    {neededSgpa.toFixed(2)} SGPA
                  </span>
                </div>
                <div className="mt-3 text-xs leading-relaxed text-foreground/80">
                  {neededSgpa > 9.5
                    ? "🔴 Demanding Milestone: You will need Outstanding ('O') grades (>90% raw marks) in high-credit 4-credit courses."
                    : neededSgpa > 8.5
                    ? "🟡 Highly Achievable: Aim for 'A+' and 'A' grades across core technical subjects."
                    : "🟢 Comfortable Target: Consistent performance with 'A' and 'B+' grades will comfortably achieve this goal."}
                </div>
              </div>

              <div className="rounded-lg bg-muted/40 p-3 text-xs text-muted-foreground flex items-center justify-between">
                <span>Core subjects influence GPA 33% more than electives.</span>
                <button
                  type="button"
                  onClick={() => setLocation("/marks")}
                  className="text-primary font-semibold hover:underline text-xs"
                >
                  View Mark Ledger →
                </button>
              </div>
            </div>
          </div>

          <div className="mt-5 pt-3 border-t border-border flex justify-end">
            <button
              type="button"
              onClick={() => setLocation("/ai-assistant")}
              className="text-xs text-primary font-semibold hover:underline flex items-center gap-1"
            >
              Ask AI Assistant for a personalized study revision plan <ArrowRight className="size-3" />
            </button>
          </div>
        </Card>
      </div>

      {/* Course Attendance Safe-Skip & Shortage Recovery Matrix */}
      <Card className="p-6 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-border pb-4">
          <div>
            <h2 className="display text-lg font-bold">📊 Course Attendance Clearance & Safe-Skip Matrix</h2>
            <p className="text-xs text-muted-foreground">
              Mathematical safety buffer against the university 75% minimum semester exam threshold.
            </p>
          </div>
          <span className="rounded-full bg-primary/10 text-primary text-xs font-semibold px-3 py-1">
            Exam Cutoff: 75.0%
          </span>
        </div>

        <div className="mt-5 overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="border-b border-border bg-muted/30 text-muted-foreground uppercase text-[10px] tracking-wider">
              <tr>
                <th className="p-3">Course Code & Subject</th>
                <th className="p-3">Attended / Total</th>
                <th className="p-3">Percentage</th>
                <th className="p-3">Safe to Skip</th>
                <th className="p-3">Required Recovery</th>
                <th className="p-3">Exam Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {(d?.attendanceBuffer && d.attendanceBuffer.length > 0 ? d.attendanceBuffer : [
                { subjectCode: "CS101", subject: "Problem Solving & Python", present: 46, total: 48, percentage: 96, canSkip: 13, neededConsecutive: 0, status: "Safe" },
                { subjectCode: "CS102", subject: "Data Structures & Algorithms", present: 43, total: 46, percentage: 93, canSkip: 11, neededConsecutive: 0, status: "Safe" },
                { subjectCode: "MTH101", subject: "Engineering Mathematics", present: 38, total: 42, percentage: 90, canSkip: 8, neededConsecutive: 0, status: "Safe" },
                { subjectCode: "PHY101", subject: "Engineering Physics", present: 33, total: 40, percentage: 82, canSkip: 4, neededConsecutive: 0, status: "Safe" },
                { subjectCode: "ENG101", subject: "Technical Communication", present: 29, total: 38, percentage: 76, canSkip: 0, neededConsecutive: 1, status: "Safe" }
              ]).map((it, idx) => (
                <tr key={idx} className="hover:bg-muted/20 transition-colors">
                  <td className="p-3 font-medium">
                    <span className="font-mono text-primary font-semibold mr-1.5">{it.subjectCode}</span>
                    <span>{it.subject}</span>
                  </td>
                  <td className="p-3 font-mono">{it.present} / {it.total}</td>
                  <td className="p-3">
                    <div className="flex items-center gap-2">
                      <div className="metric-bar w-20">
                        <span
                          className={it.percentage >= 85 ? "bg-emerald-500" : it.percentage >= 75 ? "bg-amber-500" : "bg-destructive"}
                          style={{ width: `${it.percentage}%` }}
                        />
                      </div>
                      <span className="font-bold">{it.percentage}%</span>
                    </div>
                  </td>
                  <td className="p-3">
                    {it.canSkip > 0 ? (
                      <span className="inline-flex items-center gap-1 rounded bg-emerald-500/10 px-2 py-0.5 font-semibold text-emerald-600 dark:text-emerald-400">
                        🟢 Safe to miss {it.canSkip} lecture(s)
                      </span>
                    ) : (
                      <span className="text-muted-foreground">0 buffer classes</span>
                    )}
                  </td>
                  <td className="p-3">
                    {it.neededConsecutive > 0 ? (
                      <span className="inline-flex items-center gap-1 rounded bg-destructive/10 px-2 py-0.5 font-semibold text-destructive">
                        ⚠️ Must attend next {it.neededConsecutive} consecutive
                      </span>
                    ) : (
                      <span className="text-muted-foreground">—</span>
                    )}
                  </td>
                  <td className="p-3">
                    {it.percentage >= 75 ? (
                      <span className="rounded bg-emerald-500/15 px-2 py-0.5 font-semibold text-emerald-700 dark:text-emerald-300">
                        ✅ Cleared
                      </span>
                    ) : (
                      <span className="rounded bg-destructive/15 px-2 py-0.5 font-semibold text-destructive">
                        ⚠️ Condonation Needed
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      {/* AI Next Steps & Recommendations */}
      <Card className="p-6 shadow-sm">
        <div className="flex items-center justify-between border-b border-border pb-3">
          <div>
            <h2 className="display text-lg font-bold">💡 Recommended Action Items</h2>
            <p className="text-xs text-muted-foreground">
              Intelligent recommendations curated from your course submissions, exams, and attendance logs.
            </p>
          </div>
          <Sparkles className="size-5 text-accent" />
        </div>

        <div className="mt-5 grid gap-3 sm:grid-cols-2">
          {(d?.recommendations ?? [
            "Review database normalization and SQL indexing for upcoming midterm exam",
            "Maintain consistent study schedule and high attendance above 85% in core engineering modules",
            "Submit active programming assignments before Friday 11:59 PM deadline",
            "Explore technical hackathons and paper presentation symposiums"
          ]).map((r, i) => (
            <div key={i} className="flex flex-col justify-between rounded-xl border border-border bg-card p-4 hover:border-primary/50 transition-all shadow-2xs">
              <div className="flex items-start gap-3">
                <span className="grid size-6 shrink-0 place-items-center rounded-full bg-primary/10 text-xs font-bold text-primary">
                  {i + 1}
                </span>
                <span className="text-xs leading-relaxed text-foreground/90 font-medium">{r}</span>
              </div>
              <div className="mt-3 pt-2 border-t border-border/50 flex justify-end">
                <Link
                  href="/ai-assistant"
                  className="text-[11px] font-semibold text-primary hover:underline flex items-center gap-1"
                >
                  Consult AI Copilot <ArrowRight className="size-3" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}

function Profile() {
  const q = useGetMe();
  if (q.isLoading) return <SkeletonRows count={3} />;
  if (q.isError) return <ErrorState retry={() => q.refetch()} />;
  const me = q.data;
  return (
    <>
      <PageHead
        eyebrow="Account"
        title="Profile"
        description="Your identity across the Meridian workspace."
      />
      <div className="grid gap-5 lg:grid-cols-[.7fr_1.3fr]">
        <Card className="p-7">
          <div className="grid size-20 place-items-center rounded-2xl bg-primary text-2xl font-bold text-primary-foreground">
            {me?.name
              .split(" ")
              .map((x) => x[0])
              .join("")
              .slice(0, 2)}
          </div>
          <h2 className="display mt-6 text-3xl font-semibold">{me?.name}</h2>
          <p className="mt-1 text-sm text-muted-foreground">{me?.email}</p>
          <Status tone="positive">{me?.role ?? "member"}</Status>
        </Card>
        <Card className="p-7">
          <h2 className="display text-2xl font-semibold">Account details</h2>
          <div className="mt-6 grid gap-5 sm:grid-cols-2">
            <div>
              <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                Full name
              </div>
              <div className="mt-2 text-sm">{me?.name}</div>
            </div>
            <div>
              <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                Email
              </div>
              <div className="mt-2 text-sm">{me?.email}</div>
            </div>
            <div>
              <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                Access level
              </div>
              <div className="mt-2 text-sm capitalize">{me?.role}</div>
            </div>
            <div>
              <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                Member since
              </div>
              <div className="mt-2 text-sm">Current academic year</div>
            </div>
          </div>
        </Card>
      </div>
    </>
  );
}

function useAuth() {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("campusUser") || "null");
    } catch (_) {
      return null;
    }
  });
  const [role, setRole] = useState(() => {
    try {
      const u = JSON.parse(localStorage.getItem("campusUser") || "null");
      return u?.role || localStorage.getItem("mockRole") || null;
    } catch (_) {
      return localStorage.getItem("mockRole") || null;
    }
  });

  useEffect(() => {
    const handleStorageChange = () => {
      try {
        const u = JSON.parse(localStorage.getItem("campusUser") || "null");
        setUser(u);
        setRole(u?.role || localStorage.getItem("mockRole") || null);
      } catch (_) {
        setUser(null);
        setRole(localStorage.getItem("mockRole") || null);
      }
    };
    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, []);

  const isSignedIn = !!user || !!role || !!localStorage.getItem("campusToken");

  return {
    isLoaded: true,
    isSignedIn,
    user,
    role: user?.role || role || "student",
    switchRole: (newRole) => {
      const defaultUsernames = {
        student: "student1cse",
        faculty: "teacher1cse",
        admin: "admin",
        staff: "admin"
      };
      const defaultNames = {
        student: "Alex Rivera",
        faculty: "Dr. Alan Turing",
        admin: "Campus Administrator",
        staff: "Facility Staff"
      };
      const targetUsername = defaultUsernames[newRole] || "student1cse";
      const targetName = defaultNames[newRole] || "Campus Colleague";
      localStorage.setItem("mockRole", newRole);
      localStorage.setItem("campusUsername", targetUsername);
      localStorage.setItem("campusUser", JSON.stringify({
        username: targetUsername,
        role: newRole,
        name: targetName,
        department: "CSE"
      }));
      window.dispatchEvent(new Event("storage"));
      window.location.reload();
    },
    signOut: () => {
      localStorage.removeItem("campusUser");
      localStorage.removeItem("campusToken");
      localStorage.removeItem("campusUsername");
      localStorage.removeItem("mockRole");
      window.dispatchEvent(new Event("storage"));
      window.location.href = "/sign-in";
    },
  };
}

function Fees() {
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ["fees"],
    queryFn: () => customFetch("/api/fees"),
  });
  const [selectedYear, setSelectedYear] = useState("all");

  const yearlyData = data?.yearlyBreakdown || [
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
  ];

  const displayedYears = selectedYear === "all"
    ? yearlyData
    : yearlyData.filter((y) => String(y.year) === selectedYear);

  return (
    <>
      <PageHead
        eyebrow="Financial Management"
        title="Fees & Year-Wise Records"
        description="Maintain 4-year engineering fee records, payment schedules, and university invoices."
      />
      {isLoading ? (
        <div className="p-4">
          <SkeletonRows />
        </div>
      ) : isError ? (
        <div className="p-4">
          <ErrorState retry={() => refetch()} />
        </div>
      ) : (
        <div className="space-y-6">
          {/* 4-Year Summary Stats */}
          <div className="grid gap-4 sm:grid-cols-3">
            <Card className="p-5 border-l-4 border-l-primary">
              <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                Total 4-Year Program Fees
              </div>
              <div className="mt-4 display text-3xl font-bold">
                ${data?.total || 60000}
              </div>
              <div className="mt-1 text-xs text-muted-foreground">
                4 Academic Years (8 Semesters)
              </div>
            </Card>
            <Card className="p-5 border-l-4 border-l-emerald-500">
              <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                Total Amount Paid
              </div>
              <div className="mt-4 display text-3xl font-bold text-emerald-600">
                ${data?.paid || 45000}
              </div>
              <div className="mt-1 text-xs text-emerald-700">
                Receipts verified & cleared
              </div>
            </Card>
            <Card className="p-5 border-l-4 border-l-rose-500">
              <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                Pending Balance
              </div>
              <div className="mt-4 display text-3xl font-bold text-rose-600">
                ${data?.pending || 15000}
              </div>
              <div className="mt-1 text-xs text-rose-700">
                Current Year 4 Term Fee
              </div>
            </Card>
          </div>

          {/* Year-Wise Records Section */}
          <Card className="p-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border pb-4">
              <div>
                <h2 className="display text-xl font-semibold">
                  Year-Wise Fee Breakdown
                </h2>
                <p className="mt-0.5 text-xs text-muted-foreground">
                  Individual itemized breakdown for each year of the 4-year curriculum
                </p>
              </div>

              {/* Year Selector Tabs */}
              <div className="flex flex-wrap gap-1.5">
                <button
                  onClick={() => setSelectedYear("all")}
                  className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition-colors ${
                    selectedYear === "all"
                      ? "bg-primary text-primary-foreground"
                      : "bg-muted text-muted-foreground hover:bg-muted/80"
                  }`}
                >
                  All Years (1-4)
                </button>
                {[1, 2, 3, 4].map((yr) => (
                  <button
                    key={yr}
                    onClick={() => setSelectedYear(String(yr))}
                    className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition-colors ${
                      selectedYear === String(yr)
                        ? "bg-primary text-primary-foreground"
                        : "bg-muted text-muted-foreground hover:bg-muted/80"
                    }`}
                  >
                    Year {yr}
                  </button>
                ))}
              </div>
            </div>

            <div className="mt-6 grid gap-5 md:grid-cols-2">
              {displayedYears.map((yr) => (
                <div
                  key={yr.year}
                  className="rounded-xl border border-border bg-card p-5 transition-all hover:border-primary/40 hover:shadow-sm"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="rounded bg-primary/10 px-2.5 py-0.5 font-bold text-xs text-primary">
                          Year {yr.year}
                        </span>
                        <h3 className="font-semibold text-base">{yr.label}</h3>
                      </div>
                      <div className="mt-1 text-xs text-muted-foreground">
                        {yr.paidDate ? `Paid on ${yr.paidDate}` : `Due: ${yr.dueDate || "Term beginning"}`}
                      </div>
                    </div>
                    <Status tone={yr.status === "Paid" ? "positive" : yr.status === "Partial" ? "warning" : "critical"}>
                      {yr.status}
                    </Status>
                  </div>

                  {/* Itemized Fee Breakdown Table */}
                  <div className="mt-4 rounded-lg bg-muted/40 p-3 text-xs space-y-1.5 border border-border/50">
                    <div className="flex justify-between py-1 border-b border-border/40">
                      <span className="text-muted-foreground">Annual Tuition Fee:</span>
                      <span className="font-medium">${yr.tuition || 12000}</span>
                    </div>
                    <div className="flex justify-between py-1 border-b border-border/40">
                      <span className="text-muted-foreground">Engineering Lab & Workshops:</span>
                      <span className="font-medium">${yr.lab || 1500}</span>
                    </div>
                    <div className="flex justify-between py-1 border-b border-border/40">
                      <span className="text-muted-foreground">Semester Examination Fees:</span>
                      <span className="font-medium">${yr.exam || 1000}</span>
                    </div>
                    <div className="flex justify-between py-1 border-b border-border/40">
                      <span className="text-muted-foreground">Digital Library & Amenities:</span>
                      <span className="font-medium">${yr.library || 500}</span>
                    </div>
                    <div className="flex justify-between pt-1.5 font-semibold text-sm">
                      <span>Total Year Fee:</span>
                      <span>${yr.total || 15000}</span>
                    </div>
                  </div>

                  {/* Payment Status & Receipts */}
                  <div className="mt-4 flex items-center justify-between border-t border-border pt-3 text-xs">
                    <div className="space-y-0.5">
                      <div>
                        Paid: <strong className="text-emerald-600">${yr.paid || 0}</strong>
                        {yr.pending > 0 && (
                          <span className="ml-2 text-rose-600">
                            (Pending: <strong>${yr.pending}</strong>)
                          </span>
                        )}
                      </div>
                      {yr.receiptId && yr.receiptId !== "-" && (
                        <div className="text-[11px] text-muted-foreground">
                          Receipt: <span className="font-mono">{yr.receiptId}</span>
                        </div>
                      )}
                    </div>

                    {yr.status === "Paid" ? (
                      <span className="inline-flex items-center gap-1 text-xs font-semibold text-emerald-600">
                        <CheckCircle2 className="size-3.5" /> Settled
                      </span>
                    ) : (
                      <button
                        onClick={() => alert(`Payment portal for ${yr.label} will open.`)}
                        className="rounded-md bg-primary px-3 py-1.5 text-xs font-semibold text-primary-foreground hover:bg-primary/90"
                      >
                        Pay Now
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </Card>

          {/* Payment History */}
          <Card>
            <div className="border-b border-border p-4 font-semibold flex items-center justify-between">
              <span>Payment & Receipt History</span>
              <span className="text-xs font-normal text-muted-foreground">
                {(data?.history || []).length} Recorded transactions
              </span>
            </div>
            <div className="divide-y divide-border">
              {(data?.history || []).map((h) => (
                <div
                  key={h.id}
                  className="flex justify-between items-center p-4 hover:bg-muted/20 transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <div className="grid size-9 place-items-center rounded-lg bg-primary/10 text-primary">
                      <CreditCard className="size-4" />
                    </div>
                    <div>
                      <div className="font-semibold text-sm">{h.method}</div>
                      <div className="text-xs text-muted-foreground flex items-center gap-2 mt-0.5">
                        <span>{h.date}</span>
                        {h.academicYear && (
                          <span className="rounded bg-muted px-1.5 py-0.2 text-[10px] font-semibold text-muted-foreground">
                            {h.academicYear}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold text-sm">${h.amount}</div>
                    <Status tone="positive">{h.status}</Status>
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </div>
      )}
    </>
  );
}

function Settings() {
  const [, setLocation] = useLocation();
  const [saved, setSaved] = useState(false);
  const { signOut } = useAuth();
  return (
    <>
      <PageHead
        eyebrow="Account"
        title="Settings"
        description="Make Meridian work the way your day works."
      />
      <div className="max-w-3xl space-y-5">
        <Card className="p-6">
          <div className="flex items-start justify-between gap-5">
            <div>
              <h2 className="display text-2xl font-semibold">Notifications</h2>
              <p className="mt-1 text-sm text-muted-foreground">
                Choose what deserves a nudge.
              </p>
            </div>
            <Bell className="size-5 text-primary" />
          </div>
          <div className="mt-6 divide-y divide-border">
            {[
              "Important announcements",
              "Assignment deadlines",
              "Service request updates",
            ].map((x, i) => (
              <label
                key={x}
                className="flex items-center justify-between py-4 text-sm"
              >
                <span>{x}</span>
                <input
                  type="checkbox"
                  defaultChecked={i < 2}
                  className="size-4 accent-primary"
                  data-testid={`input-setting-${i}`}
                />
              </label>
            ))}
          </div>
        </Card>
        <Card className="p-6">
          <div className="flex items-start justify-between gap-5">
            <div>
              <h2 className="display text-2xl font-semibold">Workspace</h2>
              <p className="mt-1 text-sm text-muted-foreground">
                A few quiet defaults for your view.
              </p>
            </div>
            <Settings2 className="size-5 text-primary" />
          </div>
          <label className="mt-6 block text-sm">
            <span className="mb-1.5 block text-xs font-semibold text-muted-foreground">
              Default landing view
            </span>
            <select
              className="h-10 w-full rounded-lg border border-input bg-background px-3 text-sm"
              defaultValue="/"
            >
              <option value="/">Dashboard</option>
              <option value="/attendance">Attendance</option>
              <option value="/assignments">Assignments</option>
            </select>
          </label>
          <button
            onClick={() => setSaved(true)}
            className="mt-5 rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground"
            data-testid="button-save-settings"
          >
            {saved ? "Preferences saved" : "Save preferences"}
          </button>
        </Card>
        <Card className="flex items-center justify-between p-6">
          <div>
            <h2 className="font-semibold">Sign out</h2>
            <p className="mt-1 text-sm text-muted-foreground">
              End your current workspace session.
            </p>
          </div>
          <button
            onClick={() => signOut()}
            className="flex items-center gap-2 rounded-lg border border-destructive/30 px-4 py-2.5 text-sm font-semibold text-destructive hover:bg-destructive/5"
            data-testid="button-sign-out"
          >
            <LogOut className="size-4" /> Sign out
          </button>
        </Card>
      </div>
    </>
  );
}

function Auth({ mode }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("123");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (uName, pWord) => {
    const loginUser = uName || username;
    const loginPass = pWord || password;

    if (!loginUser || !loginPass) {
      setError("Please enter both username and password.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const data = await customFetch("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ username: loginUser.trim(), password: loginPass.trim() }),
      });

      // Save session credentials
      localStorage.setItem("campusToken", data.token);
      localStorage.setItem("campusUser", JSON.stringify(data.user));
      localStorage.setItem("campusUsername", data.user.username);
      localStorage.setItem("mockRole", data.user.role);

      window.dispatchEvent(new Event("storage"));
      window.location.href = "/dashboard";
    } catch (err) {
      setError(err?.data?.error || err.message || "Unable to connect to authentication server.");
    } finally {
      setLoading(false);
    }
  };

  const quickLogin = (demoUser) => {
    setUsername(demoUser);
    setPassword("123");
    handleLogin(demoUser, "123");
  };

  return (
    <div className="grid min-h-[100dvh] bg-background md:grid-cols-[.85fr_1.15fr]">
      <div className="hidden bg-primary p-10 text-primary-foreground md:flex md:flex-col md:justify-between">
        <div className="flex items-center gap-3">
          <div className="grid size-9 place-items-center rounded-lg bg-accent text-primary">
            <Command className="size-5" />
          </div>
          <span className="display text-xl font-semibold">Meridian Smart Campus</span>
        </div>
        <div className="my-auto py-12">
          <div className="mb-5 text-xs font-bold uppercase tracking-[.18em] text-accent">
            Academic & Administrative Portal
          </div>
          <h1 className="display max-w-md text-5xl font-semibold leading-tight">
            The digital core of campus life.
          </h1>
          <p className="mt-6 max-w-sm leading-7 text-primary-foreground/75">
            Role-based services connecting students, faculty, and administration across 5 Engineering departments.
          </p>
          <div className="mt-8 flex flex-wrap gap-2 text-xs">
            <span className="rounded-md bg-primary-foreground/15 px-2.5 py-1">CSE</span>
            <span className="rounded-md bg-primary-foreground/15 px-2.5 py-1">ECE</span>
            <span className="rounded-md bg-primary-foreground/15 px-2.5 py-1">MECH</span>
            <span className="rounded-md bg-primary-foreground/15 px-2.5 py-1">CIVIL</span>
            <span className="rounded-md bg-primary-foreground/15 px-2.5 py-1">EEE</span>
          </div>
        </div>
        <div className="text-xs text-primary-foreground/40">
          Secure Authentication · Default Password: 123
        </div>
      </div>

      <div className="flex items-center justify-center p-6 sm:p-10">
        <div className="w-full max-w-md space-y-6">
          <div>
            <div className="mb-2 text-xs font-bold uppercase tracking-[.16em] text-primary">
              Portal Access
            </div>
            <h1 className="display text-3xl font-semibold">Sign in to your account</h1>
            <p className="mt-2 text-sm text-muted-foreground">
              Enter your department credentials to access personalized dashboards and workflows.
            </p>
          </div>

          {error && (
            <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-3.5 text-sm text-destructive flex items-center gap-2">
              <AlertCircle className="size-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleLogin();
            }}
            className="space-y-4"
          >
            <div>
              <label className="block text-xs font-semibold text-muted-foreground mb-1.5">
                Username / Dept ID
              </label>
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="e.g. teacher1cse, student1cse, admin"
                className="h-11 w-full rounded-lg border border-input bg-background px-3.5 text-sm transition-colors focus:border-primary focus:outline-none"
                data-testid="input-login-username"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-muted-foreground mb-1.5">
                Password
              </label>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter password (default: 123)"
                  className="h-11 w-full rounded-lg border border-input bg-background px-3.5 pr-10 text-sm transition-colors focus:border-primary focus:outline-none"
                  data-testid="input-login-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-3 text-muted-foreground hover:text-foreground text-xs"
                >
                  {showPassword ? "Hide" : "Show"}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="h-11 w-full rounded-lg bg-primary font-semibold text-primary-foreground hover:bg-primary/90 transition-colors flex items-center justify-center gap-2"
              data-testid="button-submit-login"
            >
              {loading ? "Signing in…" : "Sign In"}
              <ArrowRight className="size-4" />
            </button>
          </form>

          <div className="pt-4 border-t border-border">
            <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3 flex items-center justify-between">
              <span>Quick Demo Accounts</span>
              <span className="text-[11px] text-primary lowercase font-normal">Password: 123</span>
            </div>

            <div className="space-y-2">
              <div className="text-[11px] font-semibold text-muted-foreground">Administration & Teachers:</div>
              <div className="flex flex-wrap gap-1.5">
                <button
                  type="button"
                  onClick={() => quickLogin("admin")}
                  className="px-2.5 py-1 rounded bg-muted text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  👑 admin
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("teacher1cse")}
                  className="px-2.5 py-1 rounded bg-muted text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  👨‍🏫 teacher1cse
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("teacher26cse")}
                  className="px-2.5 py-1 rounded bg-primary/20 text-primary font-medium text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                  title="Professor for CS601 (AI & Expert Systems) - 6th Semester"
                >
                  👨‍🏫 teacher26cse (Sem 6)
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("teacher1ece")}
                  className="px-2.5 py-1 rounded bg-muted text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  👨‍🏫 teacher1ece
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("teacher1mech")}
                  className="px-2.5 py-1 rounded bg-muted text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  👨‍🏫 teacher1mech
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("teacher1civil")}
                  className="px-2.5 py-1 rounded bg-muted text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  👨‍🏫 teacher1civil
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("teacher1eee")}
                  className="px-2.5 py-1 rounded bg-muted text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  👨‍🏫 teacher1eee
                </button>
              </div>

              <div className="text-[11px] font-semibold text-muted-foreground pt-1">Students (5 Depts):</div>
              <div className="flex flex-wrap gap-1.5">
                <button
                  type="button"
                  onClick={() => quickLogin("student1cse")}
                  className="px-2.5 py-1 rounded bg-primary/10 text-primary text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  🎓 student1cse (Sem 1)
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("student6cse")}
                  className="px-2.5 py-1 rounded bg-primary text-primary-foreground font-semibold text-xs hover:bg-primary/90 transition-colors font-mono shadow-sm"
                  title="6th Semester CSE Student - 5 Core Subjects"
                >
                  🎓 student6cse (Sem 6)
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("student2cse")}
                  className="px-2.5 py-1 rounded bg-primary/10 text-primary text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  🎓 student2cse (Sem 2)
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("student1ece")}
                  className="px-2.5 py-1 rounded bg-primary/10 text-primary text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  🎓 student1ece
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("student1mech")}
                  className="px-2.5 py-1 rounded bg-primary/10 text-primary text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  🎓 student1mech
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("student1civil")}
                  className="px-2.5 py-1 rounded bg-primary/10 text-primary text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  🎓 student1civil
                </button>
                <button
                  type="button"
                  onClick={() => quickLogin("student1eee")}
                  className="px-2.5 py-1 rounded bg-primary/10 text-primary text-xs hover:bg-primary hover:text-primary-foreground transition-colors font-mono"
                >
                  🎓 student1eee
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function Router() {
  const [location, setLocation] = useLocation();
  const { isLoaded, isSignedIn } = useAuth();
  useEffect(() => {
    if (isLoaded) {
      if (
        isSignedIn &&
        (location === "/" ||
          location.startsWith("/sign-in") ||
          location.startsWith("/sign-up"))
      ) {
        setLocation("/dashboard");
      } else if (
        !isSignedIn &&
        !location.startsWith("/sign-in") &&
        !location.startsWith("/sign-up")
      ) {
        setLocation("/sign-in");
      }
    }
  }, [location, isLoaded, isSignedIn, setLocation]);
  if (!isLoaded)
    return (
      <div className="p-10">
        <SkeletonRows count={5} />
      </div>
    );
  const publicPath =
    location.startsWith("/sign-in") || location.startsWith("/sign-up");
  return (
    <ErrorBoundary resetKey={location}>
      {publicPath ? (
        <Switch>
          <Route path="/sign-in">
            <Auth mode="in" />
          </Route>
          <Route path="/sign-up">
            <Auth mode="up" />
          </Route>
          <Route component={NotFound} />
        </Switch>
      ) : (
        <>
          {isSignedIn ? (
            <Shell>
              <Switch>
                <Route path="/dashboard" component={DashboardRouter} />
                <Route path="/students" component={Students} />
                <Route path="/faculty" component={Faculty} />
                <Route path="/departments" component={Departments} />
                <Route path="/subjects" component={Subjects} />
                <Route path="/timetable" component={Timetable} />
                <Route path="/attendance" component={Attendance} />
                <Route path="/marks" component={Marks} />
                <Route path="/assignments" component={Assignments} />
                <Route path="/leave-requests" component={StudentLeaveRequests} />
                <Route path="/my-leave-requests" component={TeacherLeaveRequests} />
                <Route path="/leave-approvals" component={LeaveApprovals} />
                <Route path="/announcements" component={Announcements} />
                <Route path="/events" component={Events} />
                <Route path="/complaints" component={Complaints} />
                <Route path="/notifications" component={Notifications} />
                <Route path="/ai-assistant" component={AiAssistant} />
                <Route path="/academic-insights" component={Insights} />
                <Route path="/fees" component={Fees} />
                <Route path="/profile" component={Profile} />
                <Route path="/settings" component={Settings} />
                <Route component={NotFound} />
              </Switch>
            </Shell>
          ) : null}
        </>
      )}
    </ErrorBoundary>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <Router />
        <Toaster />
      </TooltipProvider>
    </QueryClientProvider>
  );
}
export default App;
