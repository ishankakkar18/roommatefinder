package in.flatmate.chat;

import java.security.Principal;

/** Principal attached to a STOMP session at CONNECT. getName() is the user id. */
public record StompPrincipal(Long id, String email) implements Principal {

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}
