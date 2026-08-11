<template>
  <div class="notification-page">
    <div class="flex justify-between items-center mb-4">
      <h2>我的通知</h2>
      <button v-if="notifications.length" class="btn btn-outline text-sm" @click="markAllRead">全部标为已读</button>
    </div>
    <div class="notification-list">
      <div v-for="n in notifications" :key="n.id" class="notification-item card" :class="{ unread: !n.isRead }" @click="handleClick(n)">
        <div class="flex items-center gap-2">
          <span class="type-badge">{{ n.typeDesc }}</span>
          <span class="title">{{ n.title }}</span>
          <span class="time text-secondary text-xs">{{ n.relativeTime }}</span>
        </div>
        <p class="content text-secondary text-sm mt-4">{{ n.content }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api'
import type { NotificationVO } from '@/types'

const router = useRouter()
const notifications = ref<NotificationVO[]>([])

async function load() {
  const res = await request.get<any, any>('/notification/list')
  notifications.value = res.data.records
}

async function markAllRead() {
  await request.post<any, any>('/notification/read/all')
  notifications.value.forEach(n => n.isRead = true)
}

function handleClick(n: NotificationVO) {
  if (!n.isRead) {
    request.post<any, any>(`/notification/read/${n.id}`)
    n.isRead = true
  }
  if (n.relatedType === 1 && n.relatedId) {
    router.push(`/article/${n.relatedId}`)
  }
}

onMounted(load)
</script>

<style scoped>
.notification-page { max-width: 800px; margin: 0 auto; }
.notification-item { margin-bottom: 12px; cursor: pointer; }
.notification-item.unread { border-left: 3px solid var(--primary); }
.type-badge { font-size: 12px; padding: 2px 8px; border-radius: 4px; background: #eff6ff; color: var(--primary); }
.title { font-weight: 500; }
</style>
