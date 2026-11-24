import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { cartApi } from '../api/api'
import type { Cart } from '../api/types'
import { useAuth } from '../auth/AuthContext'

interface CartContextValue {
  cart: Cart | null
  refresh: () => Promise<void>
  /** Atualiza o estado com a resposta de uma operação (a API devolve o carrinho). */
  setCart: (cart: Cart) => void
}

const CartContext = createContext<CartContextValue | null>(null)

/** Mantém o carrinho acessível em qualquer tela (ex.: contador no cabeçalho). */
export function CartProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const [cart, setCart] = useState<Cart | null>(null)

  const refresh = useCallback(async () => {
    if (!user) {
      setCart(null)
      return
    }
    try {
      setCart(await cartApi.get())
    } catch {
      setCart(null)
    }
  }, [user])

  useEffect(() => {
    // oxlint-disable-next-line react/set-state-in-effect -- buscar dados da API é sincronizar com um sistema externo
    void refresh()
  }, [refresh])

  const value = useMemo(() => ({ cart, refresh, setCart }), [cart, refresh])

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

// oxlint-disable-next-line react/only-export-components -- provider e hook ficam juntos de propósito
export function useCart(): CartContextValue {
  const context = useContext(CartContext)
  if (!context) throw new Error('useCart precisa estar dentro de <CartProvider>')
  return context
}
