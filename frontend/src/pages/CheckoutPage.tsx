import { usePageTitle } from '../brand'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { addressApi, orderApi } from '../api/api'
import { useCart } from '../cart/CartContext'
import { AddressForm } from '../components/AddressForm'
import { Alert, Empty, Loading, PageTitle } from '../components/ui'
import { formatMoney, formatZip } from '../format'
import { errorMessage, useAsync } from '../useAsync'

export function CheckoutPage() {
  usePageTitle('Entrega')
  const { cart, refresh } = useCart()
  const navigate = useNavigate()
  const addresses = useAsync(addressApi.list)
  const [chosenId, setAddressId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  // Valor derivado: o que o usuário escolheu ou, se nada, o endereço principal.
  const list = addresses.data ?? []
  const addressId = chosenId ?? (list.find((a) => a.defaultAddress) ?? list[0])?.id ?? null

  if (!cart || addresses.loading) return <Loading />

  if (cart.items.length === 0) {
    return (
      <section>
        <PageTitle eyebrow="Checkout" title="Entrega" />
        <Empty title="Seu carrinho está vazio.">
          <Link to="/" className="btn">
            Ver catálogo
          </Link>
        </Empty>
      </section>
    )
  }

  async function placeOrder() {
    if (addressId === null) return
    setSubmitting(true)
    setError(null)
    try {
      const order = await orderApi.checkout(addressId)
      await refresh()
      navigate(`/orders/${order.id}`, { state: { justCreated: true } })
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section>
      <ol className="steps">
        <li className="done">01 Carrinho</li>
        <li className="current">02 Entrega</li>
        <li>03 Pagamento</li>
      </ol>
      <PageTitle title="Endereço de entrega" />

      <div className="two-col">
        <div>
          {list.length === 0 ? (
            <>
              <p className="lede">Cadastre um endereço para continuar.</p>
              <AddressForm
                submitLabel="Salvar e usar este endereço"
                onSubmit={async (input) => {
                  const created = await addressApi.create(input)
                  await addresses.reload()
                  setAddressId(created.id)
                }}
              />
            </>
          ) : (
            <>
              <div className="option-list" role="radiogroup" aria-label="Endereço de entrega">
                {list.map((a) => (
                  <label key={a.id} className={`option ${addressId === a.id ? 'selected' : ''}`}>
                    <input
                      type="radio"
                      name="address"
                      checked={addressId === a.id}
                      onChange={() => setAddressId(a.id)}
                    />
                    <div className="option-body">
                      <strong>
                        {a.recipientName}
                        {a.defaultAddress && <span className="tag">Principal</span>}
                      </strong>
                      <span className="muted">
                        {a.street}, {a.number}
                        {a.complement ? ` — ${a.complement}` : ''} · {a.district}
                      </span>
                      <span className="muted">
                        {a.city} / {a.state} · <span className="mono">{formatZip(a.zipCode)}</span>
                      </span>
                    </div>
                  </label>
                ))}
              </div>
              <p className="small spaced">
                <Link to="/addresses">Gerenciar endereços</Link>
              </p>
            </>
          )}
        </div>

        <aside className="summary">
          <span className="eyebrow">Seu pedido</span>
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
          <Alert>{error}</Alert>
          <button className="btn btn-block" disabled={addressId === null || submitting} onClick={placeOrder}>
            {submitting ? 'Reservando itens…' : 'Confirmar pedido'}
          </button>
          <p className="small muted">Os itens ficam reservados. O pagamento é a próxima etapa.</p>
        </aside>
      </div>
    </section>
  )
}
