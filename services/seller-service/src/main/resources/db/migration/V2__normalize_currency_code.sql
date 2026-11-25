ALTER TABLE seller_offer
    ALTER COLUMN currency_code TYPE VARCHAR(3)
    USING RTRIM(currency_code)::VARCHAR(3);

ALTER TABLE seller_offer
    ADD CONSTRAINT seller_offer_currency_code_ck
    CHECK (currency_code ~ '^[A-Z]{3}$');
