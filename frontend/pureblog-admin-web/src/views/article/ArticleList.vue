<template>
  <div>
    <h1 class="page-title">文章管理</h1>
    <div class="filter-bar mb-4">
      <el-input v-model="keyword" placeholder="搜索文章标题" style="width:300px" clearable @change="loadData" />
    </div>
    <el-table :data="articles" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="authorName" label="作者" width="120" />
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="likeCount" label="点赞" width="80" />
      <el-table-column prop="commentCount" label="评论" width="80" />
      <el-table-column prop="publishedAt" label="发布时间" width="160">
        <template #default="{ row }">{{ formatDate(row.publishedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="edit(row.id)">编辑</el-button>
          <el-button text type="primary" size="small" @click="offline(row.id)">下架</el-button>
          <el-button text type="danger" size="small" @click="deleteArticle(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="mt-4"
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { articleAdminApi } from '@/api/article'
import type { ArticleListVO } from '@/types'
import dayjs from 'dayjs'

const router = useRouter()
const articles = ref<ArticleListVO[]>([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const keyword = ref('')

function edit(id: number) {
  router.push({ name: 'article-edit', query: { id } })
}

async function loadData() {
  const res = await articleAdminApi.getList({ page: page.value, size: size.value, keyword: keyword.value })
  articles.value = res.data.records
  total.value = res.data.total
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

function formatDate(date: string) { return dayjs(date).format('YYYY-MM-DD HH:mm') }

onMounted(loadData)
</script>
