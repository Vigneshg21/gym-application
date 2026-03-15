import type {
  ApiErrorPayload,
  DashboardResponse,
  DownloadResponse,
  InvoiceResponse,
  MemberResponse,
  MembershipPlanResponse,
  MembershipResponse,
  TelegramConnectSessionResponse,
  TelegramConnectionSyncResponse,
} from "../types";

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
  });

  const contentType = response.headers.get("content-type") ?? "";
  const payload = contentType.includes("application/json") ? await response.json() : await response.text();

  if (!response.ok) {
    throw new Error(extractErrorMessage(payload));
  }

  return payload as T;
}

async function requestFile(path: string): Promise<DownloadResponse> {
  const response = await fetch(`${API_BASE_URL}${path}`);
  if (!response.ok) {
    const contentType = response.headers.get("content-type") ?? "";
    const payload = contentType.includes("application/json") ? await response.json() : await response.text();
    throw new Error(extractErrorMessage(payload));
  }

  return {
    blob: await response.blob(),
    fileName: readFileName(response.headers.get("content-disposition")),
  };
}

function readFileName(contentDisposition: string | null): string | null {
  if (!contentDisposition) {
    return null;
  }

  const match = contentDisposition.match(/filename="?([^";]+)"?/i);
  return match ? match[1] : null;
}

function extractErrorMessage(payload: unknown): string {
  if (typeof payload === "string") {
    return payload;
  }

  const errorPayload = payload as ApiErrorPayload | null;
  if (!errorPayload) {
    return "Request failed";
  }

  if (errorPayload.fieldErrors && Object.keys(errorPayload.fieldErrors).length > 0) {
    const firstFieldError = Object.entries(errorPayload.fieldErrors)[0];
    return `${firstFieldError[0]}: ${firstFieldError[1]}`;
  }

  return errorPayload.message ?? errorPayload.error ?? "Request failed";
}

export const gymApi = {
  getDashboard: () => request<DashboardResponse>("/dashboard"),
  getMembers: () => request<MemberResponse[]>("/members"),
  createMember: (body: Record<string, unknown>) =>
    request<MemberResponse>("/members", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  downloadMemberCard: (memberId: string) => requestFile(`/members/${memberId}/card-document`),
  sendMemberCard: (memberId: string) =>
    request<{ status: string }>(`/members/${memberId}/card-notifications`, {
      method: "POST",
    }),
  updateMemberTelegramChat: (memberId: string, body: Record<string, unknown>) =>
    request<MemberResponse>(`/members/${memberId}/telegram-chat`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  createTelegramConnectSession: (memberId: string) =>
    request<TelegramConnectSessionResponse>(`/members/${memberId}/telegram-connect`, {
      method: "POST",
    }),
  syncTelegramConnections: () =>
    request<TelegramConnectionSyncResponse>("/members/telegram-connections/sync", {
      method: "POST",
    }),
  getPlans: () => request<MembershipPlanResponse[]>("/membership-plans"),
  createPlan: (body: Record<string, unknown>) =>
    request<MembershipPlanResponse>("/membership-plans", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  getMemberships: () => request<MembershipResponse[]>("/memberships"),
  createMembership: (body: Record<string, unknown>) =>
    request("/memberships", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  downloadMembershipCard: (membershipId: string) => requestFile(`/memberships/${membershipId}/card-document`),
  sendMembershipCard: (membershipId: string) =>
    request<{ status: string }>(`/memberships/${membershipId}/card-notifications`, {
      method: "POST",
    }),
  renewMembership: (membershipId: string, body: Record<string, unknown>) =>
    request(`/memberships/${membershipId}/renew`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  getInvoices: () => request<InvoiceResponse[]>("/invoices"),
  recordPayment: (invoiceId: string, body: Record<string, unknown>) =>
    request(`/invoices/${invoiceId}/payments`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  sendInvoiceReceipt: (invoiceId: string) =>
    request<{ status: string }>(`/invoices/${invoiceId}/receipt-notifications`, {
      method: "POST",
    }),
  sendAnnouncement: (body: Record<string, unknown>) =>
    request<{ status: string }>("/notifications/announcements", {
      method: "POST",
      body: JSON.stringify(body),
    }),
};