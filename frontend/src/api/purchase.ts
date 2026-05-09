import { get, post, put, del } from './request'

export function getPurchaseOrders(params: { page?: number; size?: number; status?: string; deptId?: number }) {
  return get('/purchases', params)
}

export function getPurchaseOrderById(id: number) {
  return get(`/purchases/${id}`)
}

export function createPurchaseOrder(data: Record<string, unknown>) {
  return post('/purchases', data)
}

export function updatePurchaseOrder(data: Record<string, unknown>) {
  return put('/purchases', data)
}

export function deletePurchaseOrder(id: number) {
  return del(`/purchases/${id}`)
}

export function submitPurchaseOrder(id: number) {
  return put(`/purchases/${id}/submit`, {})
}

export function approvePurchaseOrder(id: number, data: { action: string; comment?: string }) {
  return put(`/purchases/${id}/approve`, data as unknown as Record<string, unknown>)
}

export function quickInstock(id: number) {
  return put(`/purchases/${id}/instock`, {})
}

export function getApprovalRecords(orderId: number) {
  return get(`/purchases/${orderId}/approvals`)
}
