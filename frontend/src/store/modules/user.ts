import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  id: number
  username: string
  realName: string
  role: string
  phone: string
  email: string
  avatar: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const isLibrarian = computed(() => userInfo.value?.role === 'LIBRARIAN')
  const isReader = computed(() => userInfo.value?.role === 'READER')
  const role = computed(() => userInfo.value?.role || '')

  const canManageBooks = computed(() => isAdmin.value || isLibrarian.value)
  const canManageBorrows = computed(() => isAdmin.value || isLibrarian.value)
  const canManageUsers = computed(() => isAdmin.value)
  const canBorrow = computed(() => isLoggedIn.value)

  async function initUserInfo(forceRefresh = false) {
    if (userInfo.value && !forceRefresh) return
    if (!forceRefresh) {
      const infoStr = localStorage.getItem('userInfo')
      if (infoStr) {
        try {
          userInfo.value = JSON.parse(infoStr)
          return
        } catch {
          userInfo.value = null
          localStorage.removeItem('userInfo')
        }
      }
    }
    try {
      const { get: apiGet } = await import('@/api/request')
      const data = await apiGet('/auth/me') as any
      if (data) {
        setUserInfo({
          id: data.id,
          username: data.username,
          realName: data.realName || '',
          role: data.role,
          phone: data.phone || '',
          email: data.email || '',
          avatar: data.avatar || '',
        })
      }
    } catch {}
  }

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function hasPermission(requiredRoles: string[]): boolean {
    if (!userInfo.value) return false
    return requiredRoles.includes(userInfo.value.role)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    isLibrarian,
    isReader,
    role,
    canManageBooks,
    canManageBorrows,
    canManageUsers,
    canBorrow,
    initUserInfo,
    setToken,
    setUserInfo,
    logout,
    hasPermission,
  }
})
