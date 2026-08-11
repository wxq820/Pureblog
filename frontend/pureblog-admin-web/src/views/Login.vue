<template>
  <div class="login-container">
    <div class="login-box">
      <h2>PureBlog 管理后台</h2>
      <el-form @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" html-type="submit" style="width:100%" :loading="loading" size="large">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()
const loading = ref(false)
const form = ref({ username: '', password: '' })

async function handleLogin() {
  loading.value = true
  try {
    const res = await adminApi.login(form.value)
    adminStore.setAuth({ token: res.data.accessToken, user: res.data.user })
    router.push('/dashboard')
  } catch (e: any) {
    alert(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-box {
  background: white;
  padding: 40px;
  border-radius: 12px;
  width: 400px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}
.login-box h2 { text-align: center; margin-bottom: 24px; }
</style>
