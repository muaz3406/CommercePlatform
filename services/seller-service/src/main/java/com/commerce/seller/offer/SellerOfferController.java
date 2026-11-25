package com.commerce.seller.offer;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/offers")
public class SellerOfferController {

    private final SellerOfferService offerService;

    public SellerOfferController(SellerOfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOfferRequest request) {
        return offerService.createOffer(jwt.getSubject(), request);
    }
}
