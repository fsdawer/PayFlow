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
          <template v-if="isLoggedIn">
            <router-link to="/subscriptions" class="nav-link">구독 관리</router-link>
            <router-link to="/payments" class="nav-link">결제 관리</router-link>
          </template>
          <template v-else>
            <router-link to="/" class="nav-link">홈</router-link>
          </template>
        </div>
        
        <!-- Auth Buttons -->
        <div class="navbar-actions">
          <template v-if="isLoggedIn">
            <button class="nav-btn" @click="logout">로그아웃</button>
          </template>
          <template v-else>
            <router-link to="/login">
              <Button variant="ghost">로그인</Button>
            </router-link>
            <router-link to="/signup">
              <Button variant="primary">무료 시작하기</Button>
            </router-link>
          </template>
        </div>
        
        <!-- Mobile Menu Button -->
        <button class="navbar-toggle" @click="toggleMobileMenu" aria-label="메뉴">
          <span class="hamburger" :class="{ 'hamburger-open': isMobileMenuOpen }"></span>
        </button>
      </div>
      
      <!-- Mobile Menu -->
      <div v-if="isMobileMenuOpen" class="mobile-menu">
        <router-link to="/" class="mobile-link" @click="closeMobileMenu">홈</router-link>
        <router-link to="/subscriptions" class="mobile-link" @click="closeMobileMenu">구독 관리</router-link>
        <router-link to="/payments" class="mobile-link" @click="closeMobileMenu">결제 관리</router-link>
        <a href="#features" class="mobile-link" @click="closeMobileMenu">기능</a>
        <a href="#ai" class="mobile-link" @click="closeMobileMenu">AI 분석</a>
        <div class="mobile-actions">
          <template v-if="isLoggedIn">
            <Button variant="ghost" block @click="logout">로그아웃</Button>
          </template>
          <template v-else>
            <router-link to="/login" @click="closeMobileMenu">
              <Button variant="outline" block>로그인</Button>
            </router-link>
            <router-link to="/signup" @click="closeMobileMenu">
              <Button variant="primary" block>무료 시작하기</Button>
            </router-link>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import Button from './Button.vue'

const router = useRouter()
const isScrolled = ref(false)
const isMobileMenuOpen = ref(false)
const isLoggedIn = ref(false)

// 로그인 상태 확인 함수
const checkLoginStatus = () => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  isLoggedIn.value = !!token
  console.log('🔍 NavBar 로그인 상태 체크:', { token: token ? '있음' : '없음', isLoggedIn: isLoggedIn.value })
}

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
}

const logout = () => {
  localStorage.removeItem('token')
  sessionStorage.removeItem('token')
  isLoggedIn.value = false
  router.push('/login')
  closeMobileMenu()
}

// isLoggedIn 변경 감지
watch(isLoggedIn, (newVal) => {
  console.log('👀 isLoggedIn 변경됨:', newVal)
})

// 페이지 로드 시 및 라우터 변경 시 로그인 상태 확인
onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  
  // 즉시 상태 확인
  checkLoginStatus()
  
  // storage 이벤트 리스너 (다른 탭에서 로그인/로그아웃 시)
  window.addEventListener('storage', checkLoginStatus)
  
  // 라우터 변경 시마다 로그인 상태 재확인
  router.afterEach(() => {
    console.log('🔄 라우터 변경 감지')
    checkLoginStatus()
  })
  
  // 초기 로드 후 한번 더 체크 (안전장치)
  setTimeout(checkLoginStatus, 100)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('storage', checkLoginStatus)
})
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-fixed);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid transparent;
  transition: all var(--transition-base);
  height: var(--navbar-height);
  display: flex;
  align-items: center;
}

.navbar-scrolled {
  background: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
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

.nav-btn {
  background: none;
  border: none;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast);
  padding: 0;
}

.nav-btn:hover {
  color: var(--color-text-primary);
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
