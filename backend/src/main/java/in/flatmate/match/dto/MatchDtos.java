package in.flatmate.match.dto;

import in.flatmate.match.RequestStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class MatchDtos {

    private MatchDtos() {
    }

    public record CreateRequest(@NotNull Long toUserId) {
    }

    public record RequestResponse(
            Long id,
            Long fromUserId,
            String fromName,
            Long toUserId,
            String toName,
            RequestStatus status,
            int score,
            String direction,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
    }

    public record AcceptResponse(Long requestId, RequestStatus status, String conversationId) {
    }
}
