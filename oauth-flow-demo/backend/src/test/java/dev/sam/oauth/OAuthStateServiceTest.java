package dev.sam.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class OAuthStateServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-05T00:00:00Z");

    @Test
    void generatedStateCanBeConsumedExactlyOnce() {
        var service = new OAuthStateService(Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofMinutes(10));
        var session = new MockHttpSession();

        String state = service.issue(session);

        assertThat(service.validateAndConsume(session, state)).isTrue();
        assertThat(service.validateAndConsume(session, state)).isFalse();
    }

    @Test
    void rejectsMissingOrTamperedState() {
        var service = new OAuthStateService(Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofMinutes(10));
        var session = new MockHttpSession();
        service.issue(session);

        assertThat(service.validateAndConsume(session, null)).isFalse();
        assertThat(service.validateAndConsume(session, "attacker-controlled-state")).isFalse();
    }

    @Test
    void rejectsExpiredState() {
        var issuingClock = Clock.fixed(NOW, ZoneOffset.UTC);
        var session = new MockHttpSession();
        String state = new OAuthStateService(issuingClock, Duration.ofMinutes(5)).issue(session);
        var lateClock = Clock.fixed(NOW.plus(Duration.ofMinutes(6)), ZoneOffset.UTC);

        assertThat(new OAuthStateService(lateClock, Duration.ofMinutes(5)).validateAndConsume(session, state)).isFalse();
    }
}
