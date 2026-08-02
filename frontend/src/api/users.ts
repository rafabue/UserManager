import { apiClient } from './client'

export interface User {
  id: number
  name: string
  email: string
}

/**
 * Página no formato PagedModel, estável entre versões do Spring Data.
 */
export interface Page<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}

export interface CreateUserPayload {
  name: string
  email: string
  password: string
}

export interface UpdateUserPayload {
  name: string
  email: string
}

export const usersApi = {
  list(params: { name?: string; page?: number; size?: number } = {}) {
    const query = new URLSearchParams()
    if (params.name) query.set('name', params.name)
    query.set('page', String(params.page ?? 0))
    query.set('size', String(params.size ?? 10))
    return apiClient.get<Page<User>>(`/api/users?${query.toString()}`)
  },

  findById(id: number) {
    return apiClient.get<User>(`/api/users/${id}`)
  },

  create(payload: CreateUserPayload) {
    return apiClient.post<User>('/api/users', payload)
  },

  update(id: number, payload: UpdateUserPayload) {
    return apiClient.put<User>(`/api/users/${id}`, payload)
  }
}
