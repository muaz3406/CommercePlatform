package com.commerce.catalog.product;

import java.util.List;

import org.springframework.data.domain.Page;

public record ProductPageResponse(
        List<ProductResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    static ProductPageResponse from(Page<Product> products) {
        return new ProductPageResponse(
                products.getContent().stream().map(ProductResponse::from).toList(),
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages());
    }
}
