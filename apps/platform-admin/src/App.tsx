import './App.css'

function App() {
  return (
    <div className="portal">
      <aside className="sidebar">
        <a className="brand" href="/">commerce<span>ops</span></a>
        <p className="nav-label">PLATFORM</p>
        <a className="nav-item active" href="#overview">Genel bakış</a>
        <a className="nav-item" href="#catalog">Katalog moderasyonu</a>
        <a className="nav-item" href="#sellers">Satıcılar</a>
        <a className="nav-item" href="#orders">Sipariş operasyonları</a>
        <div className="sidebar-bottom">Yönetici alanı</div>
      </aside>
      <main className="content" id="overview">
        <header className="topbar"><span>Platform Yönetimi</span><span className="avatar">Y</span></header>
        <section className="welcome">
          <p className="eyebrow">OPERASYON MERKEZİ</p>
          <h1>Platforma genel bakış</h1>
          <p>Katalog, satıcı ve sipariş operasyonlarını merkezi olarak yönetin.</p>
        </section>
        <section className="stats" aria-label="Platform özeti">
          <article><span>Bekleyen ürün önerileri</span><strong>—</strong><small>Katalog moderasyonu sonraki aşamada</small></article>
          <article><span>Satıcı başvuruları</span><strong>—</strong><small>Satıcı yönetimi sonraki aşamada</small></article>
          <article><span>Aktif siparişler</span><strong>—</strong><small>Sipariş operasyonları sonraki aşamada</small></article>
        </section>
        <section className="setup-note">
          <div className="note-icon">i</div>
          <div><h2>Yetkili yönetim alanı</h2><p>Bu panel ayrı bir React uygulamasıdır. Yönetici rolleri ve servis API erişimi backend tarafında doğrulanacaktır.</p></div>
        </section>
      </main>
    </div>
  )
}

export default App
