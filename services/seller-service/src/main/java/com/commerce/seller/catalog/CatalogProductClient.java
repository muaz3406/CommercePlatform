package com.commerce.seller.catalog;

import java.util.concurrent.TimeUnit;

import com.commerce.contracts.catalog.v1.CatalogServiceGrpc;
import com.commerce.contracts.catalog.v1.GetProductRequest;
import com.commerce.contracts.catalog.v1.Product;
import io.grpc.ManagedChannel;
import org.springframework.stereotype.Component;

@Component
public class CatalogProductClient {

    private static final long REQUEST_DEADLINE_SECONDS = 1;

    private final ManagedChannel channel;

    public CatalogProductClient(ManagedChannel channel) {
        this.channel = channel;
    }

    public Product getPublishedProduct(String productId) {
        return CatalogServiceGrpc.newBlockingStub(channel)
                .withDeadlineAfter(REQUEST_DEADLINE_SECONDS, TimeUnit.SECONDS)
                .getProduct(GetProductRequest.newBuilder().setProductId(productId).build());
    }
}
