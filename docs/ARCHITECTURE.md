# Mimari ve teknoloji kararları

## Hedef ilkeler

- Domain sınırları ve veri sahipliği açık olmalı; servisler başka servisin tablolarına yazmamalı.
- Önce çalışan dikey dilim ve ölçüm; mikroservis, cache ve yatay ölçek ihtiyaç kanıtlandıkça genişletilmeli.
- Senkron istekler kısa tutulmalı; servisler arası iş akışları gerektiğinde event tabanlı ve eventual consistency ile tasarlanmalı.
- Güvenlik ve operasyonel görünürlük sonradan eklenen özellikler değil, her dikey dilimin parçası olmalı.
- Her bileşen için yedekleme, migration, gözlemlenebilirlik ve arıza davranışı tanımlanmalı.

## Uygulama kanalları ve dağıtım sınırları

`CommercePlatform` kaynak kodu monorepo'sudur; tek deployable uygulama değildir. Müşteri mağazası (`apps/customer-web`) Next.js ile, satıcı (`apps/seller-portal`) ve platform yönetimi (`apps/platform-admin`) panelleri React + Vite ile ayrı uygulamalar olarak geliştirilir. Her biri bağımsız dağıtılabilir. Üç ayrı ekran, üç ayrı kopya backend anlamına gelmez: uygulamalar ortak API Gateway üzerinden domain API'lerine erişir. UI'ya özel BFF ancak gerçek bir veri birleştirme veya bağımsız evrim ihtiyacı ortaya çıktığında eklenir.

Yetki sınırları backend'de uygulanır: müşteri kendi sipariş/verisini, satıcı yalnızca kendi teklif/stok/sipariş kapsamını görür; platform operasyon rolleri moderasyon ve yönetim işlemleri için dar kapsamlıdır. Satıcı/müşteri kimliği doğrulanmış token'dan veya backend'in doğruladığı sahiplik ilişkisinden alınır; gövdedeki istemci beyanı yetki kaynağı değildir.

Yüksek trafik hedefi için edge katmanı/CDN, stateless gateway ve servislerin yatay çoğaltılması, servis başına veri sahipliği, kuyrukla gevşetilebilen iş akışları ve uçtan uca telemetri hedeflenir. Trafik profili, SLO ve yük testleri olmadan kapasite garantisi verilmez. Cache, read replica, partitioning ve sharding darboğaz ölçüldüğünde değerlendirilir; sipariş/stok/ödeme doğruluğunun yerine geçirilmez. Uygulama ve dağıtım sınırlarının kararı `docs/adr/0007-application-and-deployment-boundaries.md` içindedir.

## Başlangıç bileşenleri

| Alan | Seçim | Kullanım / sınır |
|---|---|---|
| Servis dili | Java 21, Spring Boot 4 | İş servisleri; sürüm/uyumluluk CI'da doğrulanır. |
| İlişkisel veri | PostgreSQL | Satıcı, teklif/fiyat, stok, sepet ve sipariş gibi ilişkisel ve transactional veriler. Servis başına veri sahipliği. |
| Belge deposu | MongoDB | Kategoriye göre değişen, esnek ürün teknik özellikleri ve katalog içerik dokümanları. Servis sahipliğinde; sipariş/ödeme doğruluğunun kaynağı değil. |
| Cache | Redis | Ölçülmüş, kaybedilebilir cache/rate-limit/idempotency gibi senaryolar; authoritative iş verisi değil. |
| Kimlik | Keycloak, OAuth 2.0/OIDC | Yerel başlangıç `start-dev`; gerçek ortamda TLS, dış veritabanı, güvenli secret ve realm/client hardening zorunlu. |
| Event streaming | Apache Kafka | Domain event'leri ve asenkron iş akışları; transactional outbox, şema uyumluluğu, retry/DLQ ve idempotent consumer ile. Yerel profil tek broker'dır. |
| Arama | Elasticsearch | Kafka event'lerinden Go indexer tarafından güncellenen, yeniden oluşturulabilir arama projeksiyonu. Kaynak veri değildir. AWS'de OpenSearch Service ayrıca uyumluluk testi ister. |
| İkincil servis dili | Go | Arama indeksleme gibi asenkron ve yatay ölçeklenen worker'lar; kritik stok doğruluğunun yerine dil seçimi konmaz. |
| Container/orchestrator | Docker Compose (local), Kubernetes (AWS EKS hedefi) | Compose geliştirici deneyimi içindir; üretim Helm, policy, secret ve rollout tasarımı gerektirir. |
| Gözlemlenebilirlik | OpenTelemetry + Prometheus/Grafana | Aşama 3/4'te trace, metric, log ve alarm; servis sınırlarıyla beraber tanımlanır. |
| CI/CD ve supply chain | GitHub Actions, OCI image, SBOM ve image/dependency scan | PR kontrolleri, immutable artifact ve environment approval hedeflenir. |

