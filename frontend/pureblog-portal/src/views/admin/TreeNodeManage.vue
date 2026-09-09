<template>
  <div>
    <div class="flex items-center gap-2 mb-4">
      <el-button size="small" @click="$router.push('/admin/tree/manage')">← 返回目录树</el-button>
      <h1 class="page-title" style="margin:0">节点管理 — {{ treeName }}</h1>
    </div>

    <div class="mb-4">
      <el-button type="primary" size="small" @click="showNodeDialog()">新增节点</el-button>
    </div>

    <el-table :data="nodes" stripe v-loading="loading" row-key="id" default-expand-all style="width: 100%">
      <el-table-column prop="name" label="名称" min-width="200" align="center" />
      <el-table-column prop="color" label="颜色" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.color" :style="{ color: row.color, fontWeight: 600 }">■</span>
        </template>
      </el-table-column>
      <el-table-column prop="depth" label="层级" width="80" align="center" />
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column prop="articleCount" label="文章数" width="100" align="center" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="showNodeDialog(row)">编辑</el-button>
          <el-button text type="primary" size="small" @click="showNodeDialog({ parentId: row.id })">添加子节点</el-button>
          <el-button text type="danger" size="small" @click="removeNode(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="nodeDialogVisible" :title="nodeForm.id ? '编辑节点' : '新增节点'">
      <el-form :model="nodeForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="nodeForm.name" /></el-form-item>
        <el-form-item label="颜色"><el-color-picker v-model="nodeForm.color" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="nodeForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNode" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { treeAdminApi } from '@/api/tree-admin'
import type { TreeNodeVO } from '@/types'

const route = useRoute()
const treeId = Number(route.params.treeId)
const treeName = ref('')
const nodes = ref<TreeNodeVO[]>([])
const loading = ref(false)
const nodeDialogVisible = ref(false)
const saving = ref(false)

const nodeForm = ref({ id: 0, parentId: null as number | null, name: '', color: '', sortOrder: 0 })

async function loadData() {
  loading.value = true
  try {
    const res = await treeAdminApi.listNodes(treeId)
    nodes.value = res.data
    const treesRes = await treeAdminApi.listTrees()
    const tree = treesRes.data.find((t: any) => t.id === treeId)
    treeName.value = tree?.name || ''
  } finally { loading.value = false }
}

function showNodeDialog(row?: Partial<TreeNodeVO>) {
  nodeForm.value = row?.id
    ? { id: row.id, parentId: row.parentId ?? null, name: row.name || '', color: row.color || '', sortOrder: row.sortOrder ?? 0 }
    : { id: 0, parentId: (row?.parentId ?? null) as number | null, name: '', color: '', sortOrder: 0 }
  nodeDialogVisible.value = true
}

async function saveNode() {
  saving.value = true
  try {
    if (nodeForm.value.id) {
      await treeAdminApi.updateNode({ id: nodeForm.value.id, name: nodeForm.value.name, color: nodeForm.value.color, sortOrder: nodeForm.value.sortOrder })
    } else {
      await treeAdminApi.createNode({ treeId, parentId: nodeForm.value.parentId || 0, name: nodeForm.value.name, color: nodeForm.value.color, sortOrder: nodeForm.value.sortOrder })
    }
    ElMessage.success('保存成功')
    nodeDialogVisible.value = false
    loadData()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally { saving.value = false }
}

async function removeNode(id: number) {
  try {
    await ElMessageBox.confirm('确认删除?', '提示')
    await treeAdminApi.deleteNode(id)
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
.page-title { font-size: 20px; font-weight: 600; color: var(--text-primary); }
.mb-4 { margin-bottom: 16px; }
</style>
