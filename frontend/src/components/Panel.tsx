import type { ReactNode } from "react";

interface PanelProps {
  eyebrow?: string;
  title: string;
  description?: string;
  actions?: ReactNode;
  children: ReactNode;
}

export function Panel({ eyebrow, title, description, actions, children }: PanelProps) {
  return (
    <section className="panel">
      <header className="panel__header">
        <div>
          {eyebrow ? <p className="panel__eyebrow">{eyebrow}</p> : null}
          <h2>{title}</h2>
          {description ? <p className="panel__description">{description}</p> : null}
        </div>
        {actions ? <div className="panel__actions">{actions}</div> : null}
      </header>
      {children}
    </section>
  );
}
