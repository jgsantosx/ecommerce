import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ChevronRight, ShoppingCart, Zap } from 'lucide-react'
import { cartApi, catalogApi } from '../api/api'
import { useAuth } from '../auth/AuthContext'
import { usePageTitle } from '../brand'
import { useCart } from '../cart/CartContext'
import { ProductVisual } from '../components/ProductVisual'
import { Alert, Loading, Stepper } from '../components/ui'
import { formatMoney } from '../format'
import { errorMessage, useAsync } from '../useAsync'

export function ProductPage() {
  const id = Number(useParams().id)
  const { user } = useAuth()
  const { setCart } = useCart()
  const navigate = useNavigate()

  const product = useAsync(() => catalogApi.product(id), [id])
  const stock = useAsync(() => catalogApi.stock(id), [id])
  const images = useAsync(() => catalogApi.images(id), [id])
  usePageTitle(product.data?.name)

  const [selected, setSelected] = useState(0)
  const [quantity, setQuantity] = useState(1)
  const [adding, setAdding] = useState(false)
  const [message, setMessage] = useState<{ kind: 'error' | 'success'; text: string } | null>(null)

  if (product.loading) return <Loading />
  if (product.error || !product.data) return <Alert>{product.error ?? 'Produto não encontrado'}</Alert>

  const p = product.data
  const available = stock.data?.quantity ?? 0
  const gallery = images.data ?? []
  const max = Math.min(available, 99)

  async function addToCart(goToCart: boolean) {
    if (!user) {
      navigate('/login', { state: { from: `/products/${id}` } })
      return
    }
    setAdding(true)
    setMessage(null)
    try {
      setCart(await cartApi.add(id, quantity))
      if (goToCart) {
        navigate('/cart')
        return
      }
      setMessage({ kind: 'success', text: `${quantity} ${quantity === 1 ? 'unidade adicionada' : 'unidades adicionadas'} ao carrinho.` })
    } catch (e) {
      setMessage({ kind: 'error', text: errorMessage(e) })
    } finally {
      setAdding(false)
    }
  }

  const stockClass = available === 0 ? 'none' : available <= 3 ? 'low' : ''
  const stockText =
    available === 0
      ? 'Produto esgotado'
      : available <= 3
        ? `Últimas ${available} ${available === 1 ? 'unidade' : 'unidades'}`
        : 'Em estoque'

  return (
    <>
      <nav className="breadcrumb" aria-label="Você está em">
        <Link to="/">Início</Link>
        <ChevronRight size={14} />
        <Link to={`/?categoria=${p.category.id}#produtos`}>{p.category.name}</Link>
        <ChevronRight size={14} />
        <span>{p.name}</span>
      </nav>

      <section className="product-detail">
        <div className={`gallery ${gallery.length > 1 ? '' : 'single'}`}>
          {gallery.length > 1 && (
            <div className="thumbs">
              {gallery.map((img, i) => (
                <button
                  key={img.id}
                  className={i === selected ? 'active' : ''}
                  onClick={() => setSelected(i)}
                  aria-label={`Imagem ${i + 1}`}
                >
                  <ProductVisual productId={p.id} name={p.name} src={img.url} compact />
                </button>
              ))}
            </div>
          )}
          <ProductVisual productId={p.id} name={p.name} src={gallery[selected]?.url} />
        </div>

        <div className="product-info card">
          <div>
            <span className="eyebrow">
              {p.category.name} · Cód. {p.sku}
            </span>
            <h1>{p.name}</h1>
          </div>

          <span className="price-lg">{formatMoney(p.price)}</span>

          {!p.active ? (
            <Alert kind="info">Este produto não está mais disponível.</Alert>
          ) : (
            <>
              <span className={`stock-line ${stockClass}`}>{stockText}</span>
              {available > 0 && (
                <>
                  <div className="actions">
                    <span className="label">Quantidade</span>
                    <Stepper value={quantity} max={max} onChange={setQuantity} />
                  </div>
                  <div className="buy-row">
                    <button className="btn" onClick={() => addToCart(true)} disabled={adding}>
                      <Zap size={18} /> Comprar agora
                    </button>
                    <button className="btn btn-ghost" onClick={() => addToCart(false)} disabled={adding}>
                      <ShoppingCart size={18} /> Adicionar ao carrinho
                    </button>
                  </div>
                </>
              )}
            </>
          )}

          {message && (
            <Alert kind={message.kind}>
              {message.text}{' '}
              {message.kind === 'success' && (
                <Link to="/cart" className="btn-link">
                  Ver carrinho
                </Link>
              )}
            </Alert>
          )}

          <dl className="specs">
            <div>
              <dt>Categoria</dt>
              <dd>{p.category.name}</dd>
            </div>
            <div>
              <dt>Código</dt>
              <dd>{p.sku}</dd>
            </div>
            <div>
              <dt>Pagamento</dt>
              <dd>Pix, cartão de crédito ou boleto</dd>
            </div>
          </dl>
        </div>
      </section>

      {p.description && (
        <section className="description card">
          <h2>Descrição</h2>
          <p>{p.description}</p>
        </section>
      )}
    </>
  )
}
