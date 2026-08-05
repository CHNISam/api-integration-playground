package dev.sam.capi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomerDataHasherTest {
    private final CustomerDataHasher hasher = new CustomerDataHasher();

    @Test
    void normalizesAndHashesEmail() {
        assertThat(hasher.email("  Example@Email.COM "))
                .isEqualTo("36e96648c5410d00a7da7206c01237139f950bed21d8c729aae019dbe07964e7");
    }

    @Test
    void normalizesAndHashesPhone() {
        assertThat(hasher.phone("+1 (415) 555-2671"))
                .isEqualTo("758fbf68945f21c416814c539ab578876c8d98fb69e6da692def92cd52417fe0");
    }
}
