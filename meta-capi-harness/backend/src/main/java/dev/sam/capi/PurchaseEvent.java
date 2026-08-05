package dev.sam.capi;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class PurchaseEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventId;
    private BigDecimal purchaseValue;
    private String currency;
    private String emailHash;
    private String phoneHash;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    private int httpStatus;
    @Column(length = 1000)
    private String responseSummary;
    private Instant createdAt;
    private Instant lastAttemptAt;

    protected PurchaseEvent() {}

    public PurchaseEvent(String eventId, BigDecimal purchaseValue, String currency,
                         String emailHash, String phoneHash, Instant now) {
        this.eventId = eventId;
        this.purchaseValue = purchaseValue;
        this.currency = currency.toUpperCase();
        this.emailHash = emailHash;
        this.phoneHash = phoneHash;
        this.status = DeliveryStatus.CONFIG_REQUIRED;
        this.createdAt = now;
        this.lastAttemptAt = now;
        this.responseSummary = "Meta credentials are not configured; no request was sent";
    }

    public void record(MetaGatewayResponse response, Instant now) {
        status = response.accepted() ? DeliveryStatus.SENT : DeliveryStatus.FAILED;
        httpStatus = response.httpStatus();
        responseSummary = response.summary();
        lastAttemptAt = now;
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public BigDecimal getPurchaseValue() { return purchaseValue; }
    public String getCurrency() { return currency; }
    public String getEmailHash() { return emailHash; }
    public String getPhoneHash() { return phoneHash; }
    public DeliveryStatus getStatus() { return status; }
    public int getHttpStatus() { return httpStatus; }
    public String getResponseSummary() { return responseSummary; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
}
