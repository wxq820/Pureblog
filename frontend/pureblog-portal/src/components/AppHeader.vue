<template>
  <header class="header">
    <div class="container flex items-center justify-between">
      <div class="flex items-center gap-4">
        <router-link to="/" class="logo">PureBlog</router-link>
        <nav class="nav">
          <router-link to="/">首页</router-link>
          <router-link to="/search">搜索</router-link>
        </nav>
      </div>
      <div class="flex items-center gap-2">
        <template v-if="userStore.isLogin">
          <router-link v-if="userStore.isAuthor" to="/article/0/edit" class="btn btn-primary">写文章</router-link>
          <router-link to="/notifications" class="icon-btn">通知</router-link>
          <div class="user-menu">
            <UserAvatar :user="userStore.userInfo" size="small" />
            <span>{{ userStore.userInfo?.nickname }}</span>
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="btn btn-outline">登录</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import UserAvatar from './UserAvatar.vue'

const userStore = useUserStore()
</script>

<style scoped>
.header { background: white; border-bottom: 1px solid var(--border); padding: 12px 0; position: sticky; top: 0; z-index: 100; }
.logo { font-size: 20px; font-weight: 700; color: var(--primary); text-decoration: none; }
.nav { display: flex; gap: 16px; }
.nav a { color: var(--text-primary); font-size: 14px; }
.nav a:hover { text-decoration: none; color: var(--primary); }
.icon-btn { padding: 6px 12px; color: var(--text-secondary); font-size: 14px; }
.user-menu { display: flex; align-items: center; gap: 8px; cursor: pointer; }
</style>
