<template>
  <div class="subscription-page">
    <section class="page-hero">
      <div class="container">
        <div class="hero-content animate-fade-in-up">
          <div>
            <p class="hero-kicker">구독 관리</p>
            <h1 class="hero-title">
              내 구독을 한눈에 정리하고<br />
              <span class="text-gradient">지출 흐름을 제어하세요</span>
            </h1>
            <p class="hero-subtitle">
              결제 주기, 상태, 알림 설정까지 한 화면에서 관리합니다.
            </p>
            <div class="hero-actions">
              <Button variant="primary" @click="openCreateModal">구독 추가</Button>
              <Button variant="outline">AI 절감 분석</Button>
            </div>
          </div>
          <div class="hero-metrics glass">
            <div class="metric">
              <p class="metric-label">이번 달 지출</p>
              <p class="metric-value">₩{{ formatNumber(monthlyTotal) }}</p>
              <p class="metric-meta">활성 {{ activeCount }}개</p>
            </div>
            <div class="metric-divider"></div>
            <div class="metric">
              <p class="metric-label">다음 결제</p>
              <p class="metric-value">{{ nextBilling.name }}</p>
              <p class="metric-meta">{{ nextBilling.date }} · ₩{{ formatNumber(nextBilling.amount) }}</p>
            </div>
          </div>
        </div>
      </div>
      <div class="hero-glow"></div>
    </section>

    <section class="summary">
      <div class="container">
        <div class="summary-grid">
          <div class="summary-card card">
            <p class="summary-label">활성 구독</p>
            <h3 class="summary-value">{{ activeCount }}개</h3>
            <p class="summary-meta">이번 달 갱신 {{ upcomingCount }}건</p>
          </div>
          <div class="summary-card card">
            <p class="summary-label">일시중지</p>
            <h3 class="summary-value">{{ pausedCount }}개</h3>
            <p class="summary-meta">복구 예정 1건</p>
          </div>
          <div class="summary-card card">
            <p class="summary-label">해지된 구독</p>
            <h3 class="summary-value">{{ canceledCount }}개</h3>
            <p class="summary-meta">지난 30일 기준</p>
          </div>
          <div class="summary-card card">
            <p class="summary-label">알림 설정</p>
            <h3 class="summary-value">{{ reminderCoverage }}%</h3>
            <p class="summary-meta">D-3 / D-1 활성화</p>
          </div>
        </div>
      </div>
    </section>

    <section class="manager">
      <div class="container">
        <div v-if="errorMessage" class="page-alert glass">
          <span>⚠ {{ errorMessage }}</span>
          <button class="alert-dismiss" @click="errorMessage = ''" aria-label="알림 닫기">닫기</button>
        </div>
        <div class="manager-header">
          <div>
            <h2 class="section-title">구독 목록</h2>
            <p class="section-subtitle">결제 주기와 상태를 빠르게 점검하세요.</p>
          </div>
          <div class="filters">
            <button
              v-for="filter in filters"
              :key="filter.value"
              :class="['filter-chip', { active: activeFilter === filter.value }]"
              @click="activeFilter = filter.value"
            >
              {{ filter.label }}
            </button>
          </div>
        </div>

        <div v-if="isLoading" class="loading-state glass">
          <div class="loading-spinner"></div>
          <p>구독 정보를 불러오는 중입니다.</p>
        </div>

        <div v-else-if="filteredSubscriptions.length === 0" class="empty-state glass">
          <h3>아직 등록된 구독이 없어요</h3>
          <p>첫 구독을 추가하고 결제 일정과 알림을 관리해보세요.</p>
          <Button variant="primary" @click="openCreateModal">구독 추가하기</Button>
        </div>

        <div v-else class="subscription-grid">
          <article v-for="item in filteredSubscriptions" :key="item.subscriptionId" class="subscription-card glass">
            <div class="subscription-header">
              <div>
                <p class="subscription-category">{{ item.subscriptionsCategory || '카테고리 미지정' }}</p>
                <h3 class="subscription-name">{{ item.subscriptionsName }}</h3>
              </div>
              <span :class="['status-badge', statusClass(item.status)]">
                {{ statusLabel(item.status) }}
              </span>
            </div>
            <div class="subscription-body">
              <div class="price">
                <span class="price-amount">{{ formatCurrency(item.amount, item.currency) }}</span>
                <span class="price-cycle">/ {{ cycleLabel(item.cycleType) }}</span>
              </div>
              <div class="details">
                <div class="detail-item">
                  <span class="detail-label">결제일</span>
                  <span class="detail-value">{{ billingLabel(item) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">알림</span>
                  <span class="detail-value">
                    {{ item.reminderD3 ? 'D-3' : '—' }} · {{ item.reminderD1 ? 'D-1' : '—' }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">메모</span>
                  <span class="detail-value">{{ item.memo || '—' }}</span>
                </div>
              </div>
            </div>
            <div class="subscription-footer">
              <Button variant="ghost" @click="openEditModal(item)">수정</Button>
              <Button
                variant="outline"
                @click="togglePause(item)"
              >
                {{ item.status === 'PAUSED' ? '재개' : '일시중지' }}
              </Button>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="form-section">
      <div class="container">
        <div class="form-card card">
          <div>
            <h2 class="section-title">새 구독 등록</h2>
            <p class="section-subtitle">폼을 입력하고 등록 버튼을 누르면 바로 반영됩니다.</p>
          </div>
          <form class="subscription-form" @submit.prevent="submitCreate">
            <Input v-model="createForm.subscriptionsName" label="구독 이름" placeholder="예: 넷플릭스" required />
            <Input v-model="createForm.subscriptionsCategory" label="카테고리" placeholder="엔터테인먼트" />
            <Input v-model.number="createForm.amount" type="number" label="금액" placeholder="0" required />
            <Input v-model="createForm.currency" label="통화" placeholder="KRW" />

            <div class="field-group">
              <label class="field-label">결제 주기</label>
              <div class="field-options">
                <button
                  v-for="cycle in cycleOptions"
                  :key="cycle.value"
                  type="button"
                  :class="['option-chip', { active: createForm.cycleType === cycle.value }]"
                  @click="createForm.cycleType = cycle.value"
                >
                  {{ cycle.label }}
                </button>
              </div>
            </div>

            <div class="field-grid">
              <Input
                v-if="createForm.cycleType === 'MONTHLY'"
                v-model.number="createForm.billingDay"
                type="number"
                label="매월 결제일"
                placeholder="1-31"
              />
              <Input
                v-if="createForm.cycleType === 'WEEKLY'"
                v-model.number="createForm.billingWeekday"
                type="number"
                label="결제 요일 (0=일요일)"
                placeholder="0-6"
              />
              <Input
                v-if="createForm.cycleType === 'YEARLY'"
                v-model.number="createForm.billingMonth"
                type="number"
                label="결제 월"
                placeholder="1-12"
              />
              <Input
                v-if="createForm.cycleType === 'YEARLY'"
                v-model.number="createForm.billingDate"
                type="number"
                label="결제 일"
                placeholder="1-31"
              />
            </div>

            <div class="toggle-row">
              <label class="toggle">
                <input type="checkbox" v-model="createForm.reminderD3" />
                <span>결제 3일 전 알림</span>
              </label>
              <label class="toggle">
                <input type="checkbox" v-model="createForm.reminderD1" />
                <span>결제 1일 전 알림</span>
              </label>
            </div>

            <Input v-model="createForm.memo" label="메모" placeholder="예: 가족 공유 중" />

            <div class="form-actions">
              <Button variant="ghost" type="button" @click="resetCreateForm">초기화</Button>
              <Button variant="primary" type="submit" :loading="isSubmitting">
                등록하기
              </Button>
            </div>
          </form>
        </div>
      </div>
    </section>

    <div v-if="isCreateOpen" class="modal-overlay" @click.self="closeCreateModal">
      <div class="modal glass-strong">
        <div class="modal-header">
          <div>
            <h3>구독 추가</h3>
            <p>필수 정보를 입력하고 저장하세요.</p>
          </div>
          <button class="modal-close" @click="closeCreateModal">닫기</button>
        </div>
        <form class="modal-form" @submit.prevent="submitCreate">
          <Input v-model="createForm.subscriptionsName" label="구독 이름" placeholder="예: 넷플릭스" required />
          <Input v-model="createForm.subscriptionsCategory" label="카테고리" placeholder="엔터테인먼트" />
          <Input v-model.number="createForm.amount" type="number" label="금액" placeholder="0" required />
          <Input v-model="createForm.currency" label="통화" placeholder="KRW" />
          <div class="field-group">
            <label class="field-label">결제 주기</label>
            <div class="field-options">
              <button
                v-for="cycle in cycleOptions"
                :key="cycle.value"
                type="button"
                :class="['option-chip', { active: createForm.cycleType === cycle.value }]"
                @click="createForm.cycleType = cycle.value"
              >
                {{ cycle.label }}
              </button>
            </div>
          </div>
          <div class="field-grid">
            <Input
              v-if="createForm.cycleType === 'MONTHLY'"
              v-model.number="createForm.billingDay"
              type="number"
              label="매월 결제일"
              placeholder="1-31"
            />
            <Input
              v-if="createForm.cycleType === 'WEEKLY'"
              v-model.number="createForm.billingWeekday"
              type="number"
              label="결제 요일 (0=일요일)"
              placeholder="0-6"
            />
            <Input
              v-if="createForm.cycleType === 'YEARLY'"
              v-model.number="createForm.billingMonth"
              type="number"
              label="결제 월"
              placeholder="1-12"
            />
            <Input
              v-if="createForm.cycleType === 'YEARLY'"
              v-model.number="createForm.billingDate"
              type="number"
              label="결제 일"
              placeholder="1-31"
            />
          </div>
          <div class="toggle-row">
            <label class="toggle">
              <input type="checkbox" v-model="createForm.reminderD3" />
              <span>결제 3일 전 알림</span>
            </label>
            <label class="toggle">
              <input type="checkbox" v-model="createForm.reminderD1" />
              <span>결제 1일 전 알림</span>
            </label>
          </div>
          <Input v-model="createForm.memo" label="메모" placeholder="예: 가족 공유 중" />
          <div class="modal-actions">
            <Button variant="ghost" type="button" @click="closeCreateModal">취소</Button>
            <Button variant="primary" type="submit" :loading="isSubmitting">등록</Button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="isEditOpen" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal glass-strong">
        <div class="modal-header">
          <div>
            <h3>구독 수정</h3>
            <p>{{ editForm.subscriptionsName }} 정보를 업데이트합니다.</p>
          </div>
          <button class="modal-close" @click="closeEditModal">닫기</button>
        </div>
        <form class="modal-form" @submit.prevent="submitEdit">
          <Input v-model="editForm.subscriptionsName" label="구독 이름" placeholder="예: 넷플릭스" />
          <Input v-model="editForm.subscriptionsCategory" label="카테고리" placeholder="엔터테인먼트" />
          <Input v-model.number="editForm.amount" type="number" label="금액" placeholder="0" />
          <Input v-model="editForm.currency" label="통화" placeholder="KRW" />
          <div class="field-group">
            <label class="field-label">결제 주기</label>
            <div class="field-options">
              <button
                v-for="cycle in cycleOptions"
                :key="cycle.value"
                type="button"
                :class="['option-chip', { active: editForm.cycleType === cycle.value }]"
                @click="editForm.cycleType = cycle.value"
              >
                {{ cycle.label }}
              </button>
            </div>
          </div>
          <div class="field-grid">
            <Input
              v-if="editForm.cycleType === 'MONTHLY'"
              v-model.number="editForm.billingDay"
              type="number"
              label="매월 결제일"
              placeholder="1-31"
            />
            <Input
              v-if="editForm.cycleType === 'WEEKLY'"
              v-model.number="editForm.billingWeekday"
              type="number"
              label="결제 요일 (0=일요일)"
              placeholder="0-6"
            />
            <Input
              v-if="editForm.cycleType === 'YEARLY'"
              v-model.number="editForm.billingMonth"
              type="number"
              label="결제 월"
              placeholder="1-12"
            />
            <Input
              v-if="editForm.cycleType === 'YEARLY'"
              v-model.number="editForm.billingDate"
              type="number"
              label="결제 일"
              placeholder="1-31"
            />
          </div>
          <div class="toggle-row">
            <label class="toggle">
              <input type="checkbox" v-model="editForm.reminderD3" />
              <span>결제 3일 전 알림</span>
            </label>
            <label class="toggle">
              <input type="checkbox" v-model="editForm.reminderD1" />
              <span>결제 1일 전 알림</span>
            </label>
          </div>
          <div class="field-group">
            <label class="field-label">상태</label>
            <div class="field-options">
              <button
                v-for="status in statusOptions"
                :key="status.value"
                type="button"
                :class="['option-chip', { active: editForm.status === status.value }]"
                @click="editForm.status = status.value"
              >
                {{ status.label }}
              </button>
            </div>
          </div>
          <Input v-model="editForm.memo" label="메모" placeholder="예: 가족 공유 중" />
          <div class="modal-actions">
            <Button variant="ghost" type="button" @click="closeEditModal">취소</Button>
            <Button variant="primary" type="submit" :loading="isSubmitting">저장</Button>
          </div>
        </form>
      </div>
    </div>

    <section class="insights">
      <div class="container">
        <div class="insight-card glass-strong">
          <div>
            <h3 class="insight-title">AI 인사이트</h3>
            <p class="insight-text">
              중복된 구독 2건을 발견했습니다. 합치면 월 ₩19,800을 절감할 수 있어요.
            </p>
            <div class="insight-actions">
              <Button variant="primary">절감 플랜 보기</Button>
              <Button variant="ghost">나중에</Button>
            </div>
          </div>
          <div class="insight-list">
            <div class="insight-item">
              <span>스트리밍 A · 스트리밍 B</span>
              <span class="insight-tag">중복</span>
            </div>
            <div class="insight-item">
              <span>운동 앱 프리미엄</span>
              <span class="insight-tag warning">미사용</span>
            </div>
            <div class="insight-item">
              <span>클라우드 저장소</span>
              <span class="insight-tag">대안 있음</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'

const filters = [
  { label: '전체', value: 'all' },
  { label: '활성', value: 'ACTIVE' },
  { label: '일시중지', value: 'PAUSED' },
  { label: '해지', value: 'CANCELED' }
]

const activeFilter = ref('all')

const subscriptions = ref([])
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const isCreateOpen = ref(false)
const isEditOpen = ref(false)
const editingId = ref(null)

const cycleOptions = [
  { label: '월간', value: 'MONTHLY' },
  { label: '주간', value: 'WEEKLY' },
  { label: '연간', value: 'YEARLY' }
]

const statusOptions = [
  { label: '활성', value: 'ACTIVE' },
  { label: '일시중지', value: 'PAUSED' },
  { label: '해지', value: 'CANCELED' }
]

const createForm = reactive({
  subscriptionsName: '',
  subscriptionsCategory: '',
  amount: null,
  currency: 'KRW',
  cycleType: 'MONTHLY',
  billingDay: null,
  billingWeekday: null,
  billingMonth: null,
  billingDate: null,
  reminderD3: true,
  reminderD1: true,
  memo: ''
})

const editForm = reactive({
  subscriptionsName: '',
  subscriptionsCategory: '',
  amount: null,
  currency: 'KRW',
  cycleType: 'MONTHLY',
  billingDay: null,
  billingWeekday: null,
  billingMonth: null,
  billingDate: null,
  reminderD3: true,
  reminderD1: true,
  status: 'ACTIVE',
  memo: ''
})

const filteredSubscriptions = computed(() => {
  if (activeFilter.value === 'all') {
    return subscriptions.value
  }
  return subscriptions.value.filter((item) => item.status === activeFilter.value)
})

const activeCount = computed(() => subscriptions.value.filter((item) => item.status === 'ACTIVE').length)
const pausedCount = computed(() => subscriptions.value.filter((item) => item.status === 'PAUSED').length)
const canceledCount = computed(() => subscriptions.value.filter((item) => item.status === 'CANCELED').length)
const upcomingCount = computed(() => subscriptions.value.filter((item) => item.status === 'ACTIVE').length)

const monthlyTotal = computed(() =>
  subscriptions.value
    .filter((item) => item.status === 'ACTIVE' && item.cycleType === 'MONTHLY')
    .reduce((sum, item) => sum + item.amount, 0)
)

const reminderCoverage = computed(() => {
  if (subscriptions.value.length === 0) return 0
  const reminders = subscriptions.value.filter((item) => item.reminderD3 || item.reminderD1).length
  return Math.round((reminders / subscriptions.value.length) * 100)
})

const nextBilling = computed(() => ({
  name: subscriptions.value[0]?.subscriptionsName || '—',
  date: '예정일 계산 필요',
  amount: subscriptions.value[0]?.amount || 0
}))

const formatNumber = (value) => (value ?? 0).toLocaleString('ko-KR')

const formatCurrency = (amount, currency) => {
  const value = amount ?? 0
  if (currency && currency !== 'KRW') {
    return `${currency} ${value.toLocaleString('ko-KR')}`
  }
  return `₩${value.toLocaleString('ko-KR')}`
}

const statusLabel = (status) => {
  if (status === 'ACTIVE') return '활성'
  if (status === 'PAUSED') return '일시중지'
  return '해지'
}

const statusClass = (status) => {
  if (status === 'ACTIVE') return 'status-active'
  if (status === 'PAUSED') return 'status-paused'
  return 'status-canceled'
}

const cycleLabel = (cycle) => {
  if (cycle === 'MONTHLY') return '월'
  if (cycle === 'YEARLY') return '년'
  return '주'
}

const billingLabel = (item) => {
  if (item.cycleType === 'WEEKLY') {
    return `매주 ${weekdayLabel(item.billingWeekday)}`
  }
  if (item.cycleType === 'YEARLY') {
    return `${item.billingMonth}월 ${item.billingDate}일`
  }
  return `매월 ${item.billingDay}일`
}

const weekdayLabel = (value) => {
  const labels = ['일', '월', '화', '수', '목', '금', '토']
  return labels[value] || '월'
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

const toNumberOrNull = (value) => {
  if (value === null || value === undefined || value === '') return null
  const num = Number(value)
  return Number.isNaN(num) ? null : num
}

const loadSubscriptions = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const data = await request('/api/subscriptions')
    subscriptions.value = Array.isArray(data) ? data : []
  } catch (error) {
    errorMessage.value = error.message || '구독 정보를 불러오지 못했습니다.'
  } finally {
    isLoading.value = false
  }
}

const submitCreate = async () => {
  if (!createForm.subscriptionsName || toNumberOrNull(createForm.amount) === null) {
    errorMessage.value = '구독 이름과 금액은 필수입니다.'
    return
  }
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    const payload = {
      ...createForm,
      amount: toNumberOrNull(createForm.amount),
      billingDay: toNumberOrNull(createForm.billingDay),
      billingWeekday: toNumberOrNull(createForm.billingWeekday),
      billingMonth: toNumberOrNull(createForm.billingMonth),
      billingDate: toNumberOrNull(createForm.billingDate)
    }
    await request('/api/subscriptions', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    closeCreateModal()
    resetCreateForm()
    await loadSubscriptions()
  } catch (error) {
    errorMessage.value = error.message || '구독 등록에 실패했습니다.'
  } finally {
    isSubmitting.value = false
  }
}

const submitEdit = async () => {
  if (!editingId.value) return
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    const payload = {
      ...editForm,
      amount: toNumberOrNull(editForm.amount),
      billingDay: toNumberOrNull(editForm.billingDay),
      billingWeekday: toNumberOrNull(editForm.billingWeekday),
      billingMonth: toNumberOrNull(editForm.billingMonth),
      billingDate: toNumberOrNull(editForm.billingDate)
    }
    await request(`/api/subscriptions/${editingId.value}`, {
      method: 'PATCH',
      body: JSON.stringify(payload)
    })
    closeEditModal()
    await loadSubscriptions()
  } catch (error) {
    errorMessage.value = error.message || '구독 수정에 실패했습니다.'
  } finally {
    isSubmitting.value = false
  }
}

const togglePause = async (item) => {
  if (!item || !item.subscriptionId) return
  const nextStatus = item.status === 'PAUSED' ? 'ACTIVE' : 'PAUSED'
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    await request(`/api/subscriptions/${item.subscriptionId}`, {
      method: 'PATCH',
      body: JSON.stringify({ status: nextStatus })
    })
    await loadSubscriptions()
  } catch (error) {
    errorMessage.value = error.message || '상태 변경에 실패했습니다.'
  } finally {
    isSubmitting.value = false
  }
}

