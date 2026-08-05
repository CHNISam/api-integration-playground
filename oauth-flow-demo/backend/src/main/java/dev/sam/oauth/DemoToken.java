package dev.sam.oauth;

import java.time.Instant;

public record DemoToken(String accessToken, String refreshToken, Instant expiresAt) implements java.io.Serializable {}
