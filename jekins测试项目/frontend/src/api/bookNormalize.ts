import { get, put } from './request'

export function getPendingBooks() {
  return get('/book-normalize/pending')
}

export function normalizeBook(id: number, data: Record<string, string>) {
  return put(`/book-normalize/${id}`, data)
}

export function batchNormalize(data: Record<string, string>[]) {
  return put('/book-normalize/batch', data as unknown as Record<string, unknown>)
}

export function getNormalizeReport(params: { page: number; size: number }) {
  return get('/book-normalize/report', params)
}
