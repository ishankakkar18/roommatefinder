package in.flatmate.match;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Expires stale PENDING requests. Runs every 10 minutes. */
@Component
public class MatchRequestExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(MatchRequestExpiryJob.class);

    private final MatchRequestRepository requests;

    public MatchRequestExpiryJob(MatchRequestRepository requests) {
        this.requests = requests;
    }

    @Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT1M")
    @Transactional
    public void expireStaleRequests() {
        int expired = requests.expirePending(
                RequestStatus.PENDING, RequestStatus.EXPIRED, LocalDateTime.now());
        if (expired > 0) {
            log.info("Expired {} stale match requests", expired);
        }
    }
}
