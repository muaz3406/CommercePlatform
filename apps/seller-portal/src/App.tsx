import './App.css'

function App() {
  return (
    <div className="portal">
      <aside className="sidebar">
        <a className="brand" href="/">commerce<span>seller</span></a>
        <p className="nav-label">YÖNETİM</p>
        <a className="nav-item active" href="#overview">Genel bakış</a>
        <a className="nav-item" href="#offers">Ürün tekliflerim</a>
        <a className="nav-item" href="#orders">Siparişler</a>
        <a className="nav-item" href="#inventory">Stok</a>
        <div className="sidebar-bottom">Satıcı hesabı</div>
      </aside>
      <main className="content" id="overview">
        <header className="topbar"><span>Satıcı Portalı</span><span className="avatar">S</span></header>
        <section className="welcome">
          <p className="eyebrow">İŞİNİZİ BÜYÜTÜN</p>
          <h1>Satıcı panelinize hoş geldiniz</h1>
          <p>Tekliflerinizi, siparişlerinizi ve stok durumunuzu tek yerden yönetin.</p>
        </section>
        <section className="stats" aria-label="Satıcı özeti">
          <article><span>Aktif teklifler</span><strong>—</strong><small>Teklif API bağlantısı bekleniyor</small></article>
          <article><span>Yeni siparişler</span><strong>—</strong><small>Sipariş akışı sonraki aşamada</small></article>
          <article><span>Stok uyarıları</span><strong>—</strong><small>Stok servisi sonraki aşamada</small></article>
        </section>
        <section className="setup-note">
          <div className="note-icon">i</div>
          <div><h2>Başlangıç ekranı</h2><p>Bu panel ayrı bir React uygulamasıdır. Oturum açma ve servis API entegrasyonu sıradaki teslimatlarda eklenecek.</p></div>
        </section>
      </main>
    </div>
  )
}

export default App
