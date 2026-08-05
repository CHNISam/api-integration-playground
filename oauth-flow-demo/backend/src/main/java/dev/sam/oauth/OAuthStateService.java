package dev.sam.oauth;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class OAuthStateService {
    private static final String SESSION_KEY = OAuthStateService.class.getName() + ".state";
    private final Clock clock;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();

    public OAuthStateService() {
        this(Clock.systemUTC(), Duration.ofMinutes(10));
    }

    OAuthStateService(Clock clock, Duration ttl) {
        this.clock = clock;
        this.ttl = ttl;
    }

    public String issue(HttpSession session) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_KEY, new StoredState(value, clock.instant().plus(ttl)));
        return value;
    }

    public boolean validateAndConsume(HttpSession session, String returnedState) {
        Object raw = session.getAttribute(SESSION_KEY);
        session.removeAttribute(SESSION_KEY);
        if (!(raw instanceof StoredState expected) || returnedState == null || !clock.instant().isBefore(expected.expiresAt())) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.value().getBytes(StandardCharsets.UTF_8),
                returnedState.getBytes(StandardCharsets.UTF_8));
    }

    private record StoredState(String value, Instant expiresAt) implements java.io.Serializable {}
}
