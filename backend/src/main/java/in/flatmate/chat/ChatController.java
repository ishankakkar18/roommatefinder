package in.flatmate.chat;

import in.flatmate.auth.AppUserPrincipal;
import in.flatmate.chat.dto.ChatDtos.ConversationDto;
import in.flatmate.chat.dto.ChatDtos.MessageDto;
import in.flatmate.chat.dto.ChatDtos.ReadRequest;
import in.flatmate.chat.dto.ChatDtos.SendMessageRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sending is HTTP on purpose: it gives an ack, validation errors and retries.
 * STOMP is used only to receive. Anything else loses messages on flaky wifi.
 */
@RestController
@RequestMapping("/api/conversations")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ConversationDto> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return chatService.list(principal.id());
    }

    @GetMapping("/{id}/messages")
    public List<MessageDto> history(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @PathVariable String id,
                                    @RequestParam(required = false) Long afterId) {
        return chatService.history(principal.id(), id, afterId);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageDto> send(@AuthenticationPrincipal AppUserPrincipal principal,
                                           @PathVariable String id,
                                           @Valid @RequestBody SendMessageRequest request) {
        MessageDto message = chatService.send(principal.id(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @PathVariable String id,
                                         @Valid @RequestBody ReadRequest request) {
        chatService.markRead(principal.id(), id, request.lastReadMessageId());
        return ResponseEntity.noContent().build();
    }
}
