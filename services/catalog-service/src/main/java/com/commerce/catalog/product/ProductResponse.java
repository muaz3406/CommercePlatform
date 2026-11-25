package com.commerce.catalog.product;

import java.time.Instant;
import java.util.Map;

public record ProductResponse(
        String id,
        String name,
        String brand,
        String categoryId,
        String gtin,
        Map<String, Object> attributes,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt) {

    static ProductResponse from(Product product) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.brand(),
                product.categoryId(),
                product.gtin(),
                product.attributes(),
                product.status(),
                product.createdAt(),
                product.updatedAt());
    }
}
