import axios from 'axios'
import type { AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
})

let isRedirecting = false

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const publicPaths = ['/auth/login', '/auth/register', '/auth/forgot-password/']
    const isPublicApi = publicPaths.some((path) => config.url?.startsWith(path))
    if (!isPublicApi) {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && res.code !== undefined && res.code !== 200) {
      const message = res.message || '请求失败'

      if (res.code === 401) {
        handleUnauthorized(message)
        return Promise.reject(new Error(message))
      }

      if (res.code === 403) {
        ElMessage.error(message || '权限不足，无法执行该操作')
        return Promise.reject(new Error(message))
      }

      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return res.data !== undefined ? res.data : res
  },
  (error) => {
    if (axios.isCancel(error) || error.code === 'ERR_CANCELED') {
      return Promise.reject(error)
    }

    let message = '网络错误，请稍后重试'
    if (error.response) {
      const data = error.response.data
      switch (error.response.status) {
        case 400:
          message = data?.message || '请求参数错误'
          break
        case 401:
          message = data?.message || '未授权，请重新登录'
          handleUnauthorized(message)
          break
        case 403:
          message = data?.message || '权限不足，无法访问该资源'
          ElMessage.error(message)
          break
        case 429:
          message = data?.message || '操作过于频繁，请稍后重试'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = data?.message || message
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时，请稍后重试'
    } else if (error.message.includes('Network')) {
      message = '网络连接失败，请检查网络'
    }

    if (error.response?.status !== 401 && error.response?.status !== 403) {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

function handleUnauthorized(message?: string) {
  if (isRedirecting) return
  isRedirecting = true

  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')

  ElMessage.error(message || '登录已过期，请重新登录')

  setTimeout(() => {
    isRedirecting = false
    const currentPath = window.location.pathname
    if (currentPath !== '/login') {
      window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
    }
  }, 1500)
}

export function sanitizeInput(str: string): string {
  if (!str) return str
  return str.replace(/[<>'"&\\]/g, (char) => ({
    '<': '&lt;',
    '>': '&gt;',
    "'": '&#39;',
    '"': '&quot;',
    '&': '&amp;',
    '\\': '&#92;',
  }[char] || char))
}

export const get = <T>(url: string, params?: Record<string, unknown>) =>
  request.get<any, T>(url, { params })

export const post = <T>(url: string, data?: Record<string, unknown>, config?: AxiosRequestConfig) =>
  request.post<any, T>(url, data, config)

export const put = <T>(url: string, data?: Record<string, unknown>, config?: AxiosRequestConfig) =>
  request.put<any, T>(url, data, config)

export const del = <T>(url: string) =>
  request.delete<any, T>(url)

export default request
