<template>
  <div>
    <h1 class="page-title">文章管理</h1>
    <div class="mb-4 flex gap-2 items-center">
      <el-input v-model="keyword" placeholder="搜索文章标题" style="width:300px" clearable @change="loadData" />
    </div>
    <el-table :data="articles" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip align="center" />
      <el-table-column prop="authorName" label="作者" width="120" align="center" />
      <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
      <el-table-column prop="likeCount" label="点赞" width="80" align="center" />
      <el-table-column prop="commentCount" label="评论" width="80" align="center" />
      <el-table-column prop="publishedAt" label="发布时间" width="160" align="center">
        <template #default="{ row }">{{ formatDate(row.publishedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="edit(row.id)">编辑</el-button>
          <el-button text type="warning" size="small" @click="offline(row.id)">下架</el-button>
          <el-button text type="danger" size="small" @click="deleteArticle(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="mt-4" background
      v-model:current-page="page" v-model:page-size="size"
      :total="total" :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData" @current-change="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { articleAdminApi } from '@/api/article-admin'
import type { ArticleListVO } from '@/types'
import dayjs from 'dayjs'

const router = useRouter()
const articles = ref<ArticleListVO[]>([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const keyword = ref('')
const loading = ref(false)

function edit(id: number) {
  router.push({ path: '/article/' + id + '/edit' })
}

async function loadData() {
  loading.value = true
  try {
    const res = await articleAdminApi.getList({ page: page.value, size: size.value, keyword: keyword.value })
    articles.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function offline(id: number) {
  await articleAdminApi.offline(id)
  ElMessage.success('下架成功')
  loadData()
}

async function deleteArticle(id: number) {
  await ElMessageBox.confirm('确认删除这篇文章?', '提示')
  await articleAdminApi.delete(id)
  ElMessage.success('删除成功')
  loadData()
}

function formatDate(date: string) { return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-' }
onMounted(loadData)
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 20px; color: var(--text-primary); }
.mb-4 { margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
</style>
