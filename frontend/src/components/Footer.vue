<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <!-- Company Info -->
        <div class="footer-section">
          <h3 class="footer-logo">PayFlow</h3>
          <p class="footer-description">
            AI가 도와주는 스마트한 구독 관리 서비스.<br>
            매달 지출을 절약하세요.
          </p>
          <div class="footer-social">
            <a href="#" class="social-link" aria-label="Facebook">📘</a>
            <a href="#" class="social-link" aria-label="Twitter">🐦</a>
            <a href="#" class="social-link" aria-label="Instagram">📷</a>
            <a href="#" class="social-link" aria-label="YouTube">📺</a>
          </div>
        </div>
        
        <!-- Service Links -->
        <div class="footer-section">
          <h4 class="footer-title">서비스</h4>
          <ul class="footer-links">
            <li><a href="#features">주요 기능</a></li>
            <li><a href="#ai">AI 분석</a></li>
            <li><a href="#pricing">요금 안내</a></li>
            <li><a href="#faq">자주 묻는 질문</a></li>
          </ul>
        </div>
        
        <!-- Support Links -->
        <div class="footer-section">
          <h4 class="footer-title">고객 지원</h4>
          <ul class="footer-links">
            <li><a href="#help">도움말 센터</a></li>
            <li><a href="#contact">문의하기</a></li>
            <li><a href="#privacy">개인정보처리방침</a></li>
            <li><a href="#terms">이용약관</a></li>
          </ul>
        </div>
        
        <!-- Company Links -->
        <div class="footer-section">
          <h4 class="footer-title">회사</h4>
          <ul class="footer-links">
            <li><a href="#about">회사 소개</a></li>
            <li><a href="#blog">블로그</a></li>
            <li><a href="#careers">채용</a></li>
            <li><a href="#press">보도자료</a></li>
          </ul>
        </div>
      </div>
      
      <div class="footer-bottom">
        <p class="footer-copyright">
          © {{ currentYear }} PayFlow. All rights reserved.
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.footer {
  background: var(--color-bg-secondary);
  border-top: 1px solid var(--color-border);
  padding: var(--spacing-4xl) 0 var(--spacing-2xl);
  margin-top: var(--spacing-4xl);
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: var(--spacing-3xl);
  margin-bottom: var(--spacing-3xl);
}

.footer-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.footer-logo {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-extrabold);
  background: var(--color-gradient-2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.footer-description {
  color: var(--color-text-muted);
  line-height: var(--line-height-relaxed);
  margin: 0;
}

.footer-social {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-sm);
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  font-size: var(--font-size-xl);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
  text-decoration: none;
}

.social-link:hover {
  background: var(--color-primary);
  transform: translateY(-2px);
}

.footer-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0;
}

.footer-links {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.footer-links a {
  color: var(--color-text-muted);
  text-decoration: none;
  font-size: var(--font-size-sm);
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--color-primary-light);
}

.footer-bottom {
  padding-top: var(--spacing-xl);
  border-top: 1px solid var(--color-border);
  text-align: center;
}

.footer-copyright {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  margin: 0;
}

/* Responsive */
@media (max-width: 1024px) {
  .footer-content {
    grid-template-columns: 1fr 1fr;
    gap: var(--spacing-2xl);
  }
}

@media (max-width: 640px) {
  .footer {
    padding: var(--spacing-3xl) 0 var(--spacing-xl);
  }
  
  .footer-content {
    grid-template-columns: 1fr;
    gap: var(--spacing-xl);
  }
}
</style>
