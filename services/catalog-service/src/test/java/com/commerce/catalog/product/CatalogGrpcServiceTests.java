package com.commerce.catalog.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import com.commerce.contracts.catalog.v1.GetProductRequest;
import io.grpc.Status;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mockito;

class CatalogGrpcServiceTests {

    private Server server;
    private ManagedChannel channel;

    @AfterEach
    void stopGrpcResources() throws InterruptedException {
        if (channel != null) {
            channel.shutdownNow().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        }
        if (server != null) {
            server.shutdownNow().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    @Test
    void returnsCanonicalProductForInternalServices() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        when(repository.findByIdAndStatus("product-1", ProductStatus.ACTIVE))
                .thenReturn(Optional.of(product()));
        CatalogGrpcService service = new CatalogGrpcService(repository);
        CapturingObserver<com.commerce.contracts.catalog.v1.Product> observer = new CapturingObserver<>();

        service.getProduct(
                GetProductRequest.newBuilder().setProductId("product-1").build(),
                observer);

        assertEquals("product-1", observer.value.getProductId());
        assertEquals("ACTIVE", observer.value.getStatus().name().replace("PRODUCT_STATUS_", ""));
        assertEquals("black", observer.value.getAttributesMap().get("color"));
        assertEquals(true, observer.completed);
    }

    @Test
    void returnsNotFoundWhenProductDoesNotExist() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        when(repository.findByIdAndStatus("missing", ProductStatus.ACTIVE)).thenReturn(Optional.empty());
        CatalogGrpcService service = new CatalogGrpcService(repository);
        CapturingObserver<com.commerce.contracts.catalog.v1.Product> observer = new CapturingObserver<>();

        service.getProduct(
                GetProductRequest.newBuilder().setProductId("missing").build(),
                observer);

        Status status = Status.fromThrowable(observer.error);
        assertEquals(Status.Code.NOT_FOUND, status.getCode());
    }

    @Test
    void rejectsBlankProductId() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        CatalogGrpcService service = new CatalogGrpcService(repository);
        CapturingObserver<com.commerce.contracts.catalog.v1.Product> observer = new CapturingObserver<>();

        service.getProduct(GetProductRequest.getDefaultInstance(), observer);

        Status status = Status.fromThrowable(observer.error);
        assertEquals(Status.Code.INVALID_ARGUMENT, status.getCode());
    }

    @Test
    void generatedGrpcClientCanCallCatalogService() throws Exception {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        when(repository.findByIdAndStatus("product-1", ProductStatus.ACTIVE))
                .thenReturn(Optional.of(product()));
        server = ServerBuilder.forPort(0)
                .addService(new CatalogGrpcService(repository))
                .build()
                .start();
        channel = ManagedChannelBuilder.forAddress("localhost", server.getPort())
                .usePlaintext()
                .build();

        com.commerce.contracts.catalog.v1.Product response =
                com.commerce.contracts.catalog.v1.CatalogServiceGrpc.newBlockingStub(channel)
                        .withDeadlineAfter(1, java.util.concurrent.TimeUnit.SECONDS)
                        .getProduct(GetProductRequest.newBuilder().setProductId("product-1").build());

        assertEquals("product-1", response.getProductId());
        assertEquals("Phone", response.getName());
    }

    private Product product() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new com.commerce.catalog.product.Product(
                "product-1",
                "Phone",
                "Example",
                "electronics",
                null,
                Map.of("color", "black"),
                ProductStatus.ACTIVE,
                now,
                now,
                1L);
    }

    private static final class CapturingObserver<T> implements StreamObserver<T> {
        private T value;
        private Throwable error;
        private boolean completed;

        @Override
        public void onNext(T value) {
            this.value = value;
        }

        @Override
        public void onError(Throwable error) {
            this.error = error;
        }

        @Override
        public void onCompleted() {
            completed = true;
        }
    }
}