## Marketplace alan sınırları (başlangıç hipotezi)

- **Catalog/Product:** her fiziksel/model ürün için ortak canonical kayıt tutulur. MongoDB kategoriye özgü, esnek teknik özellik ve içerik dokümanlarının sahibi olur. Satıcı yeni ürün önerisi yapabilir; onay/moderasyon akışı olmadan ortak ürün kaydı oluşturamaz.
- **Seller/Offer:** PostgreSQL satıcı ilişkileri ile satıcıya ait offer/fiyatın kaynağı olur. Bir ürünün birçok offer'ı olabilir; offer ürün referansı kimlik ve domain event'iyle doğrulanır, satıcı ortak ürünün özelliklerini değiştiremez.
- **Inventory/Order:** ilk sürümde satıcı stok tutar ve gönderir. Stok rezervasyonu ve siparişin transactional kayıtları ilişkisel depoda kalır; atomik güncelleme/idempotency ile korunur. Bir checkout birden fazla satıcı içerirse satıcı bazlı alt-sipariş ve bağımsız gönderi/iptal durumları oluşur. Bu kayıtlar cache veya arama indeksinden okunup doğruluk kararı verilmez.
- **Search projection:** Kafka'daki versiyonlu event'leri tüketen Go worker, ürün ve satışa uygun teklifleri Elasticsearch'e yansıtır. Consumer tekrar çalışabilmeli; indeks silinince kaynaklardan yeniden kurulabilmelidir.
- **Kubernetes:** servisler yerelde önce Compose ile geliştirilir. AWS EKS deployment'ı, CI/CD, ağ/sır politikaları ve kapasite testleriyle ayrı teslim edilir; büyük cluster kurmak tek başına yüksek trafik kapasitesi sağlamaz.

Bu sınırlar ilk ürün hipotezidir; stok, ürün kimliği, satıcıya özgü katalog ve iade kuralları doğrulandıkça ADR ile güncellenir.

## Servis haritası

| Servis | Sahip olduğu iş ve veri | İletişim |
|---|---|---|
| `api-gateway` | Dış istemci sınırı: TLS, routing, request limit ve edge policy. Domain verisi tutmaz. | Dışarı HTTP/JSON; içeride gRPC |
| `catalog-service` | Ortak ürün, varyant, kategori, moderation ve esnek özellikler (MongoDB). | gRPC; `ProductPublished` Kafka event'i |
| `seller-service` | Satıcı onboarding/durumu ve satıcıya ait offer/fiyat (PostgreSQL). | Catalog doğrulaması için gRPC; Kafka |
| `inventory-service` | Offer/satıcı kapsamlı satılabilir miktar ve rezervasyon (PostgreSQL). | gRPC reserve/release/commit; Kafka |
| `order-service` | Checkout, sipariş/satıcı alt-siparişi, durable Saga state ve orchestration (PostgreSQL). | Inventory/Payment için gRPC; Kafka outbox/event |
| `payment-service` | Sağlayıcı adapter'ı, ödeme denemesi/durumu/webhook ve finansal audit (PostgreSQL). Kart verisi saklamaz. | gRPC authorize/void/refund; provider webhook ve Kafka |
| `search-indexer` (Go) | Kafka consumer; idempotent arama projeksiyonunu Elasticsearch'e yazar. | Kafka ve Elasticsearch |

