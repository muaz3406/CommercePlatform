package com.commerce.catalog.product;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse create(CreateProductRequest request) {
        Instant now = Instant.now();
        Product product = new Product(
                null,
                request.name().trim(),
                request.brand().trim(),
                request.categoryId().trim(),
                normalizeGtin(request.gtin()),
                request.attributes() == null ? Map.of() : new LinkedHashMap<>(request.attributes()),
                ProductStatus.DRAFT,
                now,
                now,
                null);
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse getPublished(String id) {
        Product product = productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductResponse.from(product);
    }

    public ProductPageResponse listPublished(int page, int size) {
        Page<Product> products = productRepository.findAllByStatus(
                ProductStatus.ACTIVE,
                PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), Sort.by(Sort.Direction.ASC, "name")));
        return ProductPageResponse.from(products);
    }

    public ProductResponse publish(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (product.status() == ProductStatus.ACTIVE) {
            return ProductResponse.from(product);
        }

        Product published = new Product(
                product.id(),
                product.name(),
                product.brand(),
                product.categoryId(),
                product.gtin(),
                product.attributes(),
                ProductStatus.ACTIVE,
                product.createdAt(),
                Instant.now(),
                product.version());
        return ProductResponse.from(productRepository.save(published));
    }

    private String normalizeGtin(String gtin) {
        return gtin == null || gtin.isBlank() ? null : gtin.trim();
    }
}
