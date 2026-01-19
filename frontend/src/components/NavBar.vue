<template>
  <nav :class="['navbar', { 'navbar-scrolled': isScrolled }]">
    <div class="container">
      <div class="navbar-content">
        <!-- Logo -->
        <router-link to="/" class="navbar-logo">
          <span class="logo-text">PayFlow</span>
        </router-link>
        
        <!-- Desktop Navigation -->
        <div class="navbar-menu">
          <router-link to="/" class="nav-link">홈</router-link>
          <router-link to="/subscriptions" class="nav-link">내 구독</router-link>
          <a href="#features" class="nav-link">기능</a>
          <a href="#ai" class="nav-link">AI 분석</a>
          <a href="#pricing" class="nav-link">요금</a>
        </div>
        
        <!-- Auth Buttons -->
        <div class="navbar-actions">
          <router-link to="/login">
            <Button variant="ghost">로그인</Button>
          </router-link>
          <router-link to="/signup">
            <Button variant="primary">무료 시작하기</Button>
          </router-link>
        </div>
        
        <!-- Mobile Menu Button -->
        <button class="navbar-toggle" @click="toggleMobileMenu" aria-label="메뉴">
          <span class="hamburger" :class="{ 'hamburger-open': isMobileMenuOpen }"></span>
        </button>
      </div>
      
      <!-- Mobile Menu -->
      <div v-if="isMobileMenuOpen" class="mobile-menu">
        <router-link to="/" class="mobile-link" @click="closeMobileMenu">홈</router-link>
        <router-link to="/subscriptions" class="mobile-link" @click="closeMobileMenu">내 구독</router-link>
        <a href="#features" class="mobile-link" @click="closeMobileMenu">기능</a>
        <a href="#ai" class="mobile-link" @click="closeMobileMenu">AI 분석</a>
        <a href="#pricing" class="mobile-link" @click="closeMobileMenu">요금</a>
        <div class="mobile-actions">
          <router-link to="/login" @click="closeMobileMenu">
            <Button variant="outline" block>로그인</Button>
          </router-link>
          <router-link to="/signup" @click="closeMobileMenu">
            <Button variant="primary" block>무료 시작하기</Button>
          </router-link>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import Button from './Button.vue'

const isScrolled = ref(false)
const isMobileMenuOpen = ref(false)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-fixed);
  background: transparent;
  transition: all var(--transition-base);
  padding: var(--spacing-lg) 0;
}

.navbar-scrolled {
  background: var(--color-bg-glass);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border);
  padding: var(--spacing-md) 0;
  box-shadow: var(--shadow-lg);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-xl);
}

.navbar-logo {
  display: flex;
  align-items: center;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-extrabold);
  color: var(--color-text-primary);
  text-decoration: none;
  transition: transform var(--transition-fast);
}

.navbar-logo:hover {
  transform: scale(1.05);
}

.logo-text {
  background: var(--color-gradient-2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.navbar-menu {
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
}

.nav-link {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
  position: relative;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--color-gradient-2);
  transition: width var(--transition-base);
}

.nav-link:hover {
  color: var(--color-text-primary);
}

.nav-link:hover::after {
  width: 100%;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.navbar-toggle {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: var(--spacing-sm);
}

.hamburger {
  display: block;
  width: 24px;
  height: 2px;
  background: var(--color-text-primary);
  position: relative;
  transition: background var(--transition-base);
}

.hamburger::before,
.hamburger::after {
  content: '';
  position: absolute;
  width: 24px;
  height: 2px;
  background: var(--color-text-primary);
  transition: all var(--transition-base);
}

.hamburger::before {
  top: -8px;
}

.hamburger::after {
  top: 8px;
}

.hamburger-open {
  background: transparent;
}

.hamburger-open::before {
  top: 0;
  transform: rotate(45deg);
}

.hamburger-open::after {
  top: 0;
  transform: rotate(-45deg);
}

.mobile-menu {
  display: none;
  flex-direction: column;
  gap: var(--spacing-md);
  padding: var(--spacing-xl) 0;
  border-top: 1px solid var(--color-border);
  margin-top: var(--spacing-lg);
}

.mobile-link {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
  text-decoration: none;
  padding: var(--spacing-sm) 0;
  transition: color var(--transition-fast);
}

.mobile-link:hover {
  color: var(--color-text-primary);
}

.mobile-actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  margin-top: var(--spacing-md);
}

/* Responsive */
@media (max-width: 768px) {
  .navbar-menu,
  .navbar-actions {
    display: none;
  }
  
  .navbar-toggle {
    display: block;
  }
  
  .mobile-menu {
    display: flex;
  }
}
</style>
