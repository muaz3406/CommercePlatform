package com.commerce.catalog.product;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 120) String brand,
        @NotBlank @Size(max = 100) String categoryId,
        @Pattern(regexp = "(?:\\d{8}|\\d{12,14})") String gtin,
        @Size(max = 100) Map<String, Object> attributes) {
}
