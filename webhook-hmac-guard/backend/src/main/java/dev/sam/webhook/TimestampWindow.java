package dev.sam.webhook;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TimestampWindow {
    private final Clock clock;
    private final Duration tolerance;

    public TimestampWindow(@Value("${app.webhook.tolerance}") Duration tolerance) {
        this(Clock.systemUTC(), tolerance);
    }

    TimestampWindow(Clock clock, Duration tolerance) {
        this.clock = clock;
        this.tolerance = tolerance;
    }

    public boolean accepts(String rawTimestamp) {
        if (rawTimestamp == null) return false;
        try {
            Instant timestamp = Instant.ofEpochSecond(Long.parseLong(rawTimestamp));
            Duration distance = Duration.between(timestamp, clock.instant()).abs();
            return distance.compareTo(tolerance) <= 0;
        } catch (NumberFormatException invalid) {
            return false;
        }
    }
}
