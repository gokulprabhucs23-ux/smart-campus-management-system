import { useState, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { customFetch } from "../../api/custom-fetch";
import {
  CalendarDays,
  CalendarCheck,
  Clock3,
  CheckCircle2,
  XCircle,
  AlertCircle,
  Plus,
  Filter,
  Search,
  User,
  GraduationCap,
  FileText,
  Trash2,
  ChevronRight,
  Info,
  CalendarPlus,
  ShieldCheck,
} from "lucide-react";
import {
  useListLeaves,
  useCreateLeave,
  useDeleteLeave,
  useListFaculty,
  useGetMe,
} from "../../api";

export function StudentLeaveRequests() {
  const me = useGetMe();
  const currentUsername = me.data?.username || "student1cse";
  const currentName = me.data?.name || "Alex Rivera";
  const currentDept = me.data?.department || "CSE";

  const { data: leaves = [], isLoading, isError, refetch } = useListLeaves({
    applicantType: "student",
    applicantUsername: currentUsername,
  });

  const { data: facultyList = [] } = useListFaculty();
  const createMutation = useCreateLeave();
  const deleteMutation = useDeleteLeave();

  // Fetch student details to resolve designated faculty mentor
  const { data: myStudentProfile } = useQuery({
    queryKey: ["myStudentProfile", currentUsername],
    queryFn: async () => {
      try {
        const students = await customFetch("/api/students");
        const myStudent = (students || []).find(
          (s) => (s.email && s.email.toLowerCase().startsWith(currentUsername.toLowerCase())) || s.name === currentName
        );
        if (myStudent?.id) {
          const details = await customFetch(`/api/students/${myStudent.id}/details`);
          return { student: myStudent, details };
        }
      } catch (_) {}
      return null;
    },
  });

  const assignedMentor = myStudentProfile?.details?.mentor || {
    name: "Dr. Marvin Minsky",
    designation: "Professor",
    department: currentDept,
    username: "teacher26cse"
  };

  const [modalOpen, setModalOpen] = useState(false);
  const [activeTab, setActiveTab] = useState("all");
  const [search, setSearch] = useState("");
  const [toastMessage, setToastMessage] = useState("");

  // Form states
  const [leaveType, setLeaveType] = useState("Medical Leave");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [assignedTeacher, setAssignedTeacher] = useState("");
  const [reason, setReason] = useState("");
  const [studentRoll, setStudentRoll] = useState("STU-2024-001");

  // Calculate duration in days
  const calculatedDays = useMemo(() => {
    if (!startDate || !endDate) return 1;
    const s = new Date(startDate);
    const e = new Date(endDate);
    if (isNaN(s.getTime()) || isNaN(e.getTime()) || e < s) return 1;
    const diffTime = Math.abs(e - s);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return diffDays;
  }, [startDate, endDate]);

  const showToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(""), 4000);
  };

  const handleCreateSubmit = (e) => {
    e.preventDefault();
    if (!startDate || !endDate) {
      alert("Please select both start and end dates.");
      return;
    }
    if (!reason.trim()) {
      alert("Please enter a reason for the leave application.");
      return;
    }

    const approverName = assignedMentor?.name || "Dr. Marvin Minsky (Mentor)";
    const approverUsername = assignedMentor?.username || "teacher26cse";

    createMutation.mutate(
      {
        applicantType: "student",
        applicantUsername: currentUsername,
        applicantName: currentName,
        department: currentDept,
        studentId: studentRoll,
        leaveType,
        startDate,
        endDate,
        totalDays: calculatedDays,
        reason,
        assignedApproverRole: "faculty",
        assignedApproverUsername: approverUsername,
        assignedApproverName: approverName,
      },
      {
        onSuccess: () => {
          setModalOpen(false);
          setReason("");
          setStartDate("");
          setEndDate("");
          showToast("Leave application successfully submitted to teacher for review!");
        },
      }
    );
  };

  const handleCancelRequest = (id) => {
    if (confirm("Are you sure you want to withdraw this pending leave request?")) {
      deleteMutation.mutate(id, {
        onSuccess: () => {
          showToast("Leave request cancelled.");
        },
      });
    }
  };

  // Filtered list
  const filteredLeaves = useMemo(() => {
    return leaves.filter((item) => {
      const matchesTab =
        activeTab === "all"
          ? true
          : activeTab === "pending"
          ? item.status === "PENDING"
          : activeTab === "approved"
          ? item.status === "APPROVED"
          : item.status === "REJECTED";

      const matchesSearch =
        !search ||
        item.leaveType?.toLowerCase().includes(search.toLowerCase()) ||
        item.reason?.toLowerCase().includes(search.toLowerCase()) ||
        item.assignedApproverName?.toLowerCase().includes(search.toLowerCase());

      return matchesTab && matchesSearch;
    });
  }, [leaves, activeTab, search]);

  // Statistics
  const totalApplied = leaves.length;
  const pendingCount = leaves.filter((l) => l.status === "PENDING").length;
  const approvedCount = leaves.filter((l) => l.status === "APPROVED").length;
  const totalApprovedDays = leaves
    .filter((l) => l.status === "APPROVED")
    .reduce((acc, curr) => acc + (curr.totalDays || 1), 0);

  return (
    <div className="space-y-6 pb-12">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed bottom-6 right-6 z-50 flex items-center gap-2 rounded-xl border border-emerald-500/30 bg-emerald-950/90 px-4 py-3 text-sm font-medium text-emerald-100 shadow-2xl backdrop-blur-md">
          <CheckCircle2 className="size-4 text-emerald-400" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col justify-between gap-4 md:flex-row md:items-end">
        <div>
          <div className="mb-1 text-[11px] font-bold uppercase tracking-[.18em] text-primary">
            Academic Services & Attendance
          </div>
          <h1 className="text-3xl font-bold tracking-tight md:text-4xl text-foreground">
            Student Leave Requests
          </h1>
          <p className="mt-1 text-sm text-muted-foreground max-w-2xl">
            Submit formal leave applications to your designated faculty mentor or department head, and monitor approval status in real time.
          </p>
        </div>
        <button
          onClick={() => setModalOpen(true)}
          className="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground shadow-md hover:bg-primary/90 transition-all hover:scale-[1.02] active:scale-[0.98]"
          data-testid="button-apply-leave"
        >
          <CalendarPlus className="size-4" />
          <span>Apply for Leave</span>
        </button>
      </div>

      {/* KPI Stats Cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="rounded-2xl border border-border/80 bg-card/60 p-5 shadow-sm backdrop-blur-sm">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Applied</span>
            <FileText className="size-4 text-primary" />
          </div>
          <div className="mt-3 text-3xl font-bold text-foreground">{totalApplied}</div>
          <p className="mt-1 text-xs text-muted-foreground">Lifetime applications</p>
        </div>

        <div className="rounded-2xl border border-amber-500/20 bg-amber-500/5 p-5 shadow-sm">
          <div className="flex items-center justify-between text-amber-700 dark:text-amber-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Awaiting Teacher</span>
            <Clock3 className="size-4" />
          </div>
          <div className="mt-3 text-3xl font-bold text-amber-900 dark:text-amber-300">
            {pendingCount}
          </div>
          <p className="mt-1 text-xs text-amber-700/80 dark:text-amber-400/80">Pending faculty review</p>
        </div>

        <div className="rounded-2xl border border-emerald-500/20 bg-emerald-500/5 p-5 shadow-sm">
          <div className="flex items-center justify-between text-emerald-700 dark:text-emerald-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Approved Leaves</span>
            <CheckCircle2 className="size-4" />
          </div>
          <div className="mt-3 text-3xl font-bold text-emerald-900 dark:text-emerald-300">
            {approvedCount}
          </div>
          <p className="mt-1 text-xs text-emerald-700/80 dark:text-emerald-400/80">Sanctioned by faculty</p>
        </div>

        <div className="rounded-2xl border border-primary/20 bg-primary/5 p-5 shadow-sm">
          <div className="flex items-center justify-between text-primary">
            <span className="text-xs font-semibold uppercase tracking-wider">Approved Days</span>
            <CalendarCheck className="size-4" />
          </div>
          <div className="mt-3 text-3xl font-bold text-foreground">{totalApprovedDays} Days</div>
          <p className="mt-1 text-xs text-muted-foreground">Excused academic days</p>
        </div>
      </div>

      {/* Main Content Card */}
      <div className="rounded-2xl border border-border bg-card shadow-sm overflow-hidden">
        {/* Filter bar */}
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 border-b border-border p-4 bg-muted/20">
          {/* Tabs */}
          <div className="flex items-center gap-1 rounded-xl bg-muted p-1 text-xs font-medium">
            {[
              { id: "all", label: "All Requests", count: leaves.length },
              { id: "pending", label: "Pending", count: pendingCount },
              { id: "approved", label: "Approved", count: approvedCount },
              { id: "rejected", label: "Declined", count: leaves.filter((l) => l.status === "REJECTED").length },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`rounded-lg px-3 py-1.5 transition-colors flex items-center gap-1.5 ${
                  activeTab === tab.id
                    ? "bg-background text-foreground font-semibold shadow-sm"
                    : "text-muted-foreground hover:text-foreground"
                }`}
              >
                <span>{tab.label}</span>
                <span className="rounded-full bg-muted-foreground/10 px-1.5 py-0.2 text-[10px]">
                  {tab.count}
                </span>
              </button>
            ))}
          </div>

          {/* Search box */}
          <div className="relative">
            <Search className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
            <input
              type="text"
              placeholder="Search leaves or faculty..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="h-9 w-full sm:w-64 rounded-xl border border-input bg-background pl-9 pr-3 text-xs placeholder:text-muted-foreground focus:border-primary focus:outline-none"
            />
          </div>
        </div>

        {/* Requests List */}
        {isLoading ? (
          <div className="p-8 space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-20 rounded-xl bg-muted/40 animate-pulse" />
            ))}
          </div>
        ) : isError ? (
          <div className="p-12 text-center">
            <AlertCircle className="mx-auto size-8 text-destructive" />
            <p className="mt-2 text-sm font-semibold">Failed to load leave records.</p>
            <button
              onClick={() => refetch()}
              className="mt-3 text-xs text-primary underline font-medium"
            >
              Try Again
            </button>
          </div>
        ) : filteredLeaves.length === 0 ? (
          <div className="p-16 text-center text-muted-foreground">
            <CalendarDays className="mx-auto size-12 stroke-[1.2] text-muted-foreground/50 mb-3" />
            <p className="text-base font-semibold text-foreground">No leave records found</p>
            <p className="mt-1 text-xs">
              {activeTab === "all"
                ? "You haven't submitted any leave applications yet."
                : `No leave applications matching "${activeTab}" filter.`}
            </p>
            {activeTab === "all" && (
              <button
                onClick={() => setModalOpen(true)}
                className="mt-4 rounded-xl bg-primary/10 px-4 py-2 text-xs font-semibold text-primary hover:bg-primary/20"
              >
                Submit First Application
              </button>
            )}
          </div>
        ) : (
          <div className="divide-y divide-border">
            {filteredLeaves.map((item) => (
              <div
                key={item.id}
                className="p-5 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 hover:bg-muted/15 transition-colors"
                data-testid={`leave-card-${item.id}`}
              >
                <div className="space-y-2 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-semibold text-base text-foreground">
                      {item.leaveType}
                    </span>
                    <span className="rounded-full bg-primary/10 text-primary px-2.5 py-0.5 text-xs font-medium">
                      {item.totalDays} {item.totalDays === 1 ? "Day" : "Days"}
                    </span>
                    {item.status === "PENDING" && (
                      <span className="inline-flex items-center gap-1 rounded-full bg-amber-500/10 px-2.5 py-0.5 text-xs font-semibold text-amber-700 dark:text-amber-400 border border-amber-500/20">
                        <Clock3 className="size-3" /> Awaiting Teacher
                      </span>
                    )}
                    {item.status === "APPROVED" && (
                      <span className="inline-flex items-center gap-1 rounded-full bg-emerald-500/10 px-2.5 py-0.5 text-xs font-semibold text-emerald-700 dark:text-emerald-400 border border-emerald-500/20">
                        <CheckCircle2 className="size-3" /> Approved by Faculty
                      </span>
                    )}
                    {item.status === "REJECTED" && (
                      <span className="inline-flex items-center gap-1 rounded-full bg-rose-500/10 px-2.5 py-0.5 text-xs font-semibold text-rose-700 dark:text-rose-400 border border-rose-500/20">
                        <XCircle className="size-3" /> Declined
                      </span>
                    )}
                  </div>

                  {/* Dates & Reviewer */}
                  <div className="flex flex-wrap items-center gap-y-1 gap-x-4 text-xs text-muted-foreground">
                    <div className="flex items-center gap-1.5 font-medium text-foreground/80">
                      <CalendarDays className="size-3.5 text-primary" />
                      <span>{item.startDate} to {item.endDate}</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <GraduationCap className="size-3.5 text-muted-foreground" />
                      <span>
                        Reviewed by: <strong className="text-foreground">{item.assignedApproverName || "Department Faculty"}</strong>
                      </span>
                    </div>
                    <span>Applied: {item.createdAt}</span>
                  </div>

                  {/* Reason */}
                  <p className="text-xs text-foreground/85 leading-relaxed bg-muted/30 p-2.5 rounded-lg border border-border/50">
                    <strong className="text-foreground/70">Reason: </strong>{item.reason}
                  </p>

                  {/* Remarks from Teacher if available */}
                  {item.reviewerRemarks && (
                    <div className="text-xs rounded-lg p-2.5 border bg-accent/20 border-accent/30 text-accent-foreground flex items-start gap-2">
                      <Info className="size-4 shrink-0 mt-0.5 text-primary" />
                      <div>
                        <strong>Faculty Remarks ({item.reviewedBy || "Teacher"}): </strong>
                        <span>{item.reviewerRemarks}</span>
                        {item.reviewedAt && (
                          <span className="block mt-0.5 text-[10px] opacity-75">
                            Decided on {item.reviewedAt}
                          </span>
                        )}
                      </div>
                    </div>
                  )}
                </div>

                {/* Actions */}
                <div className="flex items-center gap-2 self-end md:self-center shrink-0">
                  {item.status === "PENDING" && (
                    <button
                      onClick={() => handleCancelRequest(item.id)}
                      className="inline-flex items-center gap-1 rounded-lg border border-destructive/30 px-3 py-1.5 text-xs font-medium text-destructive hover:bg-destructive/10 transition-colors"
                      title="Withdraw application"
                    >
                      <Trash2 className="size-3.5" />
                      <span>Cancel</span>
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Modal: Apply for Leave */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm animate-in fade-in">
          <div className="w-full max-w-lg rounded-2xl border border-border bg-card p-6 shadow-2xl space-y-5">
            <div className="flex items-start justify-between border-b border-border pb-3">
              <div>
                <h2 className="text-xl font-bold text-foreground">Apply for Student Leave</h2>
                <p className="text-xs text-muted-foreground mt-0.5">
                  Your request will be submitted directly to your teacher/faculty for approval.
                </p>
              </div>
              <button
                onClick={() => setModalOpen(false)}
                className="rounded-lg p-1.5 text-muted-foreground hover:bg-muted transition-colors"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleCreateSubmit} className="space-y-4">
              {/* Applicant Info (read-only banner) */}
              <div className="rounded-xl bg-muted/40 p-3 text-xs flex items-center justify-between border border-border/50">
                <div>
                  <span className="text-muted-foreground">Student: </span>
                  <strong className="text-foreground">{currentName}</strong> ({currentDept})
                </div>
                <div>
                  <span className="text-muted-foreground">ID: </span>
                  <strong className="text-foreground">{studentRoll}</strong>
                </div>
              </div>

              {/* Leave Type */}
              <div>
                <label className="block text-xs font-semibold text-foreground mb-1">
                  Leave Category
                </label>
                <select
                  value={leaveType}
                  onChange={(e) => setLeaveType(e.target.value)}
                  className="w-full h-10 rounded-xl border border-input bg-background px-3 text-xs font-medium focus:border-primary focus:outline-none"
                  required
                >
                  <option value="Medical Leave">Medical Leave (Illness / Hospitalization)</option>
                  <option value="Academic / On-Duty">Academic / On-Duty (Symposium / Hackathon)</option>
                  <option value="Casual / Personal">Casual / Personal Leave</option>
                  <option value="Family / Emergency">Family Function / Emergency</option>
                </select>
              </div>

              {/* Designated Faculty Mentor */}
              <div>
                <label className="block text-xs font-semibold text-foreground mb-1">
                  Designated Faculty Mentor (Authorized Approver)
                </label>
                <div className="rounded-xl border border-primary/20 bg-primary/5 p-3 flex items-center justify-between text-xs">
                  <div>
                    <div className="font-bold text-foreground flex items-center gap-1.5">
                      <GraduationCap className="size-4 text-primary" />
                      {assignedMentor?.name || "Dr. Marvin Minsky"}
                    </div>
                    <div className="text-[11px] text-muted-foreground mt-0.5">
                      {assignedMentor?.designation || "Professor"} · {assignedMentor?.department || currentDept} Department
                    </div>
                  </div>
                  <span className="rounded-full bg-primary/10 text-primary border border-primary/20 px-2 py-0.5 text-[10px] font-bold">
                    Designated Mentor
                  </span>
                </div>
                <p className="mt-1 text-[11px] text-muted-foreground">
                  Under campus mentorship rules, only your designated mentor has authority to approve or decline this leave application.
                </p>
              </div>

              {/* Date Pickers */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-foreground mb-1">
                    Start Date
                  </label>
                  <input
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    className="w-full h-10 rounded-xl border border-input bg-background px-3 text-xs font-medium focus:border-primary focus:outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-foreground mb-1">
                    End Date
                  </label>
                  <input
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    className="w-full h-10 rounded-xl border border-input bg-background px-3 text-xs font-medium focus:border-primary focus:outline-none"
                    required
                  />
                </div>
              </div>

              {/* Duration Summary */}
              {startDate && endDate && (
                <div className="rounded-xl bg-primary/5 p-2.5 text-xs text-primary font-medium flex items-center justify-between border border-primary/20">
                  <span>Calculated Duration:</span>
                  <span className="font-bold">{calculatedDays} Calendar {calculatedDays === 1 ? "Day" : "Days"}</span>
                </div>
              )}

              {/* Reason */}
              <div>
                <label className="block text-xs font-semibold text-foreground mb-1">
                  Reason / Explanation
                </label>
                <textarea
                  rows={3}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="State the reason clearly (e.g. physician appointment, hackathon attendance, family event)..."
                  className="w-full rounded-xl border border-input bg-background p-3 text-xs placeholder:text-muted-foreground focus:border-primary focus:outline-none"
                  required
                />
              </div>

              {/* Buttons */}
              <div className="flex items-center justify-end gap-3 pt-3 border-t border-border">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="rounded-xl border border-input px-4 py-2 text-xs font-semibold text-muted-foreground hover:bg-muted transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createMutation.isPending}
                  className="rounded-xl bg-primary px-5 py-2 text-xs font-semibold text-primary-foreground hover:bg-primary/90 transition-all disabled:opacity-50"
                  data-testid="button-submit-student-leave"
                >
                  {createMutation.isPending ? "Submitting..." : "Send Request to Teacher"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
export default StudentLeaveRequests;
