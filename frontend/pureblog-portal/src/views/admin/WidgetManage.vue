<template>
  <div>
    <h1 class="page-title">小工具管理</h1>
    <div class="mb-4">
      <el-button type="primary" size="small" @click="showDialog()">新增小工具</el-button>
    </div>
    <el-table :data="widgets" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="图标" width="80" align="center">
        <template #default="{ row }">
          <div class="icon-cell" :style="{ backgroundColor: row.iconColor }">
            <el-icon><component :is="resolveWidgetIcon(row.icon, row.code)" /></el-icon>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" width="140" align="center" />
      <el-table-column prop="code" label="Code" width="140" align="center" />
      <el-table-column prop="path" label="路径" width="200" align="center" />
      <el-table-column prop="clickCount" label="点击数" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.clickCount }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button text type="primary" size="small" @click="showDialog(row)">编辑</el-button>
          <el-button text type="danger" size="small" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑小工具' : '新增小工具'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" maxlength="32" />
        </el-form-item>
        <el-form-item label="Code">
          <el-input v-model="form.code" :disabled="!!form.id" placeholder="小写字母数字下划线" maxlength="32" />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="form.path" placeholder="/widget/xxx" maxlength="128" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" maxlength="255" />
        </el-form-item>
        <el-form-item label="图标">
          <el-select v-model="form.icon" placeholder="选择一个图标" clearable style="width: 100%">
            <template #prefix>
              <el-icon v-if="form.icon"><component :is="resolveWidgetIcon(form.icon, form.code)" /></el-icon>
            </template>
            <el-option
              v-for="opt in WIDGET_ICON_OPTIONS"
              :key="opt.key"
              :label="opt.label"
              :value="opt.key"
            >
              <span class="icon-option">
                <el-icon><component :is="opt.comp" /></el-icon>
                <span>{{ opt.label }}</span>
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="图标色">
          <el-color-picker v-model="form.iconColor" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { widgetAdminApi } from '@/api/widget'
import { WIDGET_ICON_OPTIONS, resolveWidgetIcon } from '@/constants/widgetIcons'
import type { WidgetVO } from '@/types'

const widgets = ref<WidgetVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)

interface Form {
  id: number
  code: string
  name: string
  description: string
  icon: string
  iconColor: string
  path: string
  sortOrder: number
  status: number
}

const form = ref<Form>({
  id: 0, code: '', name: '', description: '',
  icon: '', iconColor: '#2563eb', path: '', sortOrder: 0, status: 1
})

async function loadData() {
  loading.value = true
  try {
    const res = await widgetAdminApi.listAll()
    widgets.value = res.data
  } finally {
    loading.value = false
  }
}

function showDialog(row?: WidgetVO) {
  form.value = row
    ? {
        id: row.id, code: row.code, name: row.name, description: row.description || '',
        icon: row.icon || '', iconColor: row.iconColor || '#2563eb', path: row.path,
        sortOrder: row.sortOrder, status: row.status
      }
    : { id: 0, code: '', name: '', description: '', icon: '', iconColor: '#2563eb', path: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.name.trim() || !form.value.path.trim()) {
    ElMessage.error('名称和路径不能为空')
    return
  }
  saving.value = true
  try {
    if (form.value.id) {
      await widgetAdminApi.update({
        id: form.value.id,
        name: form.value.name,
        description: form.value.description,
        icon: form.value.icon,
        iconColor: form.value.iconColor,
        path: form.value.path,
        sortOrder: form.value.sortOrder,
        status: form.value.status
      })
    } else {
      if (!form.value.code.trim()) {
        ElMessage.error('Code 不能为空')
        return
      }
      await widgetAdminApi.create({
        code: form.value.code,
        name: form.value.name,
        description: form.value.description,
        icon: form.value.icon,
        iconColor: form.value.iconColor,
        path: form.value.path,
        sortOrder: form.value.sortOrder,
        status: form.value.status
      })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: WidgetVO) {
  try {
    await widgetAdminApi.updateStatus(row.id, row.status === 1 ? 0 : 1)
    loadData()
  } catch (err: any) {
    ElMessage.error(err?.message || '状态切换失败')
  }
}

async function remove(row: WidgetVO) {
  try {
    await ElMessageBox.confirm(`确认删除 "${row.name}"?`, '提示', { type: 'warning' })
    await widgetAdminApi.remove(row.id)
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
.icon-cell {
  width: 40px; height: 40px; border-radius: 8px; margin: 0 auto;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; color: #fff;
}
.icon-cell :deep(svg) { width: 1em; height: 1em; }
.icon-option { display: flex; align-items: center; gap: 8px; }
</style>
