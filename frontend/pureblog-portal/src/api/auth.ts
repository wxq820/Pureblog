import request from './index'
import type { ApiResponse, AuthResponse } from '@/types'

export const authApi = {
  login: (data: { username: string; password: string }) =>
    request.post<any, ApiResponse<AuthResponse>>('/auth/login', data),
  logout: () => request.post<any, ApiResponse<void>>('/auth/logout'),
  refresh: (refreshToken: string) =>
    request.post<any, ApiResponse<AuthResponse>>('/auth/refresh', { refreshToken }),
  captcha: () => request.get<any, ApiResponse<{ key: string }>>('/auth/captcha')
}
