package dev.sam.capi;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record DeliveryResult(Long id, String eventId, BigDecimal value, String currency,
                             DeliveryStatus status, int httpStatus, String response,
                             Instant attemptedAt, Map<String, Object> payload) {
    static DeliveryResult from(PurchaseEvent event, Map<String, Object> payload) {
        return new DeliveryResult(event.getId(), event.getEventId(), event.getPurchaseValue(),
                event.getCurrency(), event.getStatus(), event.getHttpStatus(),
                event.getResponseSummary(), event.getLastAttemptAt(), payload);
    }
}
