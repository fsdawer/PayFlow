<template>
  <div class="subscription-page">
    <section class="summary animate-fade-in-up">
      <div class="container">
        <div class="header-inline">
          <h1 class="page-title">내 구독 관리</h1>
        </div>
        <div class="summary-grid">
          <div class="summary-card card">
            <p class="summary-label">활성 구독</p>
            <h3 class="summary-value">{{ activeCount }}개</h3>
            <p class="summary-meta">총 월 ₩{{ formatNumber(monthlyTotal) }}</p>
          </div>
          <div class="summary-card card">
            <p class="summary-label">일시중지</p>
            <h3 class="summary-value">{{ pausedCount }}개</h3>
          </div>
        </div>
      </div>
    </section>

    <section class="manager">
      <div class="container">
        <!-- AI Insights Section (Moved Up) -->
        <div v-if="isAILoading" class="insight-loading glass">
          <div class="loading-spinner"></div>
          <p>AI가 지출 패턴을 분석 중입니다...</p>
        </div>

        <div v-else-if="!aiInsights" class="insights-container animate-fade-in">
          <div class="insight-card glass-strong" style="display: flex; flex-direction: column; align-items: center; text-align: center; padding: 2rem;">
            <div class="insight-header">
              <h3 class="insight-title">🤖 AI 지출 분석이 아직 생성되지 않았습니다</h3>
            </div>
            <p class="insight-text" style="margin-bottom: 1.5rem;">구독 내역을 분석하여 중복 지출과 절감 포인트를 찾아드립니다.</p>
            <Button variant="primary" @click="loadAIInsights">지금 분석 시작하기</Button>
          </div>
        </div>

        <div v-else-if="aiInsights" class="insights-container animate-fade-in">
          <div class="insight-card glass-strong">
            <div class="insight-main">
              <div class="insight-header">
                <h3 class="insight-title">🤖 {{ aiInsights.title || 'AI 지출 분석 리포트' }}</h3>
                <div class="insight-meta">
                  <span class="insight-badge" v-if="aiInsights.totalSubscriptions > 0">분석 완료</span>
                  <span v-if="aiInsights.confidence" class="confidence-tag">신뢰도 {{ Math.round(aiInsights.confidence * 100) }}%</span>
                  <span v-if="aiInsights.severity === 'high'" class="severity-tag high">주의 필요</span>
                </div>
              </div>
              <p class="insight-text">{{ aiInsights.summary }}</p>
              <div class="insight-actions" v-if="aiInsights.recommendations && aiInsights.recommendations.length > 0">
                <Button variant="primary" size="sm" @click="showInsightDetails = !showInsightDetails">
                  {{ showInsightDetails ? '접기' : '상세 지출 분석' }}
                </Button>
                <Button variant="ghost" size="sm" @click="loadAIInsights">새로고침</Button>
              </div>
            </div>
            
            <div class="insight-sidebar" v-if="aiInsights.duplicates && aiInsights.duplicates.length > 0">
              <h4 class="sidebar-title">중복 구독 발견</h4>
              <div v-for="(dup, idx) in aiInsights.duplicates" :key="idx" class="insight-item">
                <div class="dup-info">
                  <span class="dup-names">{{ dup.subscriptions.join(' · ') }}</span>
                  <span class="insight-tag">{{ dup.category }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="showInsightDetails && aiInsights.recommendations" class="recommendations-area animate-fade-in">
            <h4 class="area-title">💰 비용 절감 추천</h4>
            <div class="recommendations-grid">
              <div v-for="(rec, idx) in aiInsights.recommendations" :key="idx" class="recommendation-item glass">
                <div class="rec-icon">✨</div>
                <div class="rec-content">
                  <strong class="rec-title">{{ rec.title }}</strong>
                  <p class="rec-desc">{{ rec.description }}</p>
                </div>
                <div class="rec-savings" v-if="rec.estimatedSavings > 0">
                  <span class="savings-label">예상 절감</span>
                  <span class="savings-amount">-₩{{ formatNumber(rec.estimatedSavings) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="errorMessage" class="page-alert glass">
          <span>⚠ {{ errorMessage }}</span>
          <button class="alert-dismiss" @click="errorMessage = ''" aria-label="알림 닫기">닫기</button>
        </div>
        <div class="manager-header">
          <div class="header-left">
            <h2 class="section-title">구독 목록</h2>
            <p class="section-subtitle">결제 주기와 상태를 빠르게 점검하세요.</p>
          </div>
          <div class="header-right">
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
            <Button class="add-subscription-btn" variant="primary" @click="openCreateModal">
              구독 추가
            </Button>
          </div>
        </div>

        <div v-if="isLoading" class="loading-state glass">
          <div class="loading-spinner"></div>
          <p>구독 정보를 불러오는 중입니다.</p>
        </div>

        <div v-else-if="filteredSubscriptions.length === 0" class="empty-state card animate-fade-in">
          <template v-if="activeFilter === 'PAUSED'">
            <h3>일시중지 중인 구독이 없어요!</h3>
            <p>다시 사용하고 싶지 않은 구독은 일시중지하여 관리할 수 있습니다.</p>
          </template>
          <template v-else-if="activeFilter === 'ACTIVE'">
            <h3>현재 이용 중인 구독이 없어요</h3>
            <p>새로운 구독을 추가하여 서비스를 관리해 보세요.</p>
          </template>
          <template v-else>
            <h3>아직 등록된 구독이 없어요</h3>
            <p>첫 구독을 추가하고 결제 일정과 알림을 관리해보세요.</p>
            <div style="margin-top: 20px;">
              <Button class="add-subscription-btn" variant="primary" @click="openCreateModal">구독 추가하기</Button>
            </div>
          </template>
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
                <span class="price-amount">₩{{ formatNumber(item.amount) }}</span>
                <span class="price-cycle">/ {{ cycleLabel(item.cycleType) }}</span>
              </div>
              <div class="details">
                <div class="detail-item">
                  <span class="detail-label">결제일</span>
                  <span class="detail-value">{{ billingLabel(item) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">결제수단</span>
                  <span class="detail-value text-accent">{{ item.bankName || '—' }}</span>
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
              
              <!-- 활성 상태일 때: 일시중지 가능 -->
              <template v-if="item.status === 'ACTIVE'">
                <Button variant="ghost" @click="updateStatus(item, 'PAUSED')">일시중지</Button>
              </template>
              
              <!-- 일시중지 상태일 때: 재개 가능 -->
              <template v-else-if="item.status === 'PAUSED'">
                <Button variant="primary" @click="updateStatus(item, 'ACTIVE')">재개</Button>
              </template>

              <Button variant="ghost" @click="deleteSubscription(item)">삭제</Button>
            </div>
          </article>
        </div>
      </div>
    </section>


    <!-- 모달 구독 추가 폼 -->

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
          <Input v-model.number="createForm.amount" type="number" label="금액 (원)" placeholder="14500" required />
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
          <div class="field-group">
            <label class="field-label">결제 은행/카드</label>
            <select v-model="createForm.bankName" class="modal-input">
              <option value="">은행/카드 선택 (선택사항)</option>
              <option v-for="bank in bankOptions" :key="bank" :value="bank">{{ bank }}</option>
            </select>
          </div>
          <Input v-model="createForm.memo" label="메모" placeholder="예: 가족 공유 중" />
          <div class="modal-actions">
            <Button variant="ghost" type="button" @click="closeCreateModal">취소</Button>
            <Button class="add-subscription-btn" variant="primary" type="submit" :loading="isSubmitting">등록</Button>
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
          <div class="field-group">
            <label class="field-label">결제 은행/카드</label>
            <select v-model="editForm.bankName" class="modal-input">
              <option value="">은행/카드 선택 (선택사항)</option>
              <option v-for="bank in bankOptions" :key="bank" :value="bank">{{ bank }}</option>
            </select>
          </div>
          <Input v-model="editForm.memo" label="메모" placeholder="예: 가족 공유 중" />
          <div class="modal-actions">
            <Button variant="ghost" class="btn-danger-text" type="button" @click="handleEditDelete">기록 삭제</Button>
            <div style="flex: 1"></div>
            <Button variant="ghost" type="button" @click="closeEditModal">취소</Button>
            <Button class="add-subscription-btn" variant="primary" type="submit" :loading="isSubmitting">저장</Button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'

const filters = [
  { label: '전체', value: 'all' },
  { label: '활성', value: 'ACTIVE' },
  { label: '일시중지', value: 'PAUSED' }
]

const activeFilter = ref('all')

const subscriptions = ref([])
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const isCreateOpen = ref(false)
const isEditOpen = ref(false)
const editingId = ref(null)
const aiInsights = ref(null)
const isAILoading = ref(false)
const showInsightDetails = ref(false)

const cycleOptions = [
  { label: '월간', value: 'MONTHLY' },
  { label: '주간', value: 'WEEKLY' },
  { label: '연간', value: 'YEARLY' }
]

const statusOptions = [
  { label: '활성', value: 'ACTIVE' },
  { label: '일시중지', value: 'PAUSED' }
]

const bankOptions = [
  '국민은행', '신한은행', '우리은행', '하나은행', '농협은행', '기업은행', 
  '카카오뱅크', '토스뱅크', '현대카드', '삼성카드', '비씨카드', '롯데카드'
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
  bankName: '',
  memo: ''
})

const editForm = reactive({
  subscriptionsName: '',
  subscriptionsCategory: '',
  amount: null,
  cycleType: 'MONTHLY',
  billingDay: null,
  billingWeekday: null,
  billingMonth: null,
  billingDate: null,
  reminderD3: true,
  reminderD1: true,
  status: 'ACTIVE',
  bankName: '',
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


const statusLabel = (status) => {
  if (status === 'ACTIVE') return '활성'
  return '일시중지'
}

const statusClass = (status) => {
  if (status === 'ACTIVE') return 'status-active'
  return 'status-paused'
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
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
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
    const data = await request('http://localhost:8080/api/subscriptions')
    subscriptions.value = Array.isArray(data) ? data : []
  } catch (error) {
    errorMessage.value = error.message || '구독 정보를 불러오지 못했습니다.'
  } finally {
    isLoading.value = false
  }
}

const submitCreate = async () => {
  if (!createForm.subscriptionsName || !createForm.amount) {
    errorMessage.value = '구독 이름과 금액은 필수입니다.'
    return
  }
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    const payload = {
      ...createForm,
      currency: 'KRW',  // 통화 기본값
      amount: toNumberOrNull(createForm.amount),
      bankName: createForm.bankName || null,
      billingDay: toNumberOrNull(createForm.billingDay),
      billingWeekday: toNumberOrNull(createForm.billingWeekday),
      billingMonth: toNumberOrNull(createForm.billingMonth),
      billingDate: toNumberOrNull(createForm.billingDate)
    }
    
    console.log('📝 구독 생성 요청:', payload)
    
    await request('http://localhost:8080/api/subscriptions', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    
    console.log('✅ 구독 생성 성공')
    
    closeCreateModal()
    resetCreateForm()
    await loadSubscriptions()
    
    console.log('✅ 구독 목록 새로고침 완료')
  } catch (error) {
    console.error('❌ 구독 생성 실패:', error)
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
      bankName: editForm.bankName || null,
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

const updateStatus = async (item, nextStatus) => {
  if (!item || !item.subscriptionId) return
  
  let confirmMsg = ''
  if (nextStatus === 'PAUSED') confirmMsg = '이 구독을 일시중지하시겠습니까?'
  
  if (confirmMsg && !confirm(confirmMsg)) return

  isSubmitting.value = true
  errorMessage.value = ''
  try {
    await request(`http://localhost:8080/api/subscriptions/${item.subscriptionId}`, {
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

const deleteSubscription = async (item) => {
  if (!item || !item.subscriptionId) return
  if (!confirm('정말 이 구독 기록을 완전히 삭제하시겠습니까? 모든 지출 내역 데이터가 사라지며 복구할 수 없습니다.')) return
  
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    await request(`http://localhost:8080/api/subscriptions/${item.subscriptionId}`, {
      method: 'DELETE'
    })
    await loadSubscriptions()
  } catch (error) {
    errorMessage.value = error.message || '삭제에 실패했습니다.'
  } finally {
    isSubmitting.value = false
  }
}

const handleEditDelete = async () => {
  if (!editingId.value) return
  if (confirm('정말 이 구독 기록을 완전히 삭제하시겠습니까?')) {
    await deleteSubscription({ subscriptionId: editingId.value })
    closeEditModal()
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
    cycleType: item.cycleType || 'MONTHLY',
    billingDay: item.billingDay ?? null,
    billingWeekday: item.billingWeekday ?? null,
    billingMonth: item.billingMonth ?? null,
    billingDate: item.billingDate ?? null,
    reminderD3: item.reminderD3 ?? true,
    reminderD1: item.reminderD1 ?? true,
    status: item.status || 'ACTIVE',
    bankName: item.bankName || '',
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
    cycleType: 'MONTHLY',
    billingDay: null,
    billingWeekday: null,
    billingMonth: null,
    billingDate: null,
    reminderD3: true,
    reminderD1: true,
    bankName: '',
    memo: ''
  })
}

const loadAIInsights = async () => {
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    if (!token) return

    isAILoading.value = true
    const response = await fetch('/api/ai/insights', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.ok) {
      aiInsights.value = await response.json()
      console.log('🤖 AI 인사이트 로드:', aiInsights.value)
    } else {
      console.error('AI 인사이트 로드 실패 응답:', response.status)
    }
  } catch (error) {
    console.error('AI 인사이트 로드 에러:', error)
  } finally {
    isAILoading.value = false
  }
}

onMounted(() => {
  loadSubscriptions()
  loadAIInsights()  // AI 인사이트도 함께 로드
})
</script>

<style scoped>
.subscription-page {
  width: 100%;
  padding-bottom: var(--spacing-4xl);
}

.header-inline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
}

.page-title {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-extrabold);
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
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-xl);
  flex-wrap: wrap;
  margin-bottom: var(--spacing-xl);
  background: transparent;
  padding: var(--spacing-md) 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
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
}

:deep(.add-subscription-btn.btn-primary) {
  background: linear-gradient(135deg, #10b981 0%, #06b6d4 100%);
  color: var(--color-text-primary);
  box-shadow: 0 10px 20px rgba(16, 185, 129, 0.25);
}

:deep(.add-subscription-btn.btn-primary:hover:not(:disabled)) {
  transform: translateY(-2px);
  box-shadow: 0 18px 28px rgba(6, 182, 212, 0.3);
}

:deep(.add-subscription-btn.btn-primary:active:not(:disabled)) {
  transform: translateY(0);
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
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
}

.status-canceled {
  background: rgba(239, 68, 68, 0.1);
  color: var(--color-error);
  border: 1px solid rgba(239, 68, 68, 0.2);
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
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-bg-tertiary);
}

.btn-danger-text {
  color: var(--color-error) !important;
}

.btn-danger-text:hover {
  background: rgba(239, 68, 68, 0.05) !important;
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

.modal-input,
.modal-select {
  width: 100%;
  padding: 12px 16px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
  transition: all var(--transition-base);
}

.modal-input:focus,
.modal-select:focus {
  outline: none;
  border-color: var(--color-primary);
  background: var(--color-bg);
  box-shadow: 0 0 0 4px rgba(var(--color-primary-rgb), 0.1);
}

select.modal-input {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%23475569'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 16px center;
  background-size: 20px;
  padding-right: 48px;
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

/* AI Insights Section */
.insight-loading {
  padding: var(--spacing-4xl);
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-3xl);
}

.insights-container {
  margin-bottom: var(--spacing-4xl);
}

.insight-card {
  padding: var(--spacing-3xl);
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: var(--spacing-3xl);
  align-items: start;
}

.insight-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.insight-meta {
  display: flex;
  gap: var(--spacing-sm);
  align-items: center;
}

.confidence-tag {
  font-size: 0.7rem;
  color: var(--color-text-muted);
  background: rgba(255, 255, 255, 0.05);
  padding: 0.1rem 0.6rem;
  border-radius: var(--radius-full);
  border: 1px solid var(--color-border);
}

.severity-tag.high {
  font-size: 0.7rem;
  font-weight: var(--font-weight-bold);
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
  padding: 0.1rem 0.6rem;
  border-radius: var(--radius-full);
  border: 1px solid rgba(255, 77, 79, 0.2);
}

.insight-title {
  font-size: var(--font-size-2xl);
  margin-bottom: 0px !important;
}

.insight-badge {
  font-size: var(--font-size-xs);
  padding: 0.25rem 0.75rem;
  background: var(--color-gradient-2);
  color: white;
  border-radius: var(--radius-full);
  font-weight: var(--font-weight-bold);
}

.insight-text {
  color: var(--color-text-secondary);
  line-height: var(--line-height-relaxed);
  margin-bottom: var(--spacing-2xl);
  font-size: var(--font-size-lg);
}

.insight-actions {
  display: flex;
  gap: var(--spacing-md);
}

.insight-sidebar {
  padding-left: var(--spacing-2xl);
  border-left: 1px solid var(--color-border);
}

.sidebar-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: var(--spacing-lg);
}

.recommendations-area {
  margin-top: var(--spacing-xl);
}

.area-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--spacing-lg);
  padding-left: var(--spacing-sm);
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--spacing-lg);
}

.recommendation-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
  transition: transform var(--transition-base);
}

.recommendation-item:hover {
  transform: translateY(-4px);
}

.rec-icon {
  font-size: 2rem;
}

.rec-content {
  flex: 1;
}

.rec-title {
  display: block;
  font-size: var(--font-size-base);
  margin-bottom: 2px;
}

.rec-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.rec-savings {
  text-align: right;
}

.savings-label {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.savings-amount {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: #10b981;
}

.insight-item {
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.03);
  margin-bottom: var(--spacing-md);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.insight-tag {
  display: inline-block;
  font-size: var(--font-size-xs);
  padding: 0.1rem 0.5rem;
  border-radius: var(--radius-sm);
  background: rgba(99, 102, 241, 0.2);
  color: var(--color-primary-light);
  width: fit-content;
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
