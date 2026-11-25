package com.commerce.seller.offer;

public class CatalogDependencyUnavailableException extends RuntimeException {

    public CatalogDependencyUnavailableException(Throwable cause) {
        super("Catalog service is unavailable", cause);
    }
}
