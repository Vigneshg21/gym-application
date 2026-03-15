import { useDeferredValue, useEffect, useState, useTransition } from "react";
import type { Dispatch, ReactNode, SetStateAction } from "react";
import { MetricCard } from "./components/MetricCard";
import { Panel } from "./components/Panel";
import { StatusPill } from "./components/StatusPill";
import { HeroImage } from "./components/HeroImage";
import { API_BASE_URL, gymApi } from "./lib/api";
import type {
  DashboardResponse,
  InvoiceResponse,
  MemberResponse,
  MembershipPlanResponse,
  MembershipResponse,
  TelegramConnectSessionResponse,
  TelegramConnectionSyncResponse,
  ViewId,
} from "./types";

const navigation: Array<{ id: ViewId; label: string; caption: string }> = [
  { id: "overview", label: "Overview", caption: "Pulse and revenue" },
  { id: "members", label: "Members", caption: "Onboard and manage" },
  { id: "plans", label: "Plans", caption: "Pricing and access" },
  { id: "memberships", label: "Memberships", caption: "Enroll and renew" },
  { id: "billing", label: "Billing", caption: "Invoices and collections" },
  { id: "notifications", label: "Notifications", caption: "Email, Telegram, WhatsApp" },
];

const paymentMethods = ["CASH", "CARD", "UPI", "BANK_TRANSFER", "ONLINE"] as const;
const accessLevels = ["FULL_ACCESS", "VIP_ACCESS", "LIMITED_ACCESS"] as const;

const initialMemberForm = {
  firstName: "",
  lastName: "",
  phoneNumber: "",
  whatsappNumber: "",
  email: "",
  telegramChatId: "",
  emergencyContactName: "",
  emergencyContactPhone: "",
  notes: "",
  profileImageBase64: "",
  profileImageContentType: "",
  profileImagePreview: "",
};

const initialPlanForm = {
  name: "",
  description: "",
  durationInDays: "30",
  price: "2500",
  joiningFee: "0",
  accessLevel: "FULL_ACCESS",
};

const initialMembershipForm = {
  memberId: "",
  planId: "",
  startDate: todayInputValue(),
  dueDate: todayInputValue(),
  autoRenew: true,
  agreedPrice: "",
  taxAmount: "",
  discountAmount: "",
  notes: "",
};

const initialRenewalForm = {
  membershipId: "",
  renewalDate: todayInputValue(),
  dueDate: todayInputValue(),
  autoRenew: true,
  renewalPrice: "",
  taxAmount: "",
  discountAmount: "",
  notes: "",
};

const initialPaymentForm = {
  invoiceId: "",
  amount: "",
  method: "UPI",
  referenceNumber: "",
  collectedBy: "Front Desk",
  notes: "",
};

const initialAnnouncementForm = {
  memberId: "",
  message: "Your next training assessment is live. Reply to confirm your preferred training slot.",
};

const initialTelegramLinkForm = {
  memberId: "",
  telegramChatId: "",
};

const currencyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 2,
});

const gymBrand = "Jai Fitness";

