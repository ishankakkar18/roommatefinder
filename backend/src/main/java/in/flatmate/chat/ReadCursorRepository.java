package in.flatmate.chat;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Read cursors are managed with plain SQL: the table is a two-column PK and an
 * upsert is clearer than an @IdClass entity.
 * Unread counts are always DERIVED from this cursor, never a counter column.
 */
@Repository
public class ReadCursorRepository {

    private static final String UPSERT = """
            insert into read_cursors (conversation_id, user_id, last_read_message_id)
            values (?, ?, ?) as new
            on duplicate key update
                last_read_message_id = greatest(read_cursors.last_read_message_id, new.last_read_message_id)
            """;

    private final JdbcTemplate jdbc;

    public ReadCursorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void markRead(String conversationId, Long userId, Long lastReadMessageId) {
        jdbc.update(UPSERT, conversationId, userId, lastReadMessageId);
    }

    public long unreadCount(String conversationId, Long userId) {
        Long n = jdbc.queryForObject("""
                select count(*)
                  from messages m
                 where m.conversation_id = ?
                   and m.sender_id <> ?
                   and m.id > coalesce(
                        (select last_read_message_id from read_cursors
                          where conversation_id = ? and user_id = ?), 0)
                """, Long.class, conversationId, userId, conversationId, userId);
        return n == null ? 0 : n;
    }
}
