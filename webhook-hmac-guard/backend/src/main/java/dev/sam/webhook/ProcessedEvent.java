package dev.sam.webhook;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "processed_events", uniqueConstraints = @UniqueConstraint(columnNames = "eventId"))
public class ProcessedEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventId;
    private String bodySha256;
    private Instant processedAt;

    protected ProcessedEvent() {}
    public ProcessedEvent(String eventId, String bodySha256, Instant processedAt) {
        this.eventId = eventId;
        this.bodySha256 = bodySha256;
        this.processedAt = processedAt;
    }
}
