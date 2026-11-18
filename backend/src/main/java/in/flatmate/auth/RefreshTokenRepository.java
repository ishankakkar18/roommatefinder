package in.flatmate.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Kills every live token for a user — used on replay detection. */
    @Modifying
    @Query("update RefreshToken r set r.revokedAt = :at where r.userId = :userId and r.revokedAt is null")
    int revokeAllForUser(@Param("userId") Long userId, @Param("at") LocalDateTime at);
}
