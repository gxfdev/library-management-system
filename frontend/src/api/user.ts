import { get, post, put, del } from './request'

export interface UserPageParams {
  page: number
  size: number
  keyword?: string
  role?: string
  status?: number
}

export interface UserFormData {
  id?: number
  username: string
  password?: string
  realName: string
  phone?: string
  email?: string
  role: string
  status?: number
}

export function getUserPage(params: UserPageParams) {
  return get('/users', params)
}

export function getUserById(id: number) {
  return get(`/users/${id}`)
}

export function createUser(data: UserFormData) {
  return post('/users', data)
}

export function updateUser(data: UserFormData) {
  return put('/users', data)
}

export function deleteUser(id: number) {
  return del(`/users/${id}`)
}

export function updateUserStatus(id: number, status: number) {
  return put<void>(`/users/${id}/status`, {}, { params: { status } })
}

export function resetUserPassword(id: number) {
  return put<{ password: string }>(`/users/${id}/reset-password`, {})
}

export function adminChangePassword(id: number, newPassword: string) {
  return put<void>(`/users/${id}/change-password`, { newPassword })
}

export function searchUsers(keyword: string) {
  return get('/users', { page: 1, size: 20, keyword })
}
