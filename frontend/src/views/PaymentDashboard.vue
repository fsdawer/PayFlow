<template>
  <div class="payment-dashboard">
    <section class="page-hero">
      <div class="container">
        <div class="hero-content animate-fade-in-up">
          <div>
            <p class="hero-kicker">결제 대시보드</p>
            <h1 class="hero-title">
              다가오는 결제를<br />
              <span class="text-gradient">한눈에 확인하세요</span>
            </h1>
            <p class="hero-subtitle">
              앞으로 7일 내 결제 예정 내역과 월별 지출 통계를 제공합니다.
            </p>
          </div>
          <div class="hero-metrics glass">
            <div class="metric">
              <p class="metric-label">총 결제 예정</p>
              <p class="metric-value">₩{{ formatNumber(upcomingTotal) }}</p>
              <p class="metric-meta">{{ upcomingCount }}건</p>
            </div>
          </div>
        </div>
      </div>
      <div class="hero-glow"></div>
    </section>

    <section class="dashboard-content">
      <div class="container">
        <div v-if="errorMessage" class="page-alert glass">
          <span>⚠ {{ errorMessage }}</span>
          <button class="alert-dismiss" @click="errorMessage = ''" aria-label="알림 닫기">닫기</button>
        </div>

        <!-- 다가오는 결제 섹션 -->
        <div class="section-card glass">
          <div class="section-header">
            <div>
              <h2 class="section-title">다가오는 결제</h2>
              <p class="section-subtitle">앞으로 {{ selectedDays }}일 내 결제 예정 목록입니다</p>
            </div>
            <div class="filters">
              <button
                v-for="filter in dayFilters"
                :key="filter"
                :class="['filter-chip', { active: selectedDays === filter }]"
                @click="selectedDays = filter; loadUpcomingPayments()"
              >
                {{ filter }}일
              </button>
            </div>
          </div>

          <div v-if="isLoading" class="loading-state">
            <div class="loading-spinner"></div>
            <p>결제 정보를 불러오는 중입니다...</p>
          </div>

          <div v-else-if="upcomingPayments.length === 0" class="empty-state">
            <h3>예정된 결제가 없습니다</h3>
            <p>{{ selectedDays }}일 내 결제 예정인 항목이 없어요.</p>
            <Button variant="outline" @click="selectedDays = 30; loadUpcomingPayments()">
              30일 보기
            </Button>
          </div>

          <div v-else class="payments-list">
            <article v-for="payment in upcomingPayments" :key="payment.cycleId" class="payment-card glass">
              <div class="payment-header">
                <div>
                  <p class="payment-category">{{ payment.subscriptionCategory || '기타' }}</p>
                  <h3 class="payment-name">{{ payment.subscriptionName }}</h3>
                </div>
                <span :class="['status-badge', statusClass(payment.status)]">
                  {{ statusLabel(payment.status) }}
                </span>
              </div>
              <div class="payment-body">
                <div class="payment-details">
                  <div class="detail-item">
                    <span class="detail-label">결제 예정일</span>
                    <span class="detail-value">{{ formatDate(payment.dueDate) }} (D-{{ daysUntil(payment.dueDate) }})</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">금액</span>
                    <span class="detail-value amount">₩{{ formatNumber(payment.subscriptionAmount) }}</span>
                  </div>
                </div>
              </div>
            </article>
          </div>
        </div>

        <!-- 월별 지출 통계 섹션 -->
        <div class="section-card glass">
          <div class="section-header">
            <div>
              <h2 class="section-title">월별 지출 분석</h2>
              <p class="section-subtitle">{{ selectedYear }}년 {{ selectedMonth }}월 결제 내역</p>
            </div>
            <div class="month-selector">
              <button class="month-nav" @click="changeMonth(-1)">←</button>
              <span class="month-display">{{ selectedYear }}.{{ String(selectedMonth).padStart(2, '0') }}</span>
              <button class="month-nav" @click="changeMonth(1)">→</button>
            </div>
          </div>

          <div v-if="historyLoading" class="loading-state">
            <div class="loading-spinner"></div>
            <p>지출 내역을 분석하는 중입니다...</p>
          </div>

          <div v-else>
            <div class="stats-grid">
              <div class="stat-card">
                <p class="stat-label">총 결제 금액</p>
                <p class="stat-value">₩{{ formatNumber(history.totalPaidAmount) }}</p>
                <p class="stat-meta">{{ history.paidCount }}건 완료</p>
              </div>
              <div class="stat-card">
                <p class="stat-label">미결제</p>
                <p class="stat-value pending">{{ history.pendingCount }}건</p>
              </div>
              <div class="stat-card">
                <p class="stat-label">연체</p>
                <p class="stat-value overdue">{{ history.overdueCount }}건</p>
              </div>
            </div>

            <div v-if="Object.keys(history.categoryExpenses || {}).length > 0" class="category-breakdown">
              <h3 class="breakdown-title">카테고리별 지출</h3>
              <div class="category-list">
                <div v-for="(amount, category) in history.categoryExpenses" :key="category" class="category-item">
                  <span class="category-name">{{ category }}</span>
                  <span class="category-amount">₩{{ formatNumber(amount) }}</span>
                </div>
              </div>
            </div>

            <div v-if="history.payments && history.payments.length > 0" class="history-list">
              <h3 class="breakdown-title">상세 내역</h3>
              <article v-for="payment in history.payments" :key="payment.cycleId" class="history-item glass">
                <div class="history-info">
                  <span class="history-service">{{ payment.subscriptionName }}</span>
                  <span class="history-date">{{ formatDate(payment.dueDate) }}</span>
                </div>
                <div class="history-amount-status">
                  <span class="history-amount">₩{{ formatNumber(payment.paidAmount || payment.subscriptionAmount) }}</span>
                  <span :class="['history-status', statusClass(payment.status)]">
                    {{ statusLabel(payment.status) }}
                  </span>
                </div>
              </article>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Button from '../components/Button.vue'

