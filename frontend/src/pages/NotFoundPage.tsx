import { Link } from 'react-router-dom'
import { SearchX } from 'lucide-react'
import { usePageTitle } from '../brand'
import { Empty } from '../components/ui'

export function NotFoundPage() {
  usePageTitle('Página não encontrada')
  return (
    <Empty title="Página não encontrada" icon={<SearchX size={40} strokeWidth={1.4} />}>
      <p className="muted">O endereço pode ter mudado ou não existir mais.</p>
      <Link to="/" className="btn">
        Voltar para a loja
      </Link>
    </Empty>
  )
}
