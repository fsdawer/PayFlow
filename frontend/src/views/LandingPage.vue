<template>
  <div class="landing-page">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-background"></div>
      <div class="container">
        <div class="hero-content animate-fade-in-up">
          <h1 class="hero-title">
            구독 관리, 이제<br>
            <span class="text-gradient">AI가 도와드립니다</span>
          </h1>
          <p class="hero-subtitle">
            공과금부터 구독 서비스까지 자동으로 관리하고,<br>
            AI 분석으로 매달 지출을 줄이세요
          </p>
          <div class="hero-actions">
            <router-link :to="isLoggedIn ? '/subscriptions' : '/signup'">
              <Button variant="primary" style="padding: 1.25rem 2.5rem; font-size: 1.125rem;">
                {{ isLoggedIn ? '내 구독 관리하기 →' : '무료로 시작하기 →' }}
              </Button>
            </router-link>
            <a href="#features">
              <Button variant="outline" style="padding: 1.25rem 2.5rem; font-size: 1.125rem;">
                자세히 알아보기
              </Button>
            </a>
          </div>
          <div class="hero-stats">
            <div class="stat-item">
              <div class="stat-number">10,000+</div>
              <div class="stat-label">활성 사용자</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">50,000+</div>
              <div class="stat-label">관리된 구독</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">₩5억+</div>
              <div class="stat-label">절감된 금액</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section id="features" class="features">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">PayFlow의 핵심 기능</h2>
          <p class="section-subtitle">한 번 등록으로 모든 구독을 스마트하게 관리하세요</p>
        </div>
        
        <div class="features-grid">
          <div class="feature-card card">
            <div class="feature-icon">🔔</div>
            <h3 class="feature-title">자동 알림</h3>
            <p class="feature-description">
              결제 예정일을 기준으로 사전 알림을 자동으로 발송합니다.
              더 이상 깜빡하고 연체료를 낼 걱정이 없습니다.
            </p>
          </div>
          
          <div class="feature-card card">
            <div class="feature-icon">⚡</div>
            <h3 class="feature-title">스마트 관리</h3>
            <p class="feature-description">
              예정·완료·연체 상태를 결제 주기에 따라 자동으로 관리합니다.
              한눈에 보는 구독 현황으로 재정을 투명하게 관리하세요.
            </p>
          </div>
          
          <div class="feature-card card">
            <div class="feature-icon">🤖</div>
            <h3 class="feature-title">AI 지출 분석</h3>
            <p class="feature-description">
              중복 지출이나 과도한 구독 항목을 AI가 자동으로 식별하고,
              절감할 수 있는 맞춤 추천을 제공합니다.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- AI Analysis Section -->
    <section id="ai" class="ai-section">
      <div class="container">
        <div class="ai-content">
          <div class="ai-text">
            <h2 class="ai-title">
              지출을 줄이는<br>
              <span class="text-gradient">가장 스마트한 방법</span>
            </h2>
            <p class="ai-description">
              PayFlow의 AI 엔진은 사용자의 구독 패턴을 분석하여 불필요한 지출을 자동으로 찾아냅니다.
            </p>
            <ul class="ai-features">
              <li class="ai-feature-item">
                <span class="check-icon">✓</span>
                <span>중복 구독 서비스 자동 감지</span>
              </li>
              <li class="ai-feature-item">
                <span class="check-icon">✓</span>
                <span>사용하지 않는 구독 알림</span>
              </li>
              <li class="ai-feature-item">
                <span class="check-icon">✓</span>
                <span>더 저렴한 대안 추천</span>
              </li>
              <li class="ai-feature-item">
                <span class="check-icon">✓</span>
                <span>월별 지출 트렌드 분석</span>
              </li>
            </ul>
            <router-link :to="isLoggedIn ? '/subscriptions' : '/signup'">
              <Button variant="primary" style="margin-top: 2rem; padding: 1rem 2rem;">
                {{ isLoggedIn ? '지금 분석 확인하기' : 'AI 분석 시작하기' }}
              </Button>
            </router-link>
          </div>
          <div class="ai-visual">
            <div class="ai-card glass-strong">
              <h4 class="ai-card-title">이달의 절감 기회</h4>
              <div class="savings-amount">₩45,000</div>
              <p class="savings-description">3개의 중복 구독 발견</p>
              <div class="savings-list">
                <div class="savings-item">
                  <span>넷플릭스 프리미엄</span>
                  <span class="savings-tag">중복</span>
                </div>
                <div class="savings-item">
                  <span>미사용 피트니스 앱</span>
                  <span class="savings-tag">미사용</span>
                </div>
                <div class="savings-item">
                  <span>유튜브 프리미엄</span>
                  <span class="savings-tag">대안 있음</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta">
      <div class="container">
        <div class="cta-content glass">
          <h2 class="cta-title">지금 바로 시작하세요</h2>
          <p class="cta-subtitle">
            가입하는 데 1분이면 충분합니다.<br>
            신용카드 없이 무료로 시작할 수 있어요.
          </p>
          <router-link :to="isLoggedIn ? '/subscriptions' : '/signup'">
            <Button variant="primary" style="padding: 1.25rem 3rem; font-size: 1.125rem;">
              {{ isLoggedIn ? '내 구독 보러가기' : '무료 시작하기' }}
            </Button>
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Button from '../components/Button.vue'