const openCreateModal = () => {
  isCreateOpen.value = true
}

const closeCreateModal = () => {
  isCreateOpen.value = false
}

const openEditModal = (item) => {
  if (!item) return
  editingId.value = item.subscriptionId
  Object.assign(editForm, {
    subscriptionsName: item.subscriptionsName || '',
    subscriptionsCategory: item.subscriptionsCategory || '',
    amount: item.amount ?? null,
    currency: item.currency || 'KRW',
    cycleType: item.cycleType || 'MONTHLY',
    billingDay: item.billingDay ?? null,
    billingWeekday: item.billingWeekday ?? null,
    billingMonth: item.billingMonth ?? null,
    billingDate: item.billingDate ?? null,
    reminderD3: item.reminderD3 ?? true,
    reminderD1: item.reminderD1 ?? true,
    status: item.status || 'ACTIVE',
    memo: item.memo || ''
  })
  isEditOpen.value = true
}

const closeEditModal = () => {
  isEditOpen.value = false
  editingId.value = null
}

const resetCreateForm = () => {
  Object.assign(createForm, {
    subscriptionsName: '',
    subscriptionsCategory: '',
    amount: null,
    currency: 'KRW',
    cycleType: 'MONTHLY',
    billingDay: null,
    billingWeekday: null,
    billingMonth: null,
    billingDate: null,
    reminderD3: true,
    reminderD1: true,
    memo: ''
  })
}

