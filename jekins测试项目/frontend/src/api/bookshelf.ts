import { get, post, put, del } from './request'

export function getBookshelves(params: { page?: number; size?: number; keyword?: string; deptId?: number }) {
  return get('/bookshelves', params)
}

export function getBookshelfById(id: number) {
  return get(`/bookshelves/${id}`)
}

export function createBookshelf(data: Record<string, unknown>) {
  return post('/bookshelves', data)
}

export function updateBookshelf(data: Record<string, unknown>) {
  return put('/bookshelves', data)
}

export function deleteBookshelf(id: number) {
  return del(`/bookshelves/${id}`)
}

export function getStoreys(bookshelfId: number) {
  return get(`/bookshelves/${bookshelfId}/storeys`)
}

export function createStorey(data: Record<string, unknown>) {
  return post('/bookshelves/storeys', data)
}

export function updateStorey(data: Record<string, unknown>) {
  return put('/bookshelves/storeys', data)
}

export function deleteStorey(id: number) {
  return del(`/bookshelves/storeys/${id}`)
}

export function getLocations(params: { page?: number; size?: number; storeyId?: number; status?: string }) {
  return get('/bookshelves/locations', params)
}

export function createLocation(data: Record<string, unknown>) {
  return post('/bookshelves/locations', data)
}

export function updateLocation(data: Record<string, unknown>) {
  return put('/bookshelves/locations', data)
}

export function deleteLocation(id: number) {
  return del(`/bookshelves/locations/${id}`)
}

export function assignBookToLocation(locationId: number, bookId: number) {
  return put(`/bookshelves/locations/${locationId}/assign/${bookId}`)
}

export function clearLocation(locationId: number) {
  return put(`/bookshelves/locations/${locationId}/clear`)
}
