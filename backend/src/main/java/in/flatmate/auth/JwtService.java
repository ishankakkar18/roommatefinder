package in.flatmate.auth;

import in.flatmate.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey key;
    private final AppProperties props;

    public JwtService(AppProperties props) {
        this.props = props;
        byte[] secret = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret must be at least 32 bytes for HS256 (got " + secret.length + ")");
        }
        this.key = Keys.hmacShaKeyFor(secret);
    }

    public String issueAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuer(props.jwt().issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.jwt().accessTtlMinutes(), ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /** @return the principal for a valid, unexpired token; empty otherwise. */
    public Optional<AppUserPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new AppUserPrincipal(
                    Long.valueOf(claims.getSubject()),
                    claims.get("email", String.class)));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public long accessTtlSeconds() {
        return props.jwt().accessTtlMinutes() * 60L;
    }
}