export default function App() {
  const [activeView, setActiveView] = useState<ViewId>("overview");
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
  const [members, setMembers] = useState<MemberResponse[]>([]);
  const [plans, setPlans] = useState<MembershipPlanResponse[]>([]);
  const [memberships, setMemberships] = useState<MembershipResponse[]>([]);
  const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionBusy, setActionBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [isTransitionPending, startTransition] = useTransition();

  const [memberSearch, setMemberSearch] = useState("");
  const [membershipSearch, setMembershipSearch] = useState("");
  const [invoiceSearch, setInvoiceSearch] = useState("");

  const [memberForm, setMemberForm] = useState(initialMemberForm);
  const [planForm, setPlanForm] = useState(initialPlanForm);
  const [membershipForm, setMembershipForm] = useState(initialMembershipForm);
  const [renewalForm, setRenewalForm] = useState(initialRenewalForm);
  const [paymentForm, setPaymentForm] = useState(initialPaymentForm);
  const [announcementForm, setAnnouncementForm] = useState(initialAnnouncementForm);
  const [telegramLinkForm, setTelegramLinkForm] = useState(initialTelegramLinkForm);
  const [telegramConnectSession, setTelegramConnectSession] = useState<TelegramConnectSessionResponse | null>(null);
  const [telegramSyncSummary, setTelegramSyncSummary] = useState<TelegramConnectionSyncResponse | null>(null);

  const deferredMemberSearch = useDeferredValue(memberSearch.trim().toLowerCase());
  const deferredMembershipSearch = useDeferredValue(membershipSearch.trim().toLowerCase());
  const deferredInvoiceSearch = useDeferredValue(invoiceSearch.trim().toLowerCase());

  useEffect(() => {
    void loadData();
  }, []);

  useEffect(() => {
    if (!notice) {
      return undefined;
    }

    const timeout = window.setTimeout(() => setNotice(null), 3600);
    return () => window.clearTimeout(timeout);
  }, [notice]);

  async function loadData(showLoader = true) {
    if (showLoader) {
      setLoading(true);
    }
    setError(null);

    try {
      const [dashboardPayload, membersPayload, plansPayload, membershipsPayload, invoicesPayload] = await Promise.all([
        gymApi.getDashboard(),
        gymApi.getMembers(),
        gymApi.getPlans(),
        gymApi.getMemberships(),
        gymApi.getInvoices(),
      ]);

      startTransition(() => {
        setDashboard(dashboardPayload);
        setMembers(membersPayload);
        setPlans(plansPayload);
        setMemberships(membershipsPayload);
        setInvoices(invoicesPayload);
      });
    } catch (loadError) {
      setError(readError(loadError));
    } finally {
      setLoading(false);
    }
  }

  async function runAction(successMessage: string, operation: () => Promise<unknown>, onSuccess?: () => void) {
    setActionBusy(true);
    setError(null);
    setNotice(null);

    try {
      await operation();
      onSuccess?.();
      setNotice(successMessage);
      await loadData(false);
    } catch (actionError) {
      setError(readError(actionError));
    } finally {
      setActionBusy(false);
    }
  }

  async function runActionWithResult<T>(
    successMessage: string,
    operation: () => Promise<T>,
    onSuccess: (result: T) => void,
  ) {
    setActionBusy(true);
    setError(null);
    setNotice(null);

    try {
      const result = await operation();
      onSuccess(result);
      setNotice(successMessage);
      await loadData(false);
    } catch (actionError) {
      setError(readError(actionError));
    } finally {
      setActionBusy(false);
    }
  }

  function handleInvoiceDownload(invoice: InvoiceResponse) {
    try {
      const member = members.find((candidate) => candidate.id === invoice.memberId);
      setError(null);
      setNotice(null);
      downloadInvoiceFile(invoice, member);
      setNotice(`${invoice.invoiceNumber} downloaded.`);
    } catch (downloadError) {
      setError(readError(downloadError));
    }
  }

  async function handleMemberImageSelected(file: File | null) {
    if (!file) {
      setMemberForm((current) => ({
        ...current,
        profileImageBase64: "",
        profileImageContentType: "",
        profileImagePreview: "",
      }));
      return;
    }

    if (!file.type.startsWith("image/")) {
      setError("Please choose a valid image file.");
      return;
    }

    if (file.size > 3 * 1024 * 1024) {
      setError("Profile image must be 3 MB or smaller.");
      return;
    }

    try {
      setError(null);
      const dataUrl = await readFileAsDataUrl(file);
      const base64Payload = dataUrl.split(",")[1] ?? "";
      setMemberForm((current) => ({
        ...current,
        profileImageBase64: base64Payload,
        profileImageContentType: file.type,
        profileImagePreview: dataUrl,
      }));
    } catch (fileError) {
      setError(readError(fileError));
    }
  }

  async function handleRemoteDownload(
    loader: () => Promise<{ blob: Blob; fileName: string | null }>,
    fallbackName: string,
    successMessage: string,
  ) {
    try {
      setError(null);
      setNotice(null);
      const response = await loader();
      triggerBrowserDownload(response.blob, response.fileName ?? fallbackName);
      setNotice(successMessage);
    } catch (downloadError) {
      setError(readError(downloadError));
    }
  }

  const filteredMembers = members.filter((member) => {
    if (!deferredMemberSearch) {
      return true;
    }
    return [member.memberCode, member.fullName, member.phoneNumber, member.email ?? "", member.telegramChatId ?? "", member.status]
      .join(" ")
      .toLowerCase()
      .includes(deferredMemberSearch);
  });

  const filteredMemberships = memberships.filter((membership) => {
    if (!deferredMembershipSearch) {
      return true;
    }
    return [membership.memberName, membership.planName, membership.status]
      .join(" ")
      .toLowerCase()
      .includes(deferredMembershipSearch);
  });

  const filteredInvoices = invoices.filter((invoice) => {
    if (!deferredInvoiceSearch) {
      return true;
    }
    return [invoice.invoiceNumber, invoice.memberName, invoice.status, invoice.type]
      .join(" ")
      .toLowerCase()
      .includes(deferredInvoiceSearch);
  });

  const dueSoonInvoices = [...invoices]
    .filter((invoice) => invoice.balanceDue > 0)
    .sort((left, right) => left.dueDate.localeCompare(right.dueDate))
    .slice(0, 5);

  const expiringMemberships = [...memberships]
    .sort((left, right) => left.endDate.localeCompare(right.endDate))
    .slice(0, 5);

  const billable = invoices.reduce((sum, invoice) => sum + invoice.totalAmount, 0);
  const collected = invoices.reduce((sum, invoice) => sum + invoice.amountPaid, 0);
  const collectionRate = billable === 0 ? 0 : Math.round((collected / billable) * 100);

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-mark">
          <div className="brand-mark__badge">JF</div>
          <div>
            <p className="brand-mark__eyebrow">Jai Fitness</p>
            <h1>Control Room</h1>
          </div>
        </div>

        <nav className="nav-list" aria-label="Application navigation">
          {navigation.map((item) => (
            <button
              key={item.id}
              type="button"
              className={item.id === activeView ? "nav-item nav-item--active" : "nav-item"}
              onClick={() => setActiveView(item.id)}
            >
              <span>{item.label}</span>
              <small>{item.caption}</small>
            </button>
          ))}
        </nav>

        <div className="sidebar-note">
          <p>Backend API</p>
          <strong>{API_BASE_URL}</strong>
          <span>{actionBusy || isTransitionPending ? "Syncing live data" : "Ready for operations"}</span>
        </div>
      </aside>

      <main className="main-stage">
        <header className="hero-banner">
          <div>
            <p className="hero-banner__eyebrow">Responsive gym operations suite</p>
            <h2>Memberships, collections, renewals, and multi-channel outreach in one deployable frontend.</h2>
            <p className="hero-banner__description">
              Use this command center to run daily gym workflows across billing, front-desk onboarding, and reminder campaigns.
            </p>
          </div>

          <div className="hero-banner__actions">
            <button type="button" className="button button--ghost" onClick={() => void loadData(false)} disabled={loading || actionBusy}>
              Refresh data
            </button>
            <button type="button" className="button button--primary" onClick={() => setActiveView("memberships")}>
              Open renewals
            </button>
          </div>
        </header>

        {error ? <div className="flash flash--error">{error}</div> : null}
        {notice ? <div className="flash flash--success">{notice}</div> : null}

        {loading ? (
          <section className="loading-state">
            <div className="loading-state__spinner" />
            <p>Loading control room data...</p>
          </section>
        ) : (
          <>
            {activeView === "overview" ? (
              <div className="view-stack">
                <HeroImage />
                <section className="metrics-grid">
                  <MetricCard
                    label="Monthly Revenue"
                    value={formatCurrency(dashboard?.revenueCollectedThisMonth ?? 0)}
                    hint="Cash already collected this month"
                    tone="sun"
                    icon={<SparkIcon />}
                  />
                  <MetricCard
                    label="Outstanding"
                    value={formatCurrency(dashboard?.outstandingReceivables ?? 0)}
                    hint="Receivables still open"
                    tone="ember"
                    icon={<WalletIcon />}
                  />
                  <MetricCard
                    label="Active Memberships"
                    value={String(dashboard?.activeMemberships ?? 0)}
                    hint="Members currently cleared for access"
                    tone="mint"
                    icon={<PulseIcon />}
                  />
                  <MetricCard
                    label="Collection Rate"
                    value={`${collectionRate}%`}
                    hint="Recovered versus billed invoices"
                    tone="ocean"
                    icon={<SignalIcon />}
                  />
                </section>

                <div className="two-column-grid">
                  <Panel
                    eyebrow="Operations pulse"
                    title="Daily readiness"
                    description="Quick glance indicators for team leads and front desk supervisors."
                  >
                    <div className="pulse-grid">
                      <article className="pulse-card">
                        <span>Total members</span>
                        <strong>{dashboard?.totalMembers ?? 0}</strong>
                      </article>
                      <article className="pulse-card">
                        <span>Active members</span>
                        <strong>{dashboard?.activeMembers ?? 0}</strong>
                      </article>
                      <article className="pulse-card">
                        <span>Expiring soon</span>
                        <strong>{dashboard?.membershipsExpiringSoon ?? 0}</strong>
                      </article>
                      <article className="pulse-card">
                        <span>Overdue invoices</span>
                        <strong>{dashboard?.overdueInvoices ?? 0}</strong>
                      </article>
                    </div>
                  </Panel>

                  <Panel
                    eyebrow="Queue posture"
                    title="Messaging engine"
                    description="RabbitMQ-backed reminders stay queued in the backend while this UI drives announcements and follow-ups."
                  >
                    <ul className="check-list">
                      <li>Fee reminders are scheduled automatically from the backend cron flow.</li>
                      <li>Renewal nudges are generated for memberships nearing expiry.</li>
                      <li>This frontend can trigger one-off member announcements across the configured channels.</li>
                    </ul>
                  </Panel>
                </div>

                <div className="two-column-grid">
                  <Panel
                    eyebrow="Revenue attention"
                    title="Invoices due next"
                    description="Use this list to prioritise front-desk follow-up calls or WhatsApp reminders."
                  >
                    <div className="list-stack">
                      {dueSoonInvoices.length === 0 ? (
                        <p className="empty-copy">No open invoices right now.</p>
                      ) : (
                        dueSoonInvoices.map((invoice) => (
                          <article key={invoice.id} className="list-item">
                            <div>
                              <strong>{invoice.invoiceNumber}</strong>
                              <p>{invoice.memberName}</p>
                            </div>
                            <div className="list-item__meta">
                              <StatusPill value={invoice.status} />
                              <span>{formatCurrency(invoice.balanceDue)}</span>
                              <small>Due {formatDate(invoice.dueDate)}</small>
                            </div>
                          </article>
                        ))
                      )}
                    </div>
                  </Panel>

                  <Panel
                    eyebrow="Retention watch"
                    title="Memberships expiring soon"
                    description="Great for proactive renewal conversations before access lapses."
                  >
                    <div className="list-stack">
                      {expiringMemberships.length === 0 ? (
                        <p className="empty-copy">No active memberships found.</p>
                      ) : (
                        expiringMemberships.map((membership) => (
                          <article key={membership.id} className="list-item">
                            <div>
                              <strong>{membership.memberName}</strong>
                              <p>{membership.planName}</p>
                            </div>
                            <div className="list-item__meta">
                              <StatusPill value={membership.status} />
                              <span>{daysUntil(membership.endDate)} days left</span>
                              <small>Ends {formatDate(membership.endDate)}</small>
                            </div>
                          </article>
                        ))
                      )}
                    </div>
                  </Panel>
                </div>
              </div>
            ) : null}

                        {activeView === "members" ? (
              <div className="view-stack">
                <div className="two-column-grid">
                  <Panel
                    eyebrow="Front desk"
                    title="Create member"
                    description="Capture core contact details once, then use them across billing, memberships, and campaigns."
                  >
                    <form
                      className="form-grid"
                      onSubmit={(event) => {
                        event.preventDefault();
                        void runAction(
                          "Member created successfully.",
                          () =>
                            gymApi.createMember(
                              compactPayload({
                                firstName: memberForm.firstName,
                                lastName: memberForm.lastName,
                                phoneNumber: memberForm.phoneNumber,
                                whatsappNumber: memberForm.whatsappNumber,
                                email: memberForm.email,
                                telegramChatId: memberForm.telegramChatId,
                                emergencyContactName: memberForm.emergencyContactName,
                                emergencyContactPhone: memberForm.emergencyContactPhone,
                                notes: memberForm.notes,
                                profileImageBase64: memberForm.profileImageBase64,
                                profileImageContentType: memberForm.profileImageContentType,
                              }),
                            ),
                          () => setMemberForm(initialMemberForm),
                        );
                      }}
                    >
                      <Field label="First name">
                        <input value={memberForm.firstName} onChange={(event) => updateField(setMemberForm, "firstName", event.target.value)} required />
                      </Field>
                      <Field label="Last name">
                        <input value={memberForm.lastName} onChange={(event) => updateField(setMemberForm, "lastName", event.target.value)} required />
                      </Field>
                      <Field label="Phone number">
                        <input value={memberForm.phoneNumber} onChange={(event) => updateField(setMemberForm, "phoneNumber", event.target.value)} required />
                      </Field>
                      <Field label="WhatsApp number">
                        <input value={memberForm.whatsappNumber} onChange={(event) => updateField(setMemberForm, "whatsappNumber", event.target.value)} />
                      </Field>
                      <Field label="Email">
                        <input type="email" value={memberForm.email} onChange={(event) => updateField(setMemberForm, "email", event.target.value)} />
                      </Field>
                      <Field label="Telegram chat ID">
                        <input value={memberForm.telegramChatId} onChange={(event) => updateField(setMemberForm, "telegramChatId", event.target.value)} placeholder="Optional manual link" />
                      </Field>
                      <Field label="Emergency contact">
                        <input value={memberForm.emergencyContactName} onChange={(event) => updateField(setMemberForm, "emergencyContactName", event.target.value)} />
                      </Field>
                      <Field label="Emergency phone">
                        <input value={memberForm.emergencyContactPhone} onChange={(event) => updateField(setMemberForm, "emergencyContactPhone", event.target.value)} />
                      </Field>
                      <Field label="Profile image" className="field--wide">
                        <div className="image-upload-stack">
                          <input
                            type="file"
                            accept="image/*"
                            onChange={(event) => {
                              void handleMemberImageSelected(event.target.files?.[0] ?? null);
                            }}
                          />
                          <div className="subcopy">Upload JPG or PNG up to 3 MB. This image will be used on the member card.</div>
                          {memberForm.profileImagePreview ? (
                            <div className="member-create-preview">
                              <img src={memberForm.profileImagePreview} alt="Member preview" className="member-create-preview__image" />
                              <button
                                type="button"
                                className="button button--ghost button--compact"
                                onClick={() => {
                                  setMemberForm((current) => ({
                                    ...current,
                                    profileImageBase64: "",
                                    profileImageContentType: "",
                                    profileImagePreview: "",
                                  }));
                                }}
                              >
                                Remove image
                              </button>
                            </div>
                          ) : null}
                        </div>
                      </Field>
                      <Field label="Notes" className="field--wide">
                        <textarea value={memberForm.notes} onChange={(event) => updateField(setMemberForm, "notes", event.target.value)} rows={4} />
                      </Field>
                      <div className="form-actions field--wide">
                        <button type="submit" className="button button--primary" disabled={actionBusy}>Save member</button>
                      </div>
                    </form>
                  </Panel>

                  <Panel
                    eyebrow="Telegram connect"
                    title="Connect member bot"
                    description="Generate a private start link, sync bot joins, or manually map a chat ID for direct Telegram reminders."
                  >
                    <ul className="check-list">
                      <li>Choose a member, then generate a secure bot link.</li>
                      <li>The member opens the bot link and presses Start in Telegram.</li>
                      <li>Use Sync bot starts to map their Telegram chat automatically.</li>
                    </ul>

                    <div className="form-grid">
                      <Field label="Member">
                        <select
                          value={telegramLinkForm.memberId}
                          onChange={(event) => {
                            updateField(setTelegramLinkForm, "memberId", event.target.value);
                            setTelegramConnectSession(null);
                          }}
                        >
                          <option value="">Select member</option>
                          {members.map((member) => (
                            <option key={member.id} value={member.id}>{member.fullName}</option>
                          ))}
                        </select>
                      </Field>
                      <Field label="Manual Telegram chat ID">
                        <input
                          value={telegramLinkForm.telegramChatId}
                          onChange={(event) => updateField(setTelegramLinkForm, "telegramChatId", event.target.value)}
                          placeholder="Paste a chat id if you already have it"
                        />
                      </Field>
                      <div className="form-actions field--wide">
                        <button
                          type="button"
                          className="button button--primary"
                          disabled={actionBusy || !telegramLinkForm.memberId}
                          onClick={() => {
                            void runActionWithResult(
                              "Telegram connect link generated.",
                              () => gymApi.createTelegramConnectSession(telegramLinkForm.memberId),
                              (session) => {
                                setTelegramConnectSession(session);
                                setTelegramSyncSummary(null);
                              },
                            );
                          }}
                        >
                          Generate connect link
                        </button>
                        <button
                          type="button"
                          className="button button--ghost"
                          disabled={actionBusy}
                          onClick={() => {
                            void runActionWithResult(
                              "Telegram bot updates synced.",
                              () => gymApi.syncTelegramConnections(),
                              (summary) => setTelegramSyncSummary(summary),
                            );
                          }}
                        >
                          Sync bot starts
                        </button>
                        <button
                          type="button"
                          className="button button--ghost"
                          disabled={actionBusy || !telegramLinkForm.memberId || !telegramLinkForm.telegramChatId.trim()}
                          onClick={() => {
                            void runAction(
                              "Telegram chat linked to member.",
                              () => gymApi.updateMemberTelegramChat(telegramLinkForm.memberId, { telegramChatId: telegramLinkForm.telegramChatId.trim() }),
                              () => {
                                setTelegramLinkForm((current) => ({ ...current, telegramChatId: "" }));
                                setTelegramConnectSession(null);
                              },
                            );
                          }}
                        >
                          Save chat id
                        </button>
                      </div>
                    </div>

                    {telegramConnectSession ? (
                      <article className="strategy-card telegram-connect-card">
                        <strong>{telegramConnectSession.memberName}</strong>
                        <p>Share this deep link with the member, then ask them to press Start in Telegram.</p>
                        <a className="deep-link" href={telegramConnectSession.deepLink} target="_blank" rel="noreferrer">
                          {telegramConnectSession.deepLink}
                        </a>
                        <div className="subcopy">Token: {telegramConnectSession.connectToken}</div>
                        <div className="subcopy">Expires: {formatDateTime(telegramConnectSession.expiresAt)}</div>
                      </article>
                    ) : null}

                    {telegramSyncSummary ? (
                      <div className="strategy-stack">
                        <article className="strategy-card">
                          <strong>Latest sync</strong>
                          <p>
                            Scanned {telegramSyncSummary.updatesScanned} updates, matched {telegramSyncSummary.matchedConnectRequests} connect requests,
                            linked {telegramSyncSummary.linkedMembers} member chats.
                          </p>
                        </article>
                        {telegramSyncSummary.results.slice(0, 4).map((result, index) => (
                          <article key={`${result.memberId ?? "unknown"}-${index}`} className="strategy-card">
                            <div className="plan-card__topline">
                              <strong>{result.memberName ?? "Unmatched start"}</strong>
                              <StatusPill value={result.status} />
                            </div>
                            <p>{result.detail}</p>
                            <div className="subcopy">{result.telegramChatId ?? "No chat linked yet"}</div>
                          </article>
                        ))}
                      </div>
                    ) : null}
                  </Panel>
                </div>

                <Panel
                  eyebrow="Roster"
                  title="Members"
                  description="Search the live member roster synced from the Spring Boot backend."
                  actions={<input className="search-input" placeholder="Search member, phone, email, Telegram..." value={memberSearch} onChange={(event) => setMemberSearch(event.target.value)} />}
                >
                  <ResponsiveTable
                    headers={["Member", "Phone", "WhatsApp", "Telegram", "Status", "Joined"]}
                    rows={filteredMembers.map((member) => [
                      <div key={`${member.id}-name`}>
                        <strong>{member.fullName}</strong>
                        <div className="subcopy">{member.email ?? "No email"}</div>
                      </div>,
                      member.phoneNumber,
                      member.whatsappNumber,
                      <div key={`${member.id}-telegram`}>
                        <StatusPill value={member.telegramChatId ? "Linked" : "Pending"} />
                        <div className="subcopy">{member.telegramChatId ?? "Ask member to open the bot"}</div>
                      </div>,
                      <StatusPill key={`${member.id}-status`} value={member.status} />,
                      formatDate(member.createdAt),
                    ])}
                  />
                </Panel>
              </div>
            ) : null}

            {activeView === "plans" ? (
              <div className="view-stack">
                <div className="two-column-grid">
                  <Panel eyebrow="Commercial setup" title="Create membership plan" description="Build plans for monthly, quarterly, and annual offers.">
                    <form
                      className="form-grid"
                      onSubmit={(event) => {
                        event.preventDefault();
                        void runAction(
                          "Membership plan created.",
                          () =>
                            gymApi.createPlan(
                              compactPayload({
                                ...planForm,
                                durationInDays: Number(planForm.durationInDays),
                                price: Number(planForm.price),
                                joiningFee: Number(planForm.joiningFee),
                                active: true,
                              }),
                            ),
                          () => setPlanForm(initialPlanForm),
                        );
                      }}
                    >
                      <Field label="Plan name">
                        <input value={planForm.name} onChange={(event) => updateField(setPlanForm, "name", event.target.value)} required />
                      </Field>
                      <Field label="Duration (days)">
                        <input type="number" min="1" value={planForm.durationInDays} onChange={(event) => updateField(setPlanForm, "durationInDays", event.target.value)} required />
                      </Field>
                      <Field label="Price">
                        <input type="number" min="0" step="0.01" value={planForm.price} onChange={(event) => updateField(setPlanForm, "price", event.target.value)} required />
                      </Field>
                      <Field label="Joining fee">
                        <input type="number" min="0" step="0.01" value={planForm.joiningFee} onChange={(event) => updateField(setPlanForm, "joiningFee", event.target.value)} required />
                      </Field>
                      <Field label="Access level">
                        <select value={planForm.accessLevel} onChange={(event) => updateField(setPlanForm, "accessLevel", event.target.value)}>
                          {accessLevels.map((option) => (
                            <option key={option} value={option}>{option.replace(/_/g, " ")}</option>
                          ))}
                        </select>
                      </Field>
                      <Field label="Description" className="field--wide">
                        <textarea value={planForm.description} onChange={(event) => updateField(setPlanForm, "description", event.target.value)} rows={4} />
                      </Field>
                      <div className="form-actions field--wide">
                        <button type="submit" className="button button--primary" disabled={actionBusy}>Save plan</button>
                      </div>
                    </form>
                  </Panel>

                  <Panel eyebrow="Offer wall" title="Current plans" description="Live commercial catalogue pulled from your backend plan inventory.">
                    <div className="plan-grid">
                      {plans.map((plan) => (
                        <article key={plan.id} className="plan-card">
                          <div className="plan-card__topline">
                            <StatusPill value={plan.active ? "ACTIVE" : "INACTIVE"} />
                            <span>{plan.accessLevel?.replace(/_/g, " ") ?? "Standard access"}</span>
                          </div>
                          <h3>{plan.name}</h3>
                          <strong>{formatCurrency(plan.price)}</strong>
                          <p>{plan.description ?? "No description added."}</p>
                          <div className="plan-card__meta">
                            <span>{plan.durationInDays} days</span>
                            <span>Joining {formatCurrency(plan.joiningFee)}</span>
                          </div>
                        </article>
                      ))}
                    </div>
                  </Panel>
                </div>
              </div>
            ) : null}

            {activeView === "memberships" ? (
              <div className="view-stack">
                <div className="two-column-grid">
                  <Panel eyebrow="Enrollment" title="Assign membership" description="Create a member-plan relationship and generate the signup invoice in one action.">
                    <form
                      className="form-grid"
                      onSubmit={(event) => {
                        event.preventDefault();
                        void runAction(
                          "Membership enrolled and invoice generated.",
                          () =>
                            gymApi.createMembership(
                              compactPayload({
                                ...membershipForm,
                                autoRenew: membershipForm.autoRenew,
                                agreedPrice: optionalNumber(membershipForm.agreedPrice),
                                taxAmount: optionalNumber(membershipForm.taxAmount),
                                discountAmount: optionalNumber(membershipForm.discountAmount),
                              }),
                            ),
                          () => setMembershipForm(initialMembershipForm),
                        );
                      }}
                    >
                      <Field label="Member">
                        <select value={membershipForm.memberId} onChange={(event) => updateField(setMembershipForm, "memberId", event.target.value)} required>
                          <option value="">Select member</option>
                          {members.map((member) => (
                            <option key={member.id} value={member.id}>{member.fullName}</option>
                          ))}
                        </select>
                      </Field>
                      <Field label="Plan">
                        <select value={membershipForm.planId} onChange={(event) => updateField(setMembershipForm, "planId", event.target.value)} required>
                          <option value="">Select plan</option>
                          {plans.map((plan) => (
                            <option key={plan.id} value={plan.id}>{plan.name}</option>
                          ))}
                        </select>
                      </Field>
                      <Field label="Start date">
                        <input type="date" value={membershipForm.startDate} onChange={(event) => updateField(setMembershipForm, "startDate", event.target.value)} required />
                      </Field>
                      <Field label="Invoice due date">
                        <input type="date" value={membershipForm.dueDate} onChange={(event) => updateField(setMembershipForm, "dueDate", event.target.value)} required />
                      </Field>
                      <Field label="Agreed price override">
                        <input type="number" min="0" step="0.01" value={membershipForm.agreedPrice} onChange={(event) => updateField(setMembershipForm, "agreedPrice", event.target.value)} placeholder="Use plan price" />
                      </Field>
                      <Field label="Tax amount">
                        <input type="number" min="0" step="0.01" value={membershipForm.taxAmount} onChange={(event) => updateField(setMembershipForm, "taxAmount", event.target.value)} />
                      </Field>
                      <Field label="Discount amount">
                        <input type="number" min="0" step="0.01" value={membershipForm.discountAmount} onChange={(event) => updateField(setMembershipForm, "discountAmount", event.target.value)} />
                      </Field>
                      <Field label="Auto renew">
                        <label className="toggle">
                          <input type="checkbox" checked={membershipForm.autoRenew} onChange={(event) => updateField(setMembershipForm, "autoRenew", event.target.checked)} />
                          <span>Enable automatic renewal preference</span>
                        </label>
                      </Field>
                      <Field label="Notes" className="field--wide">
                        <textarea value={membershipForm.notes} onChange={(event) => updateField(setMembershipForm, "notes", event.target.value)} rows={4} />
                      </Field>
                      <div className="form-actions field--wide">
                        <button type="submit" className="button button--primary" disabled={actionBusy}>Create membership</button>
                      </div>
                    </form>
                  </Panel>

                  <Panel eyebrow="Renewal desk" title="Renew existing membership" description="Use this for manual renewals before or after a membership expires.">
                    <form
                      className="form-grid"
                      onSubmit={(event) => {
                        event.preventDefault();
                        void runAction(
                          "Membership renewed and renewal invoice generated.",
                          () =>
                            gymApi.renewMembership(
                              renewalForm.membershipId,
                              compactPayload({
                                renewalDate: renewalForm.renewalDate,
                                dueDate: renewalForm.dueDate,
                                autoRenew: renewalForm.autoRenew,
                                renewalPrice: optionalNumber(renewalForm.renewalPrice),
                                taxAmount: optionalNumber(renewalForm.taxAmount),
                                discountAmount: optionalNumber(renewalForm.discountAmount),
                                notes: renewalForm.notes,
                              }),
                            ),
                          () => setRenewalForm(initialRenewalForm),
                        );
                      }}
                    >
                      <Field label="Membership">
                        <select value={renewalForm.membershipId} onChange={(event) => updateField(setRenewalForm, "membershipId", event.target.value)} required>
                          <option value="">Select membership</option>
                          {memberships.map((membership) => (
                            <option key={membership.id} value={membership.id}>{membership.memberName} - {membership.planName}</option>
                          ))}
                        </select>
                      </Field>
                      <Field label="Renewal date">
                        <input type="date" value={renewalForm.renewalDate} onChange={(event) => updateField(setRenewalForm, "renewalDate", event.target.value)} required />
                      </Field>
                      <Field label="Invoice due date">
                        <input type="date" value={renewalForm.dueDate} onChange={(event) => updateField(setRenewalForm, "dueDate", event.target.value)} required />
                      </Field>
                      <Field label="Renewal price override">
                        <input type="number" min="0" step="0.01" value={renewalForm.renewalPrice} onChange={(event) => updateField(setRenewalForm, "renewalPrice", event.target.value)} placeholder="Use current price" />
                      </Field>
                      <Field label="Tax amount">
                        <input type="number" min="0" step="0.01" value={renewalForm.taxAmount} onChange={(event) => updateField(setRenewalForm, "taxAmount", event.target.value)} />
                      </Field>
                      <Field label="Discount amount">
                        <input type="number" min="0" step="0.01" value={renewalForm.discountAmount} onChange={(event) => updateField(setRenewalForm, "discountAmount", event.target.value)} />
                      </Field>
                      <Field label="Auto renew">
                        <label className="toggle">
                          <input type="checkbox" checked={renewalForm.autoRenew} onChange={(event) => updateField(setRenewalForm, "autoRenew", event.target.checked)} />
                          <span>Keep auto-renew preference enabled</span>
                        </label>
                      </Field>
                      <Field label="Notes" className="field--wide">
                        <textarea value={renewalForm.notes} onChange={(event) => updateField(setRenewalForm, "notes", event.target.value)} rows={4} />
                      </Field>
                      <div className="form-actions field--wide">
                        <button type="submit" className="button button--primary" disabled={actionBusy || !renewalForm.membershipId}>Renew membership</button>
                      </div>
                    </form>
                  </Panel>
                </div>

                <Panel
                  eyebrow="Live book"
                  title="Membership roster"
                  description="Track active, expired, and renewal-ready memberships."
                  actions={<input className="search-input" placeholder="Search member or plan..." value={membershipSearch} onChange={(event) => setMembershipSearch(event.target.value)} />}
                >
                  <ResponsiveTable
                    headers={["Member", "Plan", "Window", "Price", "Status", "Auto renew"]}
                    rows={filteredMemberships.map((membership) => [
                      <div key={`${membership.id}-member`}>
                        <strong>{membership.memberName}</strong>
                        <div className="subcopy">{membership.id.slice(0, 8)}</div>
                      </div>,
                      membership.planName,
                      `${formatDate(membership.startDate)} - ${formatDate(membership.endDate)}`,
                      formatCurrency(membership.agreedPrice),
                      <StatusPill key={`${membership.id}-status`} value={membership.status} />,
                      membership.autoRenew ? "Enabled" : "Manual",
                    ])}
                  />
                </Panel>
              </div>
            ) : null}

            {activeView === "billing" ? (
              <div className="view-stack">
                <div className="two-column-grid">
                  <Panel eyebrow="Collections" title="Record payment" description="Post front-desk, online, or bank payments against an existing invoice.">
                    <form
                      className="form-grid"
                      onSubmit={(event) => {
                        event.preventDefault();
                        void runAction(
                          "Payment recorded successfully.",
                          () =>
                            gymApi.recordPayment(
                              paymentForm.invoiceId,
                              compactPayload({
                                amount: Number(paymentForm.amount),
                                method: paymentForm.method,
                                referenceNumber: paymentForm.referenceNumber,
                                collectedBy: paymentForm.collectedBy,
                                notes: paymentForm.notes,
                              }),
                            ),
                          () => setPaymentForm(initialPaymentForm),
                        );
                      }}
                    >
                      <Field label="Invoice">
                        <select value={paymentForm.invoiceId} onChange={(event) => updateField(setPaymentForm, "invoiceId", event.target.value)} required>
                          <option value="">Select invoice</option>
                          {invoices.filter((invoice) => invoice.balanceDue > 0).map((invoice) => (
                            <option key={invoice.id} value={invoice.id}>{invoice.invoiceNumber} - {invoice.memberName}</option>
                          ))}
                        </select>
                      </Field>
                      <Field label="Amount">
                        <input type="number" min="0.01" step="0.01" value={paymentForm.amount} onChange={(event) => updateField(setPaymentForm, "amount", event.target.value)} required />
                      </Field>
                      <Field label="Method">
                        <select value={paymentForm.method} onChange={(event) => updateField(setPaymentForm, "method", event.target.value)}>
                          {paymentMethods.map((method) => (
                            <option key={method} value={method}>{method.replace(/_/g, " ")}</option>
                          ))}
                        </select>
                      </Field>
                      <Field label="Reference number">
                        <input value={paymentForm.referenceNumber} onChange={(event) => updateField(setPaymentForm, "referenceNumber", event.target.value)} />
                      </Field>
                      <Field label="Collected by">
                        <input value={paymentForm.collectedBy} onChange={(event) => updateField(setPaymentForm, "collectedBy", event.target.value)} />
                      </Field>
                      <Field label="Notes" className="field--wide">
                        <textarea value={paymentForm.notes} onChange={(event) => updateField(setPaymentForm, "notes", event.target.value)} rows={4} />
                      </Field>
                      <div className="form-actions field--wide">
                        <button type="submit" className="button button--primary" disabled={actionBusy || !paymentForm.invoiceId}>Record payment</button>
                      </div>
                    </form>
                  </Panel>

                  <Panel eyebrow="Finance snapshot" title="Receivables posture" description="Outstanding dues and overdue pressure based on live invoice data.">
                    <div className="finance-cards">
                      <article className="finance-card">
                        <span>Open dues</span>
                        <strong>{formatCurrency(dashboard?.outstandingReceivables ?? 0)}</strong>
                      </article>
                      <article className="finance-card">
                        <span>Pending invoices</span>
                        <strong>{dashboard?.pendingInvoices ?? 0}</strong>
                      </article>
                      <article className="finance-card">
                        <span>Overdue invoices</span>
                        <strong>{dashboard?.overdueInvoices ?? 0}</strong>
                      </article>
                    </div>
                  </Panel>
                </div>

                <Panel
                  eyebrow="Ledger"
                  title="Invoice board"
                  description="Search by member, invoice id, status, or invoice type."
                  actions={<input className="search-input" placeholder="Search invoice, member, status..." value={invoiceSearch} onChange={(event) => setInvoiceSearch(event.target.value)} />}
                >
                  <ResponsiveTable
                    headers={["Invoice", "Member", "Due date", "Billed", "Paid", "Balance", "Status", "Actions"]}
                    rows={filteredInvoices.map((invoice) => [
                      <div key={`${invoice.id}-invoice`}>
                        <strong>{invoice.invoiceNumber}</strong>
                        <div className="subcopy">{formatEnumLabel(invoice.type)}</div>
                      </div>,
                      invoice.memberName,
                      formatDate(invoice.dueDate),
                      formatCurrency(invoice.totalAmount),
                      formatCurrency(invoice.amountPaid),
                      formatCurrency(invoice.balanceDue),
                      <StatusPill key={`${invoice.id}-status`} value={invoice.status} />,
                      <div key={`${invoice.id}-actions`} className="table-actions">
                        <button type="button" className="button button--ghost button--compact" onClick={() => handleInvoiceDownload(invoice)}>
                          Download
                        </button>
                        {invoice.status === "PAID" ? (
                          <button
                            type="button"
                            className="button button--primary button--compact"
                            disabled={actionBusy}
                            onClick={() => {
                              void runAction(
                                "Receipt queued for configured channels.",
                                () => gymApi.sendInvoiceReceipt(invoice.id),
                              );
                            }}
                          >
                            Send receipt
                          </button>
                        ) : null}
                      </div>,
                    ])}
                  />
                </Panel>
              </div>
            ) : null}

            {activeView === "notifications" ? (
              <div className="view-stack">
                <div className="two-column-grid">
                  <Panel eyebrow="Member campaigns" title="Send announcement" description="Push one-off operational or promotional messages through the backend queue for the selected member across email, Telegram, and other configured channels.">
                    <form
                      className="form-grid"
                      onSubmit={(event) => {
                        event.preventDefault();
                        void runAction(
                          "Announcement queued for member delivery.",
                          () => gymApi.sendAnnouncement(compactPayload(announcementForm)),
                          () => setAnnouncementForm(initialAnnouncementForm),
                        );
                      }}
                    >
                      <Field label="Member">
                        <select value={announcementForm.memberId} onChange={(event) => updateField(setAnnouncementForm, "memberId", event.target.value)} required>
                          <option value="">Select member</option>
                          {members.map((member) => (
                            <option key={member.id} value={member.id}>{member.fullName}</option>
                          ))}
                        </select>
                      </Field>
                      <Field label="Message" className="field--wide">
                        <textarea value={announcementForm.message} onChange={(event) => updateField(setAnnouncementForm, "message", event.target.value)} rows={6} required />
                      </Field>
                      <div className="form-actions field--wide">
                        <button type="submit" className="button button--primary" disabled={actionBusy || !announcementForm.memberId}>Queue message</button>
                      </div>
                    </form>
                  </Panel>

                  <Panel eyebrow="Automation guide" title="Reminder strategy" description="How the frontend and backend split messaging responsibilities.">
                    <div className="strategy-stack">
                      <article className="strategy-card">
                        <strong>Automated</strong>
                        <p>Fee reminders and renewal reminders are scheduled by Spring Boot and published to RabbitMQ.</p>
                      </article>
                      <article className="strategy-card">
                        <strong>Manual</strong>
                        <p>Announcements from this screen go straight into the same messaging pipeline for consistent delivery behavior.</p>
                      </article>
                      <article className="strategy-card">
                        <strong>Telegram-ready</strong>
                        <p>Linked members receive Telegram announcements and reminders in their own chat once they connect your bot.</p>
                      </article>
                    </div>
                  </Panel>
                </div>
              </div>
            ) : null}
          </>
        )}
      </main>
    </div>
  );
}

