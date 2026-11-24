import { usePageTitle } from '../../brand'
import { Fragment, useState } from 'react'
import { adminApi } from '../../api/api'
import type { OrderStatus } from '../../api/types'
import { Alert, Empty, Loading, PageTitle, StatusBadge } from '../../components/ui'
import { formatDate, formatMoney, formatZip, orderNextStatus, orderNumber, orderStatusLabel } from '../../format'
import { errorMessage, useAsync } from '../../useAsync'

const actionLabel: Record<OrderStatus, string> = {
  PENDING_PAYMENT: 'Aguardar',
  PAID: 'Marcar pago',
  SHIPPED: 'Enviar',
  DELIVERED: 'Entregue',
  CANCELED: 'Cancelar',
}

export function AdminOrdersPage() {
  usePageTitle('Admin · Pedidos')
  const [filter, setFilter] = useState<OrderStatus | ''>('')
  const orders = useAsync(() => adminApi.orders(filter || undefined), [filter])
  const [expanded, setExpanded] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function changeStatus(id: number, status: OrderStatus) {
    if (status === 'CANCELED' && !confirm('Cancelar o pedido? O estoque será devolvido.')) return
    setError(null)
    try {
      await adminApi.updateOrderStatus(id, status)
      await orders.reload()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  const total = (orders.data ?? []).reduce((sum, o) => sum + (o.status === 'CANCELED' ? 0 : o.total), 0)

  return (
    <div>
      <PageTitle eyebrow={`${orders.data?.length ?? 0} pedidos · ${formatMoney(total)}`} title="Pedidos">
        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value as OrderStatus | '')}
          aria-label="Filtrar status"
          className="select-compact"
        >
          <option value="">Todos os status</option>
          {(Object.keys(orderStatusLabel) as OrderStatus[]).map((s) => (
            <option key={s} value={s}>
              {orderStatusLabel[s]}
            </option>
          ))}
        </select>
      </PageTitle>
      <Alert>{error ?? orders.error}</Alert>

      {orders.loading ? (
        <Loading />
      ) : !orders.data?.length ? (
        <Empty title="Nenhum pedido." />
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Pedido</th>
              <th>Data</th>
              <th>Cliente</th>
              <th>Status</th>
              <th className="num">Total</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {orders.data.map((o) => (
              <Fragment key={o.id}>
                <tr className="row-link" onClick={() => setExpanded(expanded === o.id ? null : o.id)}>
                  <td className="mono">{orderNumber(o.id)}</td>
                  <td className="mono">{formatDate(o.createdAt)}</td>
                  <td>{o.shippingAddress.recipientName}</td>
                  <td>
                    <StatusBadge status={o.status} />
                  </td>
                  <td className="num">{formatMoney(o.total)}</td>
                  <td className="actions-cell" onClick={(e) => e.stopPropagation()}>
                    {orderNextStatus[o.status].map((next) => (
                      <button
                        key={next}
                        className={`btn btn-small ${next === 'CANCELED' ? 'btn-ghost danger' : ''}`}
                        onClick={() => changeStatus(o.id, next)}
                      >
                        {actionLabel[next]}
                      </button>
                    ))}
                  </td>
                </tr>
                {expanded === o.id && (
                  <tr className="details">
                    <td colSpan={6}>
                      <ul className="plain-list">
                        {o.items.map((i) => (
                          <li key={i.productId}>
                            <span>
                              {i.quantity} × {i.productName} <span className="mono muted">{i.productSku}</span>
                            </span>
                            <span className="mono">{formatMoney(i.subtotal)}</span>
                          </li>
                        ))}
                      </ul>
                      <p className="small muted spaced">
                        Entrega: {o.shippingAddress.street}, {o.shippingAddress.number} · {o.shippingAddress.city} /{' '}
                        {o.shippingAddress.state} · {formatZip(o.shippingAddress.zipCode)} · cliente nº {o.userId}
                      </p>
                    </td>
                  </tr>
                )}
              </Fragment>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
