import request from './request'

export function exportBooksExcel(params?: Record<string, unknown>) {
  return request.get('/export/books/excel', { params, responseType: 'blob' })
}

export function exportBooksCsv(params?: Record<string, unknown>) {
  return request.get('/export/books/csv', { params, responseType: 'blob' })
}

export function exportUsersExcel(params?: Record<string, unknown>) {
  return request.get('/export/users/excel', { params, responseType: 'blob' })
}

export function exportBorrowsExcel(params?: Record<string, unknown>) {
  return request.get('/export/borrows/excel', { params, responseType: 'blob' })
}

export function exportPurchasesExcel(params?: Record<string, unknown>) {
  return request.get('/export/purchases/excel', { params, responseType: 'blob' })
}

export function downloadBlob(data: Blob, filename: string) {
  const url = window.URL.createObjectURL(new Blob([data]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
