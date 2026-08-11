<template>
  <div>
    <h1 class="page-title">标签管理</h1>
    <div class="mb-4">
      <el-button type="primary" @click="dialogVisible = true">新增标签</el-button>
    </div>
    <el-table :data="tags" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="slug" label="别名" width="150" />
      <el-table-column prop="articleCount" label="文章数" width="100" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button text type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增标签" width="400px">
      <el-form>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="别名">
          <el-input v-model="form.slug" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api'

const tags = ref<any[]>([])
const dialogVisible = ref(false)
const form = ref({ name: '', slug: '' })

async function loadData() {
  const res = await request.get<any, any>('/tag/list')
  tags.value = res.data
}

async function handleCreate() {
  await request.post<any, any>('/admin/tag/create', form.value)
  ElMessage.success('创建成功')
  dialogVisible.value = false
  form.value = { name: '', slug: '' }
  loadData()
}

async function handleDelete(id: number) {
  await request.delete<any, any>(`/admin/tag/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
