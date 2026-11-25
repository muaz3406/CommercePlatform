# Ürün ve teslimat yol haritası

Bu plan, büyük bir alışveriş platformunu küçük, uçtan uca doğrulanabilir teslimatlara böler. Trafik hedefi, ürün kapsamı ve ekip kapasitesi henüz net olmadığı için ölçek iddiaları ölçüm ve aşamalı kapasite planlamasıyla doğrulanacaktır.

## Aşama 0 — Ürün keşfi ve mühendislik temeli (başlandı)

- [x] Proje amacı, mimari ilkeler ve teknoloji kararlarını kaydet.
- [x] `main` / `develop` branch yaklaşımını belirle.
- [x] Java 21 / Spring Boot tabanlı derlenebilir servis iskeleti ekle.
- [x] Yerel PostgreSQL, Redis ve Keycloak; profilli Kafka ve Elasticsearch altyapısı ekle.
- [x] Marketplace servis haritasını, Protobuf/gRPC sözleşme politikasını ve Saga/Outbox kararlarını yaz.
- [ ] MVP kapsamı, kullanıcı rolleri, temel iş kuralları ve ilk ölçülebilir SLO'ları netleştir.
- [ ] Uzak Git deposu, PR şablonu, branch protection, CI pipeline ve GitHub deployment environments kur.

**Çıkış ölçütü:** yeni geliştirici README adımlarını izleyerek projeyi başlatabilir; trunk branch'e merge yalnızca tanımlı CI kontrollerinden geçerek yapılabilir.

## Aşama 1 — Servis kontratları ve katalog/teklif dikey dilimi

- Maven multi-module iskeleti: `catalog`, `seller`, `inventory`, `order`, `payment`; canonical Protobuf contracts ve Java/Go codegen/compatibility kontrolü.
- Ortak ürün kaydı/teknik özelliklerini (MongoDB) satıcı teklif/fiyatından (PostgreSQL) ayır; satıcıların aynı ürüne birden fazla offer açmasını uygula.
- İlk sürüm fulfillment'ı satıcı gönderimli tut; stok offer/satıcı kapsamındadır. Multi-seller checkout için satıcı bazlı alt-sipariş sınırını tasarla.
- Flyway migration'ları, Mongo indeks/validation stratejisi; katalog ve satıcı teklif API'leri, doğrulama, sayfalama ve OpenAPI sözleşmesi.
- Keycloak realm/client, OAuth 2.0 Resource Server, rol/izin modeli ve negatif erişim testleri.
- Test piramidi: unit, PostgreSQL Testcontainers entegrasyon testleri, API sözleşme testleri.
- Güvenlik temeli: threat model, rate limit, güvenli hata yanıtları, dependency/container taraması.

**Çıkış ölçütü:** satıcı yalnızca kendi teklifini yönetebilir; katalog ve teklif verisi yeniden kurulabilir; migration ve negatif auth testleri CI'da geçer.

## Aşama 2 — Durable checkout Saga ve stok/ödeme tutarlılığı

- Sipariş, sepet ve stok sorumluluklarını tanımla; ödeme sağlayıcısı için sandbox kullan.
- Order-owned durable Saga orchestration: stok rezervasyonu → ödeme provizyonu → sipariş onayı; timeout/failure compensation (payment void, inventory release), retry ve recovery.
- Idempotency, transactional outbox/inbox, Kafka event şemaları/versiyonlama ve retry/DLQ politikası.
- Dağıtık transaction yerine Saga/kompanzasyon; eşzamanlı stok ayırma ve crash/restart/duplicate event testleri.
- Redis'i yalnızca ölçülmüş cache/ephemeral veri senaryolarında kullan; doğruluk kaynağı PostgreSQL olsun.

**Çıkış ölçütü:** Saga adımları durable/idempotent; servis crash/restart sonrası akış kaldığı yerden sürer; duplicate checkout çift sipariş/ödeme üretmez; kompanzasyon test edilmiştir.

## Aşama 3 — Arama, müşteri ve operasyon

- Elasticsearch arama indeksi; PostgreSQL'den event/CDC tabanlı türetilebilir indeks, yeniden oluşturma ve tutarlılık kontrolü.
- Müşteri/profil ve bildirim sınırları; PII sınıflandırma, saklama ve silme akışları.
- OpenTelemetry trace/metric/log, Prometheus/Grafana ve merkezi log/alerting.
- Go tabanlı Kafka search-indexer: idempotent consumer, backoff/DLQ, event schema uyumluluğu ve Elasticsearch'e yeniden indeksleme.
- MongoDB backup/restore, index tuning, schema validation ve değişiklik yönetimi; ürün katalog dokümanlarının sahibi olarak.

**Çıkış ölçütü:** türetilmiş arama verisi kaybolduğunda kaynaktan yeniden oluşturulabilir; kritik akışlar trace edilebilir ve alarmlıdır.

## Aşama 4 — Dağıtım, dayanıklılık ve ölçek

- OCI image, SBOM, imza ve tarama; Kubernetes deployment/Helm, secrets yönetimi ve network policy.
- Liveness/readiness/startup probe, resource limit, HPA/KEDA ve kontrollü rollout/rollback.
- K6/Gatling yük testleri, kapasite hedefleri, chaos/failure senaryoları ve SLO/error budget.
- PostgreSQL yedekleme/PITR, restore tatbikatı; Redis/Kafka/Elasticsearch HA ve felaket kurtarma hedefleri.
- Staging'den üretime onay kapılı, immutable artifact kullanan CI/CD ve canary/blue-green stratejisi.

**Çıkış ölçütü:** belirlenen trafik ve hata bütçesi yük testleriyle kanıtlanır; yedekten geri dönüş ve rollback tatbikatı başarılıdır.

## Netleşen ürün kararı

- Başlangıç ürünü: çok satıcılı marketplace.
- Karar verildi: ortak canonical katalog; satıcılar ortak ürüne kendi offer/fiyat/stok bilgilerini ekler. Yeni ürün önerisi moderasyon gerektirir. İlk sürüm satıcı gönderimlidir.
- Karar verildi: farklı satıcı ürünleri tek checkout/tek ödeme ile alınabilir; order service satıcı bazlı alt-siparişler üretir.
- Karar verildi: ödeme akışı sağlayıcıdan bağımsız mock/sandbox adapter ile başlar; canlı sağlayıcı olarak iyzico sandbox hedeflenir.
- Sonraki kararlar: ilk AWS bütçe sınırı ve hedef availability/latency. Bunlar servis sınırları ve yük testlerinin kabul ölçütlerini belirler.
