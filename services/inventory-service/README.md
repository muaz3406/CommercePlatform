# Inventory Service

Offer/satıcı kapsamındaki satılabilir stok ve rezervasyonların sahipliğini üstlenecek servisdir.

## Mevcut durum

Henüz domain API'si uygulanmamıştır. Spring Boot deployable başlangıcı ve sağlık endpointi bulunur.

## Çalıştırma

```powershell
mvn -pl services/inventory-service spring-boot:run
```

HTTP adresi: `http://localhost:8083`

## Planlanan endpointler

ARCHITECTURE ve ROADMAP kararlarına göre Inventory Service:

- Offer stok miktarını PostgreSQL'de saklar.
- `ReserveStock`, `ReleaseStock` ve `CommitStock` gRPC işlemlerini sunar.
- Tüm operasyonlarda rezervasyon/order/offer kimliği ve idempotency anahtarı kullanır.
- Checkout sırasında eşzamanlı stok rezervasyonunu atomik olarak korur.

## Mevcut HTTP endpointleri

| Method | Endpoint | Açıklama |
|---|---|---|
| `GET` | `/actuator/health` | Servis sağlık durumu |
