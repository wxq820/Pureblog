import request from './index'
import type { ApiResponse, WidgetVO } from '@/types'

export const widgetApi = {
  /** 公开: 拉首页侧栏 widget 列表 (按热度降序). */
  list: () => request.get<any, ApiResponse<WidgetVO[]>>('/widget/list'),
  /** 公开: 进入 widget 时调用, 记一次点击. */
  click: (widgetId: number, deviceId: string) =>
    request.post<any, ApiResponse<boolean>>('/widget/click', { widgetId, deviceId })
}

export const widgetAdminApi = {
  listAll: () => request.get<any, ApiResponse<WidgetVO[]>>('/admin/widget/list'),
  create: (data: {
    code: string; name: string; description?: string; icon?: string
    iconColor?: string; path: string; sortOrder?: number; status?: number
  }) => request.post<any, ApiResponse<WidgetVO>>('/admin/widget/create', data),
  update: (data: {
    id: number; name?: string; description?: string; icon?: string
    iconColor?: string; path?: string; sortOrder?: number; status?: number
  }) => request.put<any, ApiResponse<void>>('/admin/widget/update', data),
  updateStatus: (id: number, status: number) =>
    request.post<any, ApiResponse<void>>(`/admin/widget/status/${id}`, null, { params: { status } }),
  remove: (id: number) =>
    request.delete<any, ApiResponse<void>>(`/admin/widget/${id}`)
}
