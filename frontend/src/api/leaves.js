import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { customFetch } from "./custom-fetch";

export const getListLeavesQueryKey = (params) => {
  return ["/api/leaves", ...(params ? [params] : [])];
};

export const listLeaves = async (params = {}) => {
  const query = new URLSearchParams();
  if (params.applicantType) query.set("applicantType", params.applicantType);
  if (params.applicantUsername) query.set("applicantUsername", params.applicantUsername);
  if (params.status) query.set("status", params.status);
  if (params.approverRole) query.set("approverRole", params.approverRole);
  if (params.scope) query.set("scope", params.scope);

  const qs = query.toString();
  const url = qs ? `/api/leaves?${qs}` : "/api/leaves";

  return customFetch(url, { method: "GET" });
};

export function useListLeaves(params = {}) {
  const queryKey = getListLeavesQueryKey(params);
  return useQuery({
    queryKey,
    queryFn: () => listLeaves(params),
  });
}

export const createLeave = async (data) => {
  return customFetch("/api/leaves", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
};

export function useCreateLeave(options = {}) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data) => createLeave(data),
    onSuccess: (...args) => {
      qc.invalidateQueries({ queryKey: ["/api/leaves"] });
      qc.invalidateQueries({ queryKey: ["/api/notifications"] });
      if (options.onSuccess) options.onSuccess(...args);
    },
    ...options,
  });
}

export const approveLeave = async ({ id, remarks }) => {
  return customFetch(`/api/leaves/${id}/approve`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ remarks: remarks || "Approved without conditions." }),
  });
};

export function useApproveLeave(options = {}) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, remarks }) => approveLeave({ id, remarks }),
    onSuccess: (...args) => {
      qc.invalidateQueries({ queryKey: ["/api/leaves"] });
      qc.invalidateQueries({ queryKey: ["/api/notifications"] });
      if (options.onSuccess) options.onSuccess(...args);
    },
    ...options,
  });
}

export const rejectLeave = async ({ id, remarks }) => {
  return customFetch(`/api/leaves/${id}/reject`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ remarks: remarks || "Declined due to academic requirements." }),
  });
};

export function useRejectLeave(options = {}) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, remarks }) => rejectLeave({ id, remarks }),
    onSuccess: (...args) => {
      qc.invalidateQueries({ queryKey: ["/api/leaves"] });
      qc.invalidateQueries({ queryKey: ["/api/notifications"] });
      if (options.onSuccess) options.onSuccess(...args);
    },
    ...options,
  });
}

export const deleteLeave = async (id) => {
  return customFetch(`/api/leaves/${id}`, {
    method: "DELETE",
  });
};

export function useDeleteLeave(options = {}) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id) => deleteLeave(id),
    onSuccess: (...args) => {
      qc.invalidateQueries({ queryKey: ["/api/leaves"] });
      if (options.onSuccess) options.onSuccess(...args);
    },
    ...options,
  });
}