Bu tablo hedef mimariyi gösterir; Go `search-indexer` henüz Maven modülü değildir. Java çalışma alanında API gateway, catalog iş akışı, Mongo persistence, OIDC korumalı HTTP ve gRPC server vardır; seller'da yerel catalog gRPC client adapter'ı ve ilk offer API/persistence dikey dilimi tamamlanmıştır. Inventory, order ve payment başlangıçta Spring Boot deployable skeleton'larıdır. Aşama 1'de catalog/offer uçtan uca akışı; Aşama 2'de durable checkout Saga uygulanır. Her domain servisi kendi migration'ını/release'ini yönetir. Yerel PostgreSQL sunucusu paylaşılabilir; veritabanı/schema ve kullanıcı servis bazında ayrılır, prod'da birbirinin DB'sine erişim verilmez.

## Servis sözleşmeleri ve ortak modeller

- `contracts/proto/` canonical API kaynağıdır. Protobuf'tan Java ve Go stub'ları üretilir; generated kaynaklar commit edilmez.
- Sözleşmeler bounded context bazında paketlenir (`commerce.catalog.v1`, `commerce.inventory.v1`). Geriye uyumlu alan eklenir; field number/type değiştirilmez veya silinen field numarası yeniden kullanılmaz. Kırıcı değişiklik yeni major package (`v2`) ve geçiş ister.
- Takımlar arasında ortak Java domain/entity/model JAR'ı paylaşılmaz. DB entity'si servis sınırını geçmez; her servis kendi modeline dönüştürür. Protobuf yalnızca wire/transport DTO'sudur.
- Ortak wire primitive'leri gerekliyse `Money` (integer minor units + ISO currency), `PageRequest`, `RequestContext` gibi dar mesajlar paylaşılır; iş kuralları ve domain entity'leri sahip serviste kalır.
- Her gRPC çağrısında deadline gerekir. Retry yalnızca idempotent operation/idempotency key ile, bounded exponential backoff+jitter ile yapılır; non-idempotent çağrı otomatik retry edilmez. Hatalar gRPC status + structured details olur.
- Servis-servis kimlik doğrulama üretimde mTLS workload identity ile zorunludur. Kullanıcı context'i doğrulanmış OIDC claim'lerinden en az ayrıcalıkla aktarılır. Local Compose portları public host'a açılmaz.
- gRPC kısa, doğrudan karar/komut içindir; Kafka asenkron event/fan-out ve eventual consistency içindir. İkisi arasında atomik transaction varmış gibi çift yazım yapılmaz.

## Önemli pattern'ler ve nerede kullanıldıkları

| Pattern | Kullanım | Neden ve sınır |
|---|---|---|
| Bounded Context + Database per Service | Bütün domain servisleri | Veri/değişiklik/arıza izolasyonu. Servisler başka servisin tablolarına yazmaz. |
| Saga orchestration | `order-service` checkout | Stok rezervasyonu → ödeme provizyonu → sipariş onayı. Hata/timeout'ta ödeme void ve stok release kompanzasyonu; merkezi, durable durum makinesiyle görünür süreç. Tek ACID transaction değildir; pending durumu ve eventual consistency ürün akışına dahildir. |
| Transactional Outbox + Inbox/idempotent consumer | Domain DB'den Kafka'ya yayın ve Kafka consumer'ları | DB commit olup event'in kaybolması ve duplicate delivery. Outbox relay/inbox dedupe ile at-least-once güvenli işlenir; exactly-once iş garantisi iddia edilmez. |
| CQRS (seçici) | `search-indexer` → Elasticsearch | Arama projeksiyonunu yazma kaynağından ayırır. Her domain'e CQRS uygulanmaz; read model gecikmesi kabul edilir ve indeks yeniden kurulur. |
| Idempotency + optimistic concurrency/atomic reservation | Checkout, payment, stock | Tekrarlanan istek ve yarış koşullarında çift sipariş/satışı önler. Stok doğruluğu PostgreSQL transaction'ındadır. |
| Timeout, bounded retry, circuit breaker, bulkhead | gRPC dependency'leri ve payment adapter | Dependency arızasının thread/pool tüketip zincirleme arızaya dönüşmesini sınırlar. |
| Cache-aside + TTL/jitter (ölçüm sonrası) | Katalog/search read path | Okuma yükünü azaltır; cache miss/invalidation normaldir. Checkout kararı stale cache'e dayanmaz. |
| Expand-contract migration | API ve DB sözleşme evrimi | Eski ve yeni sürüm birlikte çalışırken güvenli deployment/rollback sağlar. |
| Cell/shard partitioning (ölçüm sonrası) | İleride hot seller/catalog/order partition | Hot partition ve blast radius'u azaltır; başlangıçta erken sharding yapılmaz. |

