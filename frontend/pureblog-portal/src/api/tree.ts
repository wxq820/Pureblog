import request from './index'
import type { ApiResponse, SkillTreeSummary } from '@/types'

export const treeApi = {
  listPublic: () => request.get<any, ApiResponse<SkillTreeSummary[]>>('/tree/public/list'),
  getPublicByCode: (code: string) => request.get<any, ApiResponse<SkillTreeSummary>>(`/tree/public/${code}`)
}
