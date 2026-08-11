import request from './index'
import type { ApiResponse, SearchResult } from '@/types'

export const searchApi = {
  search: (params: { keyword: string; categoryId?: number; tagId?: number; sortBy?: string; sortOrder?: string; page?: number; size?: number }) =>
    request.get<any, ApiResponse<SearchResult>>('/search', { params })
}
