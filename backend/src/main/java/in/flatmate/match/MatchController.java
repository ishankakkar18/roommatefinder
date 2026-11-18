package in.flatmate.match;

import in.flatmate.auth.AppUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchingService matchingService;

    public MatchController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping
    public List<MatchCard> matches(@AuthenticationPrincipal AppUserPrincipal principal) {
        return matchingService.matchesFor(principal.id());
    }
}
