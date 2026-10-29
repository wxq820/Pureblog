<template>
  <header class="header">
    <div class="container flex items-center justify-between">
      <div class="flex items-center gap-4">
        <router-link to="/" class="logo">PureBlog</router-link>
        <el-tabs
          v-if="userStore.isLogin"
          v-model="activeTab"
          class="admin-tabs"
          @tab-click="onTabClick"
        >
          <el-tab-pane label="仪表盘" name="/admin" />
          <el-tab-pane label="文章管理" name="/admin/article/list" />
          <el-tab-pane label="分类管理" name="/admin/category/manage" />
          <el-tab-pane label="标签管理" name="/admin/tag/manage" />
          <el-tab-pane label="目录树" name="/admin/tree/manage" />
          <el-tab-pane label="小工具管理" name="/admin/widget/manage" />
        </el-tabs>
      </div>
      <div class="flex items-center gap-2">
        <template v-if="userStore.isLogin">
          <router-link v-if="userStore.isAuthor" to="/article/0/edit" class="btn btn-primary">写文章</router-link>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-menu">
              <UserAvatar :user="userStore.userInfo" size="small" />
              <span>{{ userStore.userInfo?.nickname }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="btn btn-primary">登录</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import UserAvatar from './UserAvatar.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 公开页（/、/article/:id、/search 等）下，路由不在 /admin/* 时 activeTab 必须为 ''，
// 否则 el-tabs 会自动选中 name='/admin' 的「仪表盘」tab，触发误高亮。
const activeTab = ref(route.path.startsWith('/admin') ? route.path : '')

watch(() => route.path, (path) => {
  if (path.startsWith('/admin')) activeTab.value = path
})

function onTabClick(tab: any) {
  router.push(tab.props.name)
}

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/')
  } else if (command === 'profile') {
    router.push(`/user/${userStore.userInfo?.userId}`)
  }
}
</script>

<style scoped>
.header { background: white; border-bottom: 1px solid var(--border); padding: 12px 0; position: fixed; top: 0; left: 0; right: 0; z-index: 100; }
.logo { font-size: 20px; font-weight: 700; color: var(--primary); text-decoration: none; flex-shrink: 0; }
.user-menu { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 8px; border-radius: 6px; transition: background 0.15s; }
.user-menu:hover { background: var(--bg-page); }

.admin-tabs {
  margin-bottom: 0;
}
.admin-tabs :deep(.el-tabs__header) {
  margin: 0;
}
.admin-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}
.admin-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  height: 48px;
  line-height: 48px;
  padding: 0 14px;
}
</style>
