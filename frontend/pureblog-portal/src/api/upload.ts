import request from './index'
import type { ApiResponse } from '@/types'

export interface UploadResult {
  url: string
  name: string
  size: number
}

export const uploadApi = {
  uploadImage: (file: File) => {
    const fd = new FormData()
    fd.append('file', file)
    return request.post<any, ApiResponse<UploadResult>>('/upload/image', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
