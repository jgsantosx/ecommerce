// Tipos espelhando os DTOs de resposta do backend.

export type UserRole = 'CUSTOMER' | 'ADMIN'

export interface User {
  id: number
  name: string
  email: string
  role: UserRole
  active: boolean
  createdAt: string
}

export interface LoginResponse {
  token: string
  id: number
  name: string
  email: string
  role: UserRole
}

export interface Category {
  id: number
  name: string
  createdAt: string
}

export interface Product {
  id: number
  name: string
  description: string | null
  price: number
  sku: string
  active: boolean
  category: Category
  createdAt: string
}

export interface ProductInput {
  name: string
  description: string
  price: number
  sku: string
  categoryId: number
  active?: boolean
}

export interface Stock {
  productId: number
  quantity: number
  updatedAt: string
}

export interface ProductImage {
  id: number
  productId: number
  url: string
  contentType: string
  sizeBytes: number
  createdAt: string
}

export interface CartItem {
  productId: number
  productName: string
  sku: string
  unitPrice: number
  quantity: number
  subtotal: number
}

export interface Cart {
  items: CartItem[]
  totalItems: number
  total: number
}

export interface Address {
  id: number
  recipientName: string
  zipCode: string
  street: string
  number: string
  complement: string | null
  district: string
  city: string
  state: string
  defaultAddress: boolean
  createdAt: string
}

export type AddressInput = Omit<Address, 'id' | 'createdAt' | 'complement'> & {
  complement: string
}

export type OrderStatus = 'PENDING_PAYMENT' | 'PAID' | 'SHIPPED' | 'DELIVERED' | 'CANCELED'

export interface OrderItem {
  productId: number
  productName: string
  productSku: string
  unitPrice: number
  quantity: number
  subtotal: number
}

export interface Order {
  id: number
  userId: number
  status: OrderStatus
  total: number
  shippingAddress: Omit<Address, 'id' | 'createdAt' | 'defaultAddress'>
  items: OrderItem[]
  createdAt: string
  updatedAt: string
}

export type PaymentMethod = 'PIX' | 'CREDIT_CARD' | 'BOLETO'
export type PaymentStatus = 'PENDING' | 'APPROVED' | 'DECLINED'

export interface Payment {
  id: number
  orderId: number
  method: PaymentMethod
  status: PaymentStatus
  amount: number
  providerReference: string | null
  failureReason: string | null
  createdAt: string
}

/** Formato de erro do GlobalExceptionHandler (ApiError.java). */
export interface ApiErrorBody {
  status: number
  message: string
  timestamp: string
  errors: Record<string, string> | null
}
