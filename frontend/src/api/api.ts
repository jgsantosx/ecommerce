import { http } from './client'
import type {
  Address,
  AddressInput,
  Cart,
  Category,
  LoginResponse,
  Order,
  OrderStatus,
  Payment,
  PaymentMethod,
  Product,
  ProductImage,
  ProductInput,
  Stock,
  User,
} from './types'

// Uma função por endpoint. As páginas não montam URLs: chamam estas funções.

export const authApi = {
  login: (email: string, password: string) =>
    http.post<LoginResponse>('/api/auth/login', { email, password }),
  register: (name: string, email: string, password: string) =>
    http.post<User>('/api/users', { name, email, password }),
  me: () => http.get<User>('/api/users/me'),
}

export const catalogApi = {
  categories: () => http.get<Category[]>('/api/categories'),
  products: () => http.get<Product[]>('/api/products'),
  product: (id: number) => http.get<Product>(`/api/products/${id}`),
  stock: (productId: number) => http.get<Stock>(`/api/products/${productId}/stock`),
  images: (productId: number) => http.get<ProductImage[]>(`/api/products/${productId}/images`),
}

export const cartApi = {
  get: () => http.get<Cart>('/api/cart'),
  add: (productId: number, quantity: number) =>
    http.post<Cart>('/api/cart/items', { productId, quantity }),
  update: (productId: number, quantity: number) =>
    http.put<Cart>(`/api/cart/items/${productId}`, { quantity }),
  remove: (productId: number) => http.delete<Cart>(`/api/cart/items/${productId}`),
  clear: () => http.delete('/api/cart'),
}

export const addressApi = {
  list: () => http.get<Address[]>('/api/addresses'),
  create: (input: AddressInput) => http.post<Address>('/api/addresses', input),
  update: (id: number, input: AddressInput) => http.put<Address>(`/api/addresses/${id}`, input),
  setDefault: (id: number) => http.patch<Address>(`/api/addresses/${id}/default`),
  remove: (id: number) => http.delete(`/api/addresses/${id}`),
}

export const orderApi = {
  checkout: (addressId: number) => http.post<Order>('/api/orders', { addressId }),
  list: () => http.get<Order[]>('/api/orders'),
  get: (id: number) => http.get<Order>(`/api/orders/${id}`),
  cancel: (id: number) => http.post<Order>(`/api/orders/${id}/cancel`),
  pay: (id: number, method: PaymentMethod, cardToken?: string) =>
    http.post<Payment>(`/api/orders/${id}/payments`, { method, cardToken }),
  payments: (id: number) => http.get<Payment[]>(`/api/orders/${id}/payments`),
}

export const adminApi = {
  createCategory: (name: string) => http.post<Category>('/api/categories', { name }),
  updateCategory: (id: number, name: string) => http.put<Category>(`/api/categories/${id}`, { name }),
  deleteCategory: (id: number) => http.delete(`/api/categories/${id}`),

  createProduct: (input: ProductInput) => http.post<Product>('/api/products', input),
  updateProduct: (id: number, input: ProductInput) => http.put<Product>(`/api/products/${id}`, input),
  deleteProduct: (id: number) => http.delete(`/api/products/${id}`),

  increaseStock: (productId: number, quantity: number) =>
    http.post<Stock>(`/api/products/${productId}/stock/increase`, { quantity }),
  decreaseStock: (productId: number, quantity: number) =>
    http.post<Stock>(`/api/products/${productId}/stock/decrease`, { quantity }),

  uploadImage: (productId: number, file: File) => {
    const form = new FormData()
    form.append('file', file)
    return http.post<ProductImage>(`/api/products/${productId}/images`, form)
  },
  deleteImage: (productId: number, imageId: number) =>
    http.delete(`/api/products/${productId}/images/${imageId}`),

  orders: (status?: OrderStatus) =>
    http.get<Order[]>(`/api/admin/orders${status ? `?status=${status}` : ''}`),
  updateOrderStatus: (id: number, status: OrderStatus) =>
    http.patch<Order>(`/api/admin/orders/${id}/status`, { status }),

  users: () => http.get<User[]>('/api/users'),
  setUserActive: (id: number, active: boolean) =>
    http.patch<User>(`/api/admin/users/${id}/active`, { active }),
}
