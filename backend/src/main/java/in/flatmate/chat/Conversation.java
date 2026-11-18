package in.flatmate.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Created only when a match request is ACCEPTED.
 * user_a_id < user_b_id is enforced by a CHECK constraint, which makes a
 * duplicate conversation for the same pair structurally impossible.
 */
@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private String id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "user_a_id", nullable = false)
    private Long userAId;

    @Column(name = "user_b_id", nullable = false)
    private Long userBId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ConversationStatus status = ConversationStatus.ACCEPTED;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Conversation() {
    }

    public Conversation(String id, Long requestId, Long userAId, Long userBId) {
        if (userAId >= userBId) {
            throw new IllegalArgumentException("user_a_id must be < user_b_id");
        }
        this.id = id;
        this.requestId = requestId;
        this.userAId = userAId;
        this.userBId = userBId;
    }

    public String getId() {
        return id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public Long getUserAId() {
        return userAId;
    }

    public Long getUserBId() {
        return userBId;
    }

    public ConversationStatus getStatus() {
        return status;
    }

    public void setStatus(ConversationStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean hasParticipant(Long userId) {
        return userAId.equals(userId) || userBId.equals(userId);
    }

    public Long otherParticipant(Long userId) {
        return userAId.equals(userId) ? userBId : userAId;
    }
}
