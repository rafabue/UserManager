import { useAuthStore } from '../stores/auth'

/**
 * Erro de validação por campo, extraido da propriedade `errors` do ProblemDetail.
 */
export interface FieldError {
  field: string
  message: string
}

/**
 * Erro traduzido a partir da resposta da API, no formato ProblemDetail.
 */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
    readonly fieldErrors: FieldError[] = []
  ) {
    super(message)
  }
}

interface ProblemDetail {
  title?: string
  detail?: string
  errors?: FieldError[]
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const auth = useAuthStore()

  const response = await fetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(auth.authorizationHeader ? { Authorization: auth.authorizationHeader } : {}),
      ...options.headers
    }
  })

  if (response.status === 401) {
    auth.logout()
    throw new ApiError(401, 'Credenciais invalidas ou expiradas')
  }

  if (!response.ok) {
    const problem = (await response.json().catch(() => ({}))) as ProblemDetail
    throw new ApiError(
      response.status,
      problem.detail ?? problem.title ?? 'Erro inesperado ao comunicar com o servidor',
      problem.errors ?? []
    )
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

export const apiClient = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) })
}
