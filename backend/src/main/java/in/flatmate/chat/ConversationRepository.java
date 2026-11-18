package in.flatmate.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

    /**
     * The only lookup allowed by the API. Scoping the query to the caller is
     * the first of the three authorization layers (the others are the STOMP
     * interceptor and the service-level status checks).
     */
    @Query("""
            select c from Conversation c
            where c.id = :id
              and (c.userAId = :userId or c.userBId = :userId)
            """)
    Optional<Conversation> findForParticipant(@Param("id") String id, @Param("userId") Long userId);

    Optional<Conversation> findByUserAIdAndUserBId(Long userAId, Long userBId);

    @Query("""
            select c from Conversation c
            where c.userAId = :userId or c.userBId = :userId
            order by coalesce(c.lastMessageAt, c.createdAt) desc
            """)
    List<Conversation> findAllForUser(@Param("userId") Long userId);
}
