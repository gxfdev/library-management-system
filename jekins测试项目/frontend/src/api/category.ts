import { get, post, put, del } from './request'

export interface CategoryFormData {
  id?: number
  name: string
  parentId?: number
  sortOrder?: number
  status?: number
}

export function getCategoryList() {
  return get('/categories')
}

export function getCategoryById(id: number) {
  return get(`/categories/${id}`)
}

export function createCategory(data: CategoryFormData) {
  return post('/categories', data)
}

export function updateCategory(data: CategoryFormData) {
  return put('/categories', data)
}

export function deleteCategory(id: number) {
  return del(`/categories/${id}`)
}
