import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ApiError, api, getAccessToken, post } from '../api';
import { useAuth } from '../auth';
import ConversationList from '../components/ConversationList';
import { useChatSocket } from '../hooks/useStomp';
import type { Conversation, Message } from '../types';
import { formatTime } from '../time';

export default function Chat() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const token = getAccessToken();

  const [messages, setMessages] = useState<Message[]>([]);
  const [text, setText] = useState('');
  const [otherTyping, setOtherTyping] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const bottomRef = useRef<HTMLDivElement | null>(null);
  const typingTimer = useRef<number | null>(null);
  const lastTypingSent = useRef(0);

  const conversations = useQuery({
    queryKey: ['conversations'],
    queryFn: () => api<Conversation[]>('/api/conversations'),
  });

  const conversation = conversations.data?.find((c) => c.id === id);

  const history = useQuery({
    queryKey: ['messages', id],
    queryFn: () => api<Message[]>(`/api/conversations/${id}/messages`),
    enabled: Boolean(id),
  });

  useEffect(() => {
    if (history.data) setMessages(history.data);
  }, [history.data]);

  const { sendTyping } = useChatSocket(id, token, {
    onMessage: (incoming) => {
      setMessages((prev) =>
        prev.some((m) => m.id === incoming.id || m.clientMsgId === incoming.clientMsgId)
          ? prev
          : [...prev, incoming],
      );
      setOtherTyping(false);
    },
    onRead: () => undefined,
    onTyping: (event) => {
      if (event.userId === user?.id) return;
      setOtherTyping(true);
      if (typingTimer.current) window.clearTimeout(typingTimer.current);
      typingTimer.current = window.setTimeout(() => setOtherTyping(false), 2500);
    },
    onConnected: () => {
      void history.refetch();
    },
  });

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages.length]);

  useEffect(() => {
    const last = messages[messages.length - 1];
    if (!last || !id || last.id < 0) return;
    void api(`/api/conversations/${id}/read`, {
      method: 'POST',
      body: JSON.stringify({ lastReadMessageId: last.id }),
    }).catch(() => undefined);
  }, [messages, id]);

  async function send() {
    const body = text.trim();
    if (!body || !id || !user) return;

    const clientMsgId = crypto.randomUUID();
    const optimistic: Message = {
      id: -Date.now(),
      clientMsgId,
      conversationId: id,
      senderId: user.id,
      senderName: user.name,
      body,
      createdAt: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, optimistic]);
    setText('');
    setError(null);

    try {
      const saved = await post<Message>(`/api/conversations/${id}/messages`, {
        clientMsgId,
        body,
      });
      setMessages((prev) => prev.map((m) => (m.clientMsgId === clientMsgId ? saved : m)));
    } catch (err) {
      setMessages((prev) => prev.filter((m) => m.clientMsgId !== clientMsgId));
      setError(err instanceof ApiError ? err.message : 'Message failed to send');
    }
  }

  function onType(value: string) {
    setText(value);
    const now = Date.now();
    if (now - lastTypingSent.current > 2000) {
      lastTypingSent.current = now;
      sendTyping();
    }
  }

  return (
    <>
      <header className="page-head">
        <div>
          <h1>{conversation?.otherName ?? 'Conversation'}</h1>
          <p className="sub">
            {otherTyping ? 'typing…' : 'You both accepted — talk about rent, deposit and house rules'}
          </p>
        </div>
        <Link to="/chats" className="btn btn-ghost btn-sm">
          ← All chats
        </Link>
      </header>

      <div className="safety-strip">
        <span>⚠</span>
        <span>
          Never send a deposit before visiting the room.{' '}
          <Link to="/safety" style={{ textDecoration: 'underline' }}>
            Read the safety rules
          </Link>
        </span>
      </div>

      <div className="chat-shell">
        <div className="panel" style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <ConversationList
            conversations={conversations.data ?? []}
            activeId={id}
            onSelect={(conversationId) => navigate(`/chat/${conversationId}`)}
          />
        </div>

        <div className="panel thread">
          <div className="thread-head">
            <span className="avatar">{(conversation?.otherName ?? '?').slice(0, 1).toUpperCase()}</span>
            <div className="grow">
              <h3>{conversation?.otherName ?? 'Conversation'}</h3>
              <p className="typing">{otherTyping ? 'typing…' : ''}</p>
            </div>
          </div>

          <div className="messages">
            {history.isLoading && <p className="muted">Loading messages…</p>}

            {!history.isLoading && messages.length === 0 && (
              <div className="empty">
                <div className="glyph">👋</div>
                <strong>Say hi</strong>
                <span className="muted">Try "When can I visit?" or "Is the deposit negotiable?"</span>
              </div>
            )}

            {messages.map((message) => {
              const mine = message.senderId === user?.id;
              return (
                <div
                  key={message.clientMsgId}
                  className={`bubble${mine ? ' mine' : ''}${message.id < 0 ? ' pending' : ''}`}
                >
                  {message.body}
                  <span className="meta">
                    {formatTime(message.createdAt)}
                    {message.id < 0 ? ' · sending' : ''}
                  </span>
                </div>
              );
            })}
            <div ref={bottomRef} />
          </div>

          {error && (
            <div className="alert" style={{ margin: '0 13px 10px' }}>
              {error}
            </div>
          )}

          <div className="composer">
            <input
              value={text}
              onChange={(e) => onType(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  void send();
                }
              }}
              placeholder="Write a message…"
              maxLength={2000}
            />
            <button className="btn btn-primary" onClick={() => void send()} disabled={!text.trim()}>
              Send
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
