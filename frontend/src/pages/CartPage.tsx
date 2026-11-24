import { usePageTitle } from '../brand'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { cartApi } from '../api/api'
import { useCart } from '../cart/CartContext'
import { ProductCover } from '../components/ProductVisual'
import { Alert, Empty, Loading, PageTitle, Stepper } from '../components/ui'
import { formatMoney } from '../format'
import { errorMessage } from '../useAsync'

export function CartPage() {
  usePageTitle('Carrinho')
  const { cart, setCart, refresh } = useCart()
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState<number | null>(null)

  async function run(productId: number, action: () => Promise<void>) {
    setError(null)
    setBusy(productId)
    try {
      await action()
    } catch (e) {
      setError(errorMessage(e))
      await refresh()
    } finally {
      setBusy(null)
    }
  }

  if (!cart) return <Loading />

  if (cart.items.length === 0) {
    return (
      <section>
        <PageTitle eyebrow="Carrinho" title="Carrinho" />
        <Empty title="Seu carrinho está vazio.">
          <Link to="/" className="btn">
            Ver catálogo
          </Link>
        </Empty>
      </section>
    )
  }

  return (
    <section>
      <PageTitle
        eyebrow={`${cart.totalItems} ${cart.totalItems === 1 ? 'item' : 'itens'}`}
        title="Carrinho"
      />
      <Alert>{error}</Alert>

      <div className="two-col">
        <ul className="line-items">
          {cart.items.map((item) => (
            <li key={item.productId} className="line-item">
              <ProductCover productId={item.productId} name={item.productName} compact />
              <div className="line-item-name">
                <Link to={`/products/${item.productId}`}>{item.productName}</Link>
                <span className="eyebrow">
                  {item.sku} · {formatMoney(item.unitPrice)}
                </span>
                <button
                  className="btn-link muted"
                  disabled={busy === item.productId}
                  onClick={() => run(item.productId, async () => setCart(await cartApi.remove(item.productId)))}
                >
                  Remover
                </button>
              </div>
              <Stepper
                small
                value={item.quantity}
                label={`Quantidade de ${item.productName}`}
                onChange={(quantity) =>
                  run(item.productId, async () => setCart(await cartApi.update(item.productId, quantity)))
                }
              />
              <span className="num">{formatMoney(item.subtotal)}</span>
            </li>
          ))}
        </ul>

        <aside className="summary">
          <span className="eyebrow">Resumo</span>
          {cart.items.map((item) => (
            <div key={item.productId} className="summary-line">
              <span>
                {item.quantity} × {item.productName}
              </span>
              <span className="num">{formatMoney(item.subtotal)}</span>
            </div>
          ))}
          <div className="summary-total">
            <span className="label">Total</span>
            <strong>{formatMoney(cart.total)}</strong>
          </div>
          <Link to="/checkout" className="btn btn-block">
            Fechar pedido
          </Link>
          <p className="small muted">Os preços são os atuais e ficam fixados quando o pedido é criado.</p>
        </aside>
      </div>
    </section>
  )
}
