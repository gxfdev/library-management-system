import { get, post, put } from './request'

export interface ProfileStats {
  totalBorrows: number
  activeBorrows: number
  overdueBorrows: number
}

export function getProfile() {
  return get('/profile')
}

export function updateProfile(data: Record<string, string>) {
  return put('/profile', data)
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return put('/profile/password', data)
}

export function getProfileStats() {
  return get<ProfileStats>('/profile/stats')
}

export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return post('/profile/avatar', formData as unknown as Record<string, unknown>, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000,
  })
}
