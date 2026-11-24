import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Check, ShoppingCart } from 'lucide-react'
import { cartApi, catalogApi } from '../api/api'
import type { Product } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { useCart } from '../cart/CartContext'
import { formatMoney } from '../format'
import { errorMessage, useAsync } from '../useAsync'
import { ProductCover } from './ProductVisual'

export function ProductCard({ product }: { product: Product }) {
  const { user } = useAuth()
  const { setCart } = useCart()
  const navigate = useNavigate()
  const stock = useAsync(() => catalogApi.stock(product.id), [product.id])
  const [state, setState] = useState<'idle' | 'adding' | 'added'>('idle')
  const [error, setError] = useState<string | null>(null)

  const available = stock.data?.quantity
  const soldOut = available === 0
  const low = available !== undefined && available > 0 && available <= 3

  async function add() {
    if (!user) {
      navigate('/login', { state: { from: `/products/${product.id}` } })
      return
    }
    setState('adding')
    setError(null)
    try {
      setCart(await cartApi.add(product.id, 1))
      setState('added')
      setTimeout(() => setState('idle'), 1800)
    } catch (e) {
      setError(errorMessage(e))
      setState('idle')
    }
  }

  return (
    <article className="product-card">
      {soldOut && <span className="badge-corner">Esgotado</span>}
      {low && <span className="badge-corner low">Últimas unidades</span>}

      <Link to={`/products/${product.id}`} className="card-link">
        <ProductCover productId={product.id} name={product.name} />
        <div className="product-card-body">
          <span className="eyebrow">{product.category.name}</span>
          <span className="product-card-name">{product.name}</span>
          <span className="product-card-price">{formatMoney(product.price)}</span>
        </div>
      </Link>

      <button
        className={`btn btn-small btn-block ${soldOut ? 'btn-ghost' : ''}`}
        onClick={add}
        disabled={soldOut || state === 'adding'}
      >
        {state === 'added' ? (
          <>
            <Check size={16} /> Adicionado
          </>
        ) : soldOut ? (
          'Indisponível'
        ) : (
          <>
            <ShoppingCart size={16} /> Adicionar
          </>
        )}
      </button>
      {error && <small className="field-error">{error}</small>}
    </article>
  )
}
