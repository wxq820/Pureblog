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

    <div class="content-grid">
      <aside class="left-sidebar">
        <div v-if="userStore.isLogin" class="card widget-card">
          <h3 class="section-title">小工具</h3>
          <div class="widget-grid">
            <router-link
              v-for="w in widgets"
              :key="w.id"
              :to="w.path"
              class="widget-item"
            >
              <div class="widget-icon" :style="{ backgroundColor: w.iconColor }">
                <el-icon><component :is="widgetIconComp(w)" /></el-icon>
              </div>
              <div class="widget-name">{{ w.name }}</div>
            </router-link>
            <div v-if="!widgets.length" class="empty-widgets text-secondary">暂无小工具</div>
          </div>
        </div>

        <div class="card hot-card">
          <div class="tab-header">
            <button :class="['tab-btn', { active: articleTab === 'hot' }]"
                    @click="articleTab = 'hot'">热门文章</button>
            <button :class="['tab-btn', { active: articleTab === 'latest' }]"
                    @click="articleTab = 'latest'">最新文章</button>
          </div>

          <div v-show="articleTab === 'hot'" class="hot-list">
            <div v-for="(article, i) in hotArticles" :key="article.id" class="hot-item" @click="$router.push(`/article/${article.id}`)">
              <span class="rank">{{ i + 1 }}</span>
              <span class="title">{{ article.title }}</span>
            </div>
          </div>

          <div v-show="articleTab === 'latest'" class="article-list">
            <div v-for="(article, i) in articles" :key="article.id" class="hot-item" @click="$router.push(`/article/${article.id}`)">
              <span class="rank">{{ i + 1 }}</span>
              <span class="title">{{ article.title }}</span>
            </div>
            <div v-if="loading" class="loading text-secondary">加载中...</div>
            <div v-if="!loading && articles.length === 0" class="empty text-secondary">暂无文章</div>
            <button v-if="hasMore && articleTab === 'latest'" class="btn btn-outline load-more" @click="loadMore">加载更多</button>
          </div>
        </div>

        <div class="card category-card">
          <h3 class="section-title">分类</h3>
          <div class="category-list">
            <router-link v-for="cat in categories" :key="cat.id" :to="`/?category=${cat.id}`" class="category-item">
              <span>{{ cat.name }}</span>
              <span class="text-secondary text-xs">{{ cat.articleCount }}</span>
            </router-link>
          </div>
        </div>
      </aside>

      <section class="tree-pane">
        <div v-if="trees.length > 1" class="tree-tabs">
          <button
            v-for="t in trees"
            :key="t.code"
            :class="['tree-tab-btn', { active: activeCode === t.code }]"
            :style="{ borderBottomColor: activeCode === t.code ? (t.coverColor || 'var(--primary)') : 'transparent', color: activeCode === t.code ? (t.coverColor || 'var(--primary)') : 'inherit' }"
            @click="switchTree(t.code)"
          >{{ t.name }}</button>
        </div>
        <SkillTree v-if="activeTree" :tree="activeTree" class="skill-tree-section" />
        <div v-else class="empty tree-empty text-secondary">暂无目录树</div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { treeApi } from '@/api/tree'
import { widgetApi } from '@/api/widget'
import SkillTree from '@/components/SkillTree.vue'
import { useUserStore } from '@/stores/user'
import { resolveWidgetIcon } from '@/constants/widgetIcons'
import type { ArticleListVO, SkillTreeSummary, SkillTreeNode, WidgetVO } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')
const articleTab = ref<'hot' | 'latest'>('hot')
const articles = ref<ArticleListVO[]>([])
const hotArticles = ref<ArticleListVO[]>([])
const categories = ref<any[]>([])
const widgets = ref<WidgetVO[]>([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const currentTreeNodeId = ref<number | null>(null)
const currentTreeId = ref<number | null>(null)

const trees = ref<SkillTreeSummary[]>([])
const activeCode = ref<string>('')
const activeTree = ref<any>(null)

function doSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'search', query: { keyword: searchKeyword.value } })
  }
}

