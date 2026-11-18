import type { CSSProperties } from 'react';
import ScoreRing from './ScoreRing';
import type { MatchCard } from '../types';

interface Props {
  card: MatchCard;
  index: number;
  onRequest: (userId: number) => void;
  busy: boolean;
  requested: boolean;
}

export default function MatchCardItem({ card, index, onRequest, busy, requested }: Props) {
  const meta = [card.age ? `${card.age}` : null, card.areaName, card.budgetLabel]
    .filter(Boolean)
    .join(' · ');

  return (
    <article className="match-card rise" style={{ ['--i' as string]: index } as CSSProperties}>
      <ScoreRing value={card.score} />

      <div className="match-body">
        <div className="match-top">
          <h3>{card.displayName ?? 'Someone'}</h3>
          <span className="match-meta">{meta}</span>
        </div>

        {card.aboutMe && <p className="match-about">{card.aboutMe}</p>}

        <div className="chips">
          {card.reasons.map((reason) => (
            <span key={reason} className="chip chip-good">
              ✓ {reason}
            </span>
          ))}
          {card.frictions.map((friction) => (
            <span key={friction} className="chip chip-warn">
              ⚠ {friction}
            </span>
          ))}
        </div>
      </div>

      <button
        className="btn btn-primary"
        disabled={busy || requested}
        onClick={() => onRequest(card.userId)}
      >
        {requested ? 'Requested' : busy ? 'Sending…' : 'Send request'}
      </button>
    </article>
  );
}
