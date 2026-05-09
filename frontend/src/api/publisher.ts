import { get, post, put, del } from './request'

export function getPublishers(params: { page?: number; size?: number; keyword?: string }) {
  return get('/publishers', params)
}

export function getPublisherById(id: number) {
  return get(`/publishers/${id}`)
}

export function createPublisher(data: Record<string, unknown>) {
  return post('/publishers', data)
}

export function updatePublisher(data: Record<string, unknown>) {
  return put('/publishers', data)
}

export function deletePublisher(id: number) {
  return del(`/publishers/${id}`)
}
