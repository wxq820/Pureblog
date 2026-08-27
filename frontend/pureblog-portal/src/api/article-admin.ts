import request from './index'
import type { ApiResponse, ArticleListVO, PageResult } from '@/types'

export interface ArticleQuery {
  authorId?: number
  status?: number
  keyword?: string
  page?: number
  size?: number
}

export const articleAdminApi = {
  getList: (query: ArticleQuery) =>
    request.get<any, ApiResponse<PageResult<ArticleListVO>>>('/admin/article/list', { params: query }),
  offline: (id: number) =>
    request.post<any, ApiResponse<void>>(`/admin/article/offline/${id}`),
  delete: (id: number) =>
    request.delete<any, ApiResponse<void>>(`/admin/article/${id}`)
}
