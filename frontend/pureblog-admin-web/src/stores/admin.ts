import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const token = ref<string>(localStorage.getItem('admin_token') || '')
  const userInfo = ref<any>(JSON.parse(localStorage.getItem('admin_user') || 'null'))

  function setAuth(data: { token: string; user: any }) {
    token.value = data.token
    userInfo.value = data.user
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_user', JSON.stringify(data.user))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  return { token, userInfo, setAuth, logout }
})
