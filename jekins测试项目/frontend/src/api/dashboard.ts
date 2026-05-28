import { get } from './request'

export interface DashboardStats {
  totalBooks: number
  totalUsers: number
  todayBorrows: number
  overdueCount: number
  totalBorrows: number
  availableBooks: number
}

export interface InventoryStats {
  totalBooks: number
  availableBooks: number
  borrowedBooks: number
  lowStockBooks: number
}

export function getDashboardStats() {
  return get<DashboardStats>('/dashboard/stats')
}

export function getInventoryStats() {
  return get<InventoryStats>('/inventory/stats')
}

export function getBorrowTrend() {
  return get<Array<{ label: string; value: number }>>('/statistics/borrow-trend')
}

export function getCategoryDistribution() {
  return get<Array<{ name: string; count: number; percentage: number; color: string }>>('/statistics/category-distribution')
}

export function getHotBooks() {
  return get<Array<{ title: string; borrowCount: number }>>('/statistics/hot-books')
}

export function getMonthlyTrend(months?: number) {
  return get<Array<{ label: string; value: number }>>('/statistics/monthly-trend', months ? { months } : undefined)
}
