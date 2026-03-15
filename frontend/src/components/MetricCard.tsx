import type { ReactNode } from "react";

interface MetricCardProps {
  label: string;
  value: string;
  hint: string;
  tone?: "sun" | "ocean" | "mint" | "ember";
  icon: ReactNode;
}

export function MetricCard({ label, value, hint, tone = "sun", icon }: MetricCardProps) {
  return (
    <article className={`metric-card metric-card--${tone}`}>
      <div className="metric-card__icon">{icon}</div>
      <div>
        <p className="metric-card__label">{label}</p>
        <h3 className="metric-card__value">{value}</h3>
        <p className="metric-card__hint">{hint}</p>
      </div>
    </article>
  );
}
