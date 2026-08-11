<template>
  <div class="profile-page" v-if="profile">
    <div class="profile-header card flex gap-4">
      <img :src="profile.avatarUrl || 'https://via.placeholder.com/80'" class="avatar" />
      <div class="info">
        <h2>{{ profile.nickname }}</h2>
        <p class="text-secondary text-sm">@{{ profile.username }}</p>
        <p class="bio mt-4">{{ profile.bio }}</p>
        <div class="stats flex gap-4 mt-4 text-sm">
          <span><strong>{{ profile.articleCount }}</strong> 文章</span>
          <span><strong>{{ profile.followerCount }}</strong> 粉丝</span>
          <span><strong>{{ profile.followingCount }}</strong> 关注</span>
        </div>
        <button v-if="userStore.isLogin && userStore.userInfo?.userId !== profile.userId" class="btn mt-4" :class="profile.isFollowing ? 'btn-outline' : 'btn-primary'" @click="toggleFollow">
          {{ profile.isFollowing ? '取消关注' : '关注' }}
        </button>
      </div>
    </div>
    <div class="articles mt-4">
      <h3>他的文章</h3>
      <div class="article-list">
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { userApi } from '@/api/user'
import { articleApi } from '@/api/article'
import { useUserStore } from '@/stores/user'
import ArticleCard from '@/components/ArticleCard.vue'
import type { UserProfileVO, ArticleListVO } from '@/types'

const route = useRoute()
const userStore = useUserStore()
const profile = ref<UserProfileVO | null>(null)
const articles = ref<ArticleListVO[]>([])

async function loadProfile() {
  const res = await userApi.getUserProfile(Number(route.params.id))
  profile.value = res.data
}

async function loadArticles() {
  const res = await articleApi.getAuthorArticles(Number(route.params.id))
  articles.value = res.data
}

async function toggleFollow() {
  if (!userStore.isLogin) { window.location.href = '/login'; return }
  if (profile.value?.isFollowing) {
    await userApi.unfollow(profile.value!.userId)
    profile.value.isFollowing = false
    profile.value.followerCount--
  } else {
    await userApi.follow(profile.value!.userId)
    profile.value.isFollowing = true
    profile.value.followerCount++
  }
}

onMounted(() => { loadProfile(); loadArticles() })
</script>

<style scoped>
.profile-page { max-width: 800px; margin: 0 auto; }
.avatar { width: 120px; height: 120px; border-radius: 50%; object-fit: cover; }
.article-list { display: flex; flex-direction: column; gap: 16px; margin-top: 16px; }
</style>
