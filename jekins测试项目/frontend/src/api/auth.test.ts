import { describe, it, expect, vi, beforeEach } from 'vitest'

const mockGet = vi.fn()
const mockPost = vi.fn()

vi.mock('./request', () => ({
  get: (...args: any[]) => mockGet(...args),
  post: (...args: any[]) => mockPost(...args),
  put: vi.fn(),
  del: vi.fn(),
}))

describe('Auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loginApi should call POST /auth/login', async () => {
    mockPost.mockResolvedValue({
      token: 'test-token',
      userInfo: { id: 1, username: 'admin', realName: '管理员', role: 'ADMIN' },
    })

    const { loginApi } = await import('./auth')
    const result = await loginApi({ username: 'admin', password: 'admin123' })
    expect(mockPost).toHaveBeenCalledWith('/auth/login', {
      username: 'admin',
      password: 'admin123',
    })
  })

  it('registerApi should call POST /auth/register', async () => {
    mockPost.mockResolvedValue({ code: 200 })

    const { registerApi } = await import('./auth')
    await registerApi({ username: 'newuser', password: 'pass123', realName: 'New User' })
    expect(mockPost).toHaveBeenCalledWith('/auth/register', {
      username: 'newuser',
      password: 'pass123',
      realName: 'New User',
    })
  })

  it('getCaptchaImage should call GET /captcha/image', async () => {
    mockGet.mockResolvedValue({
      image: 'data:image/png;base64,xxx',
      captchaKey: 'key123',
    })

    const { getCaptchaImage } = await import('./auth')
    await getCaptchaImage()
    expect(mockGet).toHaveBeenCalledWith('/captcha/image')
  })

  it('forgotPasswordSendCode should call POST /auth/forgot-password/send-code', async () => {
    mockPost.mockResolvedValue({ code: '1234', message: 'success' })

    const { forgotPasswordSendCode } = await import('./auth')
    await forgotPasswordSendCode('13800138000')
    expect(mockPost).toHaveBeenCalledWith('/auth/forgot-password/send-code', {
      phone: '13800138000',
    })
  })

  it('forgotPasswordReset should call POST /auth/forgot-password/reset', async () => {
    mockPost.mockResolvedValue({})

    const { forgotPasswordReset } = await import('./auth')
    await forgotPasswordReset('13800138000', '1234', 'newpass123')
    expect(mockPost).toHaveBeenCalledWith('/auth/forgot-password/reset', {
      phone: '13800138000',
      code: '1234',
      newPassword: 'newpass123',
    })
  })
})
