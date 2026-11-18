package in.flatmate.profile;

import in.flatmate.auth.AppUserPrincipal;
import in.flatmate.profile.dto.ProfileDtos.ProfileRequest;
import in.flatmate.profile.dto.ProfileDtos.ProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return profileService.find(principal.id())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping
    public ProfileResponse upsert(@AuthenticationPrincipal AppUserPrincipal principal,
                                  @Valid @RequestBody ProfileRequest request) {
        return profileService.upsert(principal.id(), request);
    }
}
