<template>
  <div>
    <h1 class="page-title flex justify-between items-center">
      <span>节点管理 - {{ treeName || '#' + treeId }}</span>
      <div class="flex gap-2">
        <el-button @click="$router.back()">返回</el-button>
        <el-button type="primary" @click="openCreateDialog(null)">新增根节点</el-button>
      </div>
    </h1>

    <el-table
      v-loading="loading"
      :data="flatNodes"
      row-key="id"
      :tree-props="{ children: 'children' }"
      default-expand-all
      stripe
      border
    >
      <el-table-column prop="name" label="名称" min-width="200" />
      <el-table-column label="层级" width="100">
        <template #default="{ row }">depth={{ row.depth }}</template>
      </el-table-column>
      <el-table-column prop="articleCount" label="文章数" width="100" />
      <el-table-column prop="color" label="主色" width="100">
        <template #default="{ row }">
          <span v-if="row.color" :style="{ display: 'inline-block', width: '16px', height: '16px', borderRadius: '50%', background: row.color }"></span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="320">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openCreateDialog(row)">新增子节点</el-button>
          <el-button text type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button text type="primary" size="small" @click="openMoveDialog(row)">移动</el-button>
          <el-button text type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="nodeDialogVisible" :title="nodeForm.id ? '编辑节点' : '新增节点'" width="420px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="nodeForm.name" /></el-form-item>
        <el-form-item label="主色"><el-color-picker v-model="nodeForm.color" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="nodeForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNode">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="moveDialogVisible" title="移动节点" width="420px">
      <el-form label-width="100px">
        <el-form-item label="目标父节点">
          <el-cascader
            v-model="moveForm.newParentId"
            :options="cascadeOptions"
            :props="cascaderProps"
            placeholder="根节点(顶层)"
            clearable
            check-strictly
          />
          <div class="text-secondary text-xs">留空表示移动到根</div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="moveForm.newSortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMove">移动</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { treeAdminApi } from '@/api/tree'
import type { TreeNodeVO, TreeVO } from '@/types'

const route = useRoute()
const treeId = computed(() => Number(route.params.treeId))

const allNodes = ref<TreeNodeVO[]>([])
const treeName = ref('')
const loading = ref(false)

const flatNodes = computed<TreeNodeVO[]>(() => {
  return buildTree(allNodes.value)
})

function buildTree(nodes: TreeNodeVO[]): TreeNodeVO[] {
  const map = new Map<number, TreeNodeVO & { children: TreeNodeVO[] }>()
  nodes.forEach(n => map.set(n.id, { ...n, children: [] }))
  const roots: (TreeNodeVO & { children: TreeNodeVO[] })[] = []
  map.forEach(n => {
    if (n.parentId && n.parentId !== 0) {
      const p = map.get(n.parentId)
      if (p) p.children.push(n)
      else roots.push(n)
    } else {
      roots.push(n)
    }
  })
  return roots
}

const cascaderProps = { value: 'id', label: 'name', emitPath: false, checkStrictly: true }
const cascadeOptions = computed(() => buildTree(allNodes.value))

async function load() {
  loading.value = true
  try {
    const res = await treeAdminApi.listTrees()
    const t: TreeVO | undefined = (res.data || []).find(x => x.id === treeId.value)
    treeName.value = t?.name || ''
    const nodeRes = await treeAdminApi.listNodes(treeId.value)
    allNodes.value = nodeRes.data
  } finally {
    loading.value = false
  }
}

const nodeDialogVisible = ref(false)
const nodeForm = reactive({ id: 0, parentId: 0, name: '', color: '', sortOrder: 0 })

function openCreateDialog(parent: TreeNodeVO | null) {
  Object.assign(nodeForm, {
    id: 0,
    parentId: parent ? parent.id : 0,
    name: '',
    color: '',
    sortOrder: 0
  })
  nodeDialogVisible.value = true
}

function openEditDialog(row: TreeNodeVO) {
  Object.assign(nodeForm, {
    id: row.id,
    parentId: row.parentId,
    name: row.name,
    color: row.color || '',
    sortOrder: row.sortOrder
  })
  nodeDialogVisible.value = true
}

async function saveNode() {
  if (!nodeForm.name) return ElMessage.warning('请填写名称')
  if (nodeForm.id) {
    await treeAdminApi.updateNode({
      id: nodeForm.id,
      name: nodeForm.name,
      color: nodeForm.color,
      sortOrder: nodeForm.sortOrder
    })
  } else {
    await treeAdminApi.createNode({
      treeId: treeId.value,
      parentId: nodeForm.parentId || 0,
      name: nodeForm.name,
      color: nodeForm.color,
      sortOrder: nodeForm.sortOrder
    })
  }
  ElMessage.success('已保存')
  nodeDialogVisible.value = false
  load()
}

const moveDialogVisible = ref(false)
const moveForm = reactive({ id: 0, newParentId: null as number | null, newSortOrder: 0 })

function openMoveDialog(row: TreeNodeVO) {
  moveForm.id = row.id
  moveForm.newParentId = row.parentId && row.parentId !== 0 ? row.parentId : null
  moveForm.newSortOrder = row.sortOrder
  moveDialogVisible.value = true
}

async function confirmMove() {
  await treeAdminApi.moveNode({
    id: moveForm.id,
    newParentId: moveForm.newParentId || 0,
    newSortOrder: moveForm.newSortOrder
  })
  ElMessage.success('已移动')
  moveDialogVisible.value = false
  load()
}

async function del(row: TreeNodeVO) {
  await ElMessageBox.confirm(`确认删除节点「${row.name}」?`, '提示')
  await treeAdminApi.deleteNode(row.id)
  ElMessage.success('已删除')
  load()
}

watch(treeId, () => load())
onMounted(load)
</script>
