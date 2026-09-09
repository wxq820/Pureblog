<template>
  <div class="admin-shell">
    <AppHeader />
    <main class="admin-main">
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-main {
  flex: 1;
  background: var(--bg-page);
  padding-top: 72px;
}

.admin-content {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 24px 20px;
  min-height: 100%;
}
</style>
