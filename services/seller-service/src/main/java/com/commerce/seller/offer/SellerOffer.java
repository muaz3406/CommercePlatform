package com.commerce.seller.offer;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "seller_offer",
        uniqueConstraints = @UniqueConstraint(
                name = "seller_offer_seller_product_uq",
                columnNames = {"seller_id", "product_id"}))
public class SellerOffer {

    @Id
    private UUID id;

    @Column(name = "seller_id", nullable = false, length = 128)
    private String sellerId;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "price_amount_minor", nullable = false)
    private long priceAmountMinor;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OfferStatus status;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SellerOffer() {
    }

    public SellerOffer(
            String sellerId,
            String productId,
            long priceAmountMinor,
            String currencyCode) {
        this.id = UUID.randomUUID();
        this.sellerId = sellerId;
        this.productId = productId;
        this.priceAmountMinor = priceAmountMinor;
        this.currencyCode = currencyCode;
        this.status = OfferStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getProductId() {
        return productId;
    }

    public long getPriceAmountMinor() {
        return priceAmountMinor;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
