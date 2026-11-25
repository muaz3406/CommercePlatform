# ADR 0001: Başlangıç servis sınırları

- Durum: Kabul edildi (ilk ürün gereksinimleriyle yeniden değerlendirilecek)
- Tarih: 2026-11-24

## Bağlam

Proje mikroservis hedefli olsa da katalog, stok, sipariş, ödeme ve müşteri sınırları henüz ürün kurallarıyla doğrulanmadı. Çok erken servis bölmek dağıtık işlem ve işletim maliyeti doğurur.

## Karar

İlk deploy edilebilir bileşen `catalog-service` olarak başlatılır. Servisler daha sonra bounded context ve bağımsız ölçek/ekip/dağıtım ihtiyacıyla ayrılır. Her servis veri sahipliğine ve API/event sözleşmesine sahip olur; servisler arası DB paylaşımı yapılmaz.

## Sonuçlar

- Erken dönemde küçük ve test edilebilir bir dikey dilim elde edilir.
- Katalog servisinden sonra diğer sınırlar ürün kurallarıyla karar verilir; her servis bağımsız deploy edilebilir hedeflenir.
- Yerel geliştirmede tek PostgreSQL instance kullanılabilir, ancak production'da servis veri sahipliği ve erişim izolasyonu korunmalıdır.
- Her servisi ilk günden ayrı altyapı/cluster ile işletme kararı alınmamıştır.
