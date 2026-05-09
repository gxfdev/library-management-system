import { get, post, put } from './request'
import type { PageResult, BorrowRecord } from '@/types'

export interface BorrowPageParams {
  page: number
  size: number
  keyword?: string
  status?: string
}

export interface BorrowFormData {
  userId?: number
  bookId: number
  borrowDays?: number
  remark?: string
}

export function getBorrowPage(params: BorrowPageParams) {
  return get<PageResult<BorrowRecord>>('/borrows', params)
}

export function borrowBook(data: BorrowFormData) {
  return post('/borrows/self', { bookId: data.bookId, borrowDays: data.borrowDays, remark: data.remark })
}

export function adminBorrowBook(data: BorrowFormData) {
  return post('/borrows', data)
}

export function returnBook(id: number) {
  return put(`/borrows/${id}/return`, {})
}

export function selfReturnBook(id: number) {
  return put(`/borrows/${id}/self-return`, {})
}

export function renewBook(id: number) {
  return put(`/borrows/${id}/renew`, {})
}

export function getMyBorrows(params: { page: number; size: number; status?: string }) {
  return get<PageResult<BorrowRecord>>('/borrows/my', params)
}
