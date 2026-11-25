# Seller Service

Satıcı ilişkilerini ve satıcıya ait ürün tekliflerini PostgreSQL'de saklar. Katalogdaki yayınlanmış ürünü gRPC ile doğruladıktan sonra teklif oluşturur.

## Çalıştırma

```powershell
mvn -pl services/seller-service spring-boot:run "-Dspring-boot.run.profiles=local"
```

HTTP adresi: `http://localhost:8082`

Seller verisi için PostgreSQL `seller` şeması kullanılır.

## Sorumluluklar

- Bir satıcının ürün teklifini oluşturma
- Satıcı + ürün için tekil teklif kuralını uygulama
- Catalog Service'ten ürünün yayınlanmış olduğunu doğrulama
- Teklif fiyatını en küçük para birimi cinsinden saklama

## Kimlik doğrulama

- Teklif oluşturma `seller` realm rolü gerektirir.
- Satıcı kimliği JWT'nin `sub` claim'inden alınır; istemci gövdeden satıcı kimliği göndermez.
- Sağlık endpointleri herkese açık.

## HTTP endpointleri

| Method | Endpoint | Yetki | Açıklama |
|---|---|---|---|
| `GET` | `/actuator/health` | Public | Servis sağlık durumu |
| `POST` | `/api/v1/seller/offers` | `seller` | Oturumdaki satıcı için teklif oluşturur |

İstek gövdesi:

```json
{
  "productId": "ürün-id",
  "priceAmountMinor": 12500,
  "currencyCode": "TRY"
}
```

- `priceAmountMinor` pozitif olmalıdır.
- `currencyCode` üç büyük harften oluşmalıdır.
- Aynı satıcının aynı ürün için ikinci teklifi `409 Conflict` döner.
- Ürün katalogda bulunmuyorsa veya yayınlanmamışsa `404 Not Found` döner.
- Catalog gRPC servisine ulaşılamıyorsa `503 Service Unavailable` döner.
