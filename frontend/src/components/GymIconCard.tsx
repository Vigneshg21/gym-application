export function GymIconCard({ icon, label, value }: { icon: string; label: string; value: string | number }) {
  return (
    <div className="gym-icon-card">
      <img src={icon} alt={label} className="gym-icon-card__img" />
      <div className="gym-icon-card__content">
        <p className="gym-icon-card__label">{label}</p>
        <p className="gym-icon-card__value">{value}</p>
      </div>
    </div>
  );
}
