package com.commerce.catalog.product;

import java.util.Map;

import com.commerce.contracts.catalog.v1.CatalogServiceGrpc;
import com.commerce.contracts.catalog.v1.GetProductRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

@Component
public class CatalogGrpcService extends CatalogServiceGrpc.CatalogServiceImplBase {

    private final ProductRepository productRepository;

    public CatalogGrpcService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void getProduct(GetProductRequest request, StreamObserver<com.commerce.contracts.catalog.v1.Product> observer) {
        String productId = request.getProductId();
        if (productId.isBlank()) {
            observer.onError(Status.INVALID_ARGUMENT
                    .withDescription("product_id must not be blank")
                    .asRuntimeException());
            return;
        }

        Product product = productRepository.findByIdAndStatus(productId, ProductStatus.ACTIVE)
                .orElse(null);
        if (product == null) {
            observer.onError(Status.NOT_FOUND
                    .withDescription("Published product was not found")
                    .asRuntimeException());
            return;
        }

        com.commerce.contracts.catalog.v1.Product.Builder response =
                com.commerce.contracts.catalog.v1.Product.newBuilder()
                        .setProductId(product.id())
                        .setName(product.name())
                        .setBrand(product.brand())
                        .setCategoryId(product.categoryId())
                        .setStatus(com.commerce.contracts.catalog.v1.ProductStatus.PRODUCT_STATUS_ACTIVE)
                        .setVersion(product.version() == null ? 0 : product.version());
        if (product.gtin() != null) {
            response.setGtin(product.gtin());
        }
        if (product.attributes() != null) {
            for (Map.Entry<String, Object> attribute : product.attributes().entrySet()) {
                if (attribute.getValue() != null) {
                    response.putAttributes(attribute.getKey(), String.valueOf(attribute.getValue()));
                }
            }
        }

        observer.onNext(response.build());
        observer.onCompleted();
    }
}
