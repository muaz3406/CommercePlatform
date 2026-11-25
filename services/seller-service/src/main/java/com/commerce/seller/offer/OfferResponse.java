package com.commerce.seller.offer;

import java.time.Instant;
import java.util.UUID;

public record OfferResponse(
        UUID offerId,
        String sellerId,
        String productId,
        long priceAmountMinor,
        String currencyCode,
        OfferStatus status,
        Instant createdAt) {

    static OfferResponse from(SellerOffer offer) {
        return new OfferResponse(
                offer.getId(),
                offer.getSellerId(),
                offer.getProductId(),
                offer.getPriceAmountMinor(),
                offer.getCurrencyCode(),
                offer.getStatus(),
                offer.getCreatedAt());
    }
}
