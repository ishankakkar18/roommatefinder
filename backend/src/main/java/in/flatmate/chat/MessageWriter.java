package in.flatmate.chat;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Idempotent insert. JPA cannot express ON DUPLICATE KEY UPDATE cleanly, and
 * catching a constraint violation would poison the surrounding transaction,
 * so this one statement goes through JdbcTemplate.
 *
 * LAST_INSERT_ID(id) makes MySQL return the EXISTING row's id on a duplicate,
 * which is what makes a retried send return the same message instead of a 409.
 */
@Component
public class MessageWriter {

    private static final String INSERT = """
            insert into messages (client_msg_id, conversation_id, sender_id, body)
            values (?, ?, ?, ?)
            on duplicate key update id = last_insert_id(id)
            """;

    private final JdbcTemplate jdbc;

    public MessageWriter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** @return the id of the inserted row, or of the existing row on retry. */
    public long insertOrGetId(String conversationId, Long senderId, String clientMsgId, String body) {
        KeyHolder keys = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, clientMsgId);
            ps.setString(2, conversationId);
            ps.setLong(3, senderId);
            ps.setString(4, body);
            return ps;
        }, keys);

        Number key = keys.getKey();
        if (key != null) {
            return key.longValue();
        }
        // Defensive fallback: some connectors do not surface LAST_INSERT_ID.
        return jdbc.queryForObject("""
                select id from messages where conversation_id = ? and client_msg_id = ?
                """, Long.class, conversationId, clientMsgId);
    }
}
