package dev.sam.oauth;

import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OAuthController {
    private static final String USER_ID = "authenticated-user-id";
    private static final String DEMO_TOKEN = "refresh-lab-token";
    private final OAuthStateService stateService;
    private final GitHubOAuthClient gitHub;
    private final OAuthUserRepository users;
    private final String frontendUrl;
    private final Clock clock = Clock.systemUTC();

    public OAuthController(OAuthStateService stateService, GitHubOAuthClient gitHub, OAuthUserRepository users,
                           @Value("${app.frontend-url}") String frontendUrl) {
        this.stateService = stateService;
        this.gitHub = gitHub;
        this.users = users;
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/oauth/github/start")
    public ResponseEntity<Void> start(HttpSession session) {
        if (!gitHub.configured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(gitHub.authorizationUri(stateService.issue(session))).build();
    }

    @GetMapping("/oauth/github/callback")
    @Transactional
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpSession session) {
        if (error != null) {
            return redirect("/?oauth=denied");
        }
        if (code == null || !stateService.validateAndConsume(session, state)) {
            return redirect("/?oauth=invalid_state");
        }
        try {
            var identity = gitHub.authenticate(code);
            var user = users.findByProviderAndProviderUserId("github", identity.id())
                    .orElseGet(() -> new OAuthUser("github", identity.id(), identity.login(), identity.name(), identity.avatarUrl(), clock.instant()));
            user.updateProfile(identity.login(), identity.name(), identity.avatarUrl(), clock.instant());
            session.setAttribute(USER_ID, users.save(user).getId());
            return redirect("/?oauth=success");
        } catch (RuntimeException failure) {
            return redirect("/?oauth=provider_error");
        }
    }

    @GetMapping("/session")
    public Map<String, Object> session(HttpSession session) {
        Object userId = session.getAttribute(USER_ID);
        if (!(userId instanceof Long id)) {
            return Map.of("authenticated", false, "providerConfigured", gitHub.configured());
        }
        return users.findById(id)
                .<Map<String, Object>>map(user -> Map.of(
                        "authenticated", true,
                        "providerConfigured", gitHub.configured(),
                        "login", user.getLogin(),
                        "displayName", user.getDisplayName(),
                        "avatarUrl", user.getAvatarUrl() == null ? "" : user.getAvatarUrl()))
                .orElseGet(() -> Map.of("authenticated", false, "providerConfigured", gitHub.configured()));
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @PostMapping("/token-lab/seed-expired")
    public Map<String, Object> seedExpired(HttpSession session) {
        session.setAttribute(DEMO_TOKEN, new DemoToken("expired-access", "local-refresh-token", Instant.EPOCH));
        return Map.of("status", "expired token loaded", "next", "Call /api/token-lab/resource");
    }

    @GetMapping("/token-lab/resource")
    public Map<String, Object> protectedResource(HttpSession session) {
        Object raw = session.getAttribute(DEMO_TOKEN);
        if (!(raw instanceof DemoToken current)) {
            return Map.of("status", "missing_token", "refreshed", false);
        }
        var service = new RefreshableTokenService(
                refresh -> new DemoToken("refreshed-" + refresh.substring(0, 5), refresh, clock.instant().plusSeconds(120)),
                clock);
        boolean expired = !current.expiresAt().isAfter(clock.instant());
        DemoToken usable = service.ensureUsable(current);
        session.setAttribute(DEMO_TOKEN, usable);
        return Map.of("status", "resource_returned", "refreshed", expired, "accessTokenPreview", usable.accessToken().substring(0, 10) + "…");
    }

    private ResponseEntity<Void> redirect(String path) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(frontendUrl + path)).build();
    }
}
