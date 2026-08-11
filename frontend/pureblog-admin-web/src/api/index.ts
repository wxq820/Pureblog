import axios from 'axios'
import { useAdminStore } from '@/stores/admin'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const adminStore = useAdminStore()
  if (adminStore.token) {
    config.headers.Authorization = `Bearer ${adminStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      const adminStore = useAdminStore()
      adminStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(error.response?.data || error)
  }
)

export default request
