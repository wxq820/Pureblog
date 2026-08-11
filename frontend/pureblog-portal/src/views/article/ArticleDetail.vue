<template>
  <article class="article-detail" v-if="article">
    <div class="article-header">
      <div class="tags" v-if="article.tags?.length">
        <span v-for="tag in article.tags" :key="tag.id" class="tag">{{ tag.name }}</span>
      </div>
      <h1 class="title">{{ article.title }}</h1>
      <div class="meta flex items-center gap-4 text-secondary text-sm">
        <router-link :to="`/user/${article.author.userId}`" class="author">
          <img :src="article.author.avatarUrl || 'https://via.placeholder.com/32'" class="avatar" />
          {{ article.author.nickname }}
        </router-link>
        <span>{{ formatDate(article.publishedAt) }}</span>
        <span>阅读 {{ article.viewCount }}</span>
        <span>字数 {{ article.wordCount }}</span>
      </div>
    </div>

    <div v-if="article.coverUrl" class="cover">
      <img :src="article.coverUrl" :alt="article.title" />
    </div>

    <div class="content" v-html="article.htmlContent || article.content"></div>

    <div class="actions mt-4">
      <button class="action-btn" :class="{ active: article.isLiked }" @click="toggleLike">
        <span>{{ article.isLiked ? '已点赞' : '点赞' }}</span>
        <span>{{ article.likeCount }}</span>
      </button>
      <button class="action-btn" :class="{ active: article.isCollected }" @click="toggleCollect">
        <span>{{ article.isCollected ? '已收藏' : '收藏' }}</span>
        <span>{{ article.collectCount }}</span>
      </button>
    </div>

    <div class="author-box card mt-4">
      <router-link :to="`/user/${article.author.userId}`" class="flex items-center gap-4">
        <img :src="article.author.avatarUrl || 'https://via.placeholder.com/80'" class="avatar-lg" />
        <div>
          <div class="nickname">{{ article.author.nickname }}</div>
          <div class="text-secondary text-sm">粉丝 {{ article.author.followerCount }}</div>
        </div>
      </router-link>
    </div>

    <div class="comment-section mt-4">
      <h3>评论 ({{ article.commentCount }})</h3>
      <div v-if="userStore.isLogin" class="comment-form">
        <textarea v-model="commentContent" class="input" rows="3" placeholder="写下你的评论..."></textarea>
        <button class="btn btn-primary mt-4" @click="submitComment">发表评论</button>
      </div>
      <div v-else class="text-secondary">
        <router-link to="/login">登录</router-link>后发表评论
      </div>
      <div class="comment-list mt-4">
        <div v-for="comment in comments" :key="comment.id" class="comment-item card">
          <div class="comment-header flex items-center gap-2">
            <img :src="comment.avatarUrl || 'https://via.placeholder.com/32'" class="avatar-sm" />
            <span>{{ comment.nickname }}</span>
            <span class="text-secondary text-xs">{{ formatRelative(comment.createdAt) }}</span>
          </div>
          <p class="comment-content mt-4">{{ comment.content }}</p>
          <div v-if="comment.replies?.length" class="replies mt-4">
            <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <span class="nickname">{{ reply.nickname }}</span>
              <span class="text-secondary"> 回复 </span>
              <span class="nickname">{{ reply.replyToNickname }}</span>
              <span class="text-secondary">: {{ reply.content }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { articleApi } from '@/api/article'
import { commentApi } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import type { ArticleDetailVO, CommentVO } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const route = useRoute()
const userStore = useUserStore()
const article = ref<ArticleDetailVO | null>(null)
const comments = ref<CommentVO[]>([])
const commentContent = ref('')

async function loadArticle() {
  const res = await articleApi.getDetail(Number(route.params.id))
  article.value = res.data
}

async function loadComments() {
  const res = await commentApi.getComments(Number(route.params.id))
  comments.value = res.data.records
}

async function toggleLike() {
  if (!userStore.isLogin) { window.location.href = '/login'; return }
  if (article.value?.isLiked) {
    await commentApi.unlike(article.value!.id)
    article.value.isLiked = false
    article.value.likeCount--
  } else {
    await commentApi.like(article.value!.id)
    article.value.isLiked = true
    article.value.likeCount++
  }
}

async function toggleCollect() {
  if (!userStore.isLogin) { window.location.href = '/login'; return }
  if (article.value?.isCollected) {
    await commentApi.uncollect(article.value!.id)
    article.value.isCollected = false
    article.value.collectCount--
  } else {
    await commentApi.collect(article.value!.id)
    article.value.isCollected = true
    article.value.collectCount++
  }
}

async function submitComment() {
  if (!commentContent.value.trim()) return
  await commentApi.create({ articleId: article.value!.id, content: commentContent.value })
  commentContent.value = ''
  loadComments()
}

function formatDate(date: string) { return dayjs(date).format('YYYY-MM-DD HH:mm') }
function formatRelative(date: string) { return dayjs(date).fromNow() }

onMounted(() => { loadArticle(); loadComments() })
</script>

<style scoped>
.article-detail { max-width: 800px; margin: 0 auto; }
.article-header .tags { margin-bottom: 12px; }
.title { font-size: 28px; margin: 12px 0; }
.avatar { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; }
.avatar-lg { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; }
.avatar-sm { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; }
.author { display: flex; align-items: center; gap: 8px; color: var(--text-primary); }
.cover img { width: 100%; border-radius: var(--radius); margin-bottom: 24px; }
.content { font-size: 16px; line-height: 1.8; margin: 24px 0; }
.content :deep(h1), .content :deep(h2), .content :deep(h3) { margin: 24px 0 12px; font-weight: 600; }
.content :deep(p) { margin-bottom: 16px; }
.content :deep(pre) { background: #1f2937; color: #f3f4f6; padding: 16px; border-radius: 8px; overflow-x: auto; }
.content :deep(code) { font-family: monospace; background: #f3f4f6; padding: 2px 6px; border-radius: 4px; font-size: 14px; }
.content :deep(pre code) { background: none; padding: 0; }
.content :deep(blockquote) { border-left: 4px solid var(--primary); padding-left: 16px; color: var(--text-secondary); margin: 16px 0; }
.actions { display: flex; gap: 16px; padding: 16px 0; border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); }
.action-btn { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border: 1px solid var(--border); border-radius: var(--radius); background: white; cursor: pointer; font-size: 14px; }
.action-btn.active { border-color: var(--primary); color: var(--primary); background: #eff6ff; }
.comment-form textarea { resize: vertical; }
.comment-item { margin-bottom: 12px; }
.nickname { font-weight: 500; }
.replies { background: #f9fafb; padding: 12px; border-radius: 6px; }
.reply-item { margin-bottom: 8px; font-size: 14px; }
</style>
