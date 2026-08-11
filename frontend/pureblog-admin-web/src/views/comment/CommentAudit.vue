<template>
  <div>
    <h1 class="page-title">评论审核</h1>
    <el-table :data="comments" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="articleTitle" label="文章" min-width="150" show-overflow-tooltip />
      <el-table-column prop="nickname" label="用户" width="120" />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" width="160">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button text type="success" size="small" @click="audit(row.id, true)">通过</el-button>
          <el-button text type="danger" size="small" @click="audit(row.id, false)">拒绝</el-button>
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
import type { AdminCommentVO } from '@/types'
import dayjs from 'dayjs'

const comments = ref<AdminCommentVO[]>([])
const page = ref(1)
const size = ref(20)
const total = ref(0)

async function loadData() {
  const res = await adminApi.getPendingComments(page.value, size.value)
  comments.value = res.data.records
  total.value = res.data.total
}

async function audit(id: number, approve: boolean) {
  await adminApi.auditComment(id, approve)
  ElMessage.success(approve ? '已通过' : '已拒绝')
  loadData()
}

function formatDate(date: string) { return dayjs(date).format('YYYY-MM-DD HH:mm') }

onMounted(loadData)
</script>
