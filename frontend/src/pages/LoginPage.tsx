import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { usePageTitle } from '../brand'
import { Alert, Field } from '../components/ui'
import { errorMessage } from '../useAsync'

export function LoginPage() {
  usePageTitle('Entrar')
  const { login } = useAuth()
  const navigate = useNavigate()
  const state = useLocation().state as { from?: string; registered?: boolean } | null
  const from = state?.from ?? '/'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await login(email, password)
      navigate(from, { replace: true })
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="auth">
      <div className="auth-card">
        <h1>Entrar</h1>
        <p className="lede">Acesse sua conta para comprar e acompanhar seus pedidos.</p>

        <form className="form" onSubmit={handleSubmit}>
          {state?.registered && <Alert kind="success">Conta criada! Agora é só entrar.</Alert>}
          <Alert>{error}</Alert>
          <Field label="E-mail">
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
              placeholder="voce@email.com"
            />
          </Field>
          <Field label="Senha">
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
            />
          </Field>
          <button className="btn btn-block" disabled={loading}>
            {loading ? 'Entrando…' : 'Entrar'}
          </button>
          <p className="small muted">
            Ainda não tem conta? <Link to="/register" className="btn-link">Cadastre-se</Link>
          </p>
        </form>
      </div>
    </section>
  )
}
