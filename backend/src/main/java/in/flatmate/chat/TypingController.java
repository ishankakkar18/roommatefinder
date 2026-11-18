package in.flatmate.chat;

import in.flatmate.chat.dto.ChatDtos.TypingEvent;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Typing indicators travel as STOMP broadcasts and never touch the database —
 * one row per keystroke would be the fastest way to melt the messages table.
 */
@Controller
public class TypingController {

    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    public TypingController(ChatService chatService, SimpMessagingTemplate template) {
        this.chatService = chatService;
        this.template = template;
    }

    @MessageMapping("/conversations/{id}/typing")
    public void typing(@DestinationVariable String id, Principal principal) {
        if (principal == null) {
            return;
        }
        Long userId = Long.valueOf(principal.getName());
        chatService.assertParticipant(id, userId);
        template.convertAndSend(
                "/topic/conversations/" + id + "/typing",
                new TypingEvent(userId, true));
    }
}
