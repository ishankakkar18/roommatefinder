import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api';
import type { Conversation } from '../types';
import { formatTime } from '../time';

export default function Chats() {
  const navigate = useNavigate();

  const conversations = useQuery({
    queryKey: ['conversations'],
    queryFn: () => api<Conversation[]>('/api/conversations'),
  });

  const list = conversations.data ?? [];

  return (
    <>
      <header className="page-head">
        <div>
          <h1>Chats</h1>
          <p className="sub">
            {list.length > 0
              ? `${list.length} open ${list.length === 1 ? 'conversation' : 'conversations'}`
              : 'Conversations open once a request is accepted'}
          </p>
        </div>
      </header>

      {conversations.isLoading && <div className="skeleton" />}

      {!conversations.isLoading && list.length === 0 && (
        <div className="empty">
          <div className="glyph">💬</div>
          <strong>No conversations yet</strong>
          <span className="muted">
            Accept a request on the Requests page and the chat opens here.
          </span>
        </div>
      )}

      <div className="stack">
        {list.map((conversation, index) => (
          <button
            key={conversation.id}
            className="req-row rise"
            style={{ ['--i' as string]: index, textAlign: 'left', font: 'inherit', color: 'inherit' }}
            onClick={() => navigate(`/chat/${conversation.id}`)}
          >
            <span className="avatar">
              {(conversation.otherName ?? '?').slice(0, 1).toUpperCase()}
            </span>
            <div className="grow">
              <div className="spread">
                <h3>{conversation.otherName ?? 'Someone'}</h3>
                {conversation.lastMessageAt && (
                  <span className="muted">{formatTime(conversation.lastMessageAt)}</span>
                )}
              </div>
              <p className="muted" style={{ marginTop: 2 }}>
                {conversation.lastMessage ?? 'No messages yet'}
              </p>
            </div>
            {conversation.unread > 0 && <span className="nav-badge">{conversation.unread}</span>}
          </button>
        ))}
      </div>
    </>
  );
}
