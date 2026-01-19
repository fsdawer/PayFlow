<template>
  <div class="app">
    <NavBar v-if="!isAuthPage" />
    <main :class="['main-content', { 'auth-page': isAuthPage }]">
      <RouterView />
    </main>
    <Footer v-if="!isAuthPage" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'
import Footer from './components/Footer.vue'

const route = useRoute()

// Hide NavBar and Footer on auth pages
const isAuthPage = computed(() => {
  return route.path === '/login' || route.path === '/signup'
})
</script>

<style>
.app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  padding-top: 80px; /* NavBar height */
}

.main-content.auth-page {
  padding-top: 0;
}
</style>

