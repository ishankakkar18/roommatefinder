package in.flatmate.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    Optional<MatchRequest> findByFromUserIdAndToUserId(Long fromUserId, Long toUserId);

    boolean existsByFromUserIdAndToUserId(Long fromUserId, Long toUserId);

    long countByFromUserIdAndStatus(Long fromUserId, RequestStatus status);

    List<MatchRequest> findByToUserIdAndStatusOrderByCreatedAtDesc(Long toUserId, RequestStatus status);

    List<MatchRequest> findByFromUserIdOrderByCreatedAtDesc(Long fromUserId);

    /** Bulk-expire stale pending requests. Called by the scheduled job. */
    @Modifying
    @Query("""
            update MatchRequest r
               set r.status = :expired
             where r.status = :pending
               and r.expiresAt < :now
            """)
    int expirePending(@Param("pending") RequestStatus pending,
                      @Param("expired") RequestStatus expired,
                      @Param("now") LocalDateTime now);
}
