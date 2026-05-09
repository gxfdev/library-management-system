import { get, post, put } from './request'

export function generateBorrowCode(bookId: number) {
  return post('/borrow-codes/generate', {}, { params: { bookId } })
}

export function verifyBorrowCode(code: string) {
  return post('/borrow-codes/verify', {}, { params: { code } })
}

export function getMyBorrowCodes(params: { page: number; size: number }) {
  return get('/borrow-codes/my', params)
}

export function getBorrowCodePage(params: { page: number; size: number; status?: string }) {
  return get('/borrow-codes', params)
}

export function invalidateBorrowCode(id: number) {
  return put(`/borrow-codes/${id}/invalidate`, {})
}