interface FieldProps {
  label: string;
  className?: string;
  children: ReactNode;
}

function Field({ label, className, children }: FieldProps) {
  return (
    <label className={`field ${className ?? ""}`.trim()}>
      <span>{label}</span>
      {children}
    </label>
  );
}

function ResponsiveTable({ headers, rows }: { headers: string[]; rows: ReactNode[][] }) {
  if (rows.length === 0) {
    return <p className="empty-copy">No records to show yet.</p>;
  }

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            {headers.map((header) => (
              <th key={header}>{header}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr key={`row-${rowIndex}`}>
              {row.map((cell, cellIndex) => (
                <td key={`cell-${rowIndex}-${cellIndex}`} data-label={headers[cellIndex]}>
                  {cell}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function updateField<T extends Record<string, unknown>, K extends keyof T>(
  setter: Dispatch<SetStateAction<T>>,
  key: K,
  value: T[K],
) {
  setter((current) => ({
    ...current,
    [key]: value,
  }));
}

function compactPayload(payload: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(payload).filter(([, value]) => value !== "" && value !== undefined && value !== null),
  );
}

function optionalNumber(value: string) {
  return value.trim() ? Number(value) : undefined;
}

function readError(error: unknown) {
  if (error instanceof Error) {
    return error.message;
  }
  return "Unexpected request failure";
}

function formatEnumLabel(value: string) {
  return value.replace(/_/g, " ");
}

function escapeHtml(value: string) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function downloadInvoiceFile(invoice: InvoiceResponse, member?: MemberResponse) {
  const blob = new Blob([buildInvoiceDocument(invoice, member)], { type: "text/html;charset=utf-8" });
  const downloadUrl = window.URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = downloadUrl;
  anchor.download = `${invoice.invoiceNumber}.html`;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  window.setTimeout(() => window.URL.revokeObjectURL(downloadUrl), 0);
}

function buildInvoiceDocument(invoice: InvoiceResponse, member?: MemberResponse) {
  const documentTitle = invoice.status === "PAID" ? "Payment Receipt" : "Invoice";
  const notesMarkup = invoice.notes
    ? `
      <section class="notes">
        <h3>Notes</h3>
        <p>${escapeHtml(invoice.notes).replace(/\n/g, "<br />")}</p>
      </section>
    `
    : "";
  const memberContact = [
    member?.phoneNumber ? `Phone: ${member.phoneNumber}` : null,
    member?.whatsappNumber ? `WhatsApp: ${member.whatsappNumber}` : null,
    member?.email ? `Email: ${member.email}` : null,
    member?.telegramChatId ? `Telegram: ${member.telegramChatId}` : null,
  ]
    .filter((value): value is string => Boolean(value))
    .map((value) => `<div>${escapeHtml(value)}</div>`)
    .join("");

  return `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>${escapeHtml(invoice.invoiceNumber)} - ${documentTitle}</title>
    <style>
      :root {
        color-scheme: light;
        font-family: "Segoe UI", sans-serif;
        --bg: #f6f0e6;
        --card: #fffaf3;
        --line: #e5d8c3;
        --text: #192431;
        --muted: #5c6972;
        --accent: #ff6b35;
        --accent-soft: rgba(255, 107, 53, 0.12);
      }

      * {
        box-sizing: border-box;
      }

      body {
        margin: 0;
        padding: 32px 16px;
        background: linear-gradient(160deg, #fbf7ef 0%, #f1e4d1 100%);
        color: var(--text);
      }

      .sheet {
        max-width: 860px;
        margin: 0 auto;
        padding: 32px;
        border: 1px solid var(--line);
        border-radius: 24px;
        background: var(--card);
      }

      .topline,
      .meta-grid,
      .summary-grid,
      .totals-row {
        display: grid;
        gap: 16px;
      }

      .topline,
      .totals-row {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }

      .meta-grid,
      .summary-grid {
        grid-template-columns: repeat(3, minmax(0, 1fr));
        margin-top: 24px;
      }

      .brand {
        font-size: 13px;
        letter-spacing: 0.16em;
        text-transform: uppercase;
        color: var(--accent);
        font-weight: 700;
      }

      h1,
      h2,
      h3,
      p {
        margin: 0;
      }

      h1 {
        margin-top: 8px;
        font-size: 32px;
      }

      .muted,
      .meta-card span,
      .summary-card span,
      .footer {
        color: var(--muted);
      }

      .status {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 10px 14px;
        border-radius: 999px;
        background: var(--accent-soft);
        color: #b24c18;
        font-weight: 700;
      }

      .meta-card,
      .summary-card,
      .notes,
      table {
        border: 1px solid var(--line);
        border-radius: 18px;
        background: #fff;
      }

      .meta-card,
      .summary-card,
      .notes {
        padding: 18px;
      }

      .meta-card strong,
      .summary-card strong {
        display: block;
        margin-top: 8px;
        font-size: 18px;
      }

      table {
        width: 100%;
        margin-top: 24px;
        border-collapse: collapse;
        overflow: hidden;
      }

      th,
      td {
        padding: 14px 18px;
        border-bottom: 1px solid var(--line);
        text-align: left;
      }

      th {
        background: rgba(255, 107, 53, 0.06);
        font-size: 13px;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: var(--muted);
      }

      tr:last-child td {
        border-bottom: 0;
      }

      .totals-row {
        margin-top: 24px;
      }

      .footer {
        margin-top: 24px;
        font-size: 14px;
      }

      @media print {
        body {
          padding: 0;
          background: #fff;
        }

        .sheet {
          border: 0;
          border-radius: 0;
          max-width: none;
        }
      }

      @media (max-width: 720px) {
        .topline,
        .meta-grid,
        .summary-grid,
        .totals-row {
          grid-template-columns: 1fr;
        }
      }
    </style>
  </head>
  <body>
    <main class="sheet">
      <div class="topline">
        <div>
          <div class="brand">${escapeHtml(gymBrand)}</div>
          <h1>${documentTitle}</h1>
          <p class="muted">${escapeHtml(invoice.invoiceNumber)} for ${escapeHtml(invoice.memberName)}</p>
        </div>
        <div>
          <div class="status">${escapeHtml(formatEnumLabel(invoice.status))}</div>
          <p class="muted" style="margin-top: 12px;">Generated ${escapeHtml(formatDate(new Date().toISOString()))}</p>
        </div>
      </div>

      <div class="meta-grid">
        <section class="meta-card">
          <span>Member</span>
          <strong>${escapeHtml(member?.fullName ?? invoice.memberName)}</strong>
          ${memberContact}
        </section>
        <section class="meta-card">
          <span>Invoice Type</span>
          <strong>${escapeHtml(formatEnumLabel(invoice.type))}</strong>
        </section>
        <section class="meta-card">
          <span>Issued / Due</span>
          <strong>${escapeHtml(formatDate(invoice.issueDate))}</strong>
          <div class="muted">Due ${escapeHtml(formatDate(invoice.dueDate))}</div>
        </section>
      </div>

      <table>
        <thead>
          <tr>
            <th>Description</th>
            <th>Value</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Base amount</td>
            <td>${escapeHtml(formatCurrency(invoice.amount))}</td>
          </tr>
          <tr>
            <td>Tax</td>
            <td>${escapeHtml(formatCurrency(invoice.taxAmount))}</td>
          </tr>
          <tr>
            <td>Discount</td>
            <td>${escapeHtml(formatCurrency(invoice.discountAmount))}</td>
          </tr>
          <tr>
            <td>Total billed</td>
            <td>${escapeHtml(formatCurrency(invoice.totalAmount))}</td>
          </tr>
          <tr>
            <td>Total paid</td>
            <td>${escapeHtml(formatCurrency(invoice.amountPaid))}</td>
          </tr>
          <tr>
            <td>Balance due</td>
            <td>${escapeHtml(formatCurrency(invoice.balanceDue))}</td>
          </tr>
        </tbody>
      </table>

      <div class="summary-grid">
        <section class="summary-card">
          <span>Receipt status</span>
          <strong>${escapeHtml(invoice.status === "PAID" ? "Ready to share" : "Open invoice")}</strong>
        </section>
        <section class="summary-card">
          <span>Search label</span>
          <strong>${escapeHtml(invoice.invoiceNumber)}</strong>
        </section>
        <section class="summary-card">
          <span>Member ledger balance</span>
          <strong>${escapeHtml(formatCurrency(invoice.balanceDue))}</strong>
        </section>
      </div>

      ${notesMarkup}

      <p class="footer">This file was exported from ${escapeHtml(gymBrand)} Control Room and can be printed or saved as PDF.</p>
    </main>
  </body>
</html>`;
}

function formatCurrency(value: number) {
  return currencyFormatter.format(value ?? 0);
}

function formatDate(value: string | null | undefined) {
  if (!value) {
    return "Not set";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(date);
}

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return "Not set";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function daysUntil(value: string) {
  const target = new Date(value);
  const today = new Date();
  const diff = target.getTime() - today.getTime();
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
}

function todayInputValue() {
  return new Date().toISOString().slice(0, 10);
}

function SparkIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M12 2 8.6 10H13l-1.3 12L15.4 14H11l1-12Z" fill="currentColor" />
    </svg>
  );
}

function WalletIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M4 6.5A2.5 2.5 0 0 1 6.5 4H19a1 1 0 1 1 0 2H6.5a.5.5 0 0 0 0 1H20v12H6a2 2 0 0 1-2-2V6.5Zm12 5.5a2 2 0 1 0 0 4h3v-4h-3Z" fill="currentColor" />
    </svg>
  );
}

function PulseIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M2 13h5l2-4 4 8 2-4h7" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function SignalIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M4 20h3v-5H4v5Zm6 0h3V9h-3v11Zm6 0h3V4h-3v16Z" fill="currentColor" />
    </svg>
  );
}




