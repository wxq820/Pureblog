<template>
  <div class="login-page">
    <div class="login-card card">
      <h2>登录</h2>
      <p class="text-secondary text-sm mt-2">个人博客 · 仅限本人使用</p>
      <form @submit.prevent="submit">
        <div class="form-group">
          <input v-model="form.username" class="input" placeholder="用户名" required />
        </div>
        <div class="form-group">
          <input v-model="form.password" type="password" class="input" placeholder="密码" required />
        </div>
        <button type="submit" class="btn btn-primary" style="width:100%; justify-content:center;">
          登录
        </button>
      </form>
      <p class="text-secondary text-xs text-center mt-4">
        没有账号？请直接在数据库 <code>pb_user</code> 表中插入账号（密码使用 BCrypt 哈希）。
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const form = ref({ username: '', password: '' })

async function submit() {
  try {
    const res = await authApi.login(form.value)
    userStore.setAuth({ token: res.data.accessToken, user: res.data.user })
    router.push('/')
  } catch (e: any) {
    alert(e.message || '登录失败')
  }
}
</script>

<style scoped>
.login-page { display: flex; justify-content: center; align-items: center; min-height: 60vh; }
.login-card { width: 400px; }
.form-group { margin-bottom: 16px; }
.text-xs { font-size: 12px; }
.mt-2 { margin-top: 8px; }
.mt-4 { margin-top: 16px; }
.text-center { text-align: center; }
code { background: #f1f5f9; padding: 2px 4px; border-radius: 3px; font-size: 11px; }
</style>
