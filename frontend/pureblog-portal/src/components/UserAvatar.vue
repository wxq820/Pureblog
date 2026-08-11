<template>
  <img
    :src="avatarUrl"
    :alt="user?.nickname || user?.username"
    :width="sizeMap[size]"
    :height="sizeMap[size]"
    class="avatar"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ user?: any; size?: 'small' | 'medium' | 'large' }>()

const sizeMap: Record<string, number> = { small: 32, medium: 48, large: 80 }

const avatarUrl = computed(() => props.user?.avatarUrl || 'https://via.placeholder.com/32')
</script>

<style scoped>
.avatar { border-radius: 50%; object-fit: cover; }
</style>
