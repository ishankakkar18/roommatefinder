package in.flatmate.match;

import in.flatmate.common.ApiException;
import in.flatmate.config.AppProperties;
import in.flatmate.profile.Profile;
import in.flatmate.profile.ProfileRepository;
import in.flatmate.profile.ProfileService;
import in.flatmate.profile.ProfileEnums.Pets;
import in.flatmate.profile.ProfileEnums.Smoke;
import in.flatmate.profile.ProfileEnums.SmokeTolerance;
import in.flatmate.profile.ProfileEnums.PetTolerance;
import in.flatmate.profile.ProfileEnums.FlatmateGenderPref;
import in.flatmate.reference.ReferenceRepository;
import in.flatmate.user.Gender;
import in.flatmate.user.User;
import in.flatmate.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates matching:
 *   1. SQL pulls the cheap pool (same city, complete, not already requested)
 *   2. Java applies the six hard filters
 *   3. MatchScorer ranks the survivors
 *
 * Kept out of SQL on purpose: the filters and weights must be unit-testable
 * and tunable without a schema change.
 */
@Service
public class MatchingService {

    private static final List<RequestStatus> BLOCKING = List.of(
            RequestStatus.PENDING, RequestStatus.ACCEPTED, RequestStatus.DECLINED);

    private final ProfileRepository profiles;
    private final UserRepository users;
    private final ReferenceRepository reference;
    private final AppProperties props;
    private final MatchScorer scorer;

    public MatchingService(ProfileRepository profiles,
                           UserRepository users,
                           ReferenceRepository reference,
                           AppProperties props) {
        this.profiles = profiles;
        this.users = users;
        this.reference = reference;
        this.props = props;
        this.scorer = new MatchScorer(props.matching().weights(), reference::sameOrAdjacent);
    }

    @Transactional(readOnly = true)
    public List<MatchCard> matchesFor(Long userId) {
        Profile me = profiles.findById(userId)
                .filter(Profile::isComplete)
                .orElseThrow(() -> ApiException.badRequest("PROFILE_INCOMPLETE",
                        "Complete your profile before browsing matches"));

        List<Profile> candidates = profiles.findCandidates(userId, me.getCityId(), BLOCKING);
        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<Long, User> people = users.findAllById(
                        candidates.stream().map(Profile::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        User meUser = users.findById(userId).orElseThrow();

        return candidates.stream()
                .filter(other -> passesHardFilters(me, other, meUser, people.get(other.getUserId())))
                .map(other -> toCard(me, other, people.get(other.getUserId())))
                .sorted(Comparator.comparingInt(MatchCard::score).reversed())
                .limit(props.matching().topN())
                .toList();
    }

    /** Scores a specific pair; the result is frozen onto the request row. */
    @Transactional(readOnly = true)
    public MatchScorer.Result scorePair(Long userId, Long otherUserId) {
        Profile a = profiles.findById(userId)
                .orElseThrow(() -> ApiException.badRequest("PROFILE_INCOMPLETE",
                        "Complete your profile first"));
        Profile b = profiles.findById(otherUserId)
                .orElseThrow(() -> ApiException.notFound("PROFILE_NOT_FOUND",
                        "That person is not available"));
        return scorer.score(a, b);
    }

    /** The six hard filters from the spec. Excluded, never penalised. */
    public boolean passesHardFilters(Profile me, Profile other, User meUser, User otherUser) {        if (Math.abs(me.getBudgetBand() - other.getBudgetBand()) > 1) {
            return false;
        }
        if (!genderOk(meUser.getGender(), me.getFlatmateGenderPref(), otherUser.getGender())
                || !genderOk(otherUser.getGender(), other.getFlatmateGenderPref(), meUser.getGender())) {
            return false;
        }
        if (Math.abs(me.getMoveIn().ordinal() - other.getMoveIn().ordinal()) > 2) {
            return false;
        }
        if (me.getSmoke() == Smoke.I_DO && other.getSmokeTolerance() == SmokeTolerance.CANT) {
            return false;
        }
        if (other.getSmoke() == Smoke.I_DO && me.getSmokeTolerance() == SmokeTolerance.CANT) {
            return false;
        }
        if (me.getPets() == Pets.HAVE && other.getPetTolerance() == PetTolerance.NO_PETS) {
            return false;
        }
        return other.getPets() != Pets.HAVE || me.getPetTolerance() != PetTolerance.NO_PETS;
    }

    private boolean genderOk(Gender owner, FlatmateGenderPref pref, Gender candidate) {        if (pref == null || candidate == null) {
            return true;
        }
        return switch (pref) {
            case NO_PREF -> true;
            case SAME_AS_MINE -> candidate == owner;
            case MALE_ONLY -> candidate == Gender.MALE;
            case FEMALE_ONLY -> candidate == Gender.FEMALE;
        };
    }

    private MatchCard toCard(Profile me, Profile other, User otherUser) {
        MatchScorer.Result result = scorer.score(me, other);
        return new MatchCard(
                other.getUserId(),
                otherUser != null ? otherUser.getName() : null,
                result.score(),
                other.getAreaId(),
                reference.areaName(other.getAreaId()).orElse(null),
                other.getAge(),
                other.getBudgetBand(),
                ProfileService.BUDGET_LABELS[other.getBudgetBand()],
                other.getAboutMe(),
                result.reasons(),
                result.frictions());
    }
}
