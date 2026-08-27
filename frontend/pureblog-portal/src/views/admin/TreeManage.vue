<template>
  <div>
    <h1 class="page-title">目录树管理</h1>
    <div class="mb-4">
      <el-button type="primary" size="small" @click="showTreeDialog()">新增目录树</el-button>
    </div>
    <el-table :data="trees" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" width="200" />
      <el-table-column prop="code" label="Code" width="160" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button text type="primary" size="small" @click="showTreeDialog(row)">编辑</el-button>
          <el-button text type="primary" size="small" @click="goNodes(row.id)">节点管理</el-button>
          <el-button text type="danger" size="small" @click="removeTree(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="treeDialogVisible" :title="treeForm.id ? '编辑目录树' : '新增目录树'">
      <el-form :model="treeForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="treeForm.name" /></el-form-item>
        <el-form-item label="Code"><el-input v-model="treeForm.code" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="treeForm.description" /></el-form-item>
        <el-form-item label="封面色"><el-color-picker v-model="treeForm.coverColor" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="treeForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="treeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTree" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { treeAdminApi } from '@/api/tree-admin'
import type { TreeVO } from '@/types'

const router = useRouter()
const trees = ref<TreeVO[]>([])
const loading = ref(false)
const treeDialogVisible = ref(false)
const saving = ref(false)

const treeForm = ref({ id: 0, name: '', code: '', description: '', coverColor: '#2563eb', sortOrder: 0 })

async function loadData() {
  loading.value = true
  try {
    const res = await treeAdminApi.listTrees()
    trees.value = res.data
  } finally { loading.value = false }
}

function showTreeDialog(row?: TreeVO) {
  treeForm.value = row
    ? { ...row }
    : { id: 0, name: '', code: '', description: '', coverColor: '#2563eb', sortOrder: 0 }
  treeDialogVisible.value = true
}

async function saveTree() {
  saving.value = true
  try {
    if (treeForm.value.id) {
      await treeAdminApi.updateTree(treeForm.value as any)
    } else {
      await treeAdminApi.createTree(treeForm.value as any)
    }
    ElMessage.success('保存成功')
    treeDialogVisible.value = false
    loadData()
  } finally { saving.value = false }
}

async function toggleStatus(row: TreeVO) {
  await treeAdminApi.updateTreeStatus(row.id, row.status === 1 ? 0 : 1)
  loadData()
}

async function removeTree(id: number) {
  await ElMessageBox.confirm('确认删除?', '提示')
  await treeAdminApi.deleteTree(id)
  ElMessage.success('已删除')
  loadData()
}

function goNodes(treeId: number) {
  router.push('/admin/tree/node/' + treeId)
}

onMounted(loadData)
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 20px; color: var(--text-primary); }
.mb-4 { margin-bottom: 16px; }
</style>
