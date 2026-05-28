import { get } from './request'

export function getPermissionMatrix() {
  return get('/permissions/matrix')
}

export function getPermissionLogs(params: { page: number; size: number; keyword?: string }) {
  return get('/permissions/logs', params)
}
