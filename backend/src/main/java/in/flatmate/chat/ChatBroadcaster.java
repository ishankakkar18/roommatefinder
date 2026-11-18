package in.flatmate.chat;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * The only place that pushes to STOMP topics.
 * AFTER_COMMIT guarantees we never broadcast a message that then rolls back.
 */
@Component
public class ChatBroadcaster {

    private final SimpMessagingTemplate template;

    public ChatBroadcaster(SimpMessagingTemplate template) {
        this.template = template;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageSent(MessageSentEvent event) {
        template.convertAndSend(topic(event.conversationId()), event.message());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReadReceipt(ReadReceiptEvent event) {
        template.convertAndSend(
                topic(event.conversationId()) + "/read",
                new ReadReceipt(event.userId(), event.lastReadMessageId()));
    }

    private String topic(String conversationId) {
        return "/topic/conversations/" + conversationId;
    }

    public record ReadReceipt(Long userId, Long lastReadMessageId) {
    }
}
