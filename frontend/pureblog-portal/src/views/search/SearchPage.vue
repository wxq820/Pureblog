<template>
  <div class="search-page">
    <div class="search-header">
      <h2>搜索结果</h2>
      <div class="search-box flex gap-2 mb-4">
        <input v-model="keyword" class="input" placeholder="输入关键词..." @keyup.enter="doSearch" />
        <button class="btn btn-primary" @click="doSearch">搜索</button>
      </div>
      <div v-if="result" class="text-secondary text-sm mb-4">
        找到 {{ result.total }} 个结果，耗时 {{ result.tookMs }}ms
      </div>
    </div>
    <div class="search-results">
      <div v-for="item in result?.articles" :key="item.articleId" class="result-item card mb-4" @click="$router.push(`/article/${item.articleId}`)">
        <div v-if="item.highlights?.length" class="highlights text-sm text-secondary mb-2">
          <span v-for="h in item.highlights" v-html="h"></span>
        </div>
        <h3 class="title" v-html="item.title"></h3>
        <p class="summary text-secondary text-sm">{{ item.summary }}</p>
        <div class="meta flex items-center gap-4 text-xs text-secondary">
          <span>{{ item.authorName }}</span>
          <span>{{ item.categoryName }}</span>
          <span>阅读 {{ item.viewCount }}</span>
          <span>点赞 {{ item.likeCount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { searchApi } from '@/api/search'
import type { SearchResult } from '@/types'

const route = useRoute()
const keyword = ref((route.query.keyword as string) || '')
const result = ref<SearchResult | null>(null)

async function doSearch() {
  if (!keyword.value.trim()) return
  const res = await searchApi.search({ keyword: keyword.value })
  result.value = res.data
}

watch(() => route.query.keyword, (kw) => {
  if (kw) { keyword.value = kw as string; doSearch() }
})

onMounted(() => { if (keyword.value) doSearch() })
</script>

<style scoped>
.search-page { max-width: 800px; margin: 0 auto; }
.result-item { cursor: pointer; }
.result-item:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.title { font-size: 18px; font-weight: 600; margin-bottom: 8px; }
.summary { margin-bottom: 8px; }
.highlights { background: #fef9c3; padding: 4px 8px; border-radius: 4px; }
.highlights :deep(em) { color: #dc2626; font-style: normal; font-weight: 600; }
</style>
