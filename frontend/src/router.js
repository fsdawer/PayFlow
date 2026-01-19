import { createRouter, createWebHistory } from 'vue-router'
import LandingPage from './views/LandingPage.vue'
import LoginPage from './views/LoginPage.vue'
import SignupPage from './views/SignupPage.vue'
import SubscriptionPage from './views/SubscriptionPage.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: LandingPage,
    meta: {
      title: 'PayFlow - AI 기반 구독 관리 서비스'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
    meta: {
      title: '로그인 - PayFlow'
    }
  },
  {
    path: '/signup',
    name: 'Signup',
    component: SignupPage,
    meta: {
      title: '회원가입 - PayFlow'
    }
  },
  {
    path: '/subscriptions',
    name: 'Subscriptions',
    component: SubscriptionPage,
    meta: {
      title: '내 구독 - PayFlow'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth'
      }
    } else {
      return { top: 0, behavior: 'smooth' }
    }
  }
})

// Update page title on route change
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || 'PayFlow'
  next()
})

export default router
