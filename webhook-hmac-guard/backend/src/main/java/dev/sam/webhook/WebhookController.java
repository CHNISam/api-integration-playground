package dev.sam.webhook;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
public class WebhookController {
    private final HmacSignatureVerifier signatures;
    private final TimestampWindow timestamps;
    private final WebhookProcessor processor;
    private final DeliveryAttemptRepository attempts;

    public WebhookController(HmacSignatureVerifier signatures, TimestampWindow timestamps,
                             WebhookProcessor processor, DeliveryAttemptRepository attempts) {
        this.signatures = signatures;
        this.timestamps = timestamps;
        this.processor = processor;
        this.attempts = attempts;
    }

    @PostMapping(path = "/webhook/receive", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> receive(
            @RequestHeader(value = "X-Signature-256", required = false) String signature,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Event-Id", required = false) String eventId,
            @RequestBody String rawBody,
            HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (eventId == null || eventId.isBlank()) {
            processor.reject(null, rawBody, ip, "missing event id");
            return ResponseEntity.badRequest().body(Map.of("status", "rejected", "reason", "missing event id"));
        }
        if (!timestamps.accepts(timestamp)) {
            processor.reject(eventId, rawBody, ip, "timestamp outside replay window");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "rejected", "reason", "expired or invalid timestamp"));
        }
        SignatureMatch match = signatures.verify(signature, timestamp, rawBody);
        if (match == SignatureMatch.INVALID) {
            processor.reject(eventId, rawBody, ip, "invalid or missing signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "rejected", "reason", "invalid signature"));
        }
        DeliveryStatus status = processor.accept(eventId, rawBody, ip, match);
        return ResponseEntity.ok(Map.of("status", status.name().toLowerCase(), "eventId", eventId));
    }

    @GetMapping("/api/events")
    public List<DeliveryView> recent() {
        return attempts.findTop50ByOrderByReceivedAtDesc().stream().map(DeliveryView::from).toList();
    }

    public record DeliveryView(Long id, String eventId, DeliveryStatus status, String reason, String sourceIp,
                               String bodySha256, String keyVersion, java.time.Instant receivedAt) {
        static DeliveryView from(DeliveryAttempt item) {
            return new DeliveryView(item.getId(), item.getEventId(), item.getStatus(), item.getReason(), item.getSourceIp(),
                    item.getBodySha256(), item.getKeyVersion(), item.getReceivedAt());
        }
    }
}
