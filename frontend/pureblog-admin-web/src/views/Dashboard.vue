<template>
  <div>
    <h1 class="page-title">仪表盘</h1>
    <div class="stat-cards">
      <div class="stat-card">
        <div class="value">{{ stats.totalArticles }}</div>
        <div class="label">文章总数</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ stats.totalUsers }}</div>
        <div class="label">用户总数</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ stats.totalComments }}</div>
        <div class="label">评论总数</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ stats.totalViews }}</div>
        <div class="label">总浏览量</div>
      </div>
    </div>
    <div class="stat-cards">
      <div class="stat-card">
        <div class="value">{{ stats.todayArticles }}</div>
        <div class="label">今日新增文章</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ stats.todayComments }}</div>
        <div class="label">今日新增评论</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ stats.pendingComments }}</div>
        <div class="label">待审核评论</div>
      </div>
      <div class="stat-card">
        <div class="value">{{ stats.todayViews }}</div>
        <div class="label">今日浏览量</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import type { DashboardVO } from '@/types'

const stats = ref<DashboardVO>({
  totalArticles: 0, totalUsers: 0, totalComments: 0, totalViews: 0,
  todayViews: 0, todayArticles: 0, todayComments: 0, pendingComments: 0
})

onMounted(async () => {
  const res = await adminApi.getDashboard()
  stats.value = res.data
})
</script>
