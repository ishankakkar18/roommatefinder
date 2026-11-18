import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, post } from '../api';
import { useAuth } from '../auth';
import MatchCardItem from '../components/MatchCardItem';
import type { MatchCard, RequestRow } from '../types';

export default function Matches() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [requested, setRequested] = useState<Set<number>>(new Set());
  const [busyId, setBusyId] = useState<number | null>(null);
  const complete = Boolean(user?.profileComplete);

  const matches = useQuery({
    queryKey: ['matches'],
    queryFn: () => api<MatchCard[]>('/api/matches'),
    enabled: complete,
  });

  const sendRequest = useMutation({
    mutationFn: (toUserId: number) => post<RequestRow>('/api/requests', { toUserId }),
    onSuccess: (_data, toUserId) => {
      setRequested((prev) => new Set(prev).add(toUserId));
      void queryClient.invalidateQueries({ queryKey: ['matches'] });
      void queryClient.invalidateQueries({ queryKey: ['requests'] });
    },
  });

  function handleRequest(userId: number) {
    setBusyId(userId);
    sendRequest.mutate(userId, { onSettled: () => setBusyId(null) });
  }

  const count = matches.data?.length ?? 0;

  return (
    <>
      <header className="page-head">
        <div>
          <h1>Matches</h1>
          <p className="sub">
            {complete
              ? count > 0
                ? `${count} ${count === 1 ? 'person' : 'people'} in your city, ranked by how you actually live`
                : 'Ranked by how you actually live'
              : 'Finish your profile to start matching'}
          </p>
        </div>
        {complete && count > 0 && <span className="chip">Top {Math.min(count, 20)}</span>}
      </header>

      {!complete && (
        <div className="card stack rise">
          <h3>One step left</h3>
          <p className="dim">
            We need your budget, area and daily habits before we can score anyone.
          </p>
          <Link to="/profile" className="btn btn-primary" style={{ alignSelf: 'flex-start' }}>
            Complete your profile
          </Link>
        </div>
      )}

      {complete && matches.isLoading && (
        <div className="stack">
          <div className="skeleton" />
          <div className="skeleton" />
          <div className="skeleton" />
        </div>
      )}

      {complete && matches.isError && (
        <div className="alert">{(matches.error as Error).message}</div>
      )}

      {complete && !matches.isLoading && count === 0 && (
        <div className="empty">
          <div className="glyph">✦</div>
          <strong>No matches yet</strong>
          <span className="muted">
            Nobody in your area fits your criteria right now. As more people join, they'll appear here.
          </span>
        </div>
      )}

      <div className="stack">
        {matches.data?.map((card, index) => (
          <MatchCardItem
            key={card.userId}
            card={card}
            index={index}
            busy={busyId === card.userId}
            requested={requested.has(card.userId)}
            onRequest={handleRequest}
          />
        ))}
      </div>
    </>
  );
}
