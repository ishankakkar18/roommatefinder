package in.flatmate.match;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * A request from one user to another. Also serves as the match-pool exclusion
 * list: any pair with PENDING/ACCEPTED/DECLINED never surfaces again.
 * WITHDRAWN/EXPIRED becomes re-requestable after the TTL window.
 */
@Entity
@Table(name = "match_requests")
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RequestStatus status = RequestStatus.PENDING;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "score_at_request", nullable = false)
    private int scoreAtRequest;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    protected MatchRequest() {
    }

    public MatchRequest(Long fromUserId, Long toUserId, int scoreAtRequest, LocalDateTime expiresAt) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.scoreAtRequest = scoreAtRequest;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public int getScoreAtRequest() {
        return scoreAtRequest;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }
}
