# ADR 0006: Checkout için durable Saga orchestration

- Durum: Kabul edildi; uygulama Aşama 2'de
- Tarih: 2026-11-24

## Bağlam

Tek checkout birden fazla satıcı offer'ı ve ayrı servislerdeki inventory, payment ve order verisini kapsar. Servisler arası ACID transaction kullanılamaz; tek bir gRPC zinciri içinde tüm süreci açık tutmak uzun timeout, crash ve tekrar isteklerinde state kaybı yaratır.

## Karar

`order-service` checkout sürecinin owner'ı ve Saga orchestrator'ıdır. Bir checkout tek müşteri ödemesiyle tamamlanır; satıcı bazlı alt-siparişler ayrı fulfillment/iptal durumlarıyla izlenir. Saga state PostgreSQL'de durable tutulur ve şu adımları yönetir:

1. Idempotency key ile checkout/order kaydını `PENDING` oluştur.
2. Her satıcı offer'ı için `inventory-service.ReserveStock` çağrısını ayrı idempotency key ile yap; rezervasyonlar ana sipariş ve satıcı alt-siparişleriyle ilişkilidir.
3. Sepet toplamı için `payment-service.AuthorizePayment` çağrısını bir kez yap; kart verisi order/payment domain'inde tutulmaz.
4. Ana siparişi ve satıcı alt-siparişlerini `CONFIRMED` yap; transactional outbox üzerinden order event'lerini yayınla.
5. Hata/timeout sonrası kalıcı state'ten retry/recovery yap. Checkout tamamlanamıyorsa tek payment provizyonunu `VoidPayment`, başarılı stok rezervasyonlarının tümünü `ReleaseStock` ile kompanzasyon olarak geri al; order durumunu `FAILED`/`COMPENSATING` gibi açık durumlarda tut.

Her adım tekrar çalıştırılabilir olmalıdır. gRPC çağrılarında deadline vardır; timeout, uzak tarafın işlemi yapmadığını kanıtlamaz, bu nedenle aynı idempotency key ve status query/reconciliation ile sonuç belirlenir. Saga tek DB transaction'ı veya exactly-once garantisi değildir; ara durum kullanıcıya gösterilir.

## Alternatifler

- **Choreography:** servisler birbirine event yayınlar; basit akışlarda uygundur fakat multi-seller checkout ve birden fazla kompanzasyonda süreç görünürlüğü/operasyonu zorlaşır.
- **Distributed 2PC:** servis ve DB'leri sıkı bağlar, partition durumunda kullanılabilirliği düşürür; seçilmedi.
- **Synchronous orchestration without durable state:** process crash'te ilerleme/kompanzasyon kaybolabileceğinden seçilmedi.

## Gerekli doğrulama

Order DB/outbox atomikliği, idempotency uniqueness, stok yarış koşulları, payment webhook imza/doğrulama, broker duplicate/delay, gRPC timeout sonrası belirsiz sonuç, process crash/restart ve her kompanzasyon adımının tekrar denemesi için integration/failure-injection testleri eklenmeden canlıya çıkamaz.
