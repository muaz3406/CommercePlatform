# ADR 0004: Ortak ürün kataloğu, veri sahipliği ve AWS hedefi

- Durum: Kabul edildi (ürün keşfi sırasında doğrulanacak)
- Tarih: 2026-11-24

## Bağlam

Ürün çok satıcılı marketplace olarak başlıyor. Aynı fiziksel ürün birden çok satıcı tarafından farklı fiyat ve stokla sunulabilmeli. Kategoriye bağlı ürün özellikleri esnek doküman yapısından yararlanırken, satıcı teklifleri, fiyat, stok rezervasyonu ve sipariş ilişkisel tutarlılık ve transaction gerektirir. Arama sorguları bu kaynakların birleştirilmiş, yüksek hacimli okuma projeksiyonuna ihtiyaç duyabilir.

## Karar

- Her fiziksel/model ürün için ortak bir canonical katalog kaydı vardır; satıcılar aynı ürün kimliğine ayrı offer/fiyat/stok kayıtları ekler. Ürün kimliği, ürün varyantı ve GTIN/marka/model eşleştirme kuralları ayrıca tanımlanır.
- Müşteri farklı satıcıların offer'larını tek checkout ve tek ödeme ile satın alabilir; order service satıcı bazlı alt-siparişler oluşturur.
- MongoDB, ortak ürün kataloğunun kategoriye göre değişen teknik özellik/içerik dokümanlarını sahiplenir.
- PostgreSQL, satıcı ve satıcıya ait offer/fiyat, stok rezervasyonu ve sipariş gibi ilişkisel ve transactional kayıtların sahibidir.
- İlk sürümde fulfillment satıcı gönderimlidir. Stok offer/satıcı bağlamında yönetilir; marketplace deposu/fulfillment daha sonra ayrı bir akış ve kapasite kararıyla eklenebilir.
- Ödeme başlangıçta sağlayıcıdan bağımsız sandbox/mock adapter ile geliştirilir; gerçek entegrasyon iyzico sandbox'a bağlanır. Kart verisi platformda saklanmaz; sağlayıcı tokenizasyonu/hosted checkout tercih edilir.
- Kafka, servisler arası versiyonlu event taşır. MongoDB/PostgreSQL arasında atomik transaction varmış gibi davranılmaz; outbox, idempotency, uzlaştırma ve yeniden oynatma stratejileri ilgili akışa eklenir.
- Go consumer, Kafka domain event'lerinden Elasticsearch arama projeksiyonunu kurar. Elasticsearch ve Redis kaynak veri değildir; kayıpları iş doğruluğunu bozmamalı ve yeniden oluşturulabilmelidir.
- Yerel geliştirmede Docker Compose kullanılır. AWS hedefi EKS/ECR, RDS PostgreSQL, ElastiCache Redis, MSK, OpenSearch Service ve AWS üzerinde MongoDB Atlas olarak planlanır; gerçek seçimler maliyet, uyumluluk, bölge ve SLA doğrulanmadan uygulanmaz.
- Görsel varlıklar için S3/CloudFront; kimlik ve altyapı sırları için Keycloak + Secrets Manager/KMS hedeflenir.

## Sonuçlar ve kontroller

- Ortak ürün kaydı ile birden çok satıcı offer'ının yaşam döngüsü, yetkilendirme ve veri sahipliği ayrı API/event sözleşmeleriyle korunmalıdır.
- Satıcı mevcut ortak ürüne offer açabilir; yeni ürün önerisi varsa katalog moderasyon/onay akışı gerekir. Offer fiyat/stok güncellemesi canonical ürün özelliklerini değiştiremez.
- Satıcı gönderimli siparişte stok sahipliği ve gönderim durumunu ilgili satıcı/offer belirler. Birden çok satıcının offer'ı tek checkout'ta seçilirse sipariş satıcı alt-siparişlerine bölünür; teslimat ve iptal durumları bağımsız izlenir.
- Ödeme akışı imzalı webhook doğrulaması, idempotency, durum makinesi, audit kaydı ve sandbox testleri olmadan canlıya çıkamaz. İptal/iade ve satıcı hakedişleri ödeme sağlayıcısı sözleşmesi ve hukuki/finansal gereksinimler netleşince tasarlanır.
- Çift yazım kaynaklı tutarsızlıklar için outbox/uzlaştırma ve replay testleri gerekir.
- Search consumer event'i birden fazla kez alabilir; projection deterministik/idempotent tasarlanır.
- MongoDB için backup/restore, TTL/retention ve indeks maliyeti; her servis için ayrı erişim kimliği tanımlanır.
- Elasticsearch yerine OpenSearch Service seçimi API, analiz eklentileri ve sürüm uyumluluğu testlerine bağlıdır.
- EKS ve managed servisler maliyetlidir. AWS kaynağı oluşturulmadan önce hesap, bölge, bütçe alarmı ve kaynak imha planı onaylanır.
- Servis sınırları ilk ürün hipotezidir; katalog sahipliği, fulfillment ve ödeme kuralları doğrulanınca ADR güncellenir.
