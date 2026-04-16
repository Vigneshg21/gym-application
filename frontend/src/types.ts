export type ViewId = "overview" | "members" | "plans" | "memberships" | "billing" | "notifications";

export interface DashboardResponse {
  totalMembers: number;
  activeMembers: number;
  activeMemberships: number;
  membershipsExpiringSoon: number;
  overdueInvoices: number;
  pendingInvoices: number;
  revenueCollectedThisMonth: number;
  outstandingReceivables: number;
}

export interface MemberResponse {
  id: string;
  memberCode: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phoneNumber: string;
  whatsappNumber: string;
  email: string | null;
  telegramChatId: string | null;
  dateOfBirth: string | null;
  profileImageDataUrl: string | null;
  status: string;
  createdAt: string;
}

export interface TelegramConnectSessionResponse {
  memberId: string;
  memberName: string;
  connectToken: string;
  deepLink: string;
  expiresAt: string;
  telegramChatId: string | null;
}

export interface TelegramConnectionSyncResult {
  memberId: string | null;
  memberName: string | null;
  telegramChatId: string | null;
  status: string;
  detail: string;
}

export interface TelegramConnectionSyncResponse {
  updatesScanned: number;
  matchedConnectRequests: number;
  linkedMembers: number;
  results: TelegramConnectionSyncResult[];
}

export interface MembershipPlanResponse {
  id: string;
  name: string;
  description: string | null;
  durationInDays: number;
  price: number;
  joiningFee: number;
  accessLevel: string | null;
  active: boolean;
}

export interface MembershipResponse {
  id: string;
  memberId: string;
  memberName: string;
  planId: string;
  planName: string;
  startDate: string;
  endDate: string;
  status: string;
  autoRenew: boolean;
  agreedPrice: number;
  lastRenewedAt: string | null;
  notes: string | null;
}

export interface InvoiceResponse {
  id: string;
  invoiceNumber: string;
  memberId: string;
  memberName: string;
  membershipId: string | null;
  issueDate: string;
  dueDate: string;
  amount: number;
  discountAmount: number;
  taxAmount: number;
  totalAmount: number;
  amountPaid: number;
  balanceDue: number;
  status: string;
  type: string;
  notes: string | null;
}

export interface DownloadResponse {
  blob: Blob;
  fileName: string | null;
}

export interface ApiErrorPayload {
  message?: string;
  error?: string;
  fieldErrors?: Record<string, string>;
}