import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'

/**
 * Rota protegida: sem login, redireciona para /login guardando de onde veio.
 * Com adminOnly, exige role ADMIN.
 *
 * Importante: isto é só experiência de usuário. A segurança de verdade está
 * no backend — esconder um botão não impede ninguém de chamar a API.
 */
export function RequireAuth({ adminOnly = false }: { adminOnly?: boolean }) {
  const { user, isAdmin } = useAuth()
  const location = useLocation()

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (adminOnly && !isAdmin) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
