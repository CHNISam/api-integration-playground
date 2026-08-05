package dev.sam.capi;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record PurchaseRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9._:-]{3,100}") String orderId,
        @DecimalMin(value = "0.01") BigDecimal value,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency,
        @NotBlank @Email String email,
        @NotBlank String phone) {
}
