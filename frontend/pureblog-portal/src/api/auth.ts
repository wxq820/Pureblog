import request from './index'
import type { ApiResponse, AuthResponse } from '@/types'

export const authApi = {
  login: (data: { username: string; password: string; captchaCode?: string; captchaKey?: string }) =>
    request.post<any, ApiResponse<AuthResponse>>('/auth/login', data),
  register: (data: { username: string; password: string; email: string; nickname?: string }) =>
    request.post<any, ApiResponse<AuthResponse>>('/auth/register', data),
  logout: () => request.post<any, ApiResponse<void>>('/auth/logout'),
  refresh: (refreshToken: string) =>
    request.post<any, ApiResponse<AuthResponse>>('/auth/refresh', { refreshToken }),
  captcha: () => request.get<any, ApiResponse<{ key: string }>>('/auth/captcha')
}
