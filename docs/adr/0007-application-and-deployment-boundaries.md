# ADR 0007: Uygulama ve dağıtım sınırları

- Durum: Kabul edildi
- Tarih: 2026-11-25

## Bağlam

Platformun müşteri, satıcı ve platform operasyonları için farklı kullanıcı arayüzlerine ihtiyacı vardır. Bu ekranları ayrı backend'lerle çoğaltmak aynı iş kurallarını ve yetkilendirmeyi tekrarlar; tek bir arayüze bağlamak ise bu kanalların bağımsız geliştirilmesini ve dağıtılmasını zorlaştırır.

Platformun milyonlarca kullanıcıya ve yüksek sipariş hacmine ulaşması bir hedef gereksinimidir, mevcut bir kapasite garantisi değildir. Trafik profili, SLO'lar, veri hacmi ve darboğazlar ölçülmeden belirli bir altyapı boyutu veya ölçek iddiası verilemez.

## Karar

- `CommercePlatform` geliştirme deposu olarak monorepo kalır. Monorepo, çalışma zamanı veya dağıtım sınırı değildir.
- Müşteri, satıcı ve platform yöneticisi arayüzleri ayrı uygulamalar olarak tasarlanır: `apps/customer-web`, `apps/seller-portal` ve `apps/platform-admin`. Müşteri mağazası ürün/kategori sayfalarının sunucu tarafında render edilmesi ve SEO için Next.js; satıcı ve yönetim panelleri React + Vite kullanır. Her biri ayrı derlenip dağıtılabilir.
- Bu uygulamalar ortak `api-gateway` ve ilgili domain servislerini kullanır. Ekran başına backend veya domain veritabanlarına doğrudan erişim oluşturulmaz. UI'ya özel BFF, ancak kanıtlanmış veri birleştirme veya bağımsız evrim ihtiyacı çıktığında eklenir.
- Yetkilendirme her domain servisinde uygulanır. Müşteri yalnızca kendi kaynaklarına, satıcı yalnızca kendisine ait kaynaklara erişir; platform yöneticisi işlemleri dar kapsamlı rollerle korunur. İstemciden gelen kimlik/rol bilgisi yetki kanıtı sayılmaz.
- Uygulama ve servisler bağımsız dağıtılabilir hedeflenir. Edge/API katmanı ve stateless servisler gerektiğinde yatay çoğaltılır; her domain kendi verisinin sahibidir. İstek sınırları, SLO'lar, yük testleri ve gözlemlenebilirlik ölçek kararlarının parçasıdır.
- Sipariş, stok ve ödeme gibi tutarlılık gerektiren iş akışları servisler arası doğrudan veritabanı işlemiyle değil, idempotent API/event sözleşmeleri ve durable Saga/compensation ile yürütülür. Partitioning, sharding ve cache yalnızca ölçülmüş ihtiyaçla eklenir.

## Sonuçlar

- Farklı kanallar kendi yayın döngülerini ve UX'lerini korur; backend iş kuralları tekrarlanmaz.
- Servislerin bağımsız dağıtımı ekip/işletim kapasitesi, CI/CD, telemetri, güvenlik ve veri sahipliği gerektirir; bu gereksinimler aşamalı teslim edilir.
- Çok yüksek trafik hedefi yalnızca mimari tercihlerle sağlanmış sayılmaz; belirlenen trafik profiline karşı test ve operasyon tatbikatları gerekir.
- AWS kaynak boyutları ve BFF ihtiyacı bu ADR ile sabitlenmez; ürün ihtiyacı ve ölçümlerle kararlaştırılır.
