interface StatusPillProps {
  value: string;
}

export function StatusPill({ value }: StatusPillProps) {
  const normalized = value.toLowerCase();
  const tone = normalized.includes("active") || normalized.includes("paid") || normalized.includes("sent")
    ? "positive"
    : normalized.includes("overdue") || normalized.includes("expired") || normalized.includes("failed")
      ? "critical"
      : normalized.includes("pending") || normalized.includes("partial")
        ? "warning"
        : "neutral";

  return <span className={`status-pill status-pill--${tone}`}>{value.replace(/_/g, " ")}</span>;
}

