package dev.sam.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TimestampWindowTest {
    private final TimestampWindow window = new TimestampWindow(
            Clock.fixed(Instant.ofEpochSecond(1_722_816_000), ZoneOffset.UTC), Duration.ofMinutes(5));

    @Test void acceptsTimestampInsideWindow() { assertThat(window.accepts("1722815880")).isTrue(); }
    @Test void rejectsExpiredFutureOrMalformedTimestamp() {
        assertThat(window.accepts("1722815600")).isFalse();
        assertThat(window.accepts("1722816400")).isFalse();
        assertThat(window.accepts("not-a-number")).isFalse();
        assertThat(window.accepts(null)).isFalse();
    }
}
