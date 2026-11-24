import { usePageTitle } from '../../brand'
import { useState } from 'react'
import { adminApi } from '../../api/api'
import { useAuth } from '../../auth/AuthContext'
import { Alert, Loading, PageTitle } from '../../components/ui'
import { formatDate } from '../../format'
import { errorMessage, useAsync } from '../../useAsync'

export function AdminUsersPage() {
  usePageTitle('Admin · Usuários')
  const { user: me } = useAuth()
  const users = useAsync(adminApi.users)
  const [error, setError] = useState<string | null>(null)

  async function toggle(id: number, active: boolean) {
    setError(null)
    try {
      await adminApi.setUserActive(id, active)
      await users.reload()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <div>
      <PageTitle eyebrow={`${users.data?.length ?? 0} contas`} title="Usuários" />
      <Alert>{error ?? users.error}</Alert>
      {users.loading ? (
        <Loading />
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Nº</th>
              <th>Nome</th>
              <th>E-mail</th>
              <th>Perfil</th>
              <th>Desde</th>
              <th>Situação</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {users.data?.map((u) => (
              <tr key={u.id}>
                <td className="mono muted">{String(u.id).padStart(3, '0')}</td>
                <td>{u.name}</td>
                <td>{u.email}</td>
                <td className="eyebrow">{u.role === 'ADMIN' ? 'Admin' : 'Cliente'}</td>
                <td className="mono">{formatDate(u.createdAt)}</td>
                <td>
                  <span className={`status ${u.active ? 'status-paid' : 'status-canceled'}`}>
                    <i aria-hidden="true" />
                    {u.active ? 'Ativo' : 'Inativo'}
                  </span>
                </td>
                <td className="actions-cell">
                  {u.id === me?.id ? (
                    <span className="muted small">Você</span>
                  ) : (
                    <button className={`btn-link ${u.active ? 'danger' : ''}`} onClick={() => toggle(u.id, !u.active)}>
                      {u.active ? 'Desativar' : 'Reativar'}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <p className="small muted spaced">
        Uma conta desativada não consegue mais entrar. Tokens já emitidos valem até expirar (2h).
      </p>
    </div>
  )
}
