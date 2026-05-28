import { describe, it, expect, vi, beforeEach } from 'vitest'

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockPut = vi.fn()

vi.mock('./request', () => ({
  get: (...args: any[]) => mockGet(...args),
  post: (...args: any[]) => mockPost(...args),
  put: (...args: any[]) => mockPut(...args),
  del: vi.fn(),
}))

describe('Borrow API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getBorrowPage should call GET /borrows with params', async () => {
    mockGet.mockResolvedValue({ records: [], total: 0 })

    const { getBorrowPage } = await import('./borrow')
    await getBorrowPage({ page: 1, size: 10 })
    expect(mockGet).toHaveBeenCalledWith('/borrows', { page: 1, size: 10 })
  })

  it('borrowBook should call POST /borrows/self', async () => {
    mockPost.mockResolvedValue({ id: 1, status: 'BORROWING' })

    const { borrowBook } = await import('./borrow')
    await borrowBook({ bookId: 1 })
    expect(mockPost).toHaveBeenCalledWith('/borrows/self', {
      bookId: 1,
      borrowDays: undefined,
      remark: undefined,
    })
  })

  it('adminBorrowBook should call POST /borrows', async () => {
    mockPost.mockResolvedValue({ id: 1, status: 'BORROWING' })

    const { adminBorrowBook } = await import('./borrow')
    await adminBorrowBook({ userId: 3, bookId: 1 })
    expect(mockPost).toHaveBeenCalledWith('/borrows', { userId: 3, bookId: 1 })
  })

  it('returnBook should call PUT /borrows/:id/return', async () => {
    mockPut.mockResolvedValue({})

    const { returnBook } = await import('./borrow')
    await returnBook(1)
    expect(mockPut).toHaveBeenCalledWith('/borrows/1/return', {})
  })

  it('selfReturnBook should call PUT /borrows/:id/self-return', async () => {
    mockPut.mockResolvedValue({})

    const { selfReturnBook } = await import('./borrow')
    await selfReturnBook(1)
    expect(mockPut).toHaveBeenCalledWith('/borrows/1/self-return', {})
  })

  it('renewBook should call PUT /borrows/:id/renew', async () => {
    mockPut.mockResolvedValue({})

    const { renewBook } = await import('./borrow')
    await renewBook(1)
    expect(mockPut).toHaveBeenCalledWith('/borrows/1/renew', {})
  })

  it('getMyBorrows should call GET /borrows/my', async () => {
    mockGet.mockResolvedValue({ records: [], total: 0 })

    const { getMyBorrows } = await import('./borrow')
    await getMyBorrows({ page: 1, size: 10 })
    expect(mockGet).toHaveBeenCalledWith('/borrows/my', { page: 1, size: 10 })
  })
})
