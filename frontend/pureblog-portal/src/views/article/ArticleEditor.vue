<template>
  <div class="editor-page">
    <div class="editor-header flex justify-between items-center">
      <input v-model="form.title" class="title-input" placeholder="文章标题" />
      <div class="flex gap-2">
        <button class="btn btn-outline" @click="saveDraft">保存草稿</button>
        <button class="btn btn-primary" @click="publish">发布文章</button>
      </div>
    </div>
    <div class="meta-row flex gap-4 mb-4 items-center">
      <select v-model="form.categoryId" class="input">
        <option value="">选择分类</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
      </select>
      <select v-model="form.treeId" class="input" @change="onTreeChange">
        <option value="">选择目录树</option>
        <option v-for="t in trees" :key="t.id" :value="t.id">{{ t.name }}</option>
      </select>
      <select v-model="form.treeNodeId" class="input" :disabled="!form.treeId">
        <option value="">选择叶子节点</option>
        <option v-for="leaf in currentLeaves" :key="leaf.id" :value="leaf.id">{{ leaf.name }}</option>
      </select>
      <input v-model="form.coverUrl" class="input" placeholder="封面图URL" style="flex:1" />
    </div>

    <div class="toolbar flex gap-2 mb-2 items-center">
      <label class="btn btn-outline upload-btn">
        <span>{{ uploading ? '上传中…' : '上传图片' }}</span>
        <input
          type="file"
          accept="image/jpeg,image/png,image/gif,image/webp"
          style="display:none"
          :disabled="uploading"
          @change="onFileChange"
        />
      </label>
      <span class="text-secondary text-xs" v-if="form.coverUrl">封面已设</span>
      <span class="text-secondary text-xs" v-else>未选封面</span>
    </div>

    <div class="editor-body">
      <textarea
        v-model="form.content"
        class="content-textarea"
        placeholder="Markdown 正文（支持粘贴图片自动上传）..."
        @paste="onPaste"
      ></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import { articleApi } from '@/api/article'
import { treeApi } from '@/api/tree'
import { uploadApi } from '@/api/upload'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const categories = ref<any[]>([])
const trees = ref<any[]>([])
const currentNodes = ref<any[]>([])
const currentLeaves = ref<any[]>([])
const uploading = ref(false)

const form = ref({
  title: '',
  summary: '',
  content: '',
  htmlContent: '',
  coverUrl: '',
  categoryId: '' as any,
  treeId: '' as any,
  treeNodeId: '' as any,
  tagIds: [] as number[]
})

// 内容变化时同步生成 htmlContent，后端会原样存到 pb_article_content.html_content
watch(() => form.value.content, (md) => {
  form.value.htmlContent = (marked.parse(md || '') as string) || ''
})

function flattenLeaves(nodes: any[]): any[] {
  const result: any[] = []
  function walk(n: any) {
    if (!n.children || n.children.length === 0) {
      result.push({ id: n.id, name: n.name })
    } else {
      n.children.forEach(walk)
    }
  }
  nodes.forEach(walk)
  return result
}

async function onTreeChange() {
  if (!form.value.treeId) {
    currentNodes.value = []
    currentLeaves.value = []
    form.value.treeNodeId = ''
    return
  }
  const res = await treeApi.getPublicByCode(trees.value.find((t: any) => t.id === form.value.treeId)?.code || '')
  const all = res.data?.root?.children || []
  currentNodes.value = all
  currentLeaves.value = flattenLeaves(all)
  form.value.treeNodeId = ''
}

async function loadData() {
  if (!userStore.isLogin) { router.push('/login'); return }
  const catRes = await articleApi.getCategories()
  categories.value = catRes.data
  const treeRes = await treeApi.listPublic()
  trees.value = (treeRes.data || []).map((t: any) => ({ id: t.id, code: t.code, name: t.name }))

  // 如果是编辑现有文章
  const editId = Number(route.params.id)
  if (editId) {
    const detail = await articleApi.getDetail(editId)
    const a: any = detail.data
    form.value.title = a.title
    form.value.summary = a.summary
    form.value.content = a.content || ''
    form.value.htmlContent = a.htmlContent || ''
    form.value.coverUrl = a.coverUrl
    form.value.categoryId = a.category?.id
    form.value.treeId = a.treeId
    form.value.treeNodeId = a.treeNodeId
    if (a.treeId) await onTreeChange()
  }
}

async function uploadAndInsert(file: File, options?: { asCover?: boolean }) {
  if (!file) return
  if (!file.type.startsWith('image/')) {
    alert('只能上传图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    alert('图片不能超过 5MB')
    return
  }
  uploading.value = true
  try {
    const res = await uploadApi.uploadImage(file)
    const url = res.data.url
    if (options?.asCover) {
      form.value.coverUrl = url
    } else {
      const md = `\n![image](${url})\n`
      form.value.content = (form.value.content || '') + md
    }
  } catch (e: any) {
    alert('上传失败：' + (e?.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

async function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) await uploadAndInsert(file)
  target.value = ''
}

async function onPaste(e: ClipboardEvent) {
  const items = e.clipboardData?.items
  if (!items) return
  for (let i = 0; i < items.length; i++) {
    const it = items[i]
    if (it.kind === 'file' && it.type.startsWith('image/')) {
      const file = it.getAsFile()
      if (file) {
        e.preventDefault()
        await uploadAndInsert(file)
        return
      }
    }
  }
}

async function saveDraft() {
  if (!form.value.title || !form.value.treeNodeId || !form.value.categoryId) {
    alert('请填写标题、分类和目录树叶子节点')
    return
  }
  const res = await articleApi.create({ ...form.value })
  router.push(`/article/${res.data.id}`)
}

async function publish() {
  if (!form.value.title || !form.value.content) { alert('请填写标题和正文'); return }
  if (!form.value.treeNodeId) { alert('请选择目录树叶子节点'); return }
  if (!form.value.categoryId) { alert('请选择分类'); return }
  const res = await articleApi.create({ ...form.value })
  await articleApi.publish({ id: res.data.id })
  router.push(`/article/${res.data.id}`)
}

onMounted(loadData)
</script>

<style scoped>
.editor-page { max-width: 1000px; margin: 0 auto; }
.editor-header { margin-bottom: 16px; }
.title-input { font-size: 24px; font-weight: 600; border: none; outline: none; flex: 1; margin-right: 16px; background: transparent; }
.meta-row .input { max-width: 200px; }
.editor-body { background: white; border-radius: var(--radius); min-height: 500px; }
.content-textarea { width: 100%; min-height: 500px; border: 1px solid var(--border); border-radius: var(--radius); padding: 16px; font-size: 15px; font-family: monospace; resize: vertical; outline: none; }
.upload-btn { cursor: pointer; }
.upload-btn input[type=file] { display: none; }
</style>
