package in.flatmate.profile;

import in.flatmate.match.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /**
     * Candidate pool: same city, completed profile, not me, and no existing
     * match request in either direction with a blocking status.
     * Everything cheaper than a join stays here; the remaining hard filters
     * and the scoring run in MatchingService so they are unit-testable.
     */
    @Query("""
            select p from Profile p
            where p.cityId = :cityId
              and p.completedAt is not null
              and p.userId <> :userId
              and not exists (
                  select 1 from MatchRequest r
                  where (
                          (r.fromUserId = :userId and r.toUserId = p.userId)
                       or (r.fromUserId = p.userId and r.toUserId = :userId)
                        )
                    and r.status in :blockingStatuses
              )
            """)
    List<Profile> findCandidates(@Param("userId") Long userId,
                                 @Param("cityId") Integer cityId,
                                 @Param("blockingStatuses") Collection<RequestStatus> blockingStatuses);
}
