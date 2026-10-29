import request from './index'
import type { ApiResponse } from '@/types'

export interface PositionVO {
  id: number
  name: string
  cost: number | string
  remark?: string
  sortOrder: number
  status: number
  createdAt?: string
  updatedAt?: string
  latestMarketValue: number | string
  latestRecordedAt?: string
  pnl: number | string
  pnlRate?: number | string | null
  distanceToBreakEven: number | string
  recoveryProgress: number | string
  breakEven: boolean
}

export interface RecoveryDashboardVO {
  positions: PositionVO[]
  totalCost: number | string
  totalMarketValue: number | string
  totalPnl: number | string
  totalRecoveryProgress: number | string
}

export interface PositionCreateDTO {
  id?: number
  name: string
  cost: number
  remark?: string
  sortOrder?: number
  status?: number
}

export interface PositionValueDTO {
  positionId: number
  marketValue: number
  recordedAt?: string
  remark?: string
}

export const recoveryApi = {
  dashboard: () =>
    request.get<any, ApiResponse<RecoveryDashboardVO>>('/widget/recovery/dashboard'),
  savePosition: (data: PositionCreateDTO) =>
    request.post<any, ApiResponse<PositionVO>>('/widget/recovery/position/save', data),
  saveValue: (data: PositionValueDTO) =>
    request.post<any, ApiResponse<void>>('/widget/recovery/value/save', data),
  deletePosition: (id: number) =>
    request.delete<any, ApiResponse<void>>(`/widget/recovery/position/${id}`)
}
