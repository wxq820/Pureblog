<template>
  <div class="widget-detail">
    <div v-if="code === 'typing'"><TypingWidget /></div>
    <div v-else-if="code === 'random'"><RandomWidget /></div>
    <div v-else-if="code === 'ledger'"><LedgerWidget /></div>
    <div v-else-if="code === 'networth'"><NetWorthWidget /></div>
    <div v-else-if="code === 'recovery'"><RecoveryWidget /></div>
    <div v-else-if="code === 'fund'"><FundWidget /></div>
    <div v-else class="empty">未知的小工具: {{ code }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { widgetApi } from '@/api/widget'
import { getDeviceId } from '@/utils/device'
import { useUserStore } from '@/stores/user'
import TypingWidget from './TypingWidget.vue'
import RandomWidget from './RandomWidget.vue'
import LedgerWidget from './LedgerWidget.vue'
import NetWorthWidget from './NetWorthWidget.vue'
import RecoveryWidget from './RecoveryWidget.vue'
import FundWidget from './FundWidget.vue'

const route = useRoute()
const router = useRouter()
const code = computed(() => String(route.params.code || ''))
const userStore = useUserStore()

onMounted(async () => {
  // 小工具全部需要登录: 路由守卫已拦未登录, 这里再兜底一次防绕过
  if (!userStore.isLogin) {
    router.replace('/login')
    return
  }
  // 拉列表, 找到当前 code 对应的 widget, 调一次 click
  try {
    const res = await widgetApi.list()
    const w = res.data.find(x => x.code === code.value)
    if (w) {
      widgetApi.click(w.id, getDeviceId()).catch(() => {})
    }
  } catch {
    // 静默失败, 不阻塞 widget 显示
  }
})
</script>

<style scoped>
.widget-detail { padding: 0; }
.empty { padding: 48px; text-align: center; color: var(--text-secondary); }
</style>
