import { describe, it, expect, vi, beforeEach } from 'vitest'

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockPut = vi.fn()
const mockDel = vi.fn()

vi.mock('./request', () => ({
  get: (...args: any[]) => mockGet(...args),
  post: (...args: any[]) => mockPost(...args),
  put: (...args: any[]) => mockPut(...args),
  del: (...args: any[]) => mockDel(...args),
}))

describe('Book API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getBookPage should call GET /books with pagination params', async () => {
    mockGet.mockResolvedValue({ records: [], total: 0, page: 1, size: 10 })

    const { getBookPage } = await import('./book')
    await getBookPage({ page: 1, size: 10 })
    expect(mockGet).toHaveBeenCalledWith('/books', { page: 1, size: 10 })
  })

  it('getBookPage should pass keyword and categoryId filters', async () => {
    mockGet.mockResolvedValue({ records: [], total: 0 })

    const { getBookPage } = await import('./book')
    await getBookPage({ page: 1, size: 10, keyword: 'Java', categoryId: 1 })
    expect(mockGet).toHaveBeenCalledWith('/books', {
      page: 1, size: 10, keyword: 'Java', categoryId: 1,
    })
  })

  it('getBookById should call GET /books/:id', async () => {
    mockGet.mockResolvedValue({ id: 1, title: 'Java核心技术', isbn: '9787111698809' })

    const { getBookById } = await import('./book')
    await getBookById(1)
    expect(mockGet).toHaveBeenCalledWith('/books/1')
  })

  it('createBook should call POST /books', async () => {
    mockPost.mockResolvedValue({ id: 10, title: '新书' })

    const { createBook } = await import('./book')
    await createBook({ isbn: '9787111999999', title: '新书', categoryId: 1, stockTotal: 5 })
    expect(mockPost).toHaveBeenCalledWith('/books', {
      isbn: '9787111999999',
      title: '新书',
      categoryId: 1,
      stockTotal: 5,
    })
  })

  it('updateBook should call PUT /books', async () => {
    mockPut.mockResolvedValue({ id: 1, title: '更新书' })

    const { updateBook } = await import('./book')
    await updateBook({ id: 1, isbn: '9787111999999', title: '更新书', categoryId: 1, stockTotal: 5 })
    expect(mockPut).toHaveBeenCalledWith('/books', {
      id: 1,
      isbn: '9787111999999',
      title: '更新书',
      categoryId: 1,
      stockTotal: 5,
    })
  })

  it('deleteBook should call DELETE /books/:id', async () => {
    mockDel.mockResolvedValue({})

    const { deleteBook } = await import('./book')
    await deleteBook(1)
    expect(mockDel).toHaveBeenCalledWith('/books/1')
  })

  it('updateBookStatus should call PUT /books/:id/status', async () => {
    mockPut.mockResolvedValue({})

    const { updateBookStatus } = await import('./book')
    await updateBookStatus(1, 0)
    expect(mockPut).toHaveBeenCalledWith('/books/1/status', {}, { params: { status: 0 } })
  })
})
