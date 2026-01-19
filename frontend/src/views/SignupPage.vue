<template>
  <div class="signup-page">
    <div class="signup-container">
      <div class="signup-card glass-strong">
        <div class="signup-header">
          <router-link to="/" class="logo-link">
            <h1 class="logo">PayFlow</h1>
          </router-link>
          <h2 class="signup-title">회원가입</h2>
          <p class="signup-subtitle">무료로 시작하세요. 신용카드 필요 없습니다.</p>
        </div>
        
        <form class="signup-form" @submit.prevent="handleSubmit">
          <Input
            v-model="formData.name"
            type="text"
            label="이름"
            placeholder="홍길동"
            :error="errors.name"
            required
            @blur="validateName"
          />
          
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
            hint="최소 8자, 영문, 숫자, 특수문자 포함"
            required
            @blur="validatePassword"
          />
          
          <Input
            v-model="formData.passwordConfirm"
            type="password"
            label="비밀번호 확인"
            placeholder="••••••••"
            :error="errors.passwordConfirm"
            required
            @blur="validatePasswordConfirm"
          />
          
          <div class="terms-section">
            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.agreeTerms" required>
              <span>
                <a href="#terms" class="terms-link">이용약관</a> 및 
                <a href="#privacy" class="terms-link">개인정보처리방침</a>에 동의합니다
              </span>
            </label>
            
            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.agreeMarketing">
              <span>마케팅 정보 수신에 동의합니다 (선택)</span>
            </label>
          </div>
          
          <Button
            type="submit"
            variant="primary"
            :loading="isLoading"
            :disabled="!formData.agreeTerms"
            block
            style="padding: 1rem; font-size: 1rem; margin-top: 1.5rem;"
          >
            회원가입
          </Button>
        </form>
        
        <div class="signup-footer">
          <p class="login-prompt">
            이미 계정이 있으신가요?
            <router-link to="/login" class="login-link">로그인</router-link>
          </p>
        </div>
        
        <div class="divider">
          <span>또는</span>
        </div>
        
        <div class="social-signup">
          <button class="social-button" @click.prevent="handleSocialSignup('google')">
            <span class="social-icon">G</span>
            <span>Google로 가입하기</span>
          </button>
          <button class="social-button" @click.prevent="handleSocialSignup('kakao')">
            <span class="social-icon">K</span>
            <span>카카오로 가입하기</span>
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
  name: '',
  email: '',
  password: '',
  passwordConfirm: '',
  agreeTerms: false,
  agreeMarketing: false
})

const errors = reactive({
  name: '',
  email: '',
  password: '',
  passwordConfirm: ''
})

const isLoading = ref(false)

const API_BASE_URL = 'http://localhost:8080'

const request = async (url, options = {}) => {
  // 상대 경로면 절대 경로로 변환
  const fullUrl = url.startsWith('http') ? url : `${API_BASE_URL}${url}`
  
  const response = await fetch(fullUrl, {
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

const validateName = () => {
  if (!formData.name) {
    errors.name = '이름을 입력해주세요'
  } else if (formData.name.length < 2) {
    errors.name = '이름은 2자 이상이어야 합니다'
  } else {
    errors.name = ''
  }
}

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
  } else if (formData.password.length < 8) {
    errors.password = '비밀번호는 최소 8자 이상이어야 합니다'
  } else if (!/(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])/.test(formData.password)) {
    errors.password = '영문, 숫자, 특수문자를 모두 포함해야 합니다'
  } else {
    errors.password = ''
  }
}

const validatePasswordConfirm = () => {
  if (!formData.passwordConfirm) {
    errors.passwordConfirm = '비밀번호 확인을 입력해주세요'
  } else if (formData.password !== formData.passwordConfirm) {
    errors.passwordConfirm = '비밀번호가 일치하지 않습니다'
  } else {
    errors.passwordConfirm = ''
  }
}

const handleSubmit = async () => {
  validateName()
  validateEmail()
  validatePassword()
  validatePasswordConfirm()
  
  if (errors.name || errors.email || errors.password || errors.passwordConfirm) {
    return
  }
  
  if (!formData.agreeTerms) {
    alert('이용약관에 동의해주세요')
    return
  }
  
  isLoading.value = true

  try {
    await request('/api/auth/signup', {
      method: 'POST',
      body: JSON.stringify({
        name: formData.name,
        email: formData.email,
        password: formData.password
      })
    })
    alert('회원가입이 완료되었습니다. 로그인 해주세요.')
    router.push('/login')
  } catch (error) {
    alert(error.message || '회원가입에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const handleSocialSignup = (provider) => {
  console.log(`${provider} 회원가입`)
  alert(`${provider} 회원가입 기능은 백엔드 연동 후 동작합니다`)
}
</script>

<style scoped>
.signup-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl) var(--spacing-md);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(139, 92, 246, 0.05) 100%);
}

.signup-container {
  width: 100%;
  max-width: 520px;
}

.signup-card {
  padding: var(--spacing-3xl);
  animation: fadeInUp 0.6s ease-out;
}

.signup-header {
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

.signup-title {
  font-size: var(--font-size-2xl);
  margin-bottom: var(--spacing-sm);
}

.signup-subtitle {
  color: var(--color-text-muted);
  font-size: var(--font-size-base);
}

.signup-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.terms-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: 16px;
  height: 16px;
  margin-top: 2px;
  cursor: pointer;
  flex-shrink: 0;
  accent-color: var(--color-primary);
}

.terms-link {
  color: var(--color-primary-light);
  text-decoration: underline;
  transition: color var(--transition-fast);
}

.terms-link:hover {
  color: var(--color-primary);
}

.signup-footer {
  margin-top: var(--spacing-2xl);
  text-align: center;
}

.login-prompt {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.login-link {
  color: var(--color-primary-light);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
  margin-left: var(--spacing-xs);
  transition: color var(--transition-fast);
}

.login-link:hover {
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
  background: var(--color-border);
}

.divider span {
  position: relative;
  display: inline-block;
  padding: 0 var(--spacing-md);
  background: var(--color-bg-glass);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.social-signup {
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
  .signup-card {
    padding: var(--spacing-2xl);
  }
}
</style>
