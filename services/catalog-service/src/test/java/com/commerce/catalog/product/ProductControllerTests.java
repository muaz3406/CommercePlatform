package com.commerce.catalog.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;

import com.commerce.catalog.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
class ProductControllerTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ProductService productService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void publishedProductsCanBeReadWithoutAuthentication() throws Exception {
        when(productService.getPublished("product-1")).thenReturn(productResponse());

        mockMvc.perform(get("/api/v1/products/product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("product-1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void productCreationRequiresCatalogManagerRole() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Phone","brand":"Example","categoryId":"electronics"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sellerCannotCreateCanonicalProduct() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .with(jwt().authorities(() -> "ROLE_seller"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Phone","brand":"Example","categoryId":"electronics"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void catalogManagerCanCreateDraftProduct() throws Exception {
        when(productService.create(any(CreateProductRequest.class))).thenReturn(productResponse(ProductStatus.DRAFT));

        mockMvc.perform(post("/api/v1/products")
                        .with(jwt().authorities(() -> "ROLE_catalog-manager"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Phone","brand":"Example","categoryId":"electronics"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(productService).create(any(CreateProductRequest.class));
    }

    @Test
    void invalidGtinIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .with(jwt().authorities(() -> "ROLE_catalog-manager"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Phone","brand":"Example","categoryId":"electronics","gtin":"not-a-gtin"}
                                """))
                .andExpect(status().isBadRequest());
    }

    private ProductResponse productResponse() {
        return productResponse(ProductStatus.ACTIVE);
    }

    private ProductResponse productResponse(ProductStatus status) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new ProductResponse(
                "product-1",
                "Phone",
                "Example",
                "electronics",
                null,
                Map.of(),
                status,
                now,
                now);
    }
}
