package in.flatmate.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByConversationIdAndClientMsgId(String conversationId, String clientMsgId);

    /** Newest page, used when opening a chat with no cursor. */
    List<Message> findTop50ByConversationIdOrderByIdDesc(String conversationId);

    /** Last message preview for the conversation list. */
    Optional<Message> findTopByConversationIdOrderByIdDesc(String conversationId);

    /** Reconnect recovery: everything after the last id the client has seen. */
    List<Message> findByConversationIdAndIdGreaterThanOrderByIdAsc(
            String conversationId, Long afterId, Pageable pageable);
}
