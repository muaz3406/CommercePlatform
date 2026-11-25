# ADR 0003: Branch ve deployment modeli

- Durum: Kabul edildi
- Tarih: 2026-11-24

## Karar

`main` üretim sürümlerini, `develop` entegrasyon/test hattını temsil eder. Kısa ömürlü `feature/*` branch'leri PR ile `develop`'a alınır; release adayı doğrulanınca `develop` → `main` PR'ı açılır. Dallar yerel depoda oluşturulur.

Ortam konfigürasyonu branch'e bağlanmaz: aynı immutable image/artifact önce test/staging, onay sonrası production ortamına dağıtılır. Ortam sırları ve onayları CI/CD deployment environment'larında tutulur.

## Sonuçlar

- `main` ve `develop` için GitHub branch protection, zorunlu CI ve en az bir PR review yapılandırılmalıdır.
- Bu yerel çalışma alanında uzak GitHub deposu olmadığı için CI ve koruma kuralları henüz etkin değildir.
- Deployment manifestleri ortam başına güvenli konfigürasyon kullanır; kod branch'leri secret taşımaz.
