import request from './index'
import type { ApiResponse, LedgerDictVO, LedgerRecordVO } from '@/types'

export interface LedgerRecordPayload {
  recordDate: string
  platformDictId: number
  itemDictId?: number | null
  itemText?: string
  amount: number
  deviceId: string
}

export const ledgerApi = {
  getDict: (type: 1 | 2) =>
    request.get<any, ApiResponse<LedgerDictVO[]>>('/ledger/dict', { params: { type } }),
  addDict: (data: { type: 1 | 2; name: string; color?: string; icon?: string }) =>
    request.post<any, ApiResponse<LedgerDictVO>>('/ledger/dict', data),
  addRecord: (data: LedgerRecordPayload) =>
    request.post<any, ApiResponse<any>>('/ledger/record', data),
  listRecords: (deviceId: string, limit = 100) =>
    request.get<any, ApiResponse<LedgerRecordVO[]>>('/ledger/record', {
      params: { deviceId, limit }
    })
}
