import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, NavLink, Outlet, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { LayoutDashboard, LogOut, Package, Search, ShoppingCart, User } from 'lucide-react'
import { catalogApi } from '../api/api'
import { useAuth } from '../auth/AuthContext'
import { store } from '../brand'
import { useCart } from '../cart/CartContext'
import { useAsync } from '../useAsync'
import { Logo } from './Logo'

const CURRENT_YEAR = new Date().getFullYear()

function SearchBox() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const [term, setTerm] = useState(params.get('q') ?? '')

  function submit(event: FormEvent) {
    event.preventDefault()
    const q = term.trim()
    navigate(q ? `/?q=${encodeURIComponent(q)}#produtos` : '/#produtos')
  }

  return (
    <form className="header-search" role="search" onSubmit={submit}>
      <input
        type="search"
        placeholder="O que você está procurando?"
        value={term}
        onChange={(e) => setTerm(e.target.value)}
        aria-label="Buscar produtos"
      />
      <button aria-label="Buscar">
        <Search size={18} />
      </button>
    </form>
  )
}

export function Layout() {
  const { user, isAdmin, logout } = useAuth()
  const { cart } = useCart()
  const navigate = useNavigate()
  const location = useLocation()
  const [params] = useSearchParams()
  const categories = useAsync(catalogApi.categories)

  const activeCategory = location.pathname === '/' ? params.get('categoria') : null
  const count = cart?.totalItems ?? 0

  function handleLogout() {
    logout()
    navigate('/')
  }

  return (
    <div className="app">
      {store.notice && <div className="topbar">{store.notice}</div>}

      <header className="header">
        <div className="container header-main">
          <Link to="/" aria-label={`${store.fullName}, página inicial`}>
            <Logo />
          </Link>

          {/* key: recria a busca ao trocar de página, para o texto acompanhar a URL */}
          <SearchBox key={location.search} />

          <nav className="header-actions" aria-label="Conta">
            {user ? (
              <>
                <NavLink to="/orders" className="header-action">
                  <Package size={22} strokeWidth={1.8} />
                  <span className="action-text">
                    <small>Olá, {user.name.split(' ')[0]}</small>
                    Meus pedidos
                  </span>
                </NavLink>
                <button className="header-action" onClick={handleLogout} title="Sair">
                  <LogOut size={20} strokeWidth={1.8} />
                  <span className="action-text">Sair</span>
                </button>
              </>
            ) : (
              <NavLink to="/login" className="header-action">
                <User size={22} strokeWidth={1.8} />
                <span className="action-text">
                  <small>Bem-vindo</small>
                  Entrar ou cadastrar
                </span>
              </NavLink>
            )}
            <NavLink to="/cart" className="header-action" aria-label={`Carrinho com ${count} itens`}>
              <span className="icon-wrap">
                <ShoppingCart size={22} strokeWidth={1.8} />
                {count > 0 && <span className="cart-badge">{count}</span>}
              </span>
              <span className="action-text">Carrinho</span>
            </NavLink>
          </nav>
        </div>

        <nav className="category-bar" aria-label="Categorias">
          <div className="container">
            <Link to="/" className={location.pathname === '/' && !activeCategory && !params.get('q') ? 'active' : ''}>
              Início
            </Link>
            {categories.data?.map((c) => (
              <Link
                key={c.id}
                to={`/?categoria=${c.id}#produtos`}
                className={activeCategory === String(c.id) ? 'active' : ''}
              >
                {c.name}
              </Link>
            ))}
            {user && <Link to="/addresses">Endereços</Link>}
            {isAdmin && (
              <Link to="/admin" className="admin-link">
                <LayoutDashboard size={14} />
                Painel admin
              </Link>
            )}
          </div>
        </nav>
      </header>

      <main className="main">
        <Outlet />
      </main>

      <footer className="footer">
        <div className="container footer-grid">
          <div className="footer-about">
            <Logo />
            <p>{store.heroSubtitle}</p>
          </div>
          <div>
            <h4>Loja</h4>
            <ul>
              <li>
                <Link to="/">Início</Link>
              </li>
              <li>
                <Link to="/#produtos">Todos os produtos</Link>
              </li>
              <li>
                <Link to="/cart">Carrinho</Link>
              </li>
            </ul>
          </div>
          <div>
            <h4>Minha conta</h4>
            <ul>
              <li>
                <Link to="/orders">Pedidos</Link>
              </li>
              <li>
                <Link to="/addresses">Endereços</Link>
              </li>
              {!user && (
                <li>
                  <Link to="/register">Criar conta</Link>
                </li>
              )}
            </ul>
          </div>
          <div>
            <h4>Formas de pagamento</h4>
            <div className="pay-chips">
              <span>Pix</span>
              <span>Cartão de crédito</span>
              <span>Boleto</span>
            </div>
          </div>
        </div>
        <div className="container footer-bottom">
          <span>
            © {CURRENT_YEAR} {store.fullName}
          </span>
          <span>{store.notice}</span>
        </div>
      </footer>
    </div>
  )
}
