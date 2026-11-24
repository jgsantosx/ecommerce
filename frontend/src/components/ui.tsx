import type { ReactNode } from 'react'
import { Inbox, Minus, Plus } from 'lucide-react'
import type { OrderStatus } from '../api/types'
import { orderStatusLabel } from '../format'

export function Alert({ kind = 'error', children }: { kind?: 'error' | 'success' | 'info'; children: ReactNode }) {
  if (!children) return null
  return (
    <div className={`alert alert-${kind}`} role={kind === 'error' ? 'alert' : 'status'}>
      {children}
    </div>
  )
}

export function Loading() {
  return <p className="loading">Carregando…</p>
}

export function StatusBadge({ status }: { status: OrderStatus }) {
  return (
    <span className={`status status-${status.toLowerCase()}`}>
      <i aria-hidden="true" />
      {orderStatusLabel[status]}
    </span>
  )
}

export function Field({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
      {error && <small className="field-error">{error}</small>}
    </label>
  )
}

export function Empty({ title, icon, children }: { title: string; icon?: ReactNode; children?: ReactNode }) {
  return (
    <div className="empty">
      {icon ?? <Inbox size={36} strokeWidth={1.4} />}
      <p>{title}</p>
      {children}
    </div>
  )
}

/** Cabeçalho de página: texto auxiliar + título + ações à direita. */
export function PageTitle({ eyebrow, title, children }: { eyebrow?: string; title: ReactNode; children?: ReactNode }) {
  return (
    <header className="page-title">
      <div>
        {eyebrow && <span className="eyebrow">{eyebrow}</span>}
        <h1>{title}</h1>
      </div>
      {children}
    </header>
  )
}

/** Seletor de quantidade − n + (controlado). */
export function Stepper({
  value,
  min = 1,
  max = 99,
  onChange,
  small = false,
  label = 'Quantidade',
}: {
  value: number
  min?: number
  max?: number
  onChange: (value: number) => void
  small?: boolean
  label?: string
}) {
  const size = small ? 14 : 16
  return (
    <div className={`stepper ${small ? 'small' : ''}`} role="group" aria-label={label}>
      <button type="button" onClick={() => onChange(value - 1)} disabled={value <= min} aria-label="Diminuir">
        <Minus size={size} />
      </button>
      <output aria-live="polite">{value}</output>
      <button type="button" onClick={() => onChange(value + 1)} disabled={value >= max} aria-label="Aumentar">
        <Plus size={size} />
      </button>
    </div>
  )
}
