package dev.sam.capi;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MetaCapiServiceTest {
    private final PurchaseEventRepository repository;

    @Autowired
    MetaCapiServiceTest(PurchaseEventRepository repository) {
        this.repository = repository;
    }

    @Test
    void recordsConfigurationRequiredWithoutClaimingDelivery() {
        var gateway = new FakeGateway(false);
        var service = service(gateway);

        DeliveryResult result = service.send(request());

        assertThat(result.status()).isEqualTo(DeliveryStatus.CONFIG_REQUIRED);
        assertThat(gateway.calls).isZero();
        assertThat(repository.findAll()).singleElement()
                .extracting(PurchaseEvent::getEventId, PurchaseEvent::getStatus)
                .containsExactly("order-1042", DeliveryStatus.CONFIG_REQUIRED);
    }

    @Test
    void sendsThroughConfiguredGatewayAndKeepsStableIdForResend() {
        var gateway = new FakeGateway(true);
        var service = service(gateway);
        DeliveryResult first = service.send(request());

        DeliveryResult resent = service.resend(first.id());

        assertThat(first.status()).isEqualTo(DeliveryStatus.SENT);
        assertThat(resent.eventId()).isEqualTo(first.eventId());
        assertThat(gateway.calls).isEqualTo(2);
    }

    private MetaCapiService service(FakeGateway gateway) {
        return new MetaCapiService(repository, gateway,
                new MetaEventFactory(new CustomerDataHasher(), Clock.fixed(Instant.ofEpochSecond(1_700_000_000), ZoneOffset.UTC)));
    }

    private PurchaseRequest request() {
        return new PurchaseRequest("order-1042", new BigDecimal("49.90"), "USD", "buyer@example.com", "+14155552671");
    }

    private static final class FakeGateway implements MetaGateway {
        private final boolean configured;
        private int calls;

        private FakeGateway(boolean configured) { this.configured = configured; }
        public boolean configured() { return configured; }
        public MetaGatewayResponse send(Map<String, Object> event) {
            calls++;
            return new MetaGatewayResponse(true, 200, "{\"events_received\":1}");
        }
    }
}
