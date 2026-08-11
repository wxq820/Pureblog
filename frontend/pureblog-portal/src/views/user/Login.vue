<template>
  <div class="login-page">
    <div class="login-card card">
      <h2>{{ isRegister ? '注册' : '登录' }}</h2>
      <form @submit.prevent="submit">
        <div class="form-group">
          <input v-model="form.username" class="input" placeholder="用户名" required />
        </div>
        <div v-if="isRegister" class="form-group">
          <input v-model="form.email" type="email" class="input" placeholder="邮箱" required />
        </div>
        <div class="form-group">
          <input v-model="form.password" type="password" class="input" placeholder="密码" required />
        </div>
        <button type="submit" class="btn btn-primary" style="width:100%; justify-content:center;">
          {{ isRegister ? '注册' : '登录' }}
        </button>
      </form>
      <div class="toggle text-center mt-4 text-sm">
        <a href="#" @click.prevent="isRegister = !isRegister">
          {{ isRegister ? '已有账号？登录' : '没有账号？注册' }}
        </a>
      </div>
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
const isRegister = ref(false)
const form = ref({ username: '', password: '', email: '' })

async function submit() {
  try {
    let res
    if (isRegister.value) {
      res = await authApi.register(form.value)
    } else {
      res = await authApi.login(form.value)
    }
    userStore.setAuth({ token: res.data.accessToken, user: res.data.user })
    router.push('/')
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}
</script>

<style scoped>
.login-page { display: flex; justify-content: center; align-items: center; min-height: 60vh; }
.login-card { width: 400px; }
.form-group { margin-bottom: 16px; }
</style>
