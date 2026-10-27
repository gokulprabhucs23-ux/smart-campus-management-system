import { useState, useMemo } from "react";
import {
  CheckCircle2,
  XCircle,
  Clock3,
  CalendarDays,
  CalendarCheck,
  AlertCircle,
  Filter,
  Search,
  User,
  GraduationCap,
  ShieldCheck,
  FileText,
  Check,
  X,
  MessageSquare,
  Building,
  Users2,
  Info,
} from "lucide-react";
import {
  useListLeaves,
  useApproveLeave,
  useRejectLeave,
  useGetMe,
} from "../../api";

export function LeaveApprovals() {
  const me = useGetMe();
  const role = me.data?.role || "faculty";
  const isTeacher = role === "faculty";
  const isAdmin = role === "admin" || role === "staff";

  // For Admin: toggle between Faculty Leave Approvals and Student Leaves Oversight
  const [adminSection, setAdminSection] = useState("faculty"); // "faculty" | "student"

  // Fetch approvals scoped to current user role and mentor assignments
  const { data: allLeaves = [], isLoading, isError, refetch } = useListLeaves({ scope: "approvals" });

  const approveMutation = useApproveLeave();
  const rejectMutation = useRejectLeave();

  const [activeTab, setActiveTab] = useState("pending");
  const [search, setSearch] = useState("");
  const [toastMessage, setToastMessage] = useState("");
  const [toastIsError, setToastIsError] = useState(false);

  // Action Modals State
  const [selectedLeave, setSelectedLeave] = useState(null);
  const [actionType, setActionType] = useState(null); // "approve" | "reject"
  const [actionRemarks, setActionRemarks] = useState("");

  const showToast = (msg, isErr = false) => {
    setToastMessage(msg);
    setToastIsError(isErr);
    setTimeout(() => {
      setToastMessage("");
      setToastIsError(false);
    }, 4500);
  };

  // Determine current dataset based on viewer role
  const targetLeaves = useMemo(() => {
    if (isTeacher) {
      // Teachers only approve student leaves (their mentees)
      return allLeaves.filter((l) => l.applicantType === "student");
    }
    if (isAdmin) {
      if (adminSection === "faculty") {
        return allLeaves.filter((l) => l.applicantType === "faculty");
      }
      return allLeaves.filter((l) => l.applicantType === "student");
    }
    return allLeaves;
  }, [allLeaves, isTeacher, isAdmin, adminSection]);

  // Tab & search filtering
  const filteredLeaves = useMemo(() => {
    return targetLeaves.filter((item) => {
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
        item.applicantName?.toLowerCase().includes(search.toLowerCase()) ||
        item.department?.toLowerCase().includes(search.toLowerCase()) ||
        item.studentId?.toLowerCase().includes(search.toLowerCase()) ||
        item.leaveType?.toLowerCase().includes(search.toLowerCase()) ||
        item.reason?.toLowerCase().includes(search.toLowerCase());

      return matchesTab && matchesSearch;
    });
  }, [targetLeaves, activeTab, search]);

  // Statistics
  const pendingCount = targetLeaves.filter((l) => l.status === "PENDING").length;
  const approvedCount = targetLeaves.filter((l) => l.status === "APPROVED").length;
  const rejectedCount = targetLeaves.filter((l) => l.status === "REJECTED").length;

  const handleOpenActionModal = (leave, type) => {
    setSelectedLeave(leave);
    setActionType(type);
    if (type === "approve") {
      setActionRemarks(
        isTeacher
          ? "Approved. Ensure lecture slides and lab notes are completed upon resumption."
          : "Approved. Class coverage arrangement noted and sanctioned."
      );
    } else {
      setActionRemarks(
        isTeacher
          ? "Declined due to mandatory examination / practical session on scheduled date."
          : "Declined due to institutional commitments on the requested dates."
      );
    }
  };

  const handleConfirmAction = () => {
    if (!selectedLeave || !actionType) return;

    if (actionType === "approve") {
      approveMutation.mutate(
        { id: selectedLeave.id, remarks: actionRemarks },
        {
          onSuccess: () => {
            showToast(`Leave application #${selectedLeave.id} for ${selectedLeave.applicantName} APPROVED.`);
            setSelectedLeave(null);
            setActionType(null);
          },
          onError: (err) => {
            showToast(err?.data?.error || err?.message || "Failed to approve leave.", true);
          },
        }
      );
    } else {
      rejectMutation.mutate(
        { id: selectedLeave.id, remarks: actionRemarks },
        {
          onSuccess: () => {
            showToast(`Leave application #${selectedLeave.id} for ${selectedLeave.applicantName} REJECTED.`);
            setSelectedLeave(null);
            setActionType(null);
          },
          onError: (err) => {
            showToast(err?.data?.error || err?.message || "Failed to reject leave.", true);
          },
        }
      );
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Toast Notification */}
      {toastMessage && (
        <div className={`fixed bottom-6 right-6 z-50 flex items-center gap-2 rounded-xl border px-4 py-3 text-sm font-medium shadow-2xl backdrop-blur-md ${
          toastIsError
            ? "border-rose-500/30 bg-rose-950/90 text-rose-100"
            : "border-emerald-500/30 bg-emerald-950/90 text-emerald-100"
        }`}>
          {toastIsError ? <AlertCircle className="size-4 text-rose-400" /> : <CheckCircle2 className="size-4 text-emerald-400" />}
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col justify-between gap-4 md:flex-row md:items-end">
        <div>
          <div className="mb-1 text-[11px] font-bold uppercase tracking-[.18em] text-primary">
            {isTeacher ? "Teacher Portal • Mentorship & Attendance" : "Administrative Desk • Campus Oversight"}
          </div>
          <h1 className="text-3xl font-bold tracking-tight md:text-4xl text-foreground">
            {isTeacher ? "Student Leave Approvals" : "Faculty Leave Approvals"}
          </h1>
          <p className="mt-1 text-sm text-muted-foreground max-w-2xl">
            {isTeacher
              ? "Review, verify, and approve or reject leave applications submitted by students in your classes and department."
              : "Review and sanction official leave applications submitted by department faculty members."}
          </p>
        </div>

        {/* Admin Section Switcher */}
        {isAdmin && (
          <div className="flex items-center gap-1 rounded-xl bg-muted p-1 text-xs font-semibold self-start md:self-auto border border-border">
            <button
              onClick={() => setAdminSection("faculty")}
              className={`rounded-lg px-3 py-2 transition-all flex items-center gap-1.5 ${
                adminSection === "faculty"
                  ? "bg-primary text-primary-foreground shadow-sm"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              <GraduationCap className="size-4" />
              <span>Faculty Requests ({allLeaves.filter((l) => l.applicantType === "faculty" && l.status === "PENDING").length} Pending)</span>
            </button>
            <button
              onClick={() => setAdminSection("student")}
              className={`rounded-lg px-3 py-2 transition-all flex items-center gap-1.5 ${
                adminSection === "student"
                  ? "bg-primary text-primary-foreground shadow-sm"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              <User className="size-4" />
              <span>Student Leaves (Campus Overview)</span>
            </button>
          </div>
        )}
      </div>

      {/* KPI Stats Cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="rounded-2xl border border-amber-500/20 bg-amber-500/5 p-5 shadow-sm">
          <div className="flex items-center justify-between text-amber-700 dark:text-amber-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Awaiting Decision</span>
            <Clock3 className="size-4" />
          </div>
          <div className="mt-3 text-3xl font-bold text-amber-900 dark:text-amber-300">
            {pendingCount}
          </div>
          <p className="mt-1 text-xs text-amber-700/80 dark:text-amber-400/80">Requires your review</p>
        </div>

        <div className="rounded-2xl border border-emerald-500/20 bg-emerald-500/5 p-5 shadow-sm">
          <div className="flex items-center justify-between text-emerald-700 dark:text-emerald-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Approved</span>
            <CheckCircle2 className="size-4" />
          </div>
          <div className="mt-3 text-3xl font-bold text-emerald-900 dark:text-emerald-300">
            {approvedCount}
          </div>
          <p className="mt-1 text-xs text-emerald-700/80 dark:text-emerald-400/80">Sanctioned requests</p>
        </div>

        <div className="rounded-2xl border border-rose-500/20 bg-rose-500/5 p-5 shadow-sm">
          <div className="flex items-center justify-between text-rose-700 dark:text-rose-400">
            <span className="text-xs font-semibold uppercase tracking-wider">Declined</span>
            <XCircle className="size-4" />
          </div>
          <div className="mt-3 text-3xl font-bold text-rose-900 dark:text-rose-300">
            {rejectedCount}
          </div>
          <p className="mt-1 text-xs text-rose-700/80 dark:text-rose-400/80">Requests not approved</p>
        </div>

        <div className="rounded-2xl border border-border/80 bg-card/60 p-5 shadow-sm backdrop-blur-sm">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Received</span>
            <FileText className="size-4 text-primary" />
          </div>
          <div className="mt-3 text-3xl font-bold text-foreground">{targetLeaves.length}</div>
          <p className="mt-1 text-xs text-muted-foreground">Cumulative applications</p>
        </div>
      </div>

      {/* Main Table / Card Section */}
      <div className="rounded-2xl border border-border bg-card shadow-sm overflow-hidden">
        {/* Filter bar */}
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 border-b border-border p-4 bg-muted/20">
          <div className="flex items-center gap-1 rounded-xl bg-muted p-1 text-xs font-medium">
            {[
              { id: "pending", label: "Pending Action", count: pendingCount },
              { id: "approved", label: "Approved", count: approvedCount },
              { id: "rejected", label: "Declined", count: rejectedCount },
              { id: "all", label: "All Records", count: targetLeaves.length },
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

          <div className="relative">
            <Search className="absolute left-3 top-2.5 size-4 text-muted-foreground" />
            <input
              type="text"
              placeholder={isTeacher ? "Search student, ID, reason..." : "Search faculty, reason..."}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="h-9 w-full sm:w-64 rounded-xl border border-input bg-background pl-9 pr-3 text-xs placeholder:text-muted-foreground focus:border-primary focus:outline-none"
            />
          </div>
        </div>

        {/* Content */}
        {isLoading ? (
          <div className="p-8 space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-20 rounded-xl bg-muted/40 animate-pulse" />
            ))}
          </div>
        ) : isError ? (
          <div className="p-12 text-center">
            <AlertCircle className="mx-auto size-8 text-destructive" />
            <p className="mt-2 text-sm font-semibold">Failed to load leave applications.</p>
            <button
              onClick={() => refetch()}
              className="mt-3 text-xs text-primary underline font-medium"
            >
              Try Again
            </button>
          </div>
        ) : filteredLeaves.length === 0 ? (
          <div className="p-16 text-center text-muted-foreground">
            <CalendarCheck className="mx-auto size-12 stroke-[1.2] text-muted-foreground/50 mb-3" />
            <p className="text-base font-semibold text-foreground">No applications in this view</p>
            <p className="mt-1 text-xs">
              {activeTab === "pending"
                ? "All applications have been processed! No pending items requiring action."
                : `No leave applications match the "${activeTab}" filter.`}
            </p>
          </div>
        ) : (
          <div className="divide-y divide-border">
            {filteredLeaves.map((item) => (
              <div
                key={item.id}
                className="p-5 flex flex-col lg:flex-row items-start lg:items-center justify-between gap-4 hover:bg-muted/15 transition-colors"
                data-testid={`approval-row-${item.id}`}
              >
                <div className="space-y-2 flex-1">
                  {/* Title & Applicant Header */}
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-bold text-base text-foreground">
                      {item.applicantName}
                    </span>
                    <span className="rounded bg-muted px-2 py-0.5 text-[10px] font-semibold text-muted-foreground uppercase">
                      {item.applicantType === "faculty" ? "👨‍🏫 Teacher" : "🎓 Mentee"}
                    </span>
                    {isTeacher && item.applicantType === "student" && (
                      <span className="inline-flex items-center gap-1 rounded-full bg-blue-500/10 px-2 py-0.5 text-[10px] font-semibold text-blue-700 dark:text-blue-300 border border-blue-500/20">
                        <GraduationCap className="size-3" /> Your Mentee
                      </span>
                    )}
                    {item.studentId && (
                      <span className="text-xs font-mono text-muted-foreground bg-muted/60 px-1.5 py-0.5 rounded">
                        {item.studentId}
                      </span>
                    )}
                    <span className="rounded bg-primary/10 text-primary px-2 py-0.5 text-xs font-semibold">
                      {item.department || "Engineering"}
                    </span>
                    <span className="font-semibold text-xs text-foreground/80 border-l border-border pl-2">
                      {item.leaveType}
                    </span>
                    <span className="rounded-full bg-accent/30 text-accent-foreground px-2 py-0.5 text-xs font-medium">
                      {item.totalDays} {item.totalDays === 1 ? "Day" : "Days"}
                    </span>

                    {/* Status badge */}
                    {item.status === "PENDING" && (
                      <span className="inline-flex items-center gap-1 rounded-full bg-amber-500/10 px-2.5 py-0.5 text-xs font-semibold text-amber-700 dark:text-amber-400 border border-amber-500/20">
                        <Clock3 className="size-3" /> Awaiting Review
                      </span>
                    )}
                    {item.status === "APPROVED" && (
                      <span className="inline-flex items-center gap-1 rounded-full bg-emerald-500/10 px-2.5 py-0.5 text-xs font-semibold text-emerald-700 dark:text-emerald-400 border border-emerald-500/20">
                        <CheckCircle2 className="size-3" /> Approved
                      </span>
                    )}
                    {item.status === "REJECTED" && (
                      <span className="inline-flex items-center gap-1 rounded-full bg-rose-500/10 px-2.5 py-0.5 text-xs font-semibold text-rose-700 dark:text-rose-400 border border-rose-500/20">
                        <XCircle className="size-3" /> Declined
                      </span>
                    )}
                  </div>

                  {/* Dates & Submissions */}
                  <div className="flex flex-wrap items-center gap-y-1 gap-x-4 text-xs text-muted-foreground">
                    <div className="flex items-center gap-1.5 font-medium text-foreground/80">
                      <CalendarDays className="size-3.5 text-primary" />
                      <span>{item.startDate} to {item.endDate}</span>
                    </div>
                    <span>Applied on: {item.createdAt}</span>
                    {item.assignedApproverName && (
                      <span>Assigned Reviewer: {item.assignedApproverName}</span>
                    )}
                  </div>

                  {/* Substitute / coverage if faculty leave */}
                  {item.substituteTeacher && (
                    <div className="text-xs text-muted-foreground flex items-center gap-1.5 bg-muted/40 p-2 rounded-lg">
                      <Users2 className="size-3.5 text-primary shrink-0" />
                      <span>
                        <strong className="text-foreground/80">Class Coverage: </strong>
                        {item.substituteTeacher}
                      </span>
                    </div>
                  )}

                  {/* Reason */}
                  <p className="text-xs text-foreground/85 leading-relaxed bg-muted/30 p-2.5 rounded-lg border border-border/50">
                    <strong className="text-foreground/70">Reason: </strong>
                    {item.reason}
                  </p>

                  {/* Remarks Callout if already resolved */}
                  {item.reviewerRemarks && (
                    <div className="text-xs rounded-lg p-2.5 border bg-accent/20 border-accent/30 text-accent-foreground flex items-start gap-2">
                      <Info className="size-4 shrink-0 mt-0.5 text-primary" />
                      <div>
                        <strong>Review Remarks ({item.reviewedBy || "Reviewer"}): </strong>
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

                {/* Action Buttons for Approver */}
                <div className="flex items-center gap-2 self-end lg:self-center shrink-0">
                  {item.status === "PENDING" && (
                    <>
                      <button
                        onClick={() => handleOpenActionModal(item, "approve")}
                        className="inline-flex items-center gap-1.5 rounded-xl bg-emerald-600 px-3.5 py-2 text-xs font-semibold text-white shadow-sm hover:bg-emerald-700 transition-all hover:scale-[1.02] active:scale-[0.98]"
                        data-testid={`button-approve-leave-${item.id}`}
                      >
                        <Check className="size-3.5" />
                        <span>Approve</span>
                      </button>

                      <button
                        onClick={() => handleOpenActionModal(item, "reject")}
                        className="inline-flex items-center gap-1.5 rounded-xl border border-rose-500/30 bg-rose-500/10 px-3.5 py-2 text-xs font-semibold text-rose-700 dark:text-rose-400 hover:bg-rose-500/20 transition-all"
                        data-testid={`button-reject-leave-${item.id}`}
                      >
                        <X className="size-3.5" />
                        <span>Reject</span>
                      </button>
                    </>
                  )}
                  {item.status !== "PENDING" && (
                    <button
                      onClick={() => handleOpenActionModal(item, item.status === "APPROVED" ? "reject" : "approve")}
                      className="rounded-lg border border-input px-2.5 py-1 text-[11px] font-medium text-muted-foreground hover:bg-muted transition-colors"
                      title="Update decision"
                    >
                      Change Decision
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Decision Modal */}
      {selectedLeave && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm animate-in fade-in">
          <div className="w-full max-w-lg rounded-2xl border border-border bg-card p-6 shadow-2xl space-y-5">
            <div className="flex items-start justify-between border-b border-border pb-3">
              <div>
                <h2 className="text-xl font-bold text-foreground">
                  {actionType === "approve" ? "Confirm Leave Approval" : "Confirm Leave Rejection"}
                </h2>
                <p className="text-xs text-muted-foreground mt-0.5">
                  Application by <strong className="text-foreground">{selectedLeave.applicantName}</strong> ({selectedLeave.leaveType}, {selectedLeave.totalDays} Days)
                </p>
              </div>
              <button
                onClick={() => {
                  setSelectedLeave(null);
                  setActionType(null);
                }}
                className="rounded-lg p-1.5 text-muted-foreground hover:bg-muted transition-colors"
              >
                ✕
              </button>
            </div>

            <div className="space-y-4">
              <div className="rounded-xl bg-muted/40 p-3 text-xs space-y-1 border border-border/50">
                <div>
                  <span className="text-muted-foreground">Period: </span>
                  <strong className="text-foreground">{selectedLeave.startDate} to {selectedLeave.endDate}</strong> ({selectedLeave.totalDays} days)
                </div>
                <div>
                  <span className="text-muted-foreground">Reason stated: </span>
                  <span className="text-foreground/90">{selectedLeave.reason}</span>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-foreground mb-1">
                  {actionType === "approve" ? "Approval Remarks / Conditions (Optional)" : "Reason for Rejection (Mandatory)"}
                </label>
                <textarea
                  rows={3}
                  value={actionRemarks}
                  onChange={(e) => setActionRemarks(e.target.value)}
                  placeholder={
                    actionType === "approve"
                      ? "e.g. Approved. Please ensure syllabus coverage is maintained..."
                      : "e.g. Cannot be approved due to impending semester examinations..."
                  }
                  className="w-full rounded-xl border border-input bg-background p-3 text-xs placeholder:text-muted-foreground focus:border-primary focus:outline-none"
                  required={actionType === "reject"}
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-border">
                <button
                  type="button"
                  onClick={() => {
                    setSelectedLeave(null);
                    setActionType(null);
                  }}
                  className="rounded-xl border border-input px-4 py-2 text-xs font-semibold text-muted-foreground hover:bg-muted transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleConfirmAction}
                  disabled={approveMutation.isPending || rejectMutation.isPending}
                  className={`rounded-xl px-5 py-2 text-xs font-semibold text-white shadow-sm transition-all disabled:opacity-50 ${
                    actionType === "approve"
                      ? "bg-emerald-600 hover:bg-emerald-700"
                      : "bg-rose-600 hover:bg-rose-700"
                  }`}
                  data-testid="button-confirm-decision"
                >
                  {approveMutation.isPending || rejectMutation.isPending
                    ? "Updating..."
                    : actionType === "approve"
                    ? "Confirm Approval"
                    : "Confirm Rejection"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
export default LeaveApprovals;
