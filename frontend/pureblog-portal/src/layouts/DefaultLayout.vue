<template>
  <div class="layout">
    <AppHeader />
    <main class="main-content">
      <div class="container">
        <router-view />
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
</script>

<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }
.main-content { flex: 1; padding: 24px 0; }
</style>
