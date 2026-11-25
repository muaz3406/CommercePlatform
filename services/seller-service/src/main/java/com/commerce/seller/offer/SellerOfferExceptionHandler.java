package com.commerce.seller.offer;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SellerOfferExceptionHandler {

    @ExceptionHandler(OfferAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflict(OfferAlreadyExistsException exception) {
        return Map.of("error", "offer_already_exists", "message", exception.getMessage());
    }

    @ExceptionHandler(CatalogProductNotFoundException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleUnknownProduct(CatalogProductNotFoundException exception) {
        return Map.of("error", "product_not_available", "message", exception.getMessage());
    }

    @ExceptionHandler(CatalogDependencyUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleCatalogUnavailable(CatalogDependencyUnavailableException exception) {
        return Map.of("error", "catalog_unavailable", "message", exception.getMessage());
    }
}
