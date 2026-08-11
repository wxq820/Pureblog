<template>
  <div class="editor-page">
    <div class="editor-header flex justify-between items-center">
      <input v-model="form.title" class="title-input" placeholder="文章标题" />
      <div class="flex gap-2">
        <button class="btn btn-outline" @click="saveDraft">保存草稿</button>
        <button class="btn btn-primary" @click="publish">发布文章</button>
      </div>
    </div>
    <div class="meta-row flex gap-4 mb-4">
      <select v-model="form.categoryId" class="input">
        <option value="">选择分类</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
      </select>
      <input v-model="form.coverUrl" class="input" placeholder="封面图URL" style="flex:1" />
    </div>
    <div class="editor-body">
      <textarea v-model="form.content" class="content-textarea" placeholder="Markdown正文..."></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const categories = ref<any[]>([])
const form = ref({ title: '', summary: '', content: '', htmlContent: '', coverUrl: '', categoryId: '' as any, tagIds: [] as number[] })

async function loadData() {
  if (!userStore.isLogin) { router.push('/login'); return }
  const res = await articleApi.getCategories()
  categories.value = res.data
}

async function saveDraft() {
  const res = await articleApi.create({ ...form.value })
  router.push(`/article/${res.data.id}`)
}

async function publish() {
  if (!form.value.title || !form.value.content) { alert('请填写标题和正文'); return }
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
