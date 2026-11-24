import type { ApiErrorBody } from './types'

const TOKEN_KEY = 'ecommerce.token'

/**
 * Erro de API com o status HTTP e, quando houver, os erros de validação por campo.
 * Mantém a mensagem que o backend devolveu (já em português).
 */
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

// O token fica no localStorage para sobreviver a um F5. Trade-off: um script
// malicioso injetado na página (XSS) conseguiria lê-lo. Por isso nunca
// renderizamos HTML vindo da API sem escapar (o React já escapa por padrão).
export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

let onUnauthorized: () => void = () => {}

/** O AuthProvider registra aqui o que fazer quando o token expira (401). */
export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler
}

type Body = object | FormData | undefined

export async function request<T>(method: string, path: string, body?: Body): Promise<T> {
  const headers: Record<string, string> = {}
  const token = tokenStorage.get()

  if (token) headers.Authorization = `Bearer ${token}`

  let payload: BodyInit | undefined
  if (body instanceof FormData) {
    // Sem Content-Type manual: o navegador define o boundary do multipart.
    payload = body
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
    payload = JSON.stringify(body)
  }

  const response = await fetch(path, { method, headers, body: payload })

  if (response.status === 401 && token) {
    onUnauthorized()
  }

  if (!response.ok) {
    throw await toApiError(response)
  }

  if (response.status === 204) return undefined as T

  const text = await response.text()
  return (text ? JSON.parse(text) : undefined) as T
}

async function toApiError(response: Response): Promise<ApiError> {
  try {
    const data = (await response.json()) as Partial<ApiErrorBody>
    return new ApiError(
      response.status,
      data.message ?? defaultMessage(response.status),
      data.errors ?? {},
    )
  } catch {
    return new ApiError(response.status, defaultMessage(response.status))
  }
}

function defaultMessage(status: number): string {
  if (status === 401) return 'Sessão expirada ou credenciais inválidas'
  if (status === 403) return 'Você não tem permissão para esta ação'
  if (status === 404) return 'Não encontrado'
  return 'Erro inesperado. Tente novamente.'
}

export const http = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: Body) => request<T>('POST', path, body),
  put: <T>(path: string, body?: Body) => request<T>('PUT', path, body),
  patch: <T>(path: string, body?: Body) => request<T>('PATCH', path, body),
  delete: <T = void>(path: string) => request<T>('DELETE', path),
}
