import { usePageTitle } from '../brand'
import { useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { orderApi } from '../api/api'
import type { CSSProperties } from 'react'
import type { Order, PaymentMethod } from '../api/types'
import { Alert, Loading, StatusBadge } from '../components/ui'
import {
  formatDate,
  formatMoney,
  formatZip,
  orderNumber,
  orderStatusLabel,
  orderTimeline,
  paymentMethodLabel,
  paymentStatusLabel,
} from '../format'
import { errorMessage, useAsync } from '../useAsync'

function Progress({ order }: { order: Order }) {
  if (order.status === 'CANCELED') {
    return <div className="canceled-banner">Pedido cancelado. Os itens voltaram ao estoque.</div>
  }

  const index = orderTimeline.indexOf(order.status)
  const percent = (index / (orderTimeline.length - 1)) * 100

  return (
    <ol className="progress" style={{ '--progress': `${percent}%` } as CSSProperties}>
      {orderTimeline.map((status, i) => (
        <li key={status} className={i < index ? 'done' : i === index ? 'done current' : ''}>
          {orderStatusLabel[status]}
        </li>
      ))}
    </ol>
  )
}

export function OrderPage() {
  const id = Number(useParams().id)
  const justCreated = (useLocation().state as { justCreated?: boolean } | null)?.justCreated

  const order = useAsync(() => orderApi.get(id), [id])
  const payments = useAsync(() => orderApi.payments(id), [id])
  usePageTitle(`Pedido ${orderNumber(id)}`)

  const [method, setMethod] = useState<PaymentMethod>('PIX')
  const [cardToken, setCardToken] = useState('tok_visa')
  const [message, setMessage] = useState<{ kind: 'error' | 'success' | 'info'; text: string } | null>(null)
  const [busy, setBusy] = useState(false)

  if (order.loading) return <Loading />
  if (order.error || !order.data) return <Alert>{order.error ?? 'Pedido não encontrado'}</Alert>

  const o = order.data
  const hasPendingPayment = payments.data?.some((p) => p.status === 'PENDING') ?? false
  const canPay = o.status === 'PENDING_PAYMENT' && !hasPendingPayment

  async function pay() {
    setBusy(true)
    setMessage(null)
    try {
      const payment = await orderApi.pay(id, method, method === 'CREDIT_CARD' ? cardToken : undefined)
      if (payment.status === 'APPROVED') setMessage({ kind: 'success', text: 'Pagamento aprovado.' })
      else if (payment.status === 'PENDING') setMessage({ kind: 'info', text: 'Pagamento aguardando confirmação.' })
      else setMessage({ kind: 'error', text: payment.failureReason ?? 'Pagamento recusado.' })
      await Promise.all([order.reload(), payments.reload()])
    } catch (e) {
      setMessage({ kind: 'error', text: errorMessage(e) })
    } finally {
      setBusy(false)
    }
  }

  async function cancel() {
    if (!confirm('Cancelar este pedido? Os itens voltam ao estoque.')) return
    setBusy(true)
    try {
      order.setData(await orderApi.cancel(id))
    } catch (e) {
      setMessage({ kind: 'error', text: errorMessage(e) })
    } finally {
      setBusy(false)
    }
  }

  const a = o.shippingAddress

  return (
    <section>
      {o.status === 'PENDING_PAYMENT' && (
        <ol className="steps">
          <li className="done">01 Carrinho</li>
          <li className="done">02 Entrega</li>
          <li className="current">03 Pagamento</li>
        </ol>
      )}

      <Link to="/orders" className="back eyebrow">
        ← Pedidos
      </Link>
      <header className="page-title">
        <div>
          <span className="eyebrow mono">{formatDate(o.createdAt)}</span>
          <h1>Pedido {orderNumber(o.id)}</h1>
        </div>
        <StatusBadge status={o.status} />
      </header>

      {justCreated && o.status === 'PENDING_PAYMENT' && (
        <Alert kind="success">Pedido criado e itens reservados. Falta só o pagamento.</Alert>
      )}

      <Progress order={o} />

      <div className="two-col">
        <div>
          <ul className="line-items">
            {o.items.map((item) => (
              <li key={item.productId} className="line-item no-thumb">
                <div className="line-item-name">
                  <Link to={`/products/${item.productId}`}>{item.productName}</Link>
                  <span className="eyebrow">
                    {item.productSku} · {formatMoney(item.unitPrice)} cada
                  </span>
                </div>
                <span className="num muted">× {item.quantity}</span>
                <span className="num">{formatMoney(item.subtotal)}</span>
              </li>
            ))}
          </ul>

          <div className="section">
            <h3>Entrega</h3>
            <address className="address-block">
              {a.recipientName}
              <br />
              {a.street}, {a.number}
              {a.complement ? ` — ${a.complement}` : ''}
              <br />
              {a.district} · {a.city} / {a.state} · <span className="mono">{formatZip(a.zipCode)}</span>
            </address>
          </div>

          {payments.data && payments.data.length > 0 && (
            <div className="section">
              <h3>Tentativas de pagamento</h3>
              <ul className="plain-list">
                {payments.data.map((p) => (
                  <li key={p.id}>
                    <span>
                      {paymentMethodLabel[p.method]}
                      {p.failureReason && <span className="muted"> — {p.failureReason}</span>}
                    </span>
                    <span className="mono">
                      {paymentStatusLabel[p.status]} · {formatDate(p.createdAt)}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

        <aside className="summary">
          <div className="summary-total first">
            <span className="label">Total</span>
            <strong>{formatMoney(o.total)}</strong>
          </div>

          {message && <Alert kind={message.kind}>{message.text}</Alert>}

          {canPay && (
            <>
              <span className="eyebrow">Forma de pagamento</span>
              <div className="option-list" role="radiogroup" aria-label="Forma de pagamento">
                {(Object.keys(paymentMethodLabel) as PaymentMethod[]).map((m) => (
                  <label key={m} className={`option ${method === m ? 'selected' : ''}`}>
                    <input type="radio" name="method" checked={method === m} onChange={() => setMethod(m)} />
                    <div className="option-body">
                      <strong>{paymentMethodLabel[m]}</strong>
                      {m === 'BOLETO' && <span className="muted small">Fica pendente de confirmação</span>}
                    </div>
                  </label>
                ))}
              </div>
              {method === 'CREDIT_CARD' && (
                <label className="field">
                  <span>Token do cartão (simulação)</span>
                  <input className="mono" value={cardToken} onChange={(e) => setCardToken(e.target.value)} />
                  <small className="hint">Use tok_declined para simular uma recusa.</small>
                </label>
              )}
              <button className="btn btn-block" onClick={pay} disabled={busy}>
                Pagar {formatMoney(o.total)}
              </button>
            </>
          )}

          {hasPendingPayment && o.status === 'PENDING_PAYMENT' && (
            <Alert kind="info">Pagamento aguardando confirmação.</Alert>
          )}

          {o.status === 'PENDING_PAYMENT' && (
            <button className="btn-link muted" onClick={cancel} disabled={busy}>
              Cancelar pedido
            </button>
          )}
        </aside>
      </div>
    </section>
  )
}
