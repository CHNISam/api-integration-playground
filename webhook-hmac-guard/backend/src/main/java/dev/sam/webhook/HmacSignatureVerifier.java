package dev.sam.webhook;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HmacSignatureVerifier {
    private final String activeSecret;
    private final String previousSecret;

    public HmacSignatureVerifier(
            @Value("${app.webhook.active-secret}") String activeSecret,
            @Value("${app.webhook.previous-secret:}") String previousSecret) {
        this.activeSecret = activeSecret;
        this.previousSecret = previousSecret;
    }

    public SignatureMatch verify(String supplied, String timestamp, String rawBody) {
        if (supplied == null || !supplied.startsWith("sha256=") || timestamp == null) {
            return SignatureMatch.INVALID;
        }
        byte[] suppliedBytes;
        try {
            suppliedBytes = HexFormat.of().parseHex(supplied.substring(7));
        } catch (IllegalArgumentException invalidHex) {
            return SignatureMatch.INVALID;
        }
        if (matches(activeSecret, timestamp, rawBody, suppliedBytes)) return SignatureMatch.ACTIVE;
        if (!previousSecret.isBlank() && matches(previousSecret, timestamp, rawBody, suppliedBytes)) return SignatureMatch.PREVIOUS;
        return SignatureMatch.INVALID;
    }

    String sign(String secret, String timestamp, String rawBody) {
        return "sha256=" + HexFormat.of().formatHex(mac(secret, timestamp + "." + rawBody));
    }

    private boolean matches(String secret, String timestamp, String rawBody, byte[] supplied) {
        return MessageDigest.isEqual(mac(secret, timestamp + "." + rawBody), supplied);
    }

    private byte[] mac(String secret, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException impossible) {
            throw new IllegalStateException("HmacSHA256 is unavailable", impossible);
        }
    }
}
