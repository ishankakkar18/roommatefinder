package in.flatmate.chat;

/** Sent when a participant reads up to a message, so the sender's ticks can turn double. */
public record ReadReceiptEvent(String conversationId, Long userId, Long lastReadMessageId) {
}
