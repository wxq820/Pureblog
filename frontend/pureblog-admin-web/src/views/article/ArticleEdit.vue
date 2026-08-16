<template>
  <div>
    <h1 class="page-title flex justify-between items-center">
      <span>{{ isEdit ? '编辑文章' : '写文章' }}</span>
      <div class="flex gap-2">
        <el-button @click="$router.back()">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存并发布</el-button>
      </div>
    </h1>

    <el-form label-width="100px" v-loading="loading">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="文章标题" maxlength="255" show-word-limit />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="一句话简介" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="封面 URL">
        <el-input v-model="form.coverUrl" placeholder="可选" />
      </el-form-item>

      <el-form-item label="目录树">
        <el-select v-model="form.treeId" placeholder="选择目录树" style="width:260px" @change="onTreeChange">
          <el-option v-for="t in trees" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <el-cascader
          v-model="form.treeNodeId"
          :options="cascadeNodes"
          :props="{ value: 'id', label: 'name', emitPath: false, checkStrictly: true }"
          :disabled="!form.treeId"
          placeholder="选择叶子节点挂载"
          style="width: 360px; margin-left: 8px"
          clearable
        />
        <div class="text-secondary text-xs mt-1">挂载点必须是目录树的叶子节点 (无子节点的节点)</div>
      </el-form-item>

      <el-form-item label="分类" required>
        <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 360px">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="标签">
        <el-select v-model="form.tagIds" multiple filterable placeholder="选择标签 (可多选)" style="width: 360px">
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="正文" required>
        <el-input v-model="form.content" type="textarea" :rows="18" placeholder="Markdown 正文" />
      </el-form-item>

      <el-form-item label="HTML 渲染">
        <el-input v-model="form.htmlContent" type="textarea" :rows="6" placeholder="可选, 渲染后的 HTML" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { treeAdminApi, adminArticleApi } from '@/api/tree'
import request from '@/api'
import type { TreeNodeVO, TreeVO } from '@/types'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.query.id)

const loading = ref(false)
const saving = ref(false)

const trees = ref<TreeVO[]>([])
const currentNodes = ref<TreeNodeVO[]>([])
const categories = ref<any[]>([])
const tags = ref<any[]>([])

const form = reactive({
  id: 0,
  title: '',
  summary: '',
  coverUrl: '',
  treeId: null as number | null,
  treeNodeId: null as number | null,
  categoryId: null as number | null,
  tagIds: [] as number[],
  content: '',
  htmlContent: ''
})

const cascadeNodes = computed(() => currentNodes.value || [])

async function onTreeChange(treeId: number) {
  form.treeNodeId = null
  const res = await treeAdminApi.listNodes(treeId)
  currentNodes.value = res.data
}

async function load() {
  loading.value = true
  try {
    const treeRes = await treeAdminApi.listTrees()
    trees.value = treeRes.data
    if (trees.value.length > 0 && !form.treeId) {
      form.treeId = trees.value[0].id
      await onTreeChange(form.treeId)
    }
    const catRes = await request.get<any, any>('/category/list')
    categories.value = catRes.data
    const tagRes = await request.get<any, any>('/tag/list')
    tags.value = tagRes.data

    if (isEdit.value) {
      const id = Number(route.query.id)
      try {
        const r = await request.get<any, any>(`/article/public/${id}`)
        const d = r.data
        form.id = d.id
        form.title = d.title
        form.summary = d.summary
        form.coverUrl = d.coverUrl
        form.categoryId = d.category?.id || null
        form.tagIds = (d.tags || []).map((t: any) => t.id)
        form.content = d.content
        form.htmlContent = d.htmlContent
      } catch {
        ElMessage.warning('无法加载该文章')
      }
    }
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!form.title) return ElMessage.warning('请填写标题')
  if (!form.content) return ElMessage.warning('请填写正文')
  if (!form.treeNodeId) return ElMessage.warning('请选择目录树叶子节点')
  if (!form.categoryId) return ElMessage.warning('请选择分类')

  saving.value = true
  try {
    if (isEdit.value && form.id) {
      await adminArticleApi.updateArticle({
        id: form.id,
        title: form.title,
        summary: form.summary,
        coverUrl: form.coverUrl,
        treeNodeId: form.treeNodeId,
        categoryId: form.categoryId,
        tagIds: form.tagIds,
        content: form.content,
        htmlContent: form.htmlContent
      })
      ElMessage.success('已更新')
    } else {
      await adminArticleApi.createArticle({
        title: form.title,
        summary: form.summary,
        coverUrl: form.coverUrl,
        treeNodeId: form.treeNodeId,
        categoryId: form.categoryId,
        tagIds: form.tagIds,
        content: form.content,
        htmlContent: form.htmlContent
      })
      ElMessage.success('已创建并发布')
    }
    router.push({ name: 'article-list' })
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