onMounted(() => {
  loadSubscriptions()
})
</script>

<style scoped>
.subscription-page {
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

.hero-actions {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.hero-metrics {
  padding: var(--spacing-2xl);
  display: grid;
  gap: var(--spacing-xl);
}

.metric-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-sm);
}

.metric-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
}

.metric-meta {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.metric-divider {
  height: 1px;
  background: var(--color-border);
}

.hero-glow {
  position: absolute;
  top: -20%;
  right: -10%;
  width: 420px;
  height: 420px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.4), transparent 70%);
  filter: blur(12px);
  z-index: -1;
}

.summary {
  padding: var(--spacing-2xl) 0;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--spacing-lg);
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.summary-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.summary-value {
  font-size: var(--font-size-3xl);
}

.summary-meta {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.manager {
  padding: var(--spacing-3xl) 0;
}

.page-alert {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
  color: var(--color-text-primary);
}

.alert-dismiss {
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: var(--font-size-sm);
}

.loading-state,
.empty-state {
  padding: var(--spacing-2xl);
  text-align: center;
  display: grid;
  gap: var(--spacing-md);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 3px solid rgba(99, 102, 241, 0.2);
  border-top-color: var(--color-primary);
  margin: 0 auto;
  animation: spin 0.8s linear infinite;
}

.manager-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--spacing-xl);
  flex-wrap: wrap;
  margin-bottom: var(--spacing-xl);
}

