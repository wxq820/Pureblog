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
    <div class="editor-body">
      <textarea v-model="form.content" class="content-textarea" placeholder="Markdown正文..."></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { treeApi } from '@/api/tree'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const categories = ref<any[]>([])
const trees = ref<any[]>([])
const currentNodes = ref<any[]>([])
const currentLeaves = ref<any[]>([])

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
  // 后端返回完整 TreeVO,这里取 root.children 即可
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
}

async function saveDraft() {
  if (!form.value.treeNodeId || !form.value.treeNodeId) { alert('请填写完整字段'); return }
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
</style>
