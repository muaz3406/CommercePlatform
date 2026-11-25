package com.commerce.seller.offer;

import java.util.Objects;

import com.commerce.seller.catalog.CatalogProductClient;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SellerOfferService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SellerOfferRepository offerRepository;
    private final CatalogProductClient catalogProductClient;

    public SellerOfferService(
            SellerOfferRepository offerRepository,
            CatalogProductClient catalogProductClient) {
        this.offerRepository = offerRepository;
        this.catalogProductClient = catalogProductClient;
    }

    public OfferResponse createOffer(String sellerId, CreateOfferRequest request) {
        if (offerRepository.existsBySellerIdAndProductId(sellerId, request.productId())) {
            throw new OfferAlreadyExistsException();
        }

        try {
            catalogProductClient.getPublishedProduct(request.productId());
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new CatalogProductNotFoundException();
            }
            if (exception.getStatus().getCode() == Status.Code.UNAVAILABLE
                    || exception.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                throw new CatalogDependencyUnavailableException(exception);
            }
            throw exception;
        }

        try {
            SellerOffer offer = new SellerOffer(
                    sellerId,
                    request.productId(),
                    request.priceAmountMinor(),
                    request.currencyCode());
            return OfferResponse.from(offerRepository.save(offer));
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateOffer(exception)) {
                throw new OfferAlreadyExistsException();
            }
            throw exception;
        }
    }

    public OfferPageResponse listPublishedOffers(String productId, int page, int size) {
        Page<SellerOffer> offers = offerRepository.findAllByProductIdAndStatus(
                productId,
                OfferStatus.ACTIVE,
                PageRequest.of(
                        page,
                        Math.min(size, MAX_PAGE_SIZE),
                        Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by("id"))));
        return OfferPageResponse.from(offers);
    }

    private boolean isDuplicateOffer(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                return Objects.equals(violation.getConstraintName(), "seller_offer_seller_product_uq");
            }
            cause = cause.getCause();
        }
        return false;
    }
}
