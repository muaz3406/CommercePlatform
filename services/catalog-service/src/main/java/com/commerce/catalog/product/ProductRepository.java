package com.commerce.catalog.product;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findByIdAndStatus(String id, ProductStatus status);

    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);
}