Bu pattern'ler isim olarak değil failure-mode testleri ve operasyonel davranışlarıyla doğrulanır. Karar gerekçeleri `docs/adr/` altında tutulur.

## Teknoloji haritası

| Alan | Seçim | Kullanım |
|---|---|---|
| İş servisleri | Java 21, Spring Boot 4 | Gateway/domain servisleri |
| Asenkron worker | Go | Search indexer |
| Servis sözleşmesi | Protobuf + gRPC | `contracts/proto/` üzerinden code generation |
| Veri | PostgreSQL, MongoDB | Servis başına veri sahipliği ve migration |
| Dağıtık altyapı | Redis, Kafka, Elasticsearch | Cache, event stream ve türetilmiş arama |
| Kimlik/orchestrator | Keycloak, Docker Compose; hedefte Kubernetes/EKS | Local Compose yalnızca geliştirme içindir |
| CI/observability | GitHub Actions, OpenTelemetry, Prometheus/Grafana | Contract compatibility, telemetry ve operasyon |

## AWS hedefi (hemen kaynak oluşturulmaz)

| Yetkinlik / sorumluluk | Olası AWS hedefi |
|---|---|
| Kubernetes ve OCI registry | EKS + ECR; IaC için Terraform |
| İlişkisel veri / cache | RDS PostgreSQL + ElastiCache Redis; yedekleme, Multi-AZ, bağlantı havuzu ve restore tatbikatı |
| Event / arama | MSK ve OpenSearch Service; managed ürünlerin Kafka/Elasticsearch uyumluluğu uygulama testiyle doğrulanır |
| Belge verisi | MongoDB Atlas on AWS; DocumentDB MongoDB ile tamamen aynı ürün/uyumluluk varsayılmamalı |
| Görsel/veri nesnesi | S3 + CloudFront; bucket policy, encryption, upload validation ve lifecycle |
| Kimlik ve sırlar | Keycloak EKS üzerinde, HA ve PostgreSQL backend ile; Secrets Manager/KMS. Alternatif IdP ancak ayrı ürün kararıyla |
| Trafik/sınır | ALB/WAF, Route 53, private subnet ve least-privilege IAM |
| Telemetri | OpenTelemetry Collector/ADOT ve CloudWatch/Grafana entegrasyonu |

Managed servis seçimi, bölge/availability, veri yerleşimi, istenen SLA ve tahmini trafikle yeniden doğrulanacaktır. EKS ve managed data plane maliyetlidir; AWS hesabında bütçe alarmı ve kaynak temizleme planı kurulmadan kaynak yaratılmayacaktır.

## Neden her teknoloji ilk günden üretimde çalışmıyor?

Stack'e MongoDB, Go, Elasticsearch ve Kubernetes dahil; her biri bir domain işine bağlandı. Yine de hepsini ilk commit'te devreye almak, milyonlarca kullanıcıyı kaldırdığını kanıtlamaz. Önce güvenli akış kurulur; yatay ölçek ve kapasite SLO, hata enjeksiyonu ve yük testleriyle kanıtlanır.

## Güvenlik ve üretim kapıları

- Sırlar repoda veya Docker image içinde tutulmaz; deployment secret manager/Kubernetes secret entegrasyonu ve rotation gereklidir.
- Yerel Compose parolaları ve Keycloak `start-dev` üretimde kullanılmaz. Uzak erişim, TLS, least privilege, network policy ve güvenli konfigürasyon zorunludur.
- Her servis kendi verisini/migration'ını yönetir. PII envanteri, retention, şifreleme ve erişim denetimi ürün gereksinimleriyle belirlenir.
- API authN/authZ, input validation, audit, rate limit, dependency/container taraması ve threat modeling release koşuluna dönüşmelidir.
- Yüksek trafik iddiası, ölçülebilir SLO ve yük testleriyle doğrulanmadan mimari garanti olarak sunulmaz.
