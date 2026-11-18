package in.flatmate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the app.* block of application.yml.
 * Match weights live here on purpose: they are tunable without a redeploy.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Chat chat,
        Matching matching
) {

    public record Jwt(String secret, int accessTtlMinutes, int refreshTtlDays, String issuer) {
    }

    public record Chat(int maxMessagesPerMinute, int maxBodyLength, int historyPageSize,
                       boolean blockContactInfo) {
    }

    public record Matching(int topN, int maxOpenRequestsPerUser, int requestTtlDays, Weights weights) {
    }

    public record Weights(int sleep, int cleanliness, int area, int budget, int guests,
                          int moveIn, int social, int food, int smoking, int workFromHome, int pets) {

        /** Fails fast if someone edits the weights and they no longer sum to 100. */
        public int total() {
            return sleep + cleanliness + area + budget + guests + moveIn
                    + social + food + smoking + workFromHome + pets;
        }
    }
}
