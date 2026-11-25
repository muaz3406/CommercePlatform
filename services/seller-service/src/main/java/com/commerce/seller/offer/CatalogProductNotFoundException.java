package com.commerce.seller.offer;

public class CatalogProductNotFoundException extends RuntimeException {

    public CatalogProductNotFoundException() {
        super("Published catalog product not found");
    }
}
