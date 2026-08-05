package dev.sam.oauth;

@FunctionalInterface
public interface TokenRefresher {
    DemoToken refresh(String refreshToken);
}
