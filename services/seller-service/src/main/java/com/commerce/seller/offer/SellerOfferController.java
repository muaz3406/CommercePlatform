package com.commerce.seller.offer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/seller/offers")
public class SellerOfferController {

    private final SellerOfferService offerService;

    public SellerOfferController(SellerOfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public OfferPageResponse list(
            @RequestParam @NotBlank @Size(max = 64) String productId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return offerService.listPublishedOffers(productId, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOfferRequest request) {
        return offerService.createOffer(jwt.getSubject(), request);
    }
}
