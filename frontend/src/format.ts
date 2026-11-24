import type { OrderStatus, PaymentMethod, PaymentStatus } from './api/types'

const currency = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const dateTime = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

export const formatMoney = (value: number) => currency.format(value)
export const formatDate = (iso: string) => dateTime.format(new Date(iso))
export const formatZip = (zip: string) => zip.replace(/^(\d{5})(\d{3})$/, '$1-$2')
export const orderNumber = (id: number) => `#${id}`

/** Etapas exibidas na linha do tempo do pedido (cancelado é tratado à parte). */
export const orderTimeline: OrderStatus[] = ['PENDING_PAYMENT', 'PAID', 'SHIPPED', 'DELIVERED']

export const orderStatusLabel: Record<OrderStatus, string> = {
  PENDING_PAYMENT: 'Aguardando pagamento',
  PAID: 'Pago',
  SHIPPED: 'Enviado',
  DELIVERED: 'Entregue',
  CANCELED: 'Cancelado',
}

/** Espelha OrderStatus.allowedNext() do backend, para montar os botões do admin. */
export const orderNextStatus: Record<OrderStatus, OrderStatus[]> = {
  PENDING_PAYMENT: ['PAID', 'CANCELED'],
  PAID: ['SHIPPED', 'CANCELED'],
  SHIPPED: ['DELIVERED'],
  DELIVERED: [],
  CANCELED: [],
}

export const paymentMethodLabel: Record<PaymentMethod, string> = {
  PIX: 'Pix',
  CREDIT_CARD: 'Cartão de crédito',
  BOLETO: 'Boleto',
}

export const paymentStatusLabel: Record<PaymentStatus, string> = {
  PENDING: 'Pendente',
  APPROVED: 'Aprovado',
  DECLINED: 'Recusado',
}
