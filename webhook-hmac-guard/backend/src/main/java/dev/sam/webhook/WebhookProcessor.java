package dev.sam.webhook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookProcessor {
    private final ProcessedEventRepository processedEvents;
    private final DeliveryAttemptRepository attempts;
    private final Clock clock;

    public WebhookProcessor(ProcessedEventRepository processedEvents, DeliveryAttemptRepository attempts) {
        this(processedEvents, attempts, Clock.systemUTC());
    }

    WebhookProcessor(ProcessedEventRepository processedEvents, DeliveryAttemptRepository attempts, Clock clock) {
        this.processedEvents = processedEvents;
        this.attempts = attempts;
        this.clock = clock;
    }

    @Transactional
    public synchronized DeliveryStatus accept(String eventId, String rawBody, String sourceIp, SignatureMatch key) {
        String hash = sha256(rawBody);
        if (processedEvents.existsByEventId(eventId)) {
            attempts.save(new DeliveryAttempt(eventId, DeliveryStatus.DUPLICATE, "duplicate, skipped", sourceIp, hash, key.name(), clock.instant()));
            return DeliveryStatus.DUPLICATE;
        }
        processedEvents.save(new ProcessedEvent(eventId, hash, clock.instant()));
        attempts.save(new DeliveryAttempt(eventId, DeliveryStatus.PROCESSED, "accepted and processed", sourceIp, hash, key.name(), clock.instant()));
        return DeliveryStatus.PROCESSED;
    }

    public void reject(String eventId, String rawBody, String sourceIp, String reason) {
        attempts.save(new DeliveryAttempt(eventId, DeliveryStatus.REJECTED, reason, sourceIp, sha256(rawBody), null, clock.instant()));
    }

    static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
