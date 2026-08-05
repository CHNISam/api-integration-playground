package dev.sam.oauth;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "providerUserId"}))
public class OAuthUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String provider;
    private String providerUserId;
    private String login;
    private String displayName;
    private String avatarUrl;
    private Instant lastLoginAt;

    protected OAuthUser() {}

    public OAuthUser(String provider, String providerUserId, String login, String displayName, String avatarUrl, Instant lastLoginAt) {
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.login = login;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.lastLoginAt = lastLoginAt;
    }

    public void updateProfile(String login, String displayName, String avatarUrl, Instant lastLoginAt) {
        this.login = login;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.lastLoginAt = lastLoginAt;
    }

    public Long getId() { return id; }
    public String getProvider() { return provider; }
    public String getProviderUserId() { return providerUserId; }
    public String getLogin() { return login; }
    public String getDisplayName() { return displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public Instant getLastLoginAt() { return lastLoginAt; }
}
