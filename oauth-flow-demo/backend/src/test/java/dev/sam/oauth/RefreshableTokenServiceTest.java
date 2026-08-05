package dev.sam.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RefreshableTokenServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void keepsValidAccessToken() {
        var refreshCalls = new AtomicInteger();
        var service = new RefreshableTokenService(refreshToken -> {
            refreshCalls.incrementAndGet();
            return null;
        }, CLOCK);
        var token = new DemoToken("still-valid", "refresh-1", Instant.parse("2026-08-05T00:05:00Z"));

        var result = service.ensureUsable(token);

        assertThat(result).isSameAs(token);
        assertThat(refreshCalls).hasValue(0);
    }

    @Test
    void refreshesExpiredAccessToken() {
        var service = new RefreshableTokenService(
                refreshToken -> new DemoToken("fresh-access", refreshToken, Instant.parse("2026-08-05T00:05:00Z")),
                CLOCK);
        var expired = new DemoToken("expired-access", "refresh-1", Instant.parse("2026-08-04T23:59:59Z"));

        var result = service.ensureUsable(expired);

        assertThat(result.accessToken()).isEqualTo("fresh-access");
        assertThat(result.refreshToken()).isEqualTo("refresh-1");
    }
}
