import { store } from '../brand'

/**
 * Logo gerada a partir de store.name: um quadrado na cor da marca com a
 * inicial + o nome. Ao vender para outro cliente, basta trocar o nome em
 * src/brand.ts (ou substituir este componente por uma imagem do logo real).
 */
export function Logo({ showTagline = true }: { showTagline?: boolean }) {
  return (
    <span className="logo">
      <span className="logo-mark" aria-hidden="true">
        {store.name.trim().charAt(0).toUpperCase()}
      </span>
      <span className="logo-text">
        <span className="logo-word">{store.name}</span>
        {showTagline && <span className="logo-sub">{store.tagline}</span>}
      </span>
    </span>
  )
}
