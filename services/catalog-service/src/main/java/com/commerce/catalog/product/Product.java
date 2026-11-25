package com.commerce.catalog.product;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("products")
@CompoundIndex(name = "product_status_category_idx", def = "{'status': 1, 'categoryId': 1}")
public record Product(
        @Id String id,
        String name,
        String brand,
        String categoryId,
        String gtin,
        Map<String, Object> attributes,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt,
        @Version Long version) {
}
