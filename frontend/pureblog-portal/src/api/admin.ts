import request from './index'
import type { ApiResponse, DashboardVO, AdminUserVO, AdminCommentVO, PageResult } from '@/types'

export const adminApi = {
  login: (data: { username: string; password: string }) =>
    request.post<any, ApiResponse<any>>('/auth/login', data),
  getDashboard: () => request.get<any, ApiResponse<DashboardVO>>('/admin/dashboard'),
  getUserList: (page = 1, size = 20, keyword?: string) =>
    request.get<any, ApiResponse<PageResult<AdminUserVO>>>('/admin/user/list', { params: { page, size, keyword } }),
  updateUser: (data: { userId: number; role?: number; status?: number }) =>
    request.put<any, ApiResponse<void>>('/admin/user/update', data),
  disableUser: (userId: number) =>
    request.post<any, ApiResponse<void>>(`/admin/user/disable/${userId}`),
  getPendingComments: (page = 1, size = 20) =>
    request.get<any, ApiResponse<PageResult<AdminCommentVO>>>('/admin/comment/pending', { params: { page, size } }),
  auditComment: (id: number, approve: boolean) =>
    request.post<any, ApiResponse<void>>(`/admin/comment/audit/${id}`, null, { params: { approve } }),
  deleteComment: (id: number) =>
    request.delete<any, ApiResponse<void>>(`/admin/comment/${id}`)
}
