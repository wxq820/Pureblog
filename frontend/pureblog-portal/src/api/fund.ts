import request from './index'
import type { ApiResponse } from '@/types'

export interface FundBuyVO {
  id: number | string
  fundId: number | string
  buyDate: string
  price: number | string
  shares: number | string
  amount: number | string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface FundVO {
  id: number | string
  name: string
  code?: string
  type?: string
  remark?: string
  sortOrder: number
  status: number
  createdAt?: string
  updatedAt?: string
  latestNav?: number | string | null
  latestNavAt?: string
  totalShares: number | string
  totalCost: number | string
  marketValue: number | string
  pnl: number | string
  pnlRate?: number | string | null
  buys: FundBuyVO[]
}

export interface FundDashboardVO {
  funds: FundVO[]
}

export interface FundCreateDTO {
  id?: number | string
  name: string
  code?: string
  type?: string
  remark?: string
  sortOrder?: number
  status?: number
}

export interface FundBuyDTO {
  fundId: number | string
  buyDate: string
  price: number
  shares: number
  remark?: string
}

export interface FundNavDTO {
  fundId: number | string
  nav: number
  recordedAt?: string
  remark?: string
}

export const fundApi = {
  dashboard: () =>
    request.get<any, ApiResponse<FundDashboardVO>>('/widget/fund/dashboard'),
  saveFund: (data: FundCreateDTO) =>
    request.post<any, ApiResponse<FundVO>>('/widget/fund/save', data),
  saveBuy: (data: FundBuyDTO) =>
    request.post<any, ApiResponse<void>>('/widget/fund/buy/save', data),
  saveNav: (data: FundNavDTO) =>
    request.post<any, ApiResponse<void>>('/widget/fund/nav/save', data),
  deleteBuy: (id: number | string) =>
    request.delete<any, ApiResponse<void>>(`/widget/fund/buy/${id}`),
  deleteFund: (id: number | string) =>
    request.delete<any, ApiResponse<void>>(`/widget/fund/${id}`)
}
