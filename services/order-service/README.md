# Order Service

Checkout, sipariş, satıcı alt-siparişleri ve durable checkout Saga sürecinin orchestration sahibi olacak servistir.

## Mevcut durum

Henüz checkout/domain API'si uygulanmamıştır. Spring Boot deployable başlangıcı ve sağlık endpointi bulunur.

## Çalıştırma

```powershell
mvn -pl services/order-service spring-boot:run
```

HTTP adresi: `http://localhost:8084`

## Planlanan sorumluluklar

- Idempotency key ile siparişi oluşturma
- Sepetteki satıcı offer'larını satıcı alt-siparişlerine ayırma
- Inventory rezervasyonlarını yönetme
- Payment authorization işlemini başlatma
- Timeout ve hatalarda stok/ödeme kompanzasyonu
- PostgreSQL'de saga durumunu ve transactional outbox kayıtlarını saklama

## Mevcut HTTP endpointleri

| Method | Endpoint | Açıklama |
|---|---|---|
| `GET` | `/actuator/health` | Servis sağlık durumu |

Durable Saga tasarımı `docs/adr/0006-order-saga-orchestration.md` içinde ayrıntılıdır.
