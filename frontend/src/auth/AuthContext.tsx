import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '../api/api'
import { setUnauthorizedHandler, tokenStorage } from '../api/client'
import type { UserRole } from '../api/types'

export interface SessionUser {
  id: number
  name: string
  email: string
  role: UserRole
}

interface AuthContextValue {
  user: SessionUser | null
  isAdmin: boolean
  login: (email: string, password: string) => Promise<SessionUser>
  logout: () => void
}

const USER_KEY = 'ecommerce.user'

const AuthContext = createContext<AuthContextValue | null>(null)

/**
 * Lê o campo "exp" do JWT (segundos desde 1970) sem validar a assinatura.
 * Isso é só para a interface saber se o token já venceu; quem VALIDA o
 * token de verdade é sempre o backend.
 */
function isExpired(token: string): boolean {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const { exp } = JSON.parse(atob(payload)) as { exp?: number }
    return !exp || exp * 1000 <= Date.now()
  } catch {
    return true
  }
}

function loadSession(): SessionUser | null {
  const token = tokenStorage.get()
  const raw = localStorage.getItem(USER_KEY)

  if (!token || !raw || isExpired(token)) {
    tokenStorage.clear()
    localStorage.removeItem(USER_KEY)
    return null
  }

  return JSON.parse(raw) as SessionUser
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(loadSession)

  const logout = useCallback(() => {
    tokenStorage.clear()
    localStorage.removeItem(USER_KEY)
    setUser(null)
  }, [])

  useEffect(() => {
    // Qualquer 401 vindo da API com token = sessão inválida: sai do sistema.
    setUnauthorizedHandler(logout)
  }, [logout])

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login(email, password)
    const sessionUser: SessionUser = {
      id: response.id,
      name: response.name,
      email: response.email,
      role: response.role,
    }

    tokenStorage.set(response.token)
    localStorage.setItem(USER_KEY, JSON.stringify(sessionUser))
    setUser(sessionUser)

    return sessionUser
  }, [])

  const value = useMemo(
    () => ({ user, isAdmin: user?.role === 'ADMIN', login, logout }),
    [user, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// oxlint-disable-next-line react/only-export-components -- provider e hook ficam juntos de propósito
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth precisa estar dentro de <AuthProvider>')
  return context
}
