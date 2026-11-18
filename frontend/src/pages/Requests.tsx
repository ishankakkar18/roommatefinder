import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, post } from '../api';
import type { AcceptResponse, RequestRow } from '../types';
import { formatDate } from '../time';

export default function Requests() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [busyId, setBusyId] = useState<number | null>(null);

  const incoming = useQuery({
    queryKey: ['requests', 'incoming'],
    queryFn: () => api<RequestRow[]>('/api/requests/incoming'),
  });

  const outgoing = useQuery({
    queryKey: ['requests', 'outgoing'],
    queryFn: () => api<RequestRow[]>('/api/requests/outgoing'),
  });

  const refresh = () => {
    void queryClient.invalidateQueries({ queryKey: ['requests'] });
    void queryClient.invalidateQueries({ queryKey: ['matches'] });
    void queryClient.invalidateQueries({ queryKey: ['conversations'] });
  };

  const accept = useMutation({
    mutationFn: (id: number) => post<AcceptResponse>(`/api/requests/${id}/accept`),
    onSuccess: (result) => {
      refresh();
      navigate(`/chat/${result.conversationId}`);
    },
  });

  const decline = useMutation({
    mutationFn: (id: number) => post<RequestRow>(`/api/requests/${id}/decline`),
    onSuccess: refresh,
  });

  const withdraw = useMutation({
    mutationFn: (id: number) => post<RequestRow>(`/api/requests/${id}/withdraw`),
    onSuccess: refresh,
  });

  const run = (mutation: { mutate: (id: number, opts: { onSettled: () => void }) => void }, id: number) => {
    setBusyId(id);
    mutation.mutate(id, { onSettled: () => setBusyId(null) });
  };

  const pendingCount = incoming.data?.length ?? 0;

  return (
    <>
      <header className="page-head">
        <div>
          <h1>Requests</h1>
          <p className="sub">
            {pendingCount > 0
              ? `${pendingCount} waiting for your answer`
              : 'People who want to talk to you, and who you asked'}
          </p>
        </div>
      </header>

      <section className="stack" style={{ marginBottom: 34 }}>
        <div className="section-title">Incoming</div>

        {incoming.isLoading && <div className="skeleton" />}

        {!incoming.isLoading && pendingCount === 0 && (
          <div className="empty">
            <div className="glyph">📥</div>
            <strong>Nothing waiting</strong>
            <span className="muted">When someone requests you, it shows up here.</span>
          </div>
        )}

        {incoming.data?.map((row, index) => (
          <div key={row.id} className="req-row rise" style={{ ['--i' as string]: index }}>
            <span className="avatar">{(row.fromName ?? '?').slice(0, 1).toUpperCase()}</span>
            <div className="grow">
              <h3>{row.fromName ?? 'Someone'}</h3>
              <p className="muted">
                {row.score}% match · expires {formatDate(row.expiresAt)}
              </p>
            </div>
            <div className="row">
              <button
                className="btn btn-danger btn-sm"
                disabled={busyId === row.id}
                onClick={() => run(decline, row.id)}
              >
                Decline
              </button>
              <button
                className="btn btn-primary btn-sm"
                disabled={busyId === row.id}
                onClick={() => run(accept, row.id)}
              >
                Accept
              </button>
            </div>
          </div>
        ))}
      </section>

      <section className="stack">
        <div className="section-title">Sent</div>

        {outgoing.isLoading && <div className="skeleton" />}

        {!outgoing.isLoading && (outgoing.data?.length ?? 0) === 0 && (
          <div className="empty">
            <div className="glyph">✈️</div>
            <strong>No sent requests</strong>
            <span className="muted">Find someone in Matches and send the first one.</span>
          </div>
        )}

        {outgoing.data?.map((row, index) => (
          <div key={row.id} className="req-row rise" style={{ ['--i' as string]: index }}>
            <span className="avatar">{(row.toName ?? '?').slice(0, 1).toUpperCase()}</span>
            <div className="grow">
              <h3>{row.toName ?? 'Someone'}</h3>
              <p className="muted">
                {row.score}% match · sent {formatDate(row.createdAt)}
              </p>
            </div>
            {row.status === 'PENDING' ? (
              <button
                className="btn btn-sm"
                disabled={busyId === row.id}
                onClick={() => run(withdraw, row.id)}
              >
                Withdraw
              </button>
            ) : (
              <span className={`chip${row.status === 'ACCEPTED' ? ' chip-good' : ''}`}>
                {row.status.toLowerCase()}
              </span>
            )}
          </div>
        ))}
      </section>
    </>
  );
}
