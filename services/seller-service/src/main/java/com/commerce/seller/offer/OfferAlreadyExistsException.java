package com.commerce.seller.offer;

public class OfferAlreadyExistsException extends RuntimeException {

    public OfferAlreadyExistsException() {
        super("Seller already has an offer for this product");
    }
}
