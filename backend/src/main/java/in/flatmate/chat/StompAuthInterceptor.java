package in.flatmate.chat;

import in.flatmate.auth.AppUserPrincipal;
import in.flatmate.auth.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Authorization layer 2 of 3 (the others are the scoped repository query and
 * the service-level status check).
 *
 * Without this, ANY authenticated client can subscribe to ANY chat topic.
 * Spring does no per-destination authorization for STOMP topics by default,
 * and the failure is invisible in manual testing — only a test proves it.
 */
@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final String TOPIC_PREFIX = "/topic/conversations/";
    private static final String APP_PREFIX = "/app/conversations/";
    private static final String BEARER = "Bearer ";
    static final String SESSION_USER_ID = "userId";

    private final JwtService jwtService;
    private final ConversationRepository conversations;

    public StompAuthInterceptor(JwtService jwtService, ConversationRepository conversations) {
        this.jwtService = jwtService;
        this.conversations = conversations;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        switch (accessor.getCommand()) {
            case CONNECT -> onConnect(accessor);
            case SUBSCRIBE -> onSubscribe(accessor);
            case SEND -> onSend(accessor);
            default -> {
                // DISCONNECT, ACK, etc. need no checks
            }
        }
        return message;
    }

    private void onConnect(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith(BEARER)) {
            throw new MessagingException("Missing Authorization header on STOMP CONNECT");
        }
        AppUserPrincipal principal = jwtService.parse(header.substring(BEARER.length()))
                .orElseThrow(() -> new MessagingException("Invalid or expired token"));

        accessor.setUser(new StompPrincipal(principal.id(), principal.email()));

        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes != null) {
            attributes.put(SESSION_USER_ID, principal.id());
        }
    }

    private void onSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(TOPIC_PREFIX)) {
            throw new MessagingException("Forbidden subscription: " + destination);
        }
        requireParticipant(accessor, conversationId(destination.substring(TOPIC_PREFIX.length())),
                destination);
    }

    private void onSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(APP_PREFIX)) {
            throw new MessagingException("Forbidden send destination: " + destination);
        }
        requireParticipant(accessor, conversationId(destination.substring(APP_PREFIX.length())),
                destination);
    }

    private void requireParticipant(StompHeaderAccessor accessor, String conversationId,
                                    String destination) {
        Long userId = currentUserId(accessor);
        if (userId == null) {
            throw new MessagingException("Not authenticated for " + destination);
        }
        boolean participant = conversations.findForParticipant(conversationId, userId).isPresent();
        if (!participant) {
            throw new MessagingException("Forbidden: not a participant of conversation "
                    + conversationId);
        }
    }

    private Long currentUserId(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof StompPrincipal principal) {
            return principal.id();
        }
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes != null && attributes.get(SESSION_USER_ID) instanceof Long id) {
            return id;
        }
        return null;
    }

    /** "/topic/conversations/{id}/typing" -> "{id}" */
    private String conversationId(String remainder) {
        int slash = remainder.indexOf('/');
        return slash < 0 ? remainder : remainder.substring(0, slash);
    }
}
