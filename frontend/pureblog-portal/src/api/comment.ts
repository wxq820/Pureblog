import request from './index'
import type { ApiResponse, CommentVO, PageResult } from '@/types'

export const commentApi = {
  getComments: (articleId: number, page = 1, size = 20) =>
    request.get<any, ApiResponse<PageResult<CommentVO>>>(`/comment/article/${articleId}`, { params: { page, size } }),
  create: (data: { articleId: number; content: string; parentId?: number; replyToId?: number; replyToUid?: number }) =>
    request.post<any, ApiResponse<CommentVO>>('/comment/create', data),
  delete: (id: number) => request.delete<any, ApiResponse<void>>(`/comment/${id}`),
  like: (articleId: number) => request.post<any, ApiResponse<void>>(`/article/like/${articleId}`),
  unlike: (articleId: number) => request.delete<any, ApiResponse<void>>(`/article/like/${articleId}`),
  collect: (articleId: number) => request.post<any, ApiResponse<void>>(`/article/collect/${articleId}`),
  uncollect: (articleId: number) => request.delete<any, ApiResponse<void>>(`/article/collect/${articleId}`),
  getStats: (articleId: number) => request.get<any, ApiResponse<any>>(`/article/stats/${articleId}`)
}
