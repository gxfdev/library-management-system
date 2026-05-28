import { post, get } from './request'

export interface LoginParams {
  username: string
  password: string
  captchaCode?: string
  captchaKey?: string
}

export interface LoginResult {
  token: string
  userInfo: {
    id: number
    username: string
    realName: string
    role: string
    phone: string
    email: string
    avatar: string
  }
}

export interface RegisterParams {
  username: string
  password: string
  realName: string
  phone?: string
  email?: string
}

export function loginApi(data: LoginParams) {
  return post<LoginResult>('/auth/login', data)
}

export function registerApi(data: RegisterParams) {
  return post('/auth/register', data)
}

export interface CaptchaData {
  image: string
  captchaKey: string
}

export function getCaptchaImage() {
  return get<CaptchaData>('/captcha/image')
}

export function verifyCaptcha(code: string, captchaKey: string) {
  return post<boolean>('/captcha/verify', null, { params: { code, captchaKey } })
}

export function forgotPasswordSendCode(phone: string) {
  return post<{ code: string; message: string }>('/auth/forgot-password/send-code', { phone })
}

export function forgotPasswordReset(phone: string, code: string, newPassword: string) {
  return post('/auth/forgot-password/reset', { phone, code, newPassword })
}
