<template>
  <div class="home-page">
    <section class="hero">
      <h1>欢迎来到 PureBlog</h1>
      <p class="text-secondary">纯粹的技术博客，分享知识与经验</p>
      <div class="search-box">
        <input v-model="searchKeyword" class="input" placeholder="搜索文章..." @keyup.enter="doSearch" />
        <button class="btn btn-primary" @click="doSearch">搜索</button>
      </div>
    </section>

    <SkillTree :tree="currentTree" class="skill-tree-section" />

    <div class="content-grid">
      <aside class="left-sidebar">
        <div class="card">
          <h3 class="section-title">热门文章</h3>
          <div class="hot-list">
            <div v-for="(article, i) in hotArticles" :key="article.id" class="hot-item" @click="$router.push(`/article/${article.id}`)">
              <span class="rank">{{ i + 1 }}</span>
              <span class="title">{{ article.title }}</span>
            </div>
          </div>
        </div>

        <div class="card mt-4">
          <h3 class="section-title">分类</h3>
          <div class="category-list">
            <router-link v-for="cat in categories" :key="cat.id" :to="`/?category=${cat.id}`" class="category-item">
              <span>{{ cat.name }}</span>
              <span class="text-secondary text-xs">{{ cat.articleCount }}</span>
            </router-link>
          </div>
        </div>
      </aside>

      <main>
        <h2 class="section-title">最新文章</h2>
        <div class="article-list">
          <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
          <div v-if="loading" class="loading text-secondary">加载中...</div>
          <div v-if="!loading && articles.length === 0" class="empty text-secondary">暂无文章</div>
        </div>
        <button v-if="hasMore" class="btn btn-outline load-more" @click="loadMore">加载更多</button>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import SkillTree from '@/components/SkillTree.vue'
import { JAVA_SKILL_TREE } from '@/types/skillTree'
import type { ArticleListVO } from '@/types'

const router = useRouter()
const searchKeyword = ref('')
const articles = ref<ArticleListVO[]>([])
const hotArticles = ref<ArticleListVO[]>([])
const categories = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)

const currentTree = computed(() => JAVA_SKILL_TREE)

function doSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'search', query: { keyword: searchKeyword.value } })
  }
}

async function loadArticles() {
  loading.value = true
  try {
    const res = await articleApi.getList({ page: page.value, size: 10, sortBy: 'publishedAt', sortOrder: 'desc' })
    if (page.value === 1) {
      articles.value = res.data.records
    } else {
      articles.value.push(...res.data.records)
    }
    hasMore.value = res.data.page < res.data.totalPages
  } finally {
    loading.value = false
  }
}

async function loadHot() {
  const res = await articleApi.getHot(10)
  hotArticles.value = res.data
}

async function loadCategories() {
  const res = await articleApi.getCategories()
  categories.value = res.data
}

function loadMore() {
  page.value++
  loadArticles()
}

onMounted(() => {
  loadArticles()
  loadHot()
  loadCategories()
})
</script>

<style scoped>
.hero { text-align: center; padding: 48px 0; }
.hero h1 { font-size: 32px; margin-bottom: 12px; }
.search-box { max-width: 600px; margin: 24px auto 0; display: flex; gap: 8px; }
.search-box .input { flex: 1; }
.section-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 2px solid var(--primary); display: inline-block; }
.article-list { display: flex; flex-direction: column; gap: 16px; }
.load-more { width: 100%; justify-content: center; margin-top: 16px; }
.hot-list { display: flex; flex-direction: column; gap: 8px; }
.hot-item { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.hot-item .rank { width: 20px; height: 20px; background: var(--primary); color: white; border-radius: 4px; font-size: 12px; display: flex; align-items: center; justify-content: center; }
.hot-item .title { font-size: 14px; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.category-list { display: flex; flex-direction: column; gap: 8px; }
.category-item { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid var(--border); }
</style>