.section-title {
  font-size: var(--font-size-3xl);
  margin-bottom: var(--spacing-sm);
}

.section-subtitle {
  color: var(--color-text-muted);
}

.filters {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.filter-chip {
  border-radius: var(--radius-full);
  padding: 0.5rem 1.25rem;
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-base);
  font-size: var(--font-size-sm);
}

.filter-chip.active,
.filter-chip:hover {
  border-color: var(--color-primary);
  color: var(--color-text-primary);
  box-shadow: var(--shadow-glow);
}

.subscription-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--spacing-lg);
}

.subscription-card {
  padding: var(--spacing-xl);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  min-height: 300px;
}

.subscription-header {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-md);
}

.subscription-category {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  margin-bottom: var(--spacing-xs);
}

.subscription-name {
  font-size: var(--font-size-xl);
}

.status-badge {
  align-self: flex-start;
  padding: 0.35rem 0.75rem;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  text-transform: uppercase;
}

.status-active {
  background: rgba(16, 185, 129, 0.2);
  color: var(--color-success);
}

.status-paused {
  background: rgba(245, 158, 11, 0.2);
  color: var(--color-warning);
}

.status-canceled {
  background: rgba(239, 68, 68, 0.2);
  color: var(--color-error);
}

.subscription-body {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  flex: 1;
}