const dayFilters = [3, 7, 14, 30]
const selectedDays = ref(7)
const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)

const upcomingPayments = ref([])
const history = ref({
  payments: [],
  totalPaidAmount: 0,
  paidCount: 0,
  pendingCount: 0,
  overdueCount: 0,
  categoryExpenses: {}
})

const isLoading = ref(false)
const historyLoading = ref(false)
const errorMessage = ref('')

const upcomingCount = computed(() => upcomingPayments.value.length)
const upcomingTotal = computed(() =>
  upcomingPayments.value.reduce((sum, p) => sum + (p.subscriptionAmount || 0), 0)
)

const formatNumber = (value) => (value ?? 0).toLocaleString('ko-KR')

const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}월 ${date.getDate()}일`
}

const daysUntil = (dateStr) => {
  if (!dateStr) return 0
  const target = new Date(dateStr)
  const today = new Date()
  const diffTime = target - today
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return Math.max(0, diffDays)
}

const statusLabel = (status) => {
  if (status === 'PENDING') return '결제 대기'
  if (status === 'PAID') return '결제 완료'
  if (status === 'OVERDUE') return '연체'
  return '취소됨'
}

const statusClass = (status) => {
  if (status === 'PENDING') return 'status-pending'
  if (status === 'PAID') return 'status-paid'
  if (status === 'OVERDUE') return 'status-overdue'
  return 'status-cancelled'
}

const getAuthHeaders = () => {
  const token = localStorage.getItem('accessToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const request = async (url, options = {}) => {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
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

const loadUpcomingPayments = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const data = await request(`/api/payment-cycles/upcoming?days=${selectedDays.value}`)
    upcomingPayments.value = data.payments || []
  } catch (error) {
    errorMessage.value = error.message || '결제 정보를 불러오지 못했습니다.'
    upcomingPayments.value = []
  } finally {
    isLoading.value = false
  }
}

const loadHistory = async () => {
  historyLoading.value = true
  errorMessage.value = ''
  try {
    const data = await request(`/api/payment-cycles/history?year=${selectedYear.value}&month=${selectedMonth.value}`)
    history.value = data || {
      payments: [],
      totalPaidAmount: 0,
      paidCount: 0,
      pendingCount: 0,
      overdueCount: 0,
      categoryExpenses: {}
    }
  } catch (error) {
    errorMessage.value = error.message || '지출 내역을 불러오지 못했습니다.'
  } finally {
    historyLoading.value = false
  }
}

const changeMonth = (offset) => {
  let newMonth = selectedMonth.value + offset
  let newYear = selectedYear.value

  if (newMonth > 12) {
    newMonth = 1
    newYear++
  } else if (newMonth < 1) {
    newMonth = 12
    newYear--
  }

  selectedMonth.value = newMonth
  selectedYear.value = newYear
  loadHistory()
}

onMounted(() => {
  loadUpcomingPayments()
  loadHistory()
})
</script>

<style scoped>
.payment-dashboard {
  width: 100%;
  padding-bottom: var(--spacing-4xl);
}

.page-hero {
  position: relative;
  padding: var(--spacing-4xl) 0 var(--spacing-3xl);
  overflow: hidden;
}

.hero-content {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: var(--spacing-3xl);
  align-items: center;
}

.hero-kicker {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-md);
}

.hero-title {
  font-size: var(--font-size-5xl);
  margin-bottom: var(--spacing-lg);
}

.hero-subtitle {
  font-size: var(--font-size-lg);
  margin-bottom: var(--spacing-xl);
}

.hero-metrics {
  padding: var(--spacing-2xl);
}

.metric-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-sm);
}

.metric-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--spacing-xs);
}

.metric-meta {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.dashboard-content {
  padding: var(--spacing-3xl) 0;
}

.section-card {
  padding: var(--spacing-2xl);
  margin-bottom: var(--spacing-2xl);
  border-radius: var(--radius-lg);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
  flex-wrap: wrap;
  gap: var(--spacing-lg);
}

.section-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--spacing-xs);
}

.section-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.filters {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.filter-chip {
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text);
  cursor: pointer;
  transition: all 0.2s;
}

.filter-chip:hover {
  border-color: var(--color-primary);
}

.filter-chip.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.payments-list {
  display: grid;
  gap: var(--spacing-lg);
}

.payment-card {
  padding: var(--spacing-xl);
  border-radius: var(--radius-md);
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-md);
}

.payment-category {
  font-size: var(--font-size-xs);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-xs);
}

.payment-name {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.status-badge {
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
}

.status-pending {
  background: rgba(59, 130, 246, 0.1);
  color: rgb(59, 130, 246);
}

.status-paid {
  background: rgba(34, 197, 94, 0.1);
  color: rgb(34, 197, 94);
}

.status-overdue {
  background: rgba(239, 68, 68, 0.1);
  color: rgb(239, 68, 68);
}

.status-cancelled {
  background: rgba(156, 163, 175, 0.1);
  color: rgb(156, 163, 175);
}

.payment-details {
  display: grid;
  gap: var(--spacing-sm);
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.detail-value {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.detail-value.amount {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-2xl);
}

.stat-card {
  padding: var(--spacing-lg);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-border);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-sm);
}

.stat-value {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--spacing-xs);
}

.stat-value.pending {
  color: rgb(59, 130, 246);
}

.stat-value.overdue {
  color: rgb(239, 68, 68);
}

.stat-meta {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.month-selector {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.month-nav {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text);
  cursor: pointer;
  transition: all 0.2s;
}

.month-nav:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.month-display {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  min-width: 80px;
  text-align: center;
}

.category-breakdown {
  margin-top: var(--spacing-2xl);
  padding-top: var(--spacing-2xl);
  border-top: 1px solid var(--color-border);
}

.breakdown-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--spacing-lg);
}

.category-list {
  display: grid;
  gap: var(--spacing-sm);
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.02);
}

.category-name {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.category-amount {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
}

.history-list {
  margin-top: var(--spacing-2xl);
  padding-top: var(--spacing-2xl);
  border-top: 1px solid var(--color-border);
  display: grid;
  gap: var(--spacing-sm);
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-sm);
}

.history-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.history-service {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.history-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.history-amount-status {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.history-amount {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.history-status {
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
}

.loading-state,
.empty-state {
  text-align: center;
  padding: var(--spacing-4xl) var(--spacing-2xl);
}

loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto var(--spacing-lg);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state h3 {
  font-size: var(--font-size-lg);
  margin-bottom: var(--spacing-sm);
}

.empty-state p {
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-lg);
}

.page-alert {
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-lg);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgb(239, 68, 68);
  color: rgb(239, 68, 68);
}

.alert-dismiss {
  background: transparent;
  border: 1px solid currentColor;
  color: inherit;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--font-size-xs);
}

.hero-glow {
  position: absolute;
  top: -50%;
  left: -10%;
  width: 120%;
  height: 200%;
  background: radial-gradient(
    ellipse at center,
    rgba(var(--color-primary-rgb), 0.15) 0%,
    transparent 50%
  );
  pointer-events: none;
  z-index: -1;
}

@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
