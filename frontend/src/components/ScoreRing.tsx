export default function ScoreRing({ value }: { value: number }) {
  const clamped = Math.max(0, Math.min(100, Math.round(value)));
  const radius = 33;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference * (1 - clamped / 100);

  return (
    <div className="ring" role="img" aria-label={`${clamped}% match`}>
      <svg width="78" height="78" viewBox="0 0 78 78">
        <defs>
          <linearGradient id="ringGrad" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#8b6bff" />
            <stop offset="100%" stopColor="#4ea8ff" />
          </linearGradient>
        </defs>
        <circle className="track" cx="39" cy="39" r={radius} fill="none" strokeWidth="6" />
        <circle
          className="val"
          cx="39"
          cy="39"
          r={radius}
          fill="none"
          stroke="url(#ringGrad)"
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
        />
      </svg>
      <div className="label">
        {clamped}
        <small>%</small>
      </div>
    </div>
  );
}
