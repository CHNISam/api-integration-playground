package dev.sam.capi;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetaCapiService {
    private final PurchaseEventRepository repository;
    private final MetaGateway gateway;
    private final MetaEventFactory events;
    private final CustomerDataHasher hasher = new CustomerDataHasher();

    public MetaCapiService(PurchaseEventRepository repository, MetaGateway gateway, MetaEventFactory events) {
        this.repository = repository;
        this.gateway = gateway;
        this.events = events;
    }

    @Transactional
    public DeliveryResult send(PurchaseRequest request) {
        Map<String, Object> payload = events.purchase(request);
        var entity = repository.save(new PurchaseEvent(request.orderId(), request.value(), request.currency(),
                hasher.email(request.email()), hasher.phone(request.phone()), Instant.now()));
        deliverIfConfigured(entity, payload);
        return DeliveryResult.from(entity, payload);
    }

    @Transactional
    public DeliveryResult resend(long id) {
        PurchaseEvent entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Purchase event not found"));
        Map<String, Object> payload = events.purchaseFromHashes(entity.getEventId(), entity.getPurchaseValue(),
                entity.getCurrency(), entity.getEmailHash(), entity.getPhoneHash());
        deliverIfConfigured(entity, payload);
        return DeliveryResult.from(entity, payload);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResult> recent() {
        return repository.findTop20ByOrderByLastAttemptAtDesc().stream()
                .map(event -> DeliveryResult.from(event, Map.of()))
                .toList();
    }

    public boolean configured() { return gateway.configured(); }

    private void deliverIfConfigured(PurchaseEvent entity, Map<String, Object> payload) {
        if (gateway.configured()) entity.record(gateway.send(payload), Instant.now());
    }
}
