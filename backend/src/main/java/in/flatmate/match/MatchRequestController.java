package in.flatmate.match;

import in.flatmate.auth.AppUserPrincipal;
import in.flatmate.match.dto.MatchDtos.AcceptResponse;
import in.flatmate.match.dto.MatchDtos.CreateRequest;
import in.flatmate.match.dto.MatchDtos.RequestResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class MatchRequestController {

    private final MatchRequestService requestService;

    public MatchRequestController(MatchRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<RequestResponse> create(@AuthenticationPrincipal AppUserPrincipal principal,
                                                  @Valid @RequestBody CreateRequest body) {
        RequestResponse created = requestService.create(principal.id(), body.toUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/accept")
    public AcceptResponse accept(@AuthenticationPrincipal AppUserPrincipal principal,
                                 @PathVariable Long id) {
        return requestService.accept(principal.id(), id);
    }

    @PostMapping("/{id}/decline")
    public RequestResponse decline(@AuthenticationPrincipal AppUserPrincipal principal,
                                   @PathVariable Long id) {
        return requestService.decline(principal.id(), id);
    }

    @PostMapping("/{id}/withdraw")
    public RequestResponse withdraw(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @PathVariable Long id) {
        return requestService.withdraw(principal.id(), id);
    }

    @GetMapping("/incoming")
    public List<RequestResponse> incoming(@AuthenticationPrincipal AppUserPrincipal principal) {
        return requestService.incoming(principal.id());
    }

    @GetMapping("/outgoing")
    public List<RequestResponse> outgoing(@AuthenticationPrincipal AppUserPrincipal principal) {
        return requestService.outgoing(principal.id());
    }
}
