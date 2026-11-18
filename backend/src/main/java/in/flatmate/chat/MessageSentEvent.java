package in.flatmate.chat;

import in.flatmate.chat.dto.ChatDtos.MessageDto;

/**
 * Published inside the transaction, delivered only AFTER_COMMIT — a message
 * that rolls back must never reach the other user's screen.
 */
public record MessageSentEvent(String conversationId, MessageDto message) {
}
