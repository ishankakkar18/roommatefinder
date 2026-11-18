package in.flatmate.chat;

import in.flatmate.common.ApiException;
import in.flatmate.config.AppProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 20 messages per minute per (conversation, user). In-memory token bucket:
 * correct for a single instance, which is what v1 is. A second instance would
 * need a shared store (Redis) — noted rather than pretended otherwise.
 */
@Component
public class ChatRateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int perMinute;

    public ChatRateLimiter(AppProperties props) {
        this.perMinute = props.chat().maxMessagesPerMinute();
    }

    public void check(String conversationId, Long userId) {
        Bucket bucket = buckets.computeIfAbsent(
                conversationId + ":" + userId,
                key -> Bucket.builder()
                        .addLimit(Bandwidth.simple(perMinute, Duration.ofMinutes(1)))
                        .build());

        if (!bucket.tryConsume(1)) {
            throw ApiException.tooManyRequests("CHAT_RATE_LIMIT",
                    "You are sending messages too quickly. Wait a moment.");
        }
    }
}
