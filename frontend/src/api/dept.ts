import { get } from './request'

export interface Dept {
  id: number
  name: string
  parentId: number
  leader: string
  phone: string
}

export function getDeptList() {
  return get<Dept[]>('/depts')
}
