package com.commerce.seller.offer;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerOfferRepository extends JpaRepository<SellerOffer, UUID> {

    boolean existsBySellerIdAndProductId(String sellerId, String productId);
}
