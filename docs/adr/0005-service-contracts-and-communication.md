# ADR 0005: Servis sınırları ve gRPC/Protobuf sözleşmeleri

- Durum: Kabul edildi
- Tarih: 2026-11-24

## Bağlam

Marketplace servisleri Java ve Go ile geliştirilecek. Bir dilde paylaşılan domain/entity JAR'ı servisleri derleme zamanında ve runtime'da birbirine bağlar; farklı dillerde yeniden kullanılamaz. Serbest biçimli JSON ise iç servis kontratlarında belirsiz tip ve uyumsuzluk riski yaratır.

## Karar

- Deployable sınırlar: `api-gateway`, `catalog-service`, `seller-service`, `inventory-service`, `order-service`, `payment-service` ve Go `search-indexer`.
- Her domain servisinin kendi PostgreSQL database/schema ve kimliği/migration'ı vardır. Catalog esnek ortak ürün dokümanlarını MongoDB'de tutar. Bu owner sınırını local tek DB sunucusu değiştirmez.
- `contracts/src/main/proto/` canonical gRPC/Protobuf sözleşme kaynağıdır; aynı sözleşmeden Java ve Go stub'ı üretilir.
- Sözleşmeler bounded context ve major versiyonla paketlenir. Wire tipleri paylaşılır; domain modeli/ORM entity veya tüm iş mantığını içeren ortak model kütüphanesi paylaşılmaz.
- Senkron, kısa iç çağrılar gRPC'dir; domain event, fan-out ve gevşek bağlı süreç Kafka'dır. Servisler başka servisin DB'sini okumaz/yazmaz.
- Üretim gRPC'de mTLS/workload identity zorunludur. Local plaintext profili yalnızca geliştirme içindir; local gRPC portları public host'a publish edilmez.

## Sonuçlar

- `.proto` kırıcı değişikliği CI'da engellenmeli; silinen field numaraları rezerve edilmeli.
- Deadline her RPC için zorunlu; bounded retry yalnızca idempotent isteklerde yapılır.
- gRPC status, request correlation ve OTel trace context standardize edilmelidir.
- Service skeleton'ları ile tamamlanmış işlevli servis ayrımı roadmap ve repo README'sinde açık tutulur.
