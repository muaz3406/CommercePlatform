package com.commerce.seller.offer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOfferRequest(
        @NotBlank @Size(max = 64) String productId,
        @Positive long priceAmountMinor,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currencyCode) {
}
