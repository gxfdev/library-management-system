import { get, post, put, del } from './request'

export interface BookPageParams {
  page: number
  size: number
  keyword?: string
  categoryId?: number
  status?: number
}

export interface BookFormData {
  id?: number
  isbn: string
  title: string
  author?: string
  publisher?: string
  publishDate?: string
  categoryId: number
  price?: number
  pages?: number
  coverImage?: string
  description?: string
  location?: string
  stockTotal: number
  status?: number
}

export function getBookPage(params: BookPageParams) {
  return get('/books', params)
}

export function getBookById(id: number) {
  return get(`/books/${id}`)
}

export function createBook(data: BookFormData) {
  return post('/books', data)
}

export function updateBook(data: BookFormData) {
  return put('/books', data)
}

export function deleteBook(id: number) {
  return del(`/books/${id}`)
}

export function updateBookStatus(id: number, status: number) {
  return put(`/books/${id}/status`, {}, { params: { status } })
}

export function uploadBookCover(bookId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return post<string>(`/upload/book-cover/${bookId}`, formData as unknown as Record<string, unknown>, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000,
  })
}
