package dev.sam.capi;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MetaEventFactoryTest {
    @Test
    void createsAWebsitePurchaseWithHashedPiiAndStableEventId() {
        var factory = new MetaEventFactory(
                new CustomerDataHasher(),
                Clock.fixed(Instant.ofEpochSecond(1_700_000_000), ZoneOffset.UTC));

        Map<String, Object> event = factory.purchase(new PurchaseRequest(
                "order-1042", new BigDecimal("49.90"), "usd", "buyer@example.com", "+1 415 555 2671"));

        assertThat(event).containsEntry("event_name", "Purchase")
                .containsEntry("event_time", 1_700_000_000L)
                .containsEntry("event_id", "order-1042")
                .containsEntry("action_source", "website");
        @SuppressWarnings("unchecked")
        Map<String, Object> customData = (Map<String, Object>) event.get("custom_data");
        assertThat(customData)
                .containsEntry("currency", "USD")
                .containsEntry("value", new BigDecimal("49.90"));
        assertThat(event.toString()).doesNotContain("buyer@example.com", "+1 415 555 2671");
    }
}
