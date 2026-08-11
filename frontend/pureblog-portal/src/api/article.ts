import request from './index'
import type { ApiResponse, ArticleListVO, ArticleDetailVO, PageResult } from '@/types'

export interface ArticleQuery {
  categoryId?: number
  tagId?: number
  keyword?: string
  sortBy?: string
  sortOrder?: string
  page?: number
  size?: number
}

export const articleApi = {
  getList: (query: ArticleQuery) =>
    request.get<any, ApiResponse<PageResult<ArticleListVO>>>('/article/list', { params: query }),
  getDetail: (id: number) =>
    request.get<any, ApiResponse<ArticleDetailVO>>(`/article/public/${id}`),
  getHot: (limit = 10) =>
    request.get<any, ApiResponse<ArticleListVO[]>>('/article/hot', { params: { limit } }),
  getFeatured: (limit = 5) =>
    request.get<any, ApiResponse<ArticleListVO[]>>('/article/featured', { params: { limit } }),
  getAuthorArticles: (authorId: number, page = 1, size = 10) =>
    request.get<any, ApiResponse<ArticleListVO[]>>(`/article/author/${authorId}`, { params: { page, size } }),
  getCategories: () =>
    request.get<any, ApiResponse<any[]>>('/category/list'),
  getTags: () =>
    request.get<any, ApiResponse<any[]>>('/tag/list'),
  create: (data: any) => request.post<any, ApiResponse<ArticleDetailVO>>('/article/create', data),
  update: (data: any) => request.put<any, ApiResponse<ArticleDetailVO>>('/article/update', data),
  publish: (data: { id: number; isFeatured?: number; isTop?: number }) =>
    request.post<any, ApiResponse<void>>('/article/publish', data),
  offline: (id: number) => request.post<any, ApiResponse<void>>(`/article/offline/${id}`),
  delete: (id: number) => request.delete<any, ApiResponse<void>>(`/article/${id}`)
}
