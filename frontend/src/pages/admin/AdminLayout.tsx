import { NavLink, Outlet } from 'react-router-dom'
import { Package, ReceiptText, Tags, Users } from 'lucide-react'

const sections = [
  { to: '/admin', label: 'Pedidos', icon: ReceiptText, end: true },
  { to: '/admin/products', label: 'Produtos', icon: Package },
  { to: '/admin/categories', label: 'Categorias', icon: Tags },
  { to: '/admin/users', label: 'Usuários', icon: Users },
]

export function AdminLayout() {
  return (
    <section className="admin">
      <nav className="admin-nav" aria-label="Administração">
        <span className="eyebrow">Painel admin</span>
        {sections.map(({ to, label, icon: Icon, end }) => (
          <NavLink key={to} to={to} end={end}>
            <Icon size={18} strokeWidth={1.8} />
            {label}
          </NavLink>
        ))}
      </nav>
      <div>
        <Outlet />
      </div>
    </section>
  )
}
