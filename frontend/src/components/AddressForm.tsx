import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import type { Address, AddressInput } from '../api/types'
import { formatZip } from '../format'
import { Alert, Field } from './ui'

const empty: AddressInput = {
  recipientName: '',
  zipCode: '',
  street: '',
  number: '',
  complement: '',
  district: '',
  city: '',
  state: '',
  defaultAddress: false,
}

function toInput(address?: Address): AddressInput {
  if (!address) return empty
  return { ...address, zipCode: formatZip(address.zipCode), complement: address.complement ?? '' }
}

export function AddressForm({
  initial,
  onSubmit,
  onCancel,
  submitLabel = 'Salvar endereço',
}: {
  initial?: Address
  onSubmit: (input: AddressInput) => Promise<void>
  onCancel?: () => void
  submitLabel?: string
}) {
  const [form, setForm] = useState<AddressInput>(() => toInput(initial))
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [message, setMessage] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  function set<K extends keyof AddressInput>(key: K, value: AddressInput[K]) {
    setForm((current) => ({ ...current, [key]: value }))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setErrors({})
    setMessage(null)
    try {
      await onSubmit(form)
    } catch (e) {
      if (e instanceof ApiError) {
        setErrors(e.fieldErrors)
        setMessage(e.message)
      }
    } finally {
      setSaving(false)
    }
  }

  const text = (key: keyof AddressInput, label: string, extra: Record<string, unknown> = {}) => (
    <Field label={label} error={errors[key]}>
      <input
        value={String(form[key])}
        onChange={(e) => set(key, e.target.value as never)}
        {...extra}
      />
    </Field>
  )

  return (
    <form className="form" onSubmit={handleSubmit}>
      <Alert>{message}</Alert>
      {text('recipientName', 'Destinatário', { required: true, maxLength: 150 })}
      <div className="grid-2">
        {text('zipCode', 'CEP', { required: true, placeholder: '00000-000', maxLength: 9 })}
        {text('state', 'UF', { required: true, maxLength: 2, placeholder: 'SP' })}
      </div>
      {text('street', 'Rua', { required: true, maxLength: 200 })}
      <div className="grid-2">
        {text('number', 'Número', { required: true, maxLength: 20 })}
        {text('complement', 'Complemento', { maxLength: 100 })}
      </div>
      <div className="grid-2">
        {text('district', 'Bairro', { required: true, maxLength: 100 })}
        {text('city', 'Cidade', { required: true, maxLength: 100 })}
      </div>
      <label className="checkbox">
        <input
          type="checkbox"
          checked={form.defaultAddress}
          onChange={(e) => set('defaultAddress', e.target.checked)}
        />
        Usar como endereço principal
      </label>
      <div className="actions">
        <button className="btn" disabled={saving}>
          {saving ? 'Salvando…' : submitLabel}
        </button>
        {onCancel && (
          <button type="button" className="btn btn-ghost" onClick={onCancel}>
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}