async function loadArticles() {
  loading.value = true
  try {
    const params: any = {
      page: page.value,
      size: 10,
      sortBy: 'publishedAt',
      sortOrder: 'desc'
    }
    if (currentTreeNodeId.value != null) {
      params.treeNodeId = currentTreeNodeId.value
    } else if (currentTreeId.value != null) {
      params.treeId = currentTreeId.value
    }
    const res = await articleApi.getList(params)
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

async function loadWidgets() {
  if (!userStore.isLogin) {
    widgets.value = []
    return
  }
  try {
    const res = await widgetApi.list()
    widgets.value = res.data
  } catch (err) {
    // 静默: 小工具拉取失败不影响首页
    widgets.value = []
  }
}

function widgetIconComp(w: WidgetVO) {
  return resolveWidgetIcon(w.icon, w.code)
}

async function loadTrees() {
  const res = await treeApi.listPublic()
  trees.value = (res.data || []).map((t: any) => ({
    id: t.id,
    code: t.code,
    name: t.name,
    description: t.description,
    coverColor: t.coverColor
  }))
  if (trees.value.length > 0 && !activeCode.value) {
    activeCode.value = trees.value[0].code
    await switchTree(activeCode.value)
  }
}

async function switchTree(code: string) {
  activeCode.value = code
  const res = await treeApi.getPublicByCode(code)
  activeTree.value = toSkillTreeShape(res.data)
}

function toSkillTreeShape(t: any) {
  function convert(n: any, depth: number): SkillTreeNode {
    return {
      id: String(n.id),
      treeId: n.treeId,
      parentId: n.parentId || null,
      name: n.name,
      level: depth,
      color: n.color,
      children: (n.children || []).map((c: any) => convert(c, depth + 1))
    } as SkillTreeNode
  }
  return {
    id: String(t.id),
    name: t.name,
    root: convert(t.root, 0)
  }
}

function loadMore() {
  page.value++
  loadArticles()
}

onMounted(() => {
  loadArticles()
  loadHot()
  loadCategories()
  loadWidgets()
  loadTrees()
})

watch(() => route.query.nodeId, async (nodeId) => {
  if (!nodeId) {
    currentTreeNodeId.value = null
    currentTreeId.value = null
    if (articleTab.value === 'latest') {
      page.value = 1
      articles.value = []
      hasMore.value = true
      await loadArticles()
    }
    return
  }
  currentTreeNodeId.value = Number(nodeId) || null
  currentTreeId.value = route.query.treeId ? Number(route.query.treeId) : null
  if (articleTab.value !== 'latest') articleTab.value = 'latest'
  page.value = 1
  articles.value = []
  hasMore.value = true
  await loadArticles()
})

watch(articleTab, async (tab) => {
  if (tab !== 'latest') return
  page.value = 1
  articles.value = []
  hasMore.value = true
  await loadArticles()
})
</script>

<style scoped>
.hero { text-align: center; padding: 48px 0; }
.hero h1 { font-size: 32px; margin-bottom: 12px; }
.search-box { max-width: 600px; margin: 24px auto 0; display: flex; gap: 8px; }
.search-box .input { flex: 1; }
.section-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 2px solid var(--primary); display: inline-block; }
.article-list { display: flex; flex-direction: column; gap: 8px; }
.load-more { width: 100%; justify-content: center; margin-top: 16px; }
.hot-list { display: flex; flex-direction: column; gap: 8px; }
/* Tab 内容区固定高度 + 内部滚动: 热门/最新切换时卡片高度不变, 整页不会被拉长 */
.hot-card { display: flex; flex-direction: column; height: 420px; }
.hot-list, .article-list { flex: 1 1 auto; min-height: 0; overflow-y: auto; }
.hot-item { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.hot-item .rank { width: 20px; height: 20px; background: var(--primary); color: white; border-radius: 4px; font-size: 12px; display: flex; align-items: center; justify-content: center; }
.hot-item .title { font-size: 14px; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.category-list { display: flex; flex-direction: column; gap: 8px; }
.category-item { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid var(--border); }

.content-grid { display: grid; grid-template-columns: 1fr 3fr; gap: 24px; flex: 1; min-height: 0; }
/* grid-auto-rows: 未登录时小工具卡片不渲染, 行数变少也不会留空行 */
.left-sidebar { display: grid; grid-template-rows: none; grid-auto-rows: minmax(0, 1fr); gap: 16px; min-width: 0; min-height: 0; overflow: hidden; }
.tree-pane { min-height: 0; min-width: 0; display: flex; flex-direction: column; }
.left-sidebar .card { min-height: 0; overflow: auto; }
.tree-pane .skill-tree-section { width: 100%; flex: 1; min-height: 0; }

/* 小工具卡片 */
.widget-card { padding-bottom: 12px; }
.widget-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 4px;
}
.widget-item {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  text-decoration: none; color: var(--text-primary);
  padding: 8px 4px; border-radius: 8px;
  transition: all 0.15s;
}
.widget-item:hover { background: var(--bg-secondary); }
.widget-item:hover .widget-icon { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.widget-icon {
  width: 56px; height: 56px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 26px; color: #fff;
  transition: all 0.15s;
}
.widget-icon :deep(svg) { width: 1em; height: 1em; }
.widget-name { font-size: 12px; text-align: center; }
.empty-widgets { grid-column: 1 / -1; padding: 24px; text-align: center; font-size: 13px; }

.tree-tabs { display: flex; gap: 8px; padding: 0 16px; background: white; border-bottom: 1px solid var(--border); }
.tree-tab-btn { background: transparent; border: none; padding: 12px 14px; font-size: 14px; font-weight: 500; cursor: pointer; border-bottom: 3px solid transparent; color: var(--text-secondary); transition: all 0.15s; }
.tree-tab-btn:hover { color: var(--primary); }
.tree-tab-btn.active { font-weight: 600; }

.tree-empty { display: flex; align-items: center; justify-content: center; height: 100%; background: white; }

.tab-header { display: flex; gap: 0; border-bottom: 1px solid var(--border); margin: -16px -16px 12px; background: var(--bg-secondary); }
.tab-btn { flex: 1; padding: 10px; background: transparent; border: none; cursor: pointer; font-size: 14px; color: var(--text-secondary); border-bottom: 2px solid transparent; transition: all 0.15s; }
.tab-btn:hover { color: var(--primary); }
.tab-btn.active { color: var(--primary); border-bottom-color: var(--primary); font-weight: 600; background: white; }

@media (max-width: 900px) {
  .content-grid { grid-template-columns: 1fr; }
  .left-sidebar { grid-template-rows: none; grid-auto-rows: auto; }
  .tree-pane { min-height: 480px; }
}
</style>
