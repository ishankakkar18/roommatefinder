package in.flatmate.chat;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Detects off-platform contact details in a message body.
 * Rules first, no LLM: cheap, instant, and explains itself.
 *
 * Applied only when app.chat.block-contact-info = true. It defaults to false
 * because chat is only reachable AFTER a request is accepted, at which point
 * sharing a number is the whole point of the conversation.
 */
@Component
public class LinkDetector {

    private static final Pattern URL =
            Pattern.compile("(?i)\\b(https?://|www\\.)\\S+");

    /** 10-digit Indian mobile, optionally prefixed with +91 / 91. */
    private static final Pattern PHONE =
            Pattern.compile("(?<!\\d)(?:\\+?91[\\s-]?)?[6-9]\\d{9}(?!\\d)");

    private static final Pattern UPI =
            Pattern.compile("(?i)\\b[a-z0-9._-]{2,}@(okhdfcbank|okicici|oksbi|okaxis|paytm|ybl|upi|apl)\\b");

    public Optional<String> detect(String body) {
        if (body == null || body.isBlank()) {
            return Optional.empty();
        }
        if (URL.matcher(body).find()) {
            return Optional.of("URL");
        }
        if (PHONE.matcher(body).find()) {
            return Optional.of("PHONE_NUMBER");
        }
        if (UPI.matcher(body).find()) {
            return Optional.of("UPI_ID");
        }
        return Optional.empty();
    }
}
