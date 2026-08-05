package dev.sam.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class WebhookProcessorTest {
    private final ProcessedEventRepository processed = mock(ProcessedEventRepository.class);
    private final DeliveryAttemptRepository attempts = mock(DeliveryAttemptRepository.class);
    private final WebhookProcessor processor = new WebhookProcessor(processed, attempts,
            Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void processesNewEventOnce() {
        when(processed.existsByEventId("evt-1")).thenReturn(false);
        assertThat(processor.accept("evt-1", "{}", "127.0.0.1", SignatureMatch.ACTIVE)).isEqualTo(DeliveryStatus.PROCESSED);
        verify(processed).save(org.mockito.ArgumentMatchers.any(ProcessedEvent.class));
        verify(attempts).save(org.mockito.ArgumentMatchers.any(DeliveryAttempt.class));
    }

    @Test
    void duplicateReturnsSuccessWithoutProcessingAgain() {
        when(processed.existsByEventId("evt-1")).thenReturn(true);
        assertThat(processor.accept("evt-1", "{}", "127.0.0.1", SignatureMatch.ACTIVE)).isEqualTo(DeliveryStatus.DUPLICATE);
        verify(processed, never()).save(org.mockito.ArgumentMatchers.any(ProcessedEvent.class));
        verify(attempts).save(org.mockito.ArgumentMatchers.any(DeliveryAttempt.class));
    }
}
