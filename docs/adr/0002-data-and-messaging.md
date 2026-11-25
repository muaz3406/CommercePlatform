# ADR 0002: Veri ve mesajlaşma teknolojileri

- Durum: Kabul edildi
- Tarih: 2026-11-24

## Karar

Transactional iş verisi için PostgreSQL; ölçülmüş cache ve geçici dağıtık koordinasyon ihtiyaçları için Redis seçildi. Kafka, servisler arası asenkron olaylar ürün akışında gerekli olduğunda devreye alınacak. Elasticsearch yalnızca türetilmiş arama indeksi olarak kullanılacak. MongoDB ve Go başlangıç yığınına eklenmeyecek; somut gereksinim ve ölçümle yeni ADR açılacak.

## Gerekçe

Çok sayıda teknoloji kullanmak tek başına ölçek veya güvenlik sağlamaz. Birden fazla veri deposu consistency, backup, upgrade ve on-call yükü yaratır. Önce PostgreSQL üzerinde doğru model ve ölçülebilir performans kurulmalıdır.

## Sonuçlar

- Her servisin sahip olduğu veri ve migration stratejisi belgelenir.
- Kafka eklenirken transactional outbox, idempotent consumer, şema uyumluluğu ve poison-message politikası gerekir.
- Redis veya Elasticsearch kaybolduğunda iş doğruluğu bozulmamalıdır.
- MongoDB/Go eklemek için gerekçe, operasyon maliyeti ve geri dönüş planı karara eklenmelidir.
