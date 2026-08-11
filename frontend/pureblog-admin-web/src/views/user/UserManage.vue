<template>
  <div>
    <h1 class="page-title">用户管理</h1>
    <div class="filter-bar mb-4">
      <el-input v-model="keyword" placeholder="搜索用户名/昵称" style="width:300px" clearable @change="loadData" />
    </div>
    <el-table :data="users" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column prop="status" label="状态" width="80" />
      <el-table-column prop="articleCount" label="文章数" width="80" />
      <el-table-column prop="followerCount" label="粉丝数" width="80" />
      <el-table-column prop="lastLoginAt" label="最后登录" width="160">
        <template #default="{ row }">{{ formatDate(row.lastLoginAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button text type="danger" size="small" @click="disable(row.id)" v-if="row.status === '正常'">
            禁用
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="mt-4"
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import type { AdminUserVO } from '@/types'
import dayjs from 'dayjs'

const users = ref<AdminUserVO[]>([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const keyword = ref('')

async function loadData() {
  const res = await adminApi.getUserList(page.value, size.value, keyword.value)
  users.value = res.data.records
  total.value = res.data.total
}

async function disable(id: number) {
  await adminApi.disableUser(id)
  ElMessage.success('已禁用')
  loadData()
}

function formatDate(date: string) { return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-' }

onMounted(loadData)
</script>
