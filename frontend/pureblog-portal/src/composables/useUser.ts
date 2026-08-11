import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

export function useUser() {
  const userStore = useUserStore()
  return {
    user: computed(() => userStore.userInfo),
    isLogin: computed(() => userStore.isLogin),
    isAuthor: computed(() => userStore.isAuthor),
    isAdmin: computed(() => userStore.isAdmin),
    token: computed(() => userStore.token)
  }
}
