package dev.sam.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HmacSignatureVerifierTest {
    private final HmacSignatureVerifier verifier = new HmacSignatureVerifier("active-secret", "previous-secret");

    @Test
    void acceptsCorrectActiveSignature() {
        String signature = verifier.sign("active-secret", "1722816000", "{\"event\":\"paid\"}");
        assertThat(verifier.verify(signature, "1722816000", "{\"event\":\"paid\"}"))
                .isEqualTo(SignatureMatch.ACTIVE);
    }

    @Test
    void acceptsPreviousKeyDuringRotation() {
        String signature = verifier.sign("previous-secret", "1722816000", "{\"event\":\"paid\"}");
        assertThat(verifier.verify(signature, "1722816000", "{\"event\":\"paid\"}"))
                .isEqualTo(SignatureMatch.PREVIOUS);
    }

    @Test
    void rejectsWrongMissingOrBodyMismatchedSignature() {
        String signature = verifier.sign("active-secret", "1722816000", "{\"event\":\"paid\"}");
        assertThat(verifier.verify("sha256=deadbeef", "1722816000", "{\"event\":\"paid\"}"))
                .isEqualTo(SignatureMatch.INVALID);
        assertThat(verifier.verify(null, "1722816000", "{\"event\":\"paid\"}"))
                .isEqualTo(SignatureMatch.INVALID);
        assertThat(verifier.verify(signature, "1722816000", "{\"event\":\"refunded\"}"))
                .isEqualTo(SignatureMatch.INVALID);
    }
}
