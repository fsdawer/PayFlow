<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card glass-strong">
        <div class="login-header">
          <router-link to="/" class="logo-link">
            <h1 class="logo">PayFlow</h1>
          </router-link>
          <h2 class="login-title">로그인</h2>
          <p class="login-subtitle">구독 관리를 시작하세요</p>
        </div>
        
        <form class="login-form" @submit.prevent="handleSubmit">
          <Input
            v-model="formData.email"
            type="email"
            label="이메일"
            placeholder="your@email.com"
            :error="errors.email"
            required
            @blur="validateEmail"
          />
          
          <Input
            v-model="formData.password"
            type="password"
            label="비밀번호"
            placeholder="••••••••"
            :error="errors.password"
            required
            @blur="validatePassword"
          />
          
          <div class="form-options">
            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.remember">
              <span>로그인 상태 유지</span>
            </label>
            <a href="#forgot" class="forgot-link">비밀번호 찾기</a>
          </div>
          
          <Button
            type="submit"
            variant="primary"
            :loading="isLoading"
            block
            style="padding: 1rem; font-size: 1rem; margin-top: 1.5rem;"
          >
            로그인
          </Button>
        </form>
        
        <div class="login-footer">
          <p class="signup-prompt">
            아직 계정이 없으신가요?
            <router-link to="/signup" class="signup-link">회원가입</router-link>
          </p>
        </div>
        
        <div class="divider">
          <span>또는</span>
        </div>
        
        <div class="social-login">
          <button class="social-button" @click.prevent="handleSocialLogin('google')">
            <span class="social-icon">G</span>
            <span>Google로 계속하기</span>
          </button>
          <button class="social-button" @click.prevent="handleSocialLogin('kakao')">
            <span class="social-icon">K</span>
            <span>카카오로 계속하기</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import Input from '../components/Input.vue'
import Button from '../components/Button.vue'

const router = useRouter()

const formData = reactive({
  email: '',
  password: '',
  remember: false
})

const errors = reactive({
  email: '',
  password: ''
})

const isLoading = ref(false)

const validateEmail = () => {
  if (!formData.email) {
    errors.email = '이메일을 입력해주세요'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
    errors.email = '올바른 이메일 형식이 아닙니다'
  } else {
    errors.email = ''
  }
}

const validatePassword = () => {
  if (!formData.password) {
    errors.password = '비밀번호를 입력해주세요'
  } else if (formData.password.length < 6) {
    errors.password = '비밀번호는 최소 6자 이상이어야 합니다'
  } else {
    errors.password = ''
  }
}

const request = async (url, options = {}) => {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    ...options
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || '요청에 실패했습니다.')
  }
  if (response.status === 204) return null
  return response.json()
}

const handleSubmit = async () => {
  validateEmail()
  validatePassword()
  
  if (errors.email || errors.password) {
    return
  }
  
  isLoading.value = true

  try {
    console.log('🔐 로그인 시도:', { email: formData.email })
    
    const data = await request('http://localhost:8080/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        email: formData.email,
        password: formData.password
      })
    })
    
    console.log('✅ 로그인 응답:', data)
    
    if (!data?.accessToken) {
      console.error('❌ 토큰 없음:', data)
      throw new Error('토큰이 응답에 없습니다.')
    }
    
    const storage = formData.remember ? localStorage : sessionStorage
    storage.setItem('token', data.accessToken)
    
    console.log('💾 토큰 저장 완료:', {
      storage: formData.remember ? 'localStorage' : 'sessionStorage',
      token: data.accessToken.substring(0, 20) + '...'
    })
    
    alert('로그인 성공!')
    
    // 페이지를 새로고침하여 NavBar 상태 업데이트
    window.location.href = '/subscriptions'
  } catch (error) {
    console.error('❌ 로그인 실패:', error)
    alert(error.message || '로그인에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const handleSocialLogin = (provider) => {
  console.log(`${provider} 로그인`)
  alert(`${provider} 로그인 기능은 백엔드 연동 후 동작합니다`)
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl) var(--spacing-md);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(139, 92, 246, 0.05) 100%);
}

.login-container {
  width: 100%;
  max-width: 480px;
}

.login-card {
  padding: var(--spacing-3xl);
  animation: fadeInUp 0.6s ease-out;
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-3xl);
}

.logo-link {
  text-decoration: none;
}

.logo {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-extrabold);
  background: var(--color-gradient-2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: var(--spacing-lg);
}

.login-title {
  font-size: var(--font-size-2xl);
  margin-bottom: var(--spacing-sm);
}

.login-subtitle {
  color: var(--color-text-muted);
  font-size: var(--font-size-base);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--font-size-sm);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: var(--color-primary);
}

.forgot-link {
  color: var(--color-primary-light);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.forgot-link:hover {
  color: var(--color-primary);
}

.login-footer {
  margin-top: var(--spacing-2xl);
  text-align: center;
}

.signup-prompt {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.signup-link {
  color: var(--color-primary-light);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
  margin-left: var(--spacing-xs);
  transition: color var(--transition-fast);
}

.signup-link:hover {
  color: var(--color-primary);
}

.divider {
  position: relative;
  text-align: center;
  margin: var(--spacing-2xl) 0;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: rgba(16, 185, 129, 0.75);
}

.divider span {
  position: relative;
  display: inline-block;
  padding: 0 var(--spacing-md);
  background: var(--color-bg-glass);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.social-login {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.social-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-base);
}

.social-button:hover {
  background: var(--color-bg-secondary);
  border-color: var(--color-primary);
  transform: translateY(-2px);
}

.social-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: var(--color-primary);
  border-radius: var(--radius-sm);
  color: white;
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-sm);
}

/* Responsive */
@media (max-width: 640px) {
  .login-card {
    padding: var(--spacing-2xl);
  }
  
  .form-options {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
}
</style>
