import request from './index'
import type { ApiResponse, TreeVO, TreeNodeVO } from '@/types'

export const treeAdminApi = {
  listTrees: () => request.get<any, ApiResponse<TreeVO[]>>('/admin/tree/list'),
  createTree: (data: { code: string; name: string; description?: string; coverColor?: string; sortOrder?: number }) =>
    request.post<any, ApiResponse<any>>('/admin/tree/create', data),
  updateTree: (data: { id: number; name?: string; description?: string; coverColor?: string; sortOrder?: number; status?: number }) =>
    request.put<any, ApiResponse<void>>('/admin/tree/update', data),
  updateTreeStatus: (id: number, status: number) =>
    request.post<any, ApiResponse<void>>(`/admin/tree/status/${id}`, null, { params: { status } }),
  deleteTree: (id: number) =>
    request.delete<any, ApiResponse<void>>(`/admin/tree/${id}`),
  listNodes: (treeId: number) =>
    request.get<any, ApiResponse<TreeNodeVO[]>>(`/admin/tree-node/list/${treeId}`),
  createNode: (data: { treeId: number; parentId: number; name: string; color?: string; sortOrder?: number }) =>
    request.post<any, ApiResponse<void>>('/admin/tree-node/create', data),
  updateNode: (data: { id: number; name?: string; color?: string; sortOrder?: number }) =>
    request.put<any, ApiResponse<void>>('/admin/tree-node/update', data),
  moveNode: (data: { id: number; newParentId: number; newSortOrder?: number }) =>
    request.post<any, ApiResponse<void>>('/admin/tree-node/move', data),
  deleteNode: (id: number) =>
    request.delete<any, ApiResponse<void>>(`/admin/tree-node/${id}`)
}

export const adminArticleApi = {
  listForArticle: () => request.get<any, ApiResponse<TreeVO[]>>('/admin/article/tree-list'),
  createArticle: (data: any) =>
    request.post<any, ApiResponse<any>>('/admin/article/create', data),
  updateArticle: (data: any) =>
    request.put<any, ApiResponse<any>>('/admin/article/update', data),
  offline: (id: number) =>
    request.post<any, ApiResponse<void>>(`/admin/article/offline/${id}`),
  delete: (id: number) =>
    request.delete<any, ApiResponse<void>>(`/admin/article/${id}`)
}
