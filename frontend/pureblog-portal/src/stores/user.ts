import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<any>(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLogin = computed(() => !!token.value)
  const isAuthor = computed(() => userInfo.value?.roleCode >= 2)
  const isAdmin = computed(() => userInfo.value?.roleCode >= 3)

  function setAuth(data: { token: string; user: any }) {
    token.value = data.token
    userInfo.value = data.user
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.user))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, isLogin, isAuthor, isAdmin, setAuth, logout }
})
