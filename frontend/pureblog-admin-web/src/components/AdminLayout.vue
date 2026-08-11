<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="logo">PureBlog 管理后台</div>
      <nav class="menu">
        <router-link to="/dashboard" class="menu-item" active-class="active">
          Dashboard
        </router-link>
        <router-link to="/article/list" class="menu-item" active-class="active">
          文章管理
        </router-link>
        <router-link to="/comment/audit" class="menu-item" active-class="active">
          评论审核
        </router-link>
        <router-link to="/user/manage" class="menu-item" active-class="active">
          用户管理
        </router-link>
        <router-link to="/category/manage" class="menu-item" active-class="active">
          分类管理
        </router-link>
        <router-link to="/tag/manage" class="menu-item" active-class="active">
          标签管理
        </router-link>
      </nav>
    </aside>
    <div class="admin-main">
      <header class="admin-header">
        <span>{{ adminStore.userInfo?.nickname || '管理员' }}</span>
        <el-button text @click="handleLogout">退出登录</el-button>
      </header>
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

function handleLogout() {
  adminStore.logout()
  router.push('/login')
}
</script>
