package in.flatmate.auth;

import in.flatmate.auth.dto.AuthDtos.LoginRequest;
import in.flatmate.auth.dto.AuthDtos.MeResponse;
import in.flatmate.auth.dto.AuthDtos.RegisterRequest;
import in.flatmate.auth.dto.AuthDtos.TokenResponse;
import in.flatmate.common.ApiException;
import in.flatmate.config.AppProperties;
import in.flatmate.user.Gender;
import in.flatmate.user.User;
import in.flatmate.user.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;
    private final JdbcTemplate jdbc;

    public AuthService(UserRepository users,
                       RefreshTokenRepository refreshTokens,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       AppProperties props,
                       JdbcTemplate jdbc) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
        this.jdbc = jdbc;
    }

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("EMAIL_TAKEN", "An account with that email already exists");
        }

        User user = users.save(new User(
                email,
                passwordEncoder.encode(req.password()),
                req.name().trim(),
                req.gender() != null ? req.gender() : Gender.UNDISCLOSED,
                req.phone()));

        user.setLastLoginAt(LocalDateTime.now());
        users.save(user);

        return issueTokens(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email().trim())
                .orElseThrow(() -> ApiException.unauthorized("INVALID_CREDENTIALS",
                        "Email or password is incorrect"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "Email or password is incorrect");
        }

        user.setLastLoginAt(LocalDateTime.now());
        users.save(user);

        return issueTokens(user);
    }

    /**
     * Rotation with replay detection.
     * noRollbackFor keeps the revoke committed even though we then throw 401.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public TokenResponse refresh(String rawToken) {
        RefreshToken current = refreshTokens.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> ApiException.unauthorized("REFRESH_INVALID", "Invalid refresh token"));

        if (!current.isUsable()) {
            refreshTokens.revokeAllForUser(current.getUserId(), LocalDateTime.now());
            throw ApiException.unauthorized("REFRESH_REPLAY",
                    "Refresh token already used. All sessions have been revoked; sign in again.");
        }

        LocalDateTime now = LocalDateTime.now();
        String newRaw = randomToken();
        RefreshToken next = refreshTokens.save(new RefreshToken(
                current.getUserId(),
                sha256(newRaw),
                now.plusDays(props.jwt().refreshTtlDays())));

        current.setRevokedAt(now);
        current.setReplacedBy(next.getId());
        refreshTokens.save(current);

        User user = users.findById(current.getUserId())
                .orElseThrow(() -> ApiException.unauthorized("USER_GONE", "Account no longer exists"));

        return new TokenResponse(
                jwtService.issueAccessToken(user.getId(), user.getEmail()),
                newRaw,
                jwtService.accessTtlSeconds(),
                profileComplete(user.getId()));
    }

    @Transactional
    public void logout(String rawToken) {
        refreshTokens.findByTokenHash(sha256(rawToken))
                .ifPresent(rt -> {
                    rt.setRevokedAt(LocalDateTime.now());
                    refreshTokens.save(rt);
                });
    }

    public MeResponse me(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("USER_GONE", "Account no longer exists"));
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getName(),
                user.getGender() != null ? user.getGender().name() : null,
                profileComplete(userId));
    }

    // --------------------------------------------------------------- helpers

    private TokenResponse issueTokens(User user) {
        String access = jwtService.issueAccessToken(user.getId(), user.getEmail());
        String refresh = randomToken();
        refreshTokens.save(new RefreshToken(
                user.getId(),
                sha256(refresh),
                LocalDateTime.now().plusDays(props.jwt().refreshTtlDays())));
        return new TokenResponse(access, refresh, jwtService.accessTtlSeconds(),
                profileComplete(user.getId()));
    }

    private boolean profileComplete(Long userId) {
        Integer count = jdbc.queryForObject(
                "select count(*) from profiles where user_id = ? and completed_at is not null",
                Integer.class, userId);
        return count != null && count > 0;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
