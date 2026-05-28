import { get, post, put, del } from './request'

export function getNotices(params: { page?: number; size?: number; type?: string; status?: string }) {
  return get('/notices', params)
}

export function getNoticeById(id: number) {
  return get(`/notices/${id}`)
}

export function createNotice(data: Record<string, unknown>) {
  return post('/notices', data)
}

export function updateNotice(data: Record<string, unknown>) {
  return put('/notices', data)
}

export function deleteNotice(id: number) {
  return del(`/notices/${id}`)
}

export function publishNotice(id: number) {
  return put(`/notices/${id}/publish`, {})
}
