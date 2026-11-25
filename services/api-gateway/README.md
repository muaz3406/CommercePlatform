# API Gateway

Dış HTTP istemcilerini Catalog ve Seller servislerine yönlendiren Spring Cloud Gateway sınırıdır.

## Çalıştırma

```powershell
mvn -pl services/api-gateway spring-boot:run
```

Adres: `http://localhost:8088`

## Route'lar

| Route | Hedef servis | Path eşleşmesi |
|---|---|---|
| Catalog | `${CATALOG_SERVICE_URI:http://localhost:8081}` | `/api/v1/products` ve alt yolları |
| Seller | `${SELLER_SERVICE_URI:http://localhost:8082}` | `/api/v1/seller/offers` ve alt yolları |

## Sağlık kontrolü

| Method | Endpoint |
|---|---|
| `GET` | `/actuator/health` |

Gateway şu anda statik iki route sağlar. TLS, dış istek sınırlandırma, merkezi kimlik doğrulama ve kapsamlı edge policy henüz eklenmemiştir.
