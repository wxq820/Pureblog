import request from './index'
import type { ApiResponse, UserProfileVO } from '@/types'

export const userApi = {
  getCurrentUser: () => request.get<any, ApiResponse<UserProfileVO>>('/user/me'),
  getUserProfile: (userId: number) =>
    request.get<any, ApiResponse<UserProfileVO>>(`/user/public/${userId}`),
  updateProfile: (data: { nickname?: string; bio?: string; avatarUrl?: string }) =>
    request.put<any, ApiResponse<void>>('/user/profile', data),
  follow: (userId: number) => request.post<any, ApiResponse<void>>(`/user/follow/${userId}`),
  unfollow: (userId: number) => request.delete<any, ApiResponse<void>>(`/user/follow/${userId}`),
  getFollowers: (userId: number, page = 1, size = 20) =>
    request.get<any, ApiResponse<UserProfileVO[]>>(`/user/followers/${userId}`, { params: { page, size } }),
  getFollowing: (userId: number, page = 1, size = 20) =>
    request.get<any, ApiResponse<UserProfileVO[]>>(`/user/following/${userId}`, { params: { page, size } })
}
