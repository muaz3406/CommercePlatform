# Commerce Platform

Üretim kalitesinde, çok satıcılı marketplace backend'ini gerçek bir ürün ekibi yaklaşımıyla küçük ve doğrulanabilir adımlarla geliştirme alanı.

## Başlangıç durumu

- MongoDB üzerinde ürün saklayan ve OIDC korumalı HTTP API sunan `catalog-service`.
- Catalog için local profilde çalışan gRPC server ve seller'dan catalog'a gRPC client adapter'ı.
- Ayrı deployable modüller: `seller-service`, `inventory-service`, `order-service`, `payment-service`.
- `contracts/`: servisler arası Protobuf sözleşmeleri ve Java gRPC code generation.
- PostgreSQL, Redis ve Keycloak için yerel Docker Compose altyapısı.
- İsteğe bağlı Kafka, MongoDB ve Elasticsearch profilleri.
- Yol haritası, mimari ilkeler ve Git akışı `docs/` altında.

Bu depo şu anda bir başlangıç iskeletidir; henüz canlı trafiğe hazır bir ürün değildir. Üretim ortamı için kimlik, veri yönetimi, gözlemlenebilirlik, yedekleme, dağıtım ve yük testleri aşamalı olarak tamamlanmalıdır.

## Uygulama ve dağıtım yaklaşımı

`CommercePlatform` tek geliştirme deposudur; çalışma zamanında tek uygulama olmak zorunda değildir. Müşteri mağazası (`apps/customer-web`) Next.js ile; satıcı (`apps/seller-portal`) ve platform yönetimi (`apps/platform-admin`) panelleri React + Vite ile ayrı, bağımsız dağıtılabilir uygulamalardır. Bu uygulamalar henüz backend API'lerine bağlanmış değildir.

Bu arayüzler ekran başına ayrı backend yerine ortak API Gateway ve domain servislerini kullanır. Yetki servislerde doğrulanır; her uygulama yalnızca giriş yapan kişinin/rolün erişebildiği kaynakları sunar. Domain servisleri ve arayüzler monorepo'da geliştirilse de bağımsız CI/CD ve dağıtım hedefleridir. Yüksek trafik kapasitesi varsayılmaz; SLO, yük testi ve gözlemlenebilirlikle doğrulanır. Ayrıntı: [ADR 0007](docs/adr/0007-application-and-deployment-boundaries.md).

Frontend gereksinimleri: Node.js 20.19+ (veya 22.13+), npm 10.8+. Monorepo kökünden `npm install` ile bağımlılıkları kurun; sonra her uygulamayı ayrı çalıştırın:

```powershell
npm run dev:customer
npm run dev:seller
npm run dev:admin
```

Bağımsız production build'leri `npm run build:customer`, `npm run build:seller` ve `npm run build:admin` komutlarıyla alınır.

## Gereksinimler

- JDK 21
- Maven 3.9+
- Docker Desktop / Docker Engine ve Docker Compose v2

## Yerel geliştirme

1. Yerel değişken dosyasını oluşturun: `Copy-Item .env.example .env`
2. Ana bağımlılıkları ve catalog için MongoDB'yi başlatın: `docker compose --profile documents up -d`
3. Servisleri test edip sözleşmeleri yerel Maven deposuna yükleyin: `mvn install`
4. MongoDB local kullanıcı/parolasını ayarlayıp servisi başlatın:

   ```powershell
   $env:MONGO_ROOT_USERNAME = "local-root"
   $env:MONGO_ROOT_PASSWORD = "local-mongodb-change-me"
   mvn -pl services/catalog-service spring-boot:run -Dspring-boot.run.profiles=local
   ```

5. Sağlık kontrolü: `http://localhost:8081/actuator/health`
6. Katalog gRPC server yerel profilde `localhost:9091` adresinde açılır; local profile plaintext yalnızca geliştirici içindir.

