import { useEffect, useMemo } from 'react'
import { Link, useLocation, useSearchParams } from 'react-router-dom'
import { ChevronRight, CreditCard, Package, PackageCheck, ShoppingBag, Truck, XCircle } from 'lucide-react'
import { catalogApi } from '../api/api'
import type { Product } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { store, usePageTitle } from '../brand'
import { ProductCard } from '../components/ProductCard'
import { Alert, Empty, Loading } from '../components/ui'
import { useAsync } from '../useAsync'

const highlightIcons = {
  payment: CreditCard,
  tracking: Truck,
  cancel: XCircle,
}

type Sort = 'relevance' | 'price-asc' | 'price-desc' | 'name'

const sorters: Record<Sort, (a: Product, b: Product) => number> = {
  relevance: (a, b) => b.id - a.id,
  'price-asc': (a, b) => a.price - b.price,
  'price-desc': (a, b) => b.price - a.price,
  name: (a, b) => a.name.localeCompare(b.name, 'pt-BR'),
}

export function CatalogPage() {
  const { user } = useAuth()
  const location = useLocation()
  const [params, setParams] = useSearchParams()
  const products = useAsync(catalogApi.products)
  const categories = useAsync(catalogApi.categories)

  const q = params.get('q')?.trim() ?? ''
  const categoryId = params.get('categoria') ? Number(params.get('categoria')) : null
  const sort = (params.get('ordem') as Sort) ?? 'relevance'
  const isHome = !q && categoryId === null

  const category = categories.data?.find((c) => c.id === categoryId)
  usePageTitle(q ? `Busca: ${q}` : category?.name)

  const active = useMemo(() => (products.data ?? []).filter((p) => p.active), [products.data])

  const visible = useMemo(() => {
    const term = q.toLowerCase()
    return active
      .filter((p) => categoryId === null || p.category.id === categoryId)
      .filter((p) => !term || `${p.name} ${p.description ?? ''} ${p.category.name}`.toLowerCase().includes(term))
      .sort(sorters[sort] ?? sorters.relevance)
  }, [active, categoryId, q, sort])

  const countByCategory = (id: number) => active.filter((p) => p.category.id === id).length
  const usedCategories = (categories.data ?? []).filter((c) => countByCategory(c.id) > 0)

  // O React Router não rola até âncoras (#produtos) sozinho.
  useEffect(() => {
    if (location.hash === '#produtos' && !products.loading) {
      document.getElementById('produtos')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }, [location.hash, location.search, products.loading])

  function setParam(key: string, value: string | null) {
    const next = new URLSearchParams(params)
    if (value === null || value === '') next.delete(key)
    else next.set(key, value)
    setParams(next)
  }

  const title = q ? `Resultados para “${q}”` : (category?.name ?? 'Todos os produtos')

  return (
    <>
      {isHome && (
        <>
          <section className="hero">
            <div>
              <h1>{store.heroTitle}</h1>
              <p>{store.heroSubtitle}</p>
              <div className="actions">
                <a href="#produtos" className="btn">
                  <ShoppingBag size={18} /> Ver produtos
                </a>
                {!user && (
                  <Link to="/register" className="btn btn-ghost">
                    Criar conta
                  </Link>
                )}
              </div>
            </div>
            <div className="hero-art" aria-hidden="true">
              <div>
                <ShoppingBag size={44} strokeWidth={1.5} />
              </div>
              <div>
                <Package size={44} strokeWidth={1.5} />
              </div>
              <div>
                <CreditCard size={44} strokeWidth={1.5} />
              </div>
              <div>
                <PackageCheck size={44} strokeWidth={1.5} />
              </div>
            </div>
          </section>

          <section className="highlights" aria-label="Vantagens">
            {store.highlights.map((h) => {
              const Icon = highlightIcons[h.icon]
              return (
                <div key={h.title} className="highlight">
                  <Icon size={26} strokeWidth={1.7} />
                  <div>
                    <strong>{h.title}</strong>
                    <span>{h.text}</span>
                  </div>
                </div>
              )
            })}
          </section>

          {usedCategories.length > 0 && (
            <section>
              <div className="section-head">
                <h2>Categorias</h2>
              </div>
              <div className="category-tiles">
                {usedCategories.map((c) => (
                  <Link key={c.id} to={`/?categoria=${c.id}#produtos`} className="category-tile">
                    <div>
                      <strong>{c.name}</strong>
                      <span>
                        {countByCategory(c.id)} {countByCategory(c.id) === 1 ? 'produto' : 'produtos'}
                      </span>
                    </div>
                    <ChevronRight size={18} />
                  </Link>
                ))}
              </div>
            </section>
          )}
        </>
      )}

      <section id="produtos">
        {!isHome && (
          <nav className="breadcrumb" aria-label="Você está em">
            <Link to="/">Início</Link>
            <ChevronRight size={14} />
            <span>{title}</span>
          </nav>
        )}

        <div className="section-head">
          <div>
            <h2>{title}</h2>
            {!products.loading && (
              <span className="eyebrow">
                {visible.length} {visible.length === 1 ? 'produto encontrado' : 'produtos encontrados'}
              </span>
            )}
          </div>
          <label className="sort">
            Ordenar por
            <select value={sort} onChange={(e) => setParam('ordem', e.target.value === 'relevance' ? null : e.target.value)}>
              <option value="relevance">Mais recentes</option>
              <option value="price-asc">Menor preço</option>
              <option value="price-desc">Maior preço</option>
              <option value="name">Nome (A–Z)</option>
            </select>
          </label>
        </div>

        {usedCategories.length > 1 && (
          <div className="toolbar">
            <div className="chips" role="group" aria-label="Filtrar por categoria">
              <button className={`chip ${categoryId === null ? 'active' : ''}`} onClick={() => setParam('categoria', null)}>
                Todas
              </button>
              {usedCategories.map((c) => (
                <button
                  key={c.id}
                  className={`chip ${categoryId === c.id ? 'active' : ''}`}
                  onClick={() => setParam('categoria', String(c.id))}
                >
                  {c.name}
                </button>
              ))}
            </div>
          </div>
        )}

        <Alert>{products.error}</Alert>

        {products.loading ? (
          <Loading />
        ) : visible.length === 0 ? (
          <Empty title="Nenhum produto encontrado">
            <p className="muted">Tente outra busca ou veja todas as categorias.</p>
            <Link to="/" className="btn btn-ghost">
              Ver todos os produtos
            </Link>
          </Empty>
        ) : (
          <div className="product-grid">
            {visible.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}
      </section>
    </>
  )
}
