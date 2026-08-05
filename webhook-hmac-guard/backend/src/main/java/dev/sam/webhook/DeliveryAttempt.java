package dev.sam.webhook;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "delivery_attempts", indexes = @Index(name = "idx_delivery_received", columnList = "receivedAt"))
public class DeliveryAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventId;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    private String reason;
    private String sourceIp;
    private String bodySha256;
    private String keyVersion;
    private Instant receivedAt;

    protected DeliveryAttempt() {}

    public DeliveryAttempt(String eventId, DeliveryStatus status, String reason, String sourceIp, String bodySha256, String keyVersion, Instant receivedAt) {
        this.eventId = eventId;
        this.status = status;
        this.reason = reason;
        this.sourceIp = sourceIp;
        this.bodySha256 = bodySha256;
        this.keyVersion = keyVersion;
        this.receivedAt = receivedAt;
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public DeliveryStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public String getSourceIp() { return sourceIp; }
    public String getBodySha256() { return bodySha256; }
    public String getKeyVersion() { return keyVersion; }
    public Instant getReceivedAt() { return receivedAt; }
}
