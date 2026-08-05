package dev.sam.capi;

import java.time.Clock;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class MetaEventFactory {
    private final CustomerDataHasher hasher;
    private final Clock clock;

    public MetaEventFactory(CustomerDataHasher hasher, Clock clock) {
        this.hasher = hasher;
        this.clock = clock;
    }

    public Map<String, Object> purchase(PurchaseRequest request) {
        return purchaseFromHashes(request.orderId(), request.value(), request.currency(),
                hasher.email(request.email()), hasher.phone(request.phone()));
    }

    public Map<String, Object> purchaseFromHashes(String eventId, java.math.BigDecimal value,
                                                   String currency, String emailHash, String phoneHash) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("event_name", "Purchase");
        event.put("event_time", clock.instant().getEpochSecond());
        event.put("event_id", eventId);
        event.put("action_source", "website");
        event.put("user_data", Map.of("em", List.of(emailHash), "ph", List.of(phoneHash)));
        event.put("custom_data", Map.of("value", value, "currency", currency.toUpperCase()));
        return event;
    }
}
