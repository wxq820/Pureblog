<template>
  <div>
    <h1 class="page-title">仪表盘</h1>
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6"><el-statistic title="文章总数" :value="stats.totalArticles" /></el-col>
      <el-col :span="6"><el-statistic title="用户总数" :value="stats.totalUsers" /></el-col>
      <el-col :span="6"><el-statistic title="评论总数" :value="stats.totalComments" /></el-col>
      <el-col :span="6"><el-statistic title="总浏览量" :value="stats.totalViews" /></el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="6"><el-statistic title="今日新增文章" :value="stats.todayArticles" /></el-col>
      <el-col :span="6"><el-statistic title="今日新增评论" :value="stats.todayComments" /></el-col>
      <el-col :span="6"><el-statistic title="待审核评论" :value="stats.pendingComments" /></el-col>
      <el-col :span="6"><el-statistic title="今日浏览量" :value="stats.todayViews" /></el-col>
    </el-row>
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
  try {
    const res = await adminApi.getDashboard()
    stats.value = res.data
  } catch {}
})
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 20px; color: var(--text-primary); }
.mb-4 { margin-bottom: 16px; }
</style>
