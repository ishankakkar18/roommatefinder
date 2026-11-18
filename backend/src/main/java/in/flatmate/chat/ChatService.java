package in.flatmate.chat;

import in.flatmate.chat.dto.ChatDtos.ConversationDto;
import in.flatmate.chat.dto.ChatDtos.MessageDto;
import in.flatmate.chat.dto.ChatDtos.SendMessageRequest;
import in.flatmate.common.ApiException;
import in.flatmate.config.AppProperties;
import in.flatmate.user.User;
import in.flatmate.user.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Chat rules:
 *   - every entry point starts with findForParticipant (authorization layer 1)
 *   - sending is HTTP, receiving is STOMP
 *   - ordering is by messages.id, never created_at
 *   - broadcast happens after commit, never inside the transaction
 */
@Service
public class ChatService {

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final MessageWriter messageWriter;
    private final ReadCursorRepository readCursors;
    private final UserRepository users;
    private final ApplicationEventPublisher events;
    private final AppProperties props;
    private final LinkDetector linkDetector;
    private final ChatRateLimiter rateLimiter;

    public ChatService(ConversationRepository conversations,
                       MessageRepository messages,
                       MessageWriter messageWriter,
                       ReadCursorRepository readCursors,
                       UserRepository users,
                       ApplicationEventPublisher events,
                       AppProperties props,
                       LinkDetector linkDetector,
                       ChatRateLimiter rateLimiter) {
        this.conversations = conversations;
        this.messages = messages;
        this.messageWriter = messageWriter;
        this.readCursors = readCursors;
        this.users = users;
        this.events = events;
        this.props = props;
        this.linkDetector = linkDetector;
        this.rateLimiter = rateLimiter;
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> list(Long userId) {
        return conversations.findAllForUser(userId).stream()
                .map(c -> toDto(c, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageDto> history(Long userId, String conversationId, Long afterId) {
        requireParticipant(conversationId, userId);

        List<Message> rows;
        if (afterId == null || afterId <= 0) {
            rows = new ArrayList<>(messages.findTop50ByConversationIdOrderByIdDesc(conversationId));
            Collections.reverse(rows);
        } else {
            rows = messages.findByConversationIdAndIdGreaterThanOrderByIdAsc(
                    conversationId, afterId,
                    PageRequest.of(0, props.chat().historyPageSize()));
        }
        return mapAll(rows);
    }

    @Transactional
    public MessageDto send(Long userId, String conversationId, SendMessageRequest request) {
        Conversation conversation = requireAccepted(conversationId, userId);

        String body = request.body() == null ? "" : request.body().trim();
        if (body.isEmpty()) {
            throw ApiException.badRequest("EMPTY_MESSAGE", "Message body is required");
        }
        if (body.length() > props.chat().maxBodyLength()) {
            throw ApiException.badRequest("MESSAGE_TOO_LONG",
                    "Message must be at most " + props.chat().maxBodyLength() + " characters");
        }
        if (props.chat().blockContactInfo()) {
            linkDetector.detect(body).ifPresent(kind -> {
                throw new ApiException(422, "CONTACT_INFO_BLOCKED",
                        "Sharing " + kind.toLowerCase().replace('_', ' ') + " is blocked in chat");
            });
        }

        rateLimiter.check(conversationId, userId);

        long id = messageWriter.insertOrGetId(conversationId, userId, request.clientMsgId(), body);
        Message saved = messages.findById(id).orElseThrow(() ->
                new IllegalStateException("Message vanished after insert: " + id));

        conversation.setLastMessageAt(saved.getCreatedAt());
        conversations.save(conversation);

        String senderName = users.findById(userId).map(User::getName).orElse(null);
        MessageDto dto = toDto(saved, senderName);

        events.publishEvent(new MessageSentEvent(conversationId, dto));
        return dto;
    }

    @Transactional
    public void markRead(Long userId, String conversationId, Long lastReadMessageId) {
        requireParticipant(conversationId, userId);
        readCursors.markRead(conversationId, userId, lastReadMessageId);
        events.publishEvent(new ReadReceiptEvent(conversationId, userId, lastReadMessageId));
    }

    /** Used by the STOMP layer, which has no repository access of its own. */
    @Transactional(readOnly = true)
    public void assertParticipant(String conversationId, Long userId) {
        requireParticipant(conversationId, userId);
    }

    // --------------------------------------------------------------- helpers

    private Conversation requireParticipant(String conversationId, Long userId) {
        return conversations.findForParticipant(conversationId, userId)
                .orElseThrow(() -> ApiException.forbidden("NOT_A_PARTICIPANT",
                        "You are not part of this conversation"));
    }

    private Conversation requireAccepted(String conversationId, Long userId) {
        Conversation conversation = requireParticipant(conversationId, userId);
        if (conversation.getStatus() != ConversationStatus.ACCEPTED) {
            throw ApiException.forbidden("CONVERSATION_CLOSED",
                    "Conversation is " + conversation.getStatus());
        }
        return conversation;
    }

    private List<MessageDto> mapAll(List<Message> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> senderIds = rows.stream().map(Message::getSenderId).distinct().toList();
        Map<Long, User> people = users.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return rows.stream()
                .map(m -> toDto(m, people.containsKey(m.getSenderId())
                        ? people.get(m.getSenderId()).getName()
                        : null))
                .toList();
    }

    private MessageDto toDto(Message m, String senderName) {
        return new MessageDto(
                m.getId(),
                m.getClientMsgId(),
                m.getConversationId(),
                m.getSenderId(),
                senderName,
                m.getBody(),
                m.getCreatedAt());
    }

    private ConversationDto toDto(Conversation c, Long userId) {
        Long otherId = c.otherParticipant(userId);
        String otherName = users.findById(otherId).map(User::getName).orElse(null);
        String lastMessage = messages.findTopByConversationIdOrderByIdDesc(c.getId())
                .map(Message::getBody)
                .orElse(null);

        return new ConversationDto(
                c.getId(),
                otherId,
                otherName,
                lastMessage,
                c.getLastMessageAt(),
                readCursors.unreadCount(c.getId(), userId));
    }
}
