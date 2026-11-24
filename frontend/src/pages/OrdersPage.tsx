import { usePageTitle } from '../brand'
import { Link, useNavigate } from 'react-router-dom'
import { orderApi } from '../api/api'
import { Alert, Empty, Loading, PageTitle, StatusBadge } from '../components/ui'
import { formatDate, formatMoney, orderNumber } from '../format'
import { useAsync } from '../useAsync'

export function OrdersPage() {
  usePageTitle('Pedidos')
  const { data, error, loading } = useAsync(orderApi.list)
  const navigate = useNavigate()

  return (
    <section>
      <PageTitle eyebrow="Conta" title="Pedidos" />
      <Alert>{error}</Alert>
      {loading ? (
        <Loading />
      ) : !data?.length ? (
        <Empty title="Nenhum pedido ainda.">
          <Link to="/" className="btn">
            Ver catálogo
          </Link>
        </Empty>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Pedido</th>
              <th>Data</th>
              <th>Itens</th>
              <th>Status</th>
              <th className="num">Total</th>
            </tr>
          </thead>
          <tbody>
            {data.map((order) => (
              <tr key={order.id} className="row-link" onClick={() => navigate(`/orders/${order.id}`)}>
                <td className="mono">
                  <Link to={`/orders/${order.id}`}>{orderNumber(order.id)}</Link>
                </td>
                <td className="mono">{formatDate(order.createdAt)}</td>
                <td>{order.items.reduce((sum, i) => sum + i.quantity, 0)}</td>
                <td>
                  <StatusBadge status={order.status} />
                </td>
                <td className="num">{formatMoney(order.total)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
