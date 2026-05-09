export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface Book {
  id: number
  isbn: string
  title: string
  author: string
  publisher: string
  publishDate: string
  categoryId: number
  categoryName: string
  price: number
  pages: number
  coverImage: string
  description: string
  location: string
  stockTotal: number
  stockAvailable: number
  status: number
  createTime: string
  updateTime: string
}

export interface BookCategory {
  id: number
  name: string
  parentId: number
  level: number
  sortOrder: number
  status: number
  children: BookCategory[]
}

export interface BorrowRecord {
  id: number
  userId: number
  userName: string
  bookId: number
  bookTitle: string
  borrowDate: string
  dueDate: string
  returnDate: string
  renewCount: number
  status: 'BORROWING' | 'RETURNED' | 'OVERDUE'
  fineAmount: number
  remark: string
}

export interface User {
  id: number
  username: string
  realName: string
  role: string
  phone: string
  email: string
  avatar: string
  status: number
  createTime: string
  updateTime: string
}

export interface InventoryItem {
  bookId: number
  bookTitle: string
  isbn: string
  stockTotal: number
  stockAvailable: number
  borrowRate: number
}
