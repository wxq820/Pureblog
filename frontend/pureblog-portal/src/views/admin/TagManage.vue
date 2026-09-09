<template>
  <div>
    <h1 class="page-title">标签管理</h1>
    <div class="mb-4">
      <el-button type="primary" size="small" @click="showDialog()">新增标签</el-button>
    </div>
    <el-table :data="tags" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="名称" min-width="200" align="center" />
      <el-table-column prop="slug" label="别名" min-width="200" align="center" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="showDialog(row)">编辑</el-button>
          <el-button text type="danger" size="small" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑标签' : '新增标签'">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="别名"><el-input v-model="form.slug" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api'

const tags = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)

const form = ref({ id: 0, name: '', slug: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await request.get<any, any>('/tag/list')
    tags.value = res.data
  } finally { loading.value = false }
}

function showDialog(row?: any) {
  form.value = row ? { ...row } : { id: 0, name: '', slug: '' }
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (form.value.id) {
      await request.put<any, any>('/admin/tag/update', form.value)
    } else {
      await request.post<any, any>('/admin/tag/create', form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally { saving.value = false }
}

async function remove(id: number) {
  try {
    await ElMessageBox.confirm('确认删除?', '提示')
    await request.delete<any, any>(`/admin/tag/${id}`)
    ElMessage.success('已删除')
    loadData()
  } catch (err: any) {
    if (err === 'cancel' || err === 'close') return
    ElMessage.error(err?.message || '删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 20px; color: var(--text-primary); }
.mb-4 { margin-bottom: 16px; }
</style>
