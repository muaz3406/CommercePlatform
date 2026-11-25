package com.commerce.seller.offer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.commerce.contracts.catalog.v1.Product;
import com.commerce.seller.catalog.CatalogProductClient;
import io.grpc.Status;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SellerOfferServiceTests {

    @Test
    void createsOfferForPublishedCatalogProductAndUsesAuthenticatedSellerId() {
        SellerOfferRepository repository = Mockito.mock(SellerOfferRepository.class);
        CatalogProductClient catalogClient = Mockito.mock(CatalogProductClient.class);
        when(repository.existsBySellerIdAndProductId("seller-1", "product-1")).thenReturn(false);
        when(catalogClient.getPublishedProduct("product-1")).thenReturn(
                Product.newBuilder().setProductId("product-1").build());
        when(repository.save(any(SellerOffer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SellerOfferService service = new SellerOfferService(repository, catalogClient);

        OfferResponse response = service.createOffer(
                "seller-1",
                new CreateOfferRequest("product-1", 12500, "TRY"));

        assertEquals("seller-1", response.sellerId());
        assertEquals("product-1", response.productId());
        assertEquals(12500, response.priceAmountMinor());
        assertEquals("TRY", response.currencyCode());
        verify(catalogClient).getPublishedProduct("product-1");
    }

    @Test
    void rejectsProductThatIsNotPublishedInCatalog() {
        SellerOfferRepository repository = Mockito.mock(SellerOfferRepository.class);
        CatalogProductClient catalogClient = Mockito.mock(CatalogProductClient.class);
        when(repository.existsBySellerIdAndProductId("seller-1", "product-1")).thenReturn(false);
        when(catalogClient.getPublishedProduct("product-1"))
                .thenThrow(Status.NOT_FOUND.asRuntimeException());
        SellerOfferService service = new SellerOfferService(repository, catalogClient);

        assertThrows(
                CatalogProductNotFoundException.class,
                () -> service.createOffer("seller-1", new CreateOfferRequest("product-1", 12500, "TRY")));
        verify(repository, never()).save(any(SellerOffer.class));
    }

    @Test
    void doesNotCallCatalogWhenSellerAlreadyHasAnOffer() {
        SellerOfferRepository repository = Mockito.mock(SellerOfferRepository.class);
        CatalogProductClient catalogClient = Mockito.mock(CatalogProductClient.class);
        when(repository.existsBySellerIdAndProductId("seller-1", "product-1")).thenReturn(true);
        SellerOfferService service = new SellerOfferService(repository, catalogClient);

        assertThrows(
                OfferAlreadyExistsException.class,
                () -> service.createOffer("seller-1", new CreateOfferRequest("product-1", 12500, "TRY")));
        verify(catalogClient, never()).getPublishedProduct("product-1");
    }

    @Test
    void mapsCatalogDeadlineToRetryableDependencyFailure() {
        SellerOfferRepository repository = Mockito.mock(SellerOfferRepository.class);
        CatalogProductClient catalogClient = Mockito.mock(CatalogProductClient.class);
        when(repository.existsBySellerIdAndProductId("seller-1", "product-1")).thenReturn(false);
        when(catalogClient.getPublishedProduct("product-1"))
                .thenThrow(Status.DEADLINE_EXCEEDED.asRuntimeException());
        SellerOfferService service = new SellerOfferService(repository, catalogClient);

        assertThrows(
                CatalogDependencyUnavailableException.class,
                () -> service.createOffer("seller-1", new CreateOfferRequest("product-1", 12500, "TRY")));
        verify(repository, never()).save(any(SellerOffer.class));
    }
}
