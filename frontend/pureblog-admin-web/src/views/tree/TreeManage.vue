<template>
  <div>
    <h1 class="page-title">目录树管理</h1>

    <div class="mb-4 flex gap-2">
      <el-button type="primary" @click="openCreateDialog()">新增目录树</el-button>
    </div>

    <el-table :data="trees" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="编码" width="140" />
      <el-table-column prop="name" label="名称" width="180" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="节点数" width="100">
        <template #default="{ row }">{{ (row.nodes || []).length }}</template>
      </el-table-column>
      <el-table-column label="操作" width="320">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="goToNodes(row.id)">节点编辑</el-button>
          <el-button text type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button text :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button text type="danger" size="small" @click="deleteTree(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑目录树' : '新增目录树'" width="480px">
      <el-form label-width="80px">
        <el-form-item label="编码" v-if="!form.id">
          <el-input v-model="form.code" placeholder="小写字母/数字/下划线,如 java" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="如 Java 技术栈" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="主色">
          <el-color-picker v-model="form.coverColor" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { treeAdminApi } from '@/api/tree'
import type { TreeVO } from '@/types'

const router = useRouter()
const trees = ref<TreeVO[]>([])
const dialogVisible = ref(false)
const form = reactive({ id: 0, code: '', name: '', description: '', coverColor: '#2563eb' })

async function load() {
  const res = await treeAdminApi.listTrees()
  trees.value = res.data
}

function openCreateDialog() {
  Object.assign(form, { id: 0, code: '', name: '', description: '', coverColor: '#2563eb' })
  dialogVisible.value = true
}

function openEditDialog(row: TreeVO) {
  Object.assign(form, {
    id: row.id,
    code: row.code,
    name: row.name,
    description: row.description || '',
    coverColor: row.coverColor || '#2563eb'
  })
  dialogVisible.value = true
}

async function save() {
  if (!form.name) return ElMessage.warning('请填写名称')
  if (form.id) {
    await treeAdminApi.updateTree({
      id: form.id,
      name: form.name,
      description: form.description,
      coverColor: form.coverColor
    })
    ElMessage.success('已保存')
  } else {
    if (!form.code) return ElMessage.warning('请填写编码')
    await treeAdminApi.createTree({
      code: form.code,
      name: form.name,
      description: form.description,
      coverColor: form.coverColor
    })
    ElMessage.success('已创建')
  }
  dialogVisible.value = false
  load()
}

async function toggleStatus(row: TreeVO) {
  const next = row.status === 1 ? 0 : 1
  await treeAdminApi.updateTreeStatus(row.id, next)
  ElMessage.success(next === 1 ? '已启用' : '已禁用')
  load()
}

async function deleteTree(id: number) {
  await ElMessageBox.confirm('删除目录树会移除其所有节点,确认?', '提示')
  await treeAdminApi.deleteTree(id)
  ElMessage.success('已删除')
  load()
}

function goToNodes(treeId: number) {
  router.push({ name: 'tree-node-manage', params: { treeId: String(treeId) } })
}

onMounted(load)
</script>
