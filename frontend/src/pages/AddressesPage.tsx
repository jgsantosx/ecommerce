import { usePageTitle } from '../brand'
import { useState } from 'react'
import { addressApi } from '../api/api'
import type { Address } from '../api/types'
import { AddressForm } from '../components/AddressForm'
import { Alert, Empty, Loading, PageTitle } from '../components/ui'
import { formatZip } from '../format'
import { errorMessage, useAsync } from '../useAsync'

export function AddressesPage() {
  usePageTitle('Endereços')
  const addresses = useAsync(addressApi.list)
  const [editing, setEditing] = useState<Address | 'new' | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function run(action: () => Promise<unknown>) {
    setError(null)
    try {
      await action()
      await addresses.reload()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <section>
      <PageTitle eyebrow="Conta" title="Endereços">
        {editing === null && (
          <button className="btn btn-ghost" onClick={() => setEditing('new')}>
            Novo endereço
          </button>
        )}
      </PageTitle>
      <Alert>{error}</Alert>

      {editing !== null && (
        <div className="panel">
          <div className="panel-head">
            <h2>{editing === 'new' ? 'Novo endereço' : 'Editar endereço'}</h2>
          </div>
          <AddressForm
            key={editing === 'new' ? 'new' : editing.id}
            initial={editing === 'new' ? undefined : editing}
            onCancel={() => setEditing(null)}
            onSubmit={async (input) => {
              if (editing === 'new') await addressApi.create(input)
              else await addressApi.update(editing.id, input)
              setEditing(null)
              await addresses.reload()
            }}
          />
        </div>
      )}

      {addresses.loading ? (
        <Loading />
      ) : !addresses.data?.length ? (
        editing === null && <Empty title="Nenhum endereço cadastrado." />
      ) : (
        <div className="address-grid">
          {addresses.data.map((a) => (
            <article key={a.id} className="address-card">
              <div>
                <span className="eyebrow">{a.defaultAddress ? 'Principal' : 'Endereço'}</span>
                <address className="address-block spaced">
                  <strong>{a.recipientName}</strong>
                  <br />
                  {a.street}, {a.number}
                  {a.complement ? ` — ${a.complement}` : ''}
                  <br />
                  {a.district}
                  <br />
                  {a.city} / {a.state} · <span className="mono">{formatZip(a.zipCode)}</span>
                </address>
              </div>
              <div className="actions-inline">
                <button className="btn-link" onClick={() => setEditing(a)}>
                  Editar
                </button>
                {!a.defaultAddress && (
                  <button className="btn-link" onClick={() => run(() => addressApi.setDefault(a.id))}>
                    Tornar principal
                  </button>
                )}
                <button
                  className="btn-link danger"
                  onClick={() => confirm('Remover este endereço?') && run(() => addressApi.remove(a.id))}
                >
                  Remover
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
