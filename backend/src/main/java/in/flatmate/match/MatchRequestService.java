package in.flatmate.match;

import in.flatmate.chat.Conversation;
import in.flatmate.chat.ConversationRepository;
import in.flatmate.common.ApiException;
import in.flatmate.config.AppProperties;
import in.flatmate.match.dto.MatchDtos.AcceptResponse;
import in.flatmate.match.dto.MatchDtos.RequestResponse;
import in.flatmate.profile.Profile;
import in.flatmate.profile.ProfileRepository;
import in.flatmate.user.User;
import in.flatmate.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Request lifecycle: PENDING -> ACCEPTED | DECLINED | WITHDRAWN | EXPIRED.
 *
 * Accepting creates the conversation and removes the pair from both match
 * pools permanently, because match_requests doubles as the exclusion list.
 */
@Service
public class MatchRequestService {

    private final MatchRequestRepository requests;
    private final ProfileRepository profiles;
    private final UserRepository users;
    private final ConversationRepository conversations;
    private final MatchingService matching;
    private final AppProperties props;

    public MatchRequestService(MatchRequestRepository requests,
                               ProfileRepository profiles,
                               UserRepository users,
                               ConversationRepository conversations,
                               MatchingService matching,
                               AppProperties props) {
        this.requests = requests;
        this.profiles = profiles;
        this.users = users;
        this.conversations = conversations;
        this.matching = matching;
        this.props = props;
    }

    @Transactional
    public RequestResponse create(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw ApiException.badRequest("SELF_REQUEST", "You cannot request yourself");
        }
        if (requests.existsByFromUserIdAndToUserId(fromUserId, toUserId)) {
            throw ApiException.conflict("REQUEST_EXISTS", "You already sent this person a request");
        }
        if (requests.existsByFromUserIdAndToUserId(toUserId, fromUserId)) {
            throw ApiException.conflict("REQUEST_INCOMING",
                    "They already sent you a request — check your incoming list");
        }

        long open = requests.countByFromUserIdAndStatus(fromUserId, RequestStatus.PENDING);
        if (open >= props.matching().maxOpenRequestsPerUser()) {
            throw ApiException.conflict("TOO_MANY_OPEN_REQUESTS",
                    "You have " + open + " open requests. Wait for replies before sending more.");
        }

        Profile me = profiles.findById(fromUserId)
                .filter(Profile::isComplete)
                .orElseThrow(() -> ApiException.badRequest("PROFILE_INCOMPLETE",
                        "Complete your profile first"));
        Profile other = profiles.findById(toUserId)
                .filter(Profile::isComplete)
                .orElseThrow(() -> ApiException.notFound("PROFILE_NOT_FOUND",
                        "That person is not available"));

        if (!me.getCityId().equals(other.getCityId())) {
            throw ApiException.badRequest("NOT_IN_YOUR_CITY", "That person is in a different city");
        }

        User meUser = users.findById(fromUserId).orElseThrow();
        User otherUser = users.findById(toUserId).orElseThrow();

        if (!matching.passesHardFilters(me, other, meUser, otherUser)) {
            throw ApiException.badRequest("NOT_A_MATCH",
                    "You don't meet each other's criteria");
        }

        int score = matching.scorePair(fromUserId, toUserId).score();
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusDays(props.matching().requestTtlDays());

        MatchRequest saved = requests.save(
                new MatchRequest(fromUserId, toUserId, score, expiresAt));

        return toResponse(saved, Map.of(meUser.getId(), meUser, otherUser.getId(), otherUser), fromUserId);
    }

    @Transactional
    public AcceptResponse accept(Long userId, Long requestId) {
        MatchRequest request = requirePendingForRecipient(userId, requestId);

        request.setStatus(RequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        requests.save(request);

        Long a = Math.min(request.getFromUserId(), request.getToUserId());
        Long b = Math.max(request.getFromUserId(), request.getToUserId());

        Conversation conversation = conversations.findByUserAIdAndUserBId(a, b)
                .orElseGet(() -> conversations.save(
                        new Conversation(UUID.randomUUID().toString(), request.getId(), a, b)));

        return new AcceptResponse(request.getId(), request.getStatus(), conversation.getId());
    }

    @Transactional
    public RequestResponse decline(Long userId, Long requestId) {
        MatchRequest request = requirePendingForRecipient(userId, requestId);
        request.setStatus(RequestStatus.DECLINED);
        request.setRespondedAt(LocalDateTime.now());
        return toResponse(requests.save(request), names(request), userId);
    }

    @Transactional
    public RequestResponse withdraw(Long userId, Long requestId) {
        MatchRequest request = requests.findById(requestId)
                .orElseThrow(() -> ApiException.notFound("REQUEST_NOT_FOUND", "No such request"));
        if (!request.getFromUserId().equals(userId)) {
            throw ApiException.forbidden("NOT_YOUR_REQUEST", "Only the sender can withdraw");
        }
        if (!request.isPending()) {
            throw ApiException.conflict("NOT_PENDING", "Request is already " + request.getStatus());
        }
        request.setStatus(RequestStatus.WITHDRAWN);
        request.setRespondedAt(LocalDateTime.now());
        return toResponse(requests.save(request), names(request), userId);
    }

    @Transactional(readOnly = true)
    public List<RequestResponse> incoming(Long userId) {
        return list(requests.findByToUserIdAndStatusOrderByCreatedAtDesc(userId, RequestStatus.PENDING), userId);
    }

    @Transactional(readOnly = true)
    public List<RequestResponse> outgoing(Long userId) {
        return list(requests.findByFromUserIdOrderByCreatedAtDesc(userId), userId);
    }

    // ---------------------------------------------------------------- helpers

    private MatchRequest requirePendingForRecipient(Long userId, Long requestId) {
        MatchRequest request = requests.findById(requestId)
                .orElseThrow(() -> ApiException.notFound("REQUEST_NOT_FOUND", "No such request"));
        if (!request.getToUserId().equals(userId)) {
            throw ApiException.forbidden("NOT_YOUR_REQUEST", "This request was not sent to you");
        }
        if (!request.isPending()) {
            throw ApiException.conflict("NOT_PENDING", "Request is already " + request.getStatus());
        }
        return request;
    }

    private List<RequestResponse> list(List<MatchRequest> rows, Long viewerId) {
        Map<Long, User> people = loadPeople(rows);
        return rows.stream().map(r -> toResponse(r, people, viewerId)).toList();
    }

    private Map<Long, User> names(MatchRequest request) {
        return loadPeople(List.of(request));
    }

    private Map<Long, User> loadPeople(List<MatchRequest> rows) {
        List<Long> ids = rows.stream()
                .flatMap(r -> Stream.of(r.getFromUserId(), r.getToUserId()))
                .distinct()
                .toList();
        return users.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private RequestResponse toResponse(MatchRequest r, Map<Long, User> people, Long viewerId) {
        User from = people.get(r.getFromUserId());
        User to = people.get(r.getToUserId());
        return new RequestResponse(
                r.getId(),
                r.getFromUserId(),
                from != null ? from.getName() : null,
                r.getToUserId(),
                to != null ? to.getName() : null,
                r.getStatus(),
                r.getScoreAtRequest(),
                r.getFromUserId().equals(viewerId) ? "OUTGOING" : "INCOMING",
                r.getCreatedAt(),
                r.getExpiresAt());
    }
}
