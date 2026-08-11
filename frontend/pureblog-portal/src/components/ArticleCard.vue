<template>
  <div class="article-card card" @click="$router.push(`/article/${article.id}`)">
    <div v-if="article.coverUrl" class="cover">
      <img :src="article.coverUrl" :alt="article.title" />
    </div>
    <div class="content">
      <div class="flex items-center gap-2 mb-4">
        <span v-if="article.isTop" class="tag" style="background:#fee2e2; color:#dc2626;">置顶</span>
        <span v-if="article.isFeatured" class="tag">精选</span>
        <span v-if="article.categoryName" class="tag">{{ article.categoryName }}</span>
      </div>
      <h3 class="title">{{ article.title }}</h3>
      <p class="summary text-secondary text-sm">{{ article.summary }}</p>
      <div class="meta flex items-center gap-4 text-secondary text-xs">
        <span>作者: {{ article.authorName }}</span>
        <span>{{ formatDate(article.publishedAt) }}</span>
        <span>阅读 {{ article.viewCount }}</span>
        <span>点赞 {{ article.likeCount }}</span>
        <span>评论 {{ article.commentCount }}</span>
      </div>
      <div v-if="article.tagNames?.length" class="tags mt-4">
        <span v-for="tag in article.tagNames" :key="tag" class="tag">{{ tag }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ArticleListVO } from '@/types'
import dayjs from 'dayjs'

defineProps<{ article: ArticleListVO }>()

function formatDate(date: string) {
  return dayjs(date).format('YYYY-MM-DD')
}
</script>

<style scoped>
.article-card { cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; }
.article-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.cover { margin-bottom: 16px; border-radius: var(--radius); overflow: hidden; }
.cover img { width: 100%; height: 200px; object-fit: cover; }
.content .title { font-size: 18px; font-weight: 600; margin-bottom: 8px; line-height: 1.4; }
.summary { margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.tags { display: flex; flex-wrap: wrap; gap: 6px; }
</style>
