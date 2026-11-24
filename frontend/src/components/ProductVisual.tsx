import { ImageOff } from 'lucide-react'
import { catalogApi } from '../api/api'
import { useAsync } from '../useAsync'

interface VisualProps {
  productId: number
  name: string
  sku?: string
  src?: string
  compact?: boolean
}

/** Foto do produto (inteira, sem cortes) ou um espaço neutro com ícone. */
export function ProductVisual({ name, src, compact = false }: VisualProps) {
  return (
    <div className={`visual ${compact ? 'compact' : ''}`}>
      {src ? (
        <img src={src} alt={name} loading="lazy" />
      ) : (
        <div className="visual-placeholder" aria-label="Produto sem foto">
          <ImageOff size={compact ? 20 : 40} strokeWidth={1.4} />
        </div>
      )}
    </div>
  )
}

/** Mesmo visual, buscando a primeira imagem do produto na API. */
export function ProductCover(props: Omit<VisualProps, 'src'>) {
  // Uma chamada por produto: simples e suficiente agora. Com um catálogo grande,
  // o ideal é o backend já devolver a imagem principal junto do produto.
  const { data } = useAsync(() => catalogApi.images(props.productId), [props.productId])
  return <ProductVisual {...props} src={data?.[0]?.url} />
}
