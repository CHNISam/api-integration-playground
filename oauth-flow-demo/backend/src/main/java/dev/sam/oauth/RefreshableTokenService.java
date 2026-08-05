package dev.sam.oauth;

import java.time.Clock;

public class RefreshableTokenService {
    private final TokenRefresher tokenRefresher;
    private final Clock clock;

    public RefreshableTokenService(TokenRefresher tokenRefresher, Clock clock) {
        this.tokenRefresher = tokenRefresher;
        this.clock = clock;
    }

    public DemoToken ensureUsable(DemoToken current) {
        if (current.expiresAt().isAfter(clock.instant())) {
            return current;
        }
        return tokenRefresher.refresh(current.refreshToken());
    }
}
