package dev.sam.oauth;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class GitHubOAuthClient {
    private final RestClient restClient = RestClient.create();
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GitHubOAuthClient(
            @Value("${app.oauth.github.client-id:}") String clientId,
            @Value("${app.oauth.github.client-secret:}") String clientSecret,
            @Value("${app.oauth.github.redirect-uri}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public boolean configured() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    public URI authorizationUri(String state) {
        String query = "client_id=" + encode(clientId) + "&redirect_uri=" + encode(redirectUri)
                + "&scope=read:user%20user:email&state=" + encode(state);
        return URI.create("https://github.com/login/oauth/authorize?" + query);
    }

    public GitHubIdentity authenticate(String code) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        @SuppressWarnings("unchecked")
        Map<String, Object> token = restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(Map.class);
        if (token == null || token.get("access_token") == null) {
            throw new IllegalStateException("GitHub did not return an access token");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = restClient.get()
                .uri("https://api.github.com/user")
                .header("Authorization", "Bearer " + token.get("access_token"))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .retrieve()
                .body(Map.class);
        if (profile == null || profile.get("id") == null || profile.get("login") == null) {
            throw new IllegalStateException("GitHub profile response was incomplete");
        }
        return new GitHubIdentity(
                String.valueOf(profile.get("id")),
                String.valueOf(profile.get("login")),
                profile.get("name") == null ? String.valueOf(profile.get("login")) : String.valueOf(profile.get("name")),
                profile.get("avatar_url") == null ? null : String.valueOf(profile.get("avatar_url")));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record GitHubIdentity(String id, String login, String name, String avatarUrl) {}
}
