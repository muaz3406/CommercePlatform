CREATE TABLE seller_offer (
    id UUID PRIMARY KEY,
    seller_id VARCHAR(128) NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    price_amount_minor BIGINT NOT NULL CHECK (price_amount_minor > 0),
    currency_code CHAR(3) NOT NULL,
    status VARCHAR(16) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT seller_offer_seller_product_uq UNIQUE (seller_id, product_id),
    CONSTRAINT seller_offer_status_ck CHECK (status IN ('ACTIVE', 'PAUSED'))
);

CREATE INDEX seller_offer_product_status_idx
    ON seller_offer (product_id, status);
