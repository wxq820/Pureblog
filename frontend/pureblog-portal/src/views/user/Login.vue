<template>
  <div class="login-page">
    <div class="login-card card">
    <h2 class="title">登录</h2>
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const form = ref({ username: '', password: '' })

async function submit() {
  try {
    const res = await authApi.login(form.value)
    if (!res?.data?.accessToken) {
      alert(res?.message || '登录失败：服务端返回数据异常')
      return
    }
    userStore.setAuth({ token: res.data.accessToken, user: res.data.user })
    // 登录前被守卫拦下时, 跳回原来要去的页面
    const redirect = route.query.redirect
    router.push(typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/')
  } catch (e: any) {
    alert(e?.message || e?.data?.message || '登录失败')
  }
}
</script>

<style scoped>
.login-page { display: flex; justify-content: center; align-items: center; min-height: 60vh; }
.login-card { width: 400px; }
.login-card .title { text-align: center; }
.form-group { margin-bottom: 16px; }
.mt-2 { margin-top: 8px; }
</style>
