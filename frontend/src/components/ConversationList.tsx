import type { Conversation } from '../types';

export default function ConversationList({
  conversations,
  activeId,
  onSelect,
}: {
  conversations: Conversation[];
  activeId?: string;
  onSelect: (id: string) => void;
}) {
  if (conversations.length === 0) {
    return (
      <div className="empty">
        <div className="glyph">💬</div>
        <strong>No conversations yet</strong>
        <span className="muted">Accept a request to start talking.</span>
      </div>
    );
  }

  return (
    <div className="chat-list">
      {conversations.map((conversation) => (
        <button
          key={conversation.id}
          className={`conv${conversation.id === activeId ? ' active' : ''}`}
          onClick={() => onSelect(conversation.id)}
        >
          <div className="spread">
            <span className="name">{conversation.otherName ?? 'Someone'}</span>
            {conversation.unread > 0 && <span className="nav-badge">{conversation.unread}</span>}
          </div>
          <span className="preview">{conversation.lastMessage ?? 'No messages yet'}</span>
        </button>
      ))}
    </div>
  );
}
