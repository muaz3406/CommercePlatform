# Payment Service

Ödeme sağlayıcısı adapter'ını, ödeme yetkilendirme durumlarını ve finansal audit kayıtlarını sahiplenecek servistir.

## Mevcut durum

Henüz ödeme/domain API'si uygulanmamıştır. Kart verisi saklamaz; başlangıçta sandbox/mock adapter hedeflenir. Spring Boot deployable başlangıcı ve sağlık endpointi bulunur.

## Çalıştırma

```powershell
mvn -pl services/payment-service spring-boot:run
```

HTTP adresi: `http://localhost:8085`

## Planlanan sorumluluklar

- Ödeme sağlayıcısına yetkilendirme gönderme
- Yetkilendirme ve iptal işlemlerinde idempotency uygulama
- Sağlayıcı webhook'larını imza ile doğrulama
- Authorization durumunu PostgreSQL'de saklama
- Kart verisi yerine tokenized/hosted payment yöntemi kullanma
- `AuthorizePayment` ve `VoidPayment` gRPC işlemlerini sunma

## Mevcut HTTP endpointleri

| Method | Endpoint | Açıklama |
|---|---|---|
| `GET` | `/actuator/health` | Servis sağlık durumu |