const isLoggedIn = ref(false)

onMounted(() => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  isLoggedIn.value = !!token
})
</script>

<style scoped>
.landing-page {
  width: 100%;
}

/* Hero Section */
.hero {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  overflow: hidden;
  padding: var(--spacing-4xl) 0;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('/hero-bg.png');
  background-size: cover;
  background-position: center;
  opacity: 0.4;
  z-index: 0;
}

.hero-background::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(10, 14, 39, 0.8) 0%, rgba(30, 36, 69, 0.9) 100%);
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 900px;
  margin: 0 auto;
}

.hero-title {
  font-size: var(--font-size-6xl);
  font-weight: var(--font-weight-extrabold);
  line-height: 1.1;
  margin-bottom: var(--spacing-xl);
}

.hero-subtitle {
  font-size: var(--font-size-xl);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-3xl);
  line-height: var(--line-height-relaxed);
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-4xl);
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-3xl);
  max-width: 700px;
  margin: 0 auto;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-extrabold);
  background: var(--color-gradient-2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: var(--spacing-sm);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

/* Features Section */
.features {
  padding: var(--spacing-4xl) 0;
  background: var(--color-bg-secondary);
}

.section-header {
  text-align: center;
  margin-bottom: var(--spacing-4xl);
}

.section-title {
  font-size: var(--font-size-4xl);
  margin-bottom: var(--spacing-md);
}

.section-subtitle {
  font-size: var(--font-size-lg);
  color: var(--color-text-muted);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-2xl);
}

.feature-card {
  text-align: center;
  padding: var(--spacing-3xl) var(--spacing-xl);
}

.feature-icon {
  font-size: 4rem;
  margin-bottom: var(--spacing-lg);
}

.feature-title {
  font-size: var(--font-size-2xl);
  margin-bottom: var(--spacing-md);
}

.feature-description {
  color: var(--color-text-muted);
  line-height: var(--line-height-relaxed);
}

/* AI Section */
.ai-section {
  padding: var(--spacing-4xl) 0;
}

.ai-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-4xl);
  align-items: center;
}

.ai-title {
  font-size: var(--font-size-4xl);
  margin-bottom: var(--spacing-lg);
  line-height: 1.2;
}

.ai-description {
  font-size: var(--font-size-lg);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-2xl);
  line-height: var(--line-height-relaxed);
}

.ai-features {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.ai-feature-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  font-size: var(--font-size-lg);
  color: var(--color-text-secondary);
}

.check-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  color: white;
  font-weight: var(--font-weight-bold);
  flex-shrink: 0;
}

.ai-visual {
  display: flex;
  justify-content: center;
}

.ai-card {
  padding: var(--spacing-2xl);
  width: 100%;
  max-width: 400px;
}

.ai-card-title {
  font-size: var(--font-size-lg);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-lg);
}

.savings-amount {
  font-size: var(--font-size-5xl);
  font-weight: var(--font-weight-extrabold);
  background: var(--color-gradient-2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: var(--spacing-sm);
}

.savings-description {
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-xl);
}

.savings-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.savings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
}

.savings-tag {
  padding: var(--spacing-xs) var(--spacing-sm);
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

/* CTA Section */
.cta {
  padding: var(--spacing-4xl) 0;
}

.cta-content {
  text-align: center;
  padding: var(--spacing-4xl) var(--spacing-2xl);
}

.cta-title {
  font-size: var(--font-size-4xl);
  margin-bottom: var(--spacing-md);
}

.cta-subtitle {
  font-size: var(--font-size-lg);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-2xl);
  line-height: var(--line-height-relaxed);
}

/* Responsive */
@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .ai-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero {
    min-height: auto;
    padding: var(--spacing-3xl) 0;
  }
  
  .hero-title {
    font-size: var(--font-size-4xl);
  }
  
  .hero-subtitle {
    font-size: var(--font-size-base);
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .hero-stats {
    grid-template-columns: 1fr;
    gap: var(--spacing-xl);
  }
  
  .stat-number {
    font-size: var(--font-size-3xl);
  }
}
</style>