.price {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-sm);
}

.price-amount {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
}

.price-cycle {
  color: var(--color-text-muted);
}

.details {
  display: grid;
  gap: var(--spacing-md);
}

.detail-item {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.detail-label {
  color: var(--color-text-muted);
}

.detail-value {
  text-align: right;
}

.subscription-footer {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
  margin-top: auto;
}

.form-section {
  padding: var(--spacing-3xl) 0;
}

.form-card {
  display: grid;
  gap: var(--spacing-xl);
}

.subscription-form {
  display: grid;
  gap: var(--spacing-lg);
}

.field-group {
  display: grid;
  gap: var(--spacing-sm);
}

.field-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.field-options {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.option-chip {
  border-radius: var(--radius-full);
  padding: 0.5rem 1.25rem;
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-base);
  font-size: var(--font-size-sm);
}

.option-chip.active,
.option-chip:hover {
  border-color: var(--color-primary);
  color: var(--color-text-primary);
  box-shadow: var(--shadow-glow);
}

.field-grid {
  display: grid;
  gap: var(--spacing-lg);
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.toggle-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
}

.toggle {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.toggle input {
  accent-color: var(--color-primary);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.insights {
  padding: var(--spacing-3xl) 0 var(--spacing-4xl);
}

.insight-card {
  padding: var(--spacing-3xl);
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: var(--spacing-2xl);
  align-items: center;
}

.insight-title {
  font-size: var(--font-size-2xl);
  margin-bottom: var(--spacing-md);
}

.insight-text {
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xl);
}

.insight-actions {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.insight-list {
  display: grid;
  gap: var(--spacing-sm);
}

.insight-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-radius: var(--radius-lg);
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid var(--color-border);
}

.insight-tag {
  font-size: var(--font-size-xs);
  padding: 0.2rem 0.6rem;
  border-radius: var(--radius-full);
  background: rgba(99, 102, 241, 0.2);
  color: var(--color-primary-light);
}

.insight-tag.warning {
  background: rgba(245, 158, 11, 0.2);
  color: var(--color-warning);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(5, 7, 20, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
  z-index: var(--z-modal);
}

.modal {
  width: min(720px, 100%);
  max-height: 90vh;
  overflow-y: auto;
  padding: var(--spacing-2xl);
  display: grid;
  gap: var(--spacing-xl);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--spacing-lg);
}

.modal-header p {
  margin-bottom: 0;
  color: var(--color-text-muted);
}

.modal-close {
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  cursor: pointer;
}

.modal-form {
  display: grid;
  gap: var(--spacing-lg);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1024px) {
  .hero-content {
    grid-template-columns: 1fr;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .subscription-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .insight-card {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .hero-title {
    font-size: var(--font-size-4xl);
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .subscription-grid {
    grid-template-columns: 1fr;
  }

  .detail-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-value {
    text-align: left;
  }
}
</style>
