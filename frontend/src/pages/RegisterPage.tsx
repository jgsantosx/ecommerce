import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../api/api'
import { ApiError } from '../api/client'
import { usePageTitle } from '../brand'
import { Alert, Field } from '../components/ui'

export function RegisterPage() {
  usePageTitle('Criar conta')
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [message, setMessage] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setLoading(true)
    setErrors({})
    setMessage(null)
    try {
      await authApi.register(name, email, password)
      navigate('/login', { state: { registered: true } })
    } catch (e) {
      if (e instanceof ApiError) {
        setErrors(e.fieldErrors)
        setMessage(e.message)
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="auth">
      <div className="auth-card">
        <h1>Criar conta</h1>
        <p className="lede">Leva menos de um minuto.</p>

        <form className="form" onSubmit={handleSubmit}>
          <Alert>{message}</Alert>
          <Field label="Nome completo" error={errors.name}>
            <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={150} autoComplete="name" />
          </Field>
          <Field label="E-mail" error={errors.email}>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
              placeholder="voce@email.com"
            />
          </Field>
          <Field label="Senha" error={errors.password}>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
              autoComplete="new-password"
            />
          </Field>
          <p className="hint">Use pelo menos 6 caracteres.</p>
          <button className="btn btn-block" disabled={loading}>
            {loading ? 'Criando…' : 'Criar conta'}
          </button>
          <p className="small muted">
            Já tem conta? <Link to="/login" className="btn-link">Entrar</Link>
          </p>
        </form>
      </div>
    </section>
  )
}
