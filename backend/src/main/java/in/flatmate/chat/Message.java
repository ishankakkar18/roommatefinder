package in.flatmate.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * id is BIGINT AUTO_INCREMENT: sequential InnoDB inserts and a total order.
 * client_msg_id is the browser-generated idempotency key, unique per
 * conversation, so a retried send can never create a second row.
 * Ordering uses id, never created_at — two messages can share a millisecond.
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "client_msg_id", nullable = false, length = 36)
    private String clientMsgId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "conversation_id", nullable = false, length = 36)
    private String conversationId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(nullable = false, length = 2000)
    private String body;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected Message() {
    }

    public Long getId() {
        return id;
    }

    public String getClientMsgId() {
        return clientMsgId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
