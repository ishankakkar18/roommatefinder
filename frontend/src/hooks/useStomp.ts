import { Client } from '@stomp/stompjs';
import { useCallback, useEffect, useRef } from 'react';
import { WS_URL } from '../api';
import type { Message } from '../types';

interface Handlers {
  onMessage: (message: Message) => void;
  onRead: (receipt: { userId: number; lastReadMessageId: number }) => void;
  onTyping: (event: { userId: number; typing: boolean }) => void;
  onConnected: () => void;
}

/**
 * STOMP over WebSocket. Sending goes over HTTP; this only receives.
 *
 * onConnected fires on every (re)connect, so the caller can refetch anything
 * missed while the socket was down — heartbeats do not replay events.
 */
export function useChatSocket(
  conversationId: string | undefined,
  token: string | null,
  handlers: Handlers,
) {
  const handlersRef = useRef(handlers);
  handlersRef.current = handlers;
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!conversationId || !token) return;

    const client = new Client({
      brokerURL: WS_URL,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => undefined,
    });

    client.onConnect = () => {
      client.subscribe(`/topic/conversations/${conversationId}`, (frame) =>
        handlersRef.current.onMessage(JSON.parse(frame.body) as Message),
      );
      client.subscribe(`/topic/conversations/${conversationId}/read`, (frame) =>
        handlersRef.current.onRead(JSON.parse(frame.body)),
      );
      client.subscribe(`/topic/conversations/${conversationId}/typing`, (frame) =>
        handlersRef.current.onTyping(JSON.parse(frame.body)),
      );
      handlersRef.current.onConnected();
    };

    client.onStompError = (frame) => {
      console.warn('STOMP error:', frame.headers.message);
    };

    void client.activate();
    clientRef.current = client;

    return () => {
      void client.deactivate();
      clientRef.current = null;
    };
  }, [conversationId, token]);

  const sendTyping = useCallback(() => {
    const client = clientRef.current;
    if (client?.connected && conversationId) {
      client.publish({
        destination: `/app/conversations/${conversationId}/typing`,
        body: '{}',
      });
    }
  }, [conversationId]);

  return { sendTyping };
}