Şu an catalog ürün API/persistence ve gRPC server işlevlidir. Seller, inventory, order ve payment deployable Spring Boot başlangıçlarıdır; seller'da yerel catalog gRPC client adapter'ı bulunur. Bu modüllerin domain API/DB akışları, order Saga ve production mTLS henüz tamamlanmadı; roadmap sırasıyla uygulanacaktır.

Seller offer akışı ilk dikey dilim olarak eklendi: `GET http://localhost:8082/api/v1/seller/offers?productId=...` herkese açık biçimde ürünün aktif tekliflerini sayfalı döndürür; `POST http://localhost:8082/api/v1/seller/offers`, `seller` realm rolü taşıyan JWT ister. İstek `productId`, `priceAmountMinor` (en küçük para birimi) ve `currencyCode` taşır; servis katalogda aktif ürünü gRPC ile doğrular ve kendi PostgreSQL `seller` şemasına yazar.

Diğer servisleri ayrı terminal pencerelerinde başlatabilirsiniz:

```powershell
mvn -pl services/seller-service spring-boot:run '-Dspring-boot.run.profiles=local'
mvn -pl services/inventory-service spring-boot:run
mvn -pl services/order-service spring-boot:run
mvn -pl services/payment-service spring-boot:run
```

Yerel servis adresleri: PostgreSQL `localhost:5432`, Redis `localhost:6379`, Keycloak `http://localhost:8080`. Keycloak `start-dev` modundadır ve yalnızca yerel geliştirme içindir. `.env.example` içindeki örnek parolalar canlı ortamda kullanılmamalıdır.

Kafka'yı veya Elasticsearch'ü ihtiyaç olduğunda ayrıca açın:

```powershell
docker compose --profile messaging up -d
docker compose --profile documents up -d
docker compose --profile search up -d
```

Bu Compose tanımları geliştirme içindir; üretim ortamına olduğu gibi taşınmamalıdır.

## Git akışı

- `main`: üretime alınmış, sürümlenebilir kod.
- `develop`: bir sonraki sürüm için entegrasyon/test hattı.
- `feature/<kisa-konu>`: kısa ömürlü özellik dalları; PR ile `develop` dalına birleştirilir.
- Sürüm adayı doğrulandıktan sonra `develop` → `main` PR'ı açılır; dağıtım immutable image/tag üzerinden yapılır.

Ortam ayarları ve sırlar branch'lerde tutulmaz. Test ve üretim ortamları CI/CD deployment environment'larıyla ayrılmalıdır; branch yalnızca kodun entegrasyon/sürüm aşamasını belirler. Bu dallar yerel Git başlangıcında oluşturulur; uzak depo ve branch korumaları GitHub'da yapılandırılmalıdır.

## Belgeler

- [Teslimat yol haritası](docs/ROADMAP.md)
- [Mimari ve teknoloji seçimleri](docs/ARCHITECTURE.md)
- [API Gateway](services/api-gateway/README.md)
- [Catalog Service](services/catalog-service/README.md)
- [Seller Service](services/seller-service/README.md)
- [Inventory Service](services/inventory-service/README.md)
- [Order Service](services/order-service/README.md)
- [Payment Service](services/payment-service/README.md)
- [ADR 0001: Başlangıç servis sınırları](docs/adr/0001-service-boundaries.md)
- [ADR 0002: Veri ve mesajlaşma teknolojileri](docs/adr/0002-data-and-messaging.md)
- [ADR 0003: Branch ve dağıtım modeli](docs/adr/0003-branch-and-deployment.md)
- [ADR 0004: Marketplace veri sahipliği ve AWS hedefi](docs/adr/0004-marketplace-data-ownership.md)
- [ADR 0005: Servis sözleşmeleri ve gRPC](docs/adr/0005-service-contracts-and-communication.md)
- [ADR 0006: Checkout Saga orchestration](docs/adr/0006-order-saga-orchestration.md)
- [ADR 0007: Uygulama ve dağıtım sınırları](docs/adr/0007-application-and-deployment-boundaries.md)
