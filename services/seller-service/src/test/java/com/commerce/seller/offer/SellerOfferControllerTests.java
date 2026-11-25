package com.commerce.seller.offer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.commerce.seller.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

@WebMvcTest(SellerOfferController.class)
@Import({SecurityConfiguration.class, SellerOfferExceptionHandler.class})
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
class SellerOfferControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SellerOfferService offerService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void activeOffersCanBeReadWithoutAuthentication() throws Exception {
        when(offerService.listPublishedOffers("product-1", 0, 20))
                .thenReturn(new OfferPageResponse(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/v1/seller/offers").param("productId", "product-1"))
                .andExpect(status().isOk());

        verify(offerService).listPublishedOffers("product-1", 0, 20);
    }

    @Test
    void creatingOfferRequiresSellerRole() throws Exception {
        mockMvc.perform(post("/api/v1/seller/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"product-1","priceAmountMinor":12500,"currencyCode":"TRY"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonSellerRoleCannotCreateOffer() throws Exception {
        mockMvc.perform(post("/api/v1/seller/offers")
                        .with(jwt().authorities(() -> "ROLE_buyer"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"product-1","priceAmountMinor":12500,"currencyCode":"TRY"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void sellerCanCreateOfferForAuthenticatedSubject() throws Exception {
        mockMvc.perform(post("/api/v1/seller/offers")
                        .with(jwt().jwt(jwt -> jwt.subject("seller-1"))
                                .authorities(() -> "ROLE_seller"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"product-1","priceAmountMinor":12500,"currencyCode":"TRY"}
                                """))
                .andExpect(status().isCreated());

        verify(offerService).createOffer(
                "seller-1",
                new CreateOfferRequest("product-1", 12500, "TRY"));
    }

    @Test
    void unavailableCatalogProductReturnsNotFound() throws Exception {
        when(offerService.createOffer("seller-1", new CreateOfferRequest("product-1", 12500, "TRY")))
                .thenThrow(new CatalogProductNotFoundException());

        mockMvc.perform(post("/api/v1/seller/offers")
                        .with(jwt().jwt(jwt -> jwt.subject("seller-1"))
                                .authorities(() -> "ROLE_seller"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"product-1","priceAmountMinor":12500,"currencyCode":"TRY"}
                                """))
                .andExpect(status().isNotFound());
    }
}
