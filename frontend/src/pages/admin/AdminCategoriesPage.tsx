import { usePageTitle } from '../../brand'
import { useState } from 'react'
import type { FormEvent } from 'react'
import { adminApi, catalogApi } from '../../api/api'
import { Alert, Loading, PageTitle } from '../../components/ui'
import { errorMessage, useAsync } from '../../useAsync'

export function AdminCategoriesPage() {
  usePageTitle('Admin · Categorias')
  const categories = useAsync(catalogApi.categories)
  const products = useAsync(catalogApi.products)
  const [name, setName] = useState('')
  const [editing, setEditing] = useState<{ id: number; name: string } | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function run(action: () => Promise<unknown>) {
    setError(null)
    try {
      await action()
      await categories.reload()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  function create(event: FormEvent) {
    event.preventDefault()
    void run(async () => {
      await adminApi.createCategory(name)
      setName('')
    })
  }

  const countFor = (id: number) => (products.data ?? []).filter((p) => p.category.id === id).length

  return (
    <div>
      <PageTitle eyebrow={`${categories.data?.length ?? 0} categorias`} title="Categorias" />
      <Alert>{error}</Alert>

      <form className="inline-form" onSubmit={create}>
        <input placeholder="Nome da nova categoria" value={name} onChange={(e) => setName(e.target.value)} required maxLength={100} />
        <button className="btn btn-small">Adicionar</button>
      </form>

      {categories.loading ? (
        <Loading />
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Nome</th>
              <th className="num">Produtos</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {categories.data?.map((c) => (
              <tr key={c.id}>
                <td>
                  {editing?.id === c.id ? (
                    <input
                      autoFocus
                      value={editing.name}
                      onChange={(e) => setEditing({ id: c.id, name: e.target.value })}
                      aria-label="Novo nome"
                    />
                  ) : (
                    c.name
                  )}
                </td>
                <td className="num">{countFor(c.id)}</td>
                <td className="actions-cell">
                  {editing?.id === c.id ? (
                    <>
                      <button
                        className="btn btn-small"
                        onClick={() =>
                          run(async () => {
                            await adminApi.updateCategory(c.id, editing.name)
                            setEditing(null)
                          })
                        }
                      >
                        Salvar
                      </button>
                      <button className="btn-link" onClick={() => setEditing(null)}>
                        Cancelar
                      </button>
                    </>
                  ) : (
                    <>
                      <button className="btn-link" onClick={() => setEditing({ id: c.id, name: c.name })}>
                        Renomear
                      </button>
                      <button
                        className="btn-link danger"
                        onClick={() => confirm(`Remover "${c.name}"?`) && run(() => adminApi.deleteCategory(c.id))}
                      >
                        Remover
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <p className="small muted spaced">Categorias com produtos não podem ser removidas.</p>
    </div>
  )
}
