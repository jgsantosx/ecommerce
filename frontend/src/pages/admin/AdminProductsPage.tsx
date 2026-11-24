import { usePageTitle } from '../../brand'
import { useState } from 'react'
import type { FormEvent } from 'react'
import { adminApi, catalogApi } from '../../api/api'
import { ApiError } from '../../api/client'
import type { Category, Product, ProductInput } from '../../api/types'
import { ProductCover } from '../../components/ProductVisual'
import { Alert, Field, Loading, PageTitle, Stepper } from '../../components/ui'
import { formatMoney } from '../../format'
import { errorMessage, useAsync } from '../../useAsync'

export function AdminProductsPage() {
  usePageTitle('Admin · Produtos')
  const products = useAsync(catalogApi.products)
  const categories = useAsync(catalogApi.categories)
  const [editing, setEditing] = useState<Product | 'new' | null>(null)
  const [managing, setManaging] = useState<Product | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function remove(product: Product) {
    if (!confirm(`Remover "${product.name}"? Produtos já vendidos não podem ser removidos — desative-os.`)) return
    setError(null)
    try {
      await adminApi.deleteProduct(product.id)
      await products.reload()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <div>
      <PageTitle eyebrow={`${products.data?.length ?? 0} produtos`} title="Produtos">
        {editing === null && (
          <button className="btn btn-ghost" onClick={() => setEditing('new')} disabled={!categories.data?.length}>
            Novo produto
          </button>
        )}
      </PageTitle>
      {categories.data?.length === 0 && <Alert kind="info">Cadastre uma categoria antes de criar produtos.</Alert>}
      <Alert>{error ?? products.error}</Alert>

      {editing !== null && categories.data && (
        <ProductForm
          key={editing === 'new' ? 'new' : editing.id}
          initial={editing === 'new' ? undefined : editing}
          categories={categories.data}
          onCancel={() => setEditing(null)}
          onSaved={async () => {
            setEditing(null)
            await products.reload()
          }}
        />
      )}

      {managing && <StockAndImages key={managing.id} product={managing} onClose={() => setManaging(null)} />}

      {products.loading ? (
        <Loading />
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th />
              <th>Produto</th>
              <th>Referência</th>
              <th>Categoria</th>
              <th className="num">Preço</th>
              <th>Situação</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {products.data?.map((p) => (
              <tr key={p.id}>
                <td className="thumb-cell">
                  <ProductCover productId={p.id} name={p.name} compact />
                </td>
                <td>{p.name}</td>
                <td className="mono muted">{p.sku}</td>
                <td>{p.category.name}</td>
                <td className="num">{formatMoney(p.price)}</td>
                <td>
                  <span className={`status ${p.active ? 'status-paid' : 'status-canceled'}`}>
                    <i aria-hidden="true" />
                    {p.active ? 'Ativo' : 'Inativo'}
                  </span>
                </td>
                <td className="actions-cell">
                  <button className="btn-link" onClick={() => setEditing(p)}>
                    Editar
                  </button>
                  <button className="btn-link" onClick={() => setManaging(p)}>
                    Estoque e imagens
                  </button>
                  <button className="btn-link danger" onClick={() => remove(p)}>
                    Remover
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}

function ProductForm({
  initial,
  categories,
  onSaved,
  onCancel,
}: {
  initial?: Product
  categories: Category[]
  onSaved: () => Promise<void>
  onCancel: () => void
}) {
  const [form, setForm] = useState<ProductInput>({
    name: initial?.name ?? '',
    description: initial?.description ?? '',
    price: initial?.price ?? 0,
    sku: initial?.sku ?? '',
    categoryId: initial?.category.id ?? categories[0].id,
    active: initial?.active ?? true,
  })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [message, setMessage] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setErrors({})
    setMessage(null)
    try {
      if (initial) await adminApi.updateProduct(initial.id, form)
      else await adminApi.createProduct(form)
      await onSaved()
    } catch (e) {
      if (e instanceof ApiError) {
        setErrors(e.fieldErrors)
        setMessage(e.message)
      }
    }
  }

  return (
    <form className="form panel" onSubmit={handleSubmit}>
      <div className="panel-head">
        <h2>{initial ? initial.name : 'Novo produto'}</h2>
        <button type="button" className="btn-link" onClick={onCancel}>
          Fechar
        </button>
      </div>
      <Alert>{message}</Alert>
      <Field label="Nome" error={errors.name}>
        <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required maxLength={150} />
      </Field>
      <Field label="Descrição" error={errors.description}>
        <textarea
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
          maxLength={1000}
          rows={3}
        />
      </Field>
      <div className="grid-2">
        <Field label="Preço (R$)" error={errors.price}>
          <input
            className="mono"
            type="number"
            step="0.01"
            min="0.01"
            value={form.price}
            onChange={(e) => setForm({ ...form, price: Number(e.target.value) })}
            required
          />
        </Field>
        <Field label="Referência (SKU)" error={errors.sku}>
          <input
            className="mono"
            value={form.sku}
            onChange={(e) => setForm({ ...form, sku: e.target.value })}
            required
            maxLength={50}
          />
        </Field>
      </div>
      <Field label="Categoria" error={errors.categoryId}>
        <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: Number(e.target.value) })}>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </Field>
      {initial ? (
        <label className="checkbox">
          <input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} />
          Ativo (visível no catálogo)
        </label>
      ) : (
        <p className="hint">O produto é criado com estoque zero.</p>
      )}
      <div className="actions">
        <button className="btn">Salvar</button>
      </div>
    </form>
  )
}

function StockAndImages({ product, onClose }: { product: Product; onClose: () => void }) {
  const stock = useAsync(() => catalogApi.stock(product.id), [product.id])
  const images = useAsync(() => catalogApi.images(product.id), [product.id])
  const [amount, setAmount] = useState(1)
  const [error, setError] = useState<string | null>(null)
  const [uploading, setUploading] = useState(false)

  async function move(direction: 'increase' | 'decrease') {
    setError(null)
    try {
      const updated =
        direction === 'increase'
          ? await adminApi.increaseStock(product.id, amount)
          : await adminApi.decreaseStock(product.id, amount)
      stock.setData(updated)
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  async function upload(file: File | undefined) {
    if (!file) return
    setError(null)
    setUploading(true)
    try {
      await adminApi.uploadImage(product.id, file)
      await images.reload()
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setUploading(false)
    }
  }

  async function removeImage(imageId: number) {
    setError(null)
    try {
      await adminApi.deleteImage(product.id, imageId)
      await images.reload()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <div className="panel">
      <div className="panel-head">
        <h2>{product.name}</h2>
        <button className="btn-link" onClick={onClose}>
          Fechar
        </button>
      </div>
      <Alert>{error}</Alert>

      <div className="grid-2">
        <div>
          <h3>Estoque</h3>
          <p className="stock-figure">{stock.data?.quantity ?? '—'}</p>
          <p className="eyebrow">unidades disponíveis</p>
          <div className="actions spaced">
            <Stepper value={amount} max={100000} onChange={setAmount} small label="Quantidade a movimentar" />
            <button className="btn btn-small" onClick={() => move('increase')}>
              Entrada
            </button>
            <button className="btn btn-small btn-ghost" onClick={() => move('decrease')}>
              Saída
            </button>
          </div>
        </div>

        <div>
          <h3>Imagens</h3>
          <div className="image-grid">
            {images.data?.map((img) => (
              <figure key={img.id}>
                <img src={img.url} alt="" />
                <button className="btn-link danger" onClick={() => removeImage(img.id)}>
                  Remover
                </button>
              </figure>
            ))}
          </div>
          <label className="btn btn-small btn-ghost upload">
            {uploading ? 'Enviando…' : 'Enviar imagem'}
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp"
              hidden
              disabled={uploading}
              onChange={(e) => {
                void upload(e.target.files?.[0])
                e.target.value = ''
              }}
            />
          </label>
          <p className="hint">JPEG, PNG ou WEBP, até 5 MB.</p>
        </div>
      </div>
    </div>
  )
}
