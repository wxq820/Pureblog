import request from './index'
import type { ApiResponse } from '@/types'

export interface AccountCategoryVO {
  id: number | string
  name: string
  sortOrder: number
  isCountedInTotal: number
  status: number
  createdAt?: string
  updatedAt?: string
}

export interface AccountVO {
  id: number | string
  name: string
  icon?: string
  color?: string
  sortOrder: number
  status: number
  createdAt?: string
  updatedAt?: string
}

export interface NetWorthEntryVO {
  id: number | string
  snapshotId: number | string
  accountId: number | string
  accountName: string
  categoryId: number | string
  categoryName: string
  balance: number | string
}

export interface NetWorthSnapshotVO {
  id: number | string
  snapshotAt: string
  eventTag?: string
  remark?: string
  createdAt?: string
  total: number | string
  prevTotal?: number | string | null
  deltaToPrev?: number | string | null
  perCategoryTotals: Array<{
    categoryId: number | string
    categoryName: string
    isCountedInTotal: number
    total: number | string
  }>
}

export interface NetWorthSnapshotCreateDTO {
  snapshotAt: string
  eventTag?: string
  remark?: string
  entries: Array<{ accountId: number | string; categoryId: number | string; balance: string }>
}

export const netWorthApi = {
  // 登录态
  saveSnapshot: (data: NetWorthSnapshotCreateDTO) =>
    request.post<any, ApiResponse<NetWorthSnapshotVO>>('/widget/networth/snapshot/save', data),
  listSnapshots: () =>
    request.get<any, ApiResponse<NetWorthSnapshotVO[]>>('/widget/networth/snapshot/list'),
  listEntries: (id: number | string) =>
    request.get<any, ApiResponse<NetWorthEntryVO[]>>(`/widget/networth/snapshot/${id}/entries`),
  dashboard: () =>
    request.get<any, ApiResponse<NetWorthSnapshotVO>>('/widget/networth/dashboard'),
  deleteSnapshot: (id: number | string) =>
    request.delete<any, ApiResponse<void>>(`/widget/networth/snapshot/${id}`),

  // admin
  listCategories: () =>
    request.get<any, ApiResponse<AccountCategoryVO[]>>('/admin/widget/networth/category/list'),
  createCategory: (data: { name: string; sortOrder?: number; isCountedInTotal?: number; status?: number }) =>
    request.post<any, ApiResponse<AccountCategoryVO>>('/admin/widget/networth/category/create', data),
  updateCategory: (id: number | string, data: Partial<{ name: string; sortOrder: number; isCountedInTotal: number; status: number }>) =>
    request.put<any, ApiResponse<void>>(`/admin/widget/networth/category/update/${id}`, data),
  deleteCategory: (id: number | string) =>
    request.delete<any, ApiResponse<void>>(`/admin/widget/networth/category/delete/${id}`),
  listAccounts: () =>
    request.get<any, ApiResponse<AccountVO[]>>('/admin/widget/networth/account/list'),
  createAccount: (data: { name: string; icon?: string; color?: string; sortOrder?: number; status?: number }) =>
    request.post<any, ApiResponse<AccountVO>>('/admin/widget/networth/account/create', data),
  updateAccount: (id: number | string, data: Partial<{ name: string; icon: string; color: string; sortOrder: number; status: number }>) =>
    request.put<any, ApiResponse<void>>(`/admin/widget/networth/account/update/${id}`, data),
  deleteAccount: (id: number | string) =>
    request.delete<any, ApiResponse<void>>(`/admin/widget/networth/account/delete/${id}`)
}
