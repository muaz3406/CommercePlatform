package com.commerce.seller.offer;

import java.util.List;

import org.springframework.data.domain.Page;

public record OfferPageResponse(
        List<OfferResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    static OfferPageResponse from(Page<SellerOffer> offers) {
        return new OfferPageResponse(
                offers.getContent().stream().map(OfferResponse::from).toList(),
                offers.getNumber(),
                offers.getSize(),
                offers.getTotalElements(),
                offers.getTotalPages());
    }
}
