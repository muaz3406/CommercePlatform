import Link from "next/link";

export default function Home() {
  return (
    <main className="storefront">
      <header className="store-header">
        <Link className="brand" href="/">commerce</Link>
        <nav aria-label="Ana menü">
          <a href="#products">Ürünleri keşfet</a>
          <a href="#seller">Satıcı ol</a>
        </nav>
        <a className="account-link" href="#account">Hesabım</a>
      </header>

      <section className="hero">
        <p className="eyebrow">Tek yerde, birçok seçenek</p>
        <h1>Aradığını bul.<br />Güvenle alışveriş yap.</h1>
        <p className="hero-copy">
          Farklı satıcıların ürünlerini karşılaştırabileceğin yeni nesil alışveriş platformu.
        </p>
        <a className="primary-link" href="#products">Ürünleri keşfet <span aria-hidden="true">→</span></a>
      </section>

      <section className="products" id="products">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Keşfet</p>
            <h2>Öne çıkan ürünler</h2>
          </div>
          <span className="muted">Katalog yakında burada</span>
        </div>
        <div className="empty-state">
          <span className="empty-icon" aria-hidden="true">✦</span>
          <h3>Mağazamızı hazırlıyoruz</h3>
          <p>Ürün kataloğu ve satıcı teklifleri API bağlantısı tamamlandığında burada listelenecek.</p>
        </div>
      </section>

      <footer id="seller">
        <span>commerce</span>
        <span>Satıcı başvuruları yakında açılacak.</span>
      </footer>
    </main>
  );
}
