<template>
  <div class="patient-layout">
    <header class="top-nav">
      <div class="nav-container">
        <div class="nav-brand">
          <el-icon class="brand-icon"><Avatar /></el-icon>
          <span class="brand-text">口腔诊所系统 <span class="brand-sub">患者中心</span></span>
        </div>
        <div class="user-menu">
          <el-popover placement="bottom-end" :width="320" trigger="click" @show="markNoticesAsRead">
            <template #reference>
              <div class="notice-bell">
                <el-badge :value="unreadAppointmentNotices.length" :hidden="unreadAppointmentNotices.length === 0" class="notice-badge">
                  <el-button circle icon="Bell" />
                </el-badge>
              </div>
            </template>
            <div class="notice-popover">
              <div class="notice-popover-header">
                <strong>预约通知</strong>
                <span class="muted">{{ unreadAppointmentNotices.length }} 条未读</span>
              </div>
              <div v-if="recentAppointmentNotices.length === 0" class="empty-notice">暂无通知</div>
              <div v-else class="notice-list">
                <div v-for="notice in recentAppointmentNotices" :key="notice.id" class="notice-item">
                  <el-icon class="notice-icon"><InfoFilled /></el-icon>
                  <span class="notice-text">
                    {{ notice.question }}
                    <span v-if="!isNoticeRead(notice.id)" class="notice-dot">新</span>
                  </span>
                </div>
              </div>
            </div>
          </el-popover>
          <div class="user-greeting">欢迎，{{ auth.user?.profile?.name || auth.user?.username }}</div>
          <el-button plain round size="small" class="logout-btn" @click="logout">退出登录</el-button>
        </div>
      </div>
    </header>

    <div class="hero-section">
      <div class="hero-container">
        <h1>您的专属数字口腔管家</h1>
        <p>一站式预约挂号、就诊历史、药品购买与 AI 问诊评估</p>
      </div>
    </div>

    <main class="main-container">
      <div class="layout-grid">
        <aside class="side-nav">
          <div class="nav-title">服务菜单</div>
          <div class="nav-list">
            <div 
              v-for="item in tabs" 
              :key="item.name" 
              class="nav-item" 
              :class="{ active: activeTab === item.name }"
              @click="activeTab = item.name"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </div>
          </div>
        </aside>

        <section class="content-panel">
          <div class="panel-header">
            <h2>{{ currentTabLabel }}</h2>
          </div>
          <div class="panel-body">
            <el-tabs v-model="activeTab" class="hidden-header-tabs">
          <el-tab-pane label="公告浏览" name="announcements">
            <el-table :data="announcements" stripe>
              <el-table-column prop="category" label="类型" width="120" />
              <el-table-column prop="title" label="标题" width="220" />
              <el-table-column prop="content" label="内容" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="医生查询" name="doctors">
            <el-table :data="doctors" stripe>
              <el-table-column label="医生">
                <template #default="{ row }">
                  <strong>{{ row.doctor.name }}</strong>
                  <div class="muted">{{ row.doctor.title }} · {{ row.doctor.department }}</div>
                </template>
              </el-table-column>
              <el-table-column label="擅长" prop="doctor.specialty" />
              <el-table-column label="评分" width="90">
                <template #default="{ row }">{{ row.doctor.rating }}</template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button type="primary" link @click="selectDoctor(row.doctor.id)">预约</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="药品购买" name="medicines">
            <div class="toolbar">
              <el-button type="primary" icon="ShoppingCart" @click="submitOrder">按选择下单</el-button>
              <el-select v-model="deliveryMethod" style="width: 140px">
                <el-option label="到店自取" value="到店自取" />
                <el-option label="同城配送" value="同城配送" />
              </el-select>
            </div>
            <el-table :data="medicines" stripe @selection-change="selectedMedicines = $event">
              <el-table-column type="selection" width="48" />
              <el-table-column prop="name" label="药品" width="180" />
              <el-table-column prop="effect" label="功效" />
              <el-table-column prop="usageInstruction" label="说明" />
              <el-table-column prop="price" label="价格" width="90" />
              <el-table-column prop="stock" label="库存" width="90" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="在线预约" name="appointment">
            <el-form :model="appointmentForm" label-width="90px" class="narrow-form">
              <el-form-item label="医生">
                <el-select v-model="appointmentForm.doctorId" placeholder="选择医生">
                  <el-option v-for="row in doctors" :key="row.doctor.id" :label="row.doctor.name" :value="row.doctor.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="日期">
                <el-date-picker v-model="appointmentForm.visitDate" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item label="时间段">
                <el-time-select v-model="appointmentForm.timeSlot" start="09:00" step="00:30" end="18:00" />
              </el-form-item>
              <el-form-item label="症状">
                <el-input v-model="appointmentForm.symptoms" type="textarea" />
              </el-form-item>
              <el-form-item label="需求">
                <el-input v-model="appointmentForm.demand" />
              </el-form-item>
              <el-button type="primary" icon="Calendar" @click="createAppointment">提交预约</el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="订单管理" name="orders">
            <el-table :data="orders" stripe>
              <el-table-column label="订单号" prop="order.orderNo" width="180" />
              <el-table-column label="药品名称" prop="medicineNames" min-width="220" />
              <el-table-column label="金额" prop="order.totalAmount" width="100" />
              <el-table-column label="配送" prop="order.deliveryMethod" width="110" />
              <el-table-column label="状态" width="140">
                <template #default="{ row }">
                  <el-tag :type="orderStatusTag(row.order.status)">{{ orderStatusLabel(row.order.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="下单时间" prop="order.createdAt" width="180" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button type="warning" link @click="refund(row.order.id)">退款</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="预约管理" name="appointments">

            <div class="appointment-cards">
              <el-empty v-if="!appointments.length" description="暂无预约记录" :image-size="80" />
              <el-card v-for="row in appointments" :key="row.appointment.id" class="appointment-card" shadow="hover">
                <div class="apt-header">
                  <div class="apt-time">
                    <el-icon><Calendar /></el-icon>
                    <strong>{{ row.appointment.visitDate }}</strong>
                    <span>{{ row.appointment.timeSlot }}</span>
                  </div>
                  <el-tag :type="appointmentStatusTag(row.appointment.status)" effect="light" round>
                    {{ appointmentStatusLabel(row.appointment.status) }}
                  </el-tag>
                </div>
                <div class="apt-body">
                  <div class="apt-info">
                    <span class="label">就诊医生：</span>
                    <span class="value">{{ row.doctor.name }}</span>
                  </div>
                  <div class="apt-info">
                    <span class="label">初步症状：</span>
                    <span class="value">{{ row.appointment.symptoms || '未填写' }}</span>
                  </div>
                  <div v-if="row.appointment.statusReason" class="apt-info alert-text">
                    <span class="label">通知留言：</span>
                    <span class="value">{{ row.appointment.statusReason }}</span>
                  </div>
                </div>
                <div class="apt-footer">
                  <el-button type="danger" plain round size="small" :disabled="!canCancel(row.appointment.status)" @click="cancelAppointment(row.appointment.id)">
                    取消预约
                  </el-button>
                </div>
              </el-card>
            </div>
          </el-tab-pane>

          <el-tab-pane label="病例查看" name="records">
            <el-row :gutter="24">
              <el-col :md="15" :xs="24">
                <div class="section-title">就诊时间线</div>
                <el-empty v-if="!records.length" description="暂无病例记录" :image-size="80" />
                <el-timeline v-else class="record-timeline">
                  <el-timeline-item
                    v-for="row in records"
                    :key="row.record.id"
                    :timestamp="row.record.createdAt"
                    placement="top"
                    type="primary"
                    :hollow="true"
                  >
                    <el-card class="record-card" shadow="hover">
                      <div class="record-header">
                        <div class="diagnosis-title">{{ row.record.diagnosis || '初步诊断未填写' }}</div>
                        <div class="doctor-badge">
                          <el-icon><Avatar /></el-icon> 主治医生: {{ row.doctor.name }}
                        </div>
                      </div>
                      <el-descriptions :column="1" border size="small" class="record-desc mt">
                        <el-descriptions-item label="主诉">{{ row.record.chiefComplaint || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="现病史">{{ row.record.presentIllness || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="检查结果">{{ row.record.examination || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="治疗方案">{{ row.record.treatmentPlan || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="检查报告">
                          <el-link v-if="row.record.reportImagePath" :href="row.record.reportImagePath" target="_blank" type="primary">
                            <el-icon><Document /></el-icon> 查看影像/报告
                          </el-link>
                          <span v-else class="muted">无</span>
                        </el-descriptions-item>
                      </el-descriptions>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </el-col>

              <el-col :md="9" :xs="24">
                <div class="side-panel-wrapper">
                  <div class="section-title">历史用药单</div>
                  <div v-for="record in records" :key="record.record.id">
                    <div v-if="record.prescriptions?.length" class="prescription-card modern-card">
                      <div class="prescription-title">{{ record.record.diagnosis || '诊断用药' }}</div>
                      <div v-for="prescription in record.prescriptions" :key="prescription.prescription.id" class="prescription-line">
                        <div class="presc-note"><el-icon><InfoFilled /></el-icon> {{ prescription.prescription.note || '处方医嘱' }}</div>
                        <div v-for="item in prescription.items" :key="item.id" class="presc-item">
                          <span class="med-name">{{ item.medicineName }}</span>
                          <span class="med-usage">{{ item.frequency || '-' }} · 每次{{ item.dosage || '-' }} · {{ item.days || '-' }}天</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <el-empty v-if="!records.some(r => r.prescriptions?.length)" description="暂无处方药单" :image-size="60" />

                  <div class="section-title mt-large">用药提醒</div>
                  <el-alert
                    v-for="alert in reminderAlerts"
                    :key="alert.reminder.id"
                    class="mb"
                    :type="reminderAlertType(alert.level)"
                    show-icon
                    :closable="false"
                    :title="alert.title"
                  >
                    <template #default>
                      <div class="reminder-message">{{ alert.message }}</div>
                      <el-button size="small" type="danger" plain @click="quickBuy(alert.medicine)" style="margin-top:8px">
                        {{ alert.actionText || '补药' }}
                      </el-button>
                    </template>
                  </el-alert>
                  <el-empty v-if="!reminderAlerts.length" description="当前处方药量充足，暂不需要补药提醒" :image-size="60" />
                </div>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="AI牙医" name="ai">
            <el-row :gutter="16">
              <el-col :md="14" :xs="24">
                <el-input v-model="symptomInput" type="textarea" :rows="5" placeholder="请输入牙痛、牙龈出血、口臭等症状" />
                <div class="toolbar mt">
                  <el-button type="primary" icon="MagicStick" :loading="consulting" @click="consult">AI牙医初评</el-button>
                  <el-button icon="Refresh" @click="loadAll">刷新推荐</el-button>
                </div>
                <el-alert v-if="consultResult" class="mt" type="warning" :closable="false" :title="consultResult.disclaimer" />
                <div v-if="consultResult" class="result">
                  <div class="muted">模型：{{ consultResult.model }} · {{ consultResult.provider }}</div>
                  <el-table :data="consultSectionRows" stripe class="mt">
                    <el-table-column prop="title" label="项目" width="220" />
                    <el-table-column label="内容">
                      <template #default="{ row }">
                        <div class="consult-section-content">{{ row.content || '-' }}</div>
                      </template>
                    </el-table-column>
                  </el-table>
                  <el-collapse class="mt" accordion>
                    <el-collapse-item title="查看原始回答" name="raw">
                      <pre>{{ consultResult.answer }}</pre>
                    </el-collapse-item>
                  </el-collapse>
                </div>

                <el-divider v-if="consultResult" content-position="left">症状匹配推荐</el-divider>
                <div v-if="consultResult" class="recommend-grid">
                  <div class="recommend-box">
                    <h3>推荐科室</h3>
                    <el-tag v-for="dept in consultResult.recommendedDepartments" :key="dept" effect="plain">{{ dept }}</el-tag>
                  </div>
                  <div class="recommend-box">
                    <h3>推荐医生</h3>
                    <el-table :data="consultResult.recommendedDoctors || []" size="small">
                      <el-table-column prop="name" label="医生" width="90" />
                      <el-table-column prop="department" label="科室" />
                      <el-table-column label="操作" width="90">
                        <template #default="{ row }">
                          <el-button type="primary" link @click="selectDoctor(row.id)">预约</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                  <div class="recommend-box">
                    <h3>推荐药品</h3>
                    <el-table :data="consultResult.recommendedMedicines || []" size="small">
                      <el-table-column prop="name" label="药品" />
                      <el-table-column prop="price" label="价格" width="80" />
                      <el-table-column label="操作" width="90">
                        <template #default="{ row }">
                          <el-button type="success" link @click="quickBuy(row)">购买</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </div>
              </el-col>

              <el-col :md="10" :xs="24">
                <el-alert type="info" :closable="false" title="AI牙医会结合症状规则和外部模型生成初步建议，结果只用于就诊前参考，不能替代医生诊断。" />
                <div class="ai-steps">
                  <div>1. 输入主要症状，例如牙痛、牙龈出血、口腔溃疡。</div>
                  <div>2. 系统匹配科室、医生和可购买药品。</div>
                  <div>3. 外部 AI 生成初步原因和处理建议。</div>
                </div>
              </el-col>
            </el-row>
          </el-tab-pane>
        </el-tabs>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { apiGet, apiPost, apiPut } from '../api'
import { useAuthStore } from '../store'

const router = useRouter()
const auth = useAuthStore()
const activeTab = ref('announcements')
const tabs = [
  { name: 'announcements', label: '公告浏览', icon: 'Bell' },
  { name: 'doctors', label: '医生查询', icon: 'Avatar' },
  { name: 'medicines', label: '药品购买', icon: 'ShoppingBag' },
  { name: 'appointment', label: '在线预约', icon: 'Calendar' },
  { name: 'orders', label: '订单管理', icon: 'Tickets' },
  { name: 'appointments', label: '预约管理', icon: 'Calendar' },
  { name: 'records', label: '病例查看', icon: 'Document' },
  { name: 'ai', label: 'AI牙医', icon: 'MagicStick' }
]

const currentTabLabel = computed(() => tabs.find(t => t.name === activeTab.value)?.label || '')

const announcements = ref<any[]>([])
const doctors = ref<any[]>([])
const medicines = ref<any[]>([])
const selectedMedicines = ref<any[]>([])
const orders = ref<any[]>([])
const appointments = ref<any[]>([])
const records = ref<any[]>([])
const reminderAlerts = ref<any[]>([])
const messages = ref<any[]>([])
const deliveryMethod = ref('到店自取')
const symptomInput = ref('')
const consultResult = ref<any>(null)
const consulting = ref(false)
const seenNoticeIds = ref<number[]>([])
const appointmentForm = reactive({ doctorId: undefined as number | undefined, visitDate: '', timeSlot: '09:00', symptoms: '', demand: '' })

const noticeStorageKey = computed(() => `patient_notice_seen_${auth.user?.id || auth.user?.username || 'anonymous'}`)
const appointmentNotices = computed(() => messages.value.filter((item) => item.question?.startsWith('【预约通知】')))
const unreadAppointmentNotices = computed(() => appointmentNotices.value.filter((item) => !seenNoticeIds.value.includes(item.id)))
const recentAppointmentNotices = computed(() => appointmentNotices.value.slice(0, 5))

function restoreSeenNoticeIds() {
  const raw = localStorage.getItem(noticeStorageKey.value)
  if (!raw) {
    seenNoticeIds.value = []
    return
  }
  try {
    const parsed = JSON.parse(raw)
    seenNoticeIds.value = Array.isArray(parsed) ? parsed.map((item) => Number(item)).filter((item) => Number.isFinite(item)) : []
  } catch {
    seenNoticeIds.value = []
  }
}

function persistSeenNoticeIds() {
  localStorage.setItem(noticeStorageKey.value, JSON.stringify(seenNoticeIds.value))
}

function isNoticeRead(id: number) {
  return seenNoticeIds.value.includes(id)
}

function markNoticesAsRead() {
  const ids = new Set(seenNoticeIds.value)
  appointmentNotices.value.forEach((item) => ids.add(item.id))
  seenNoticeIds.value = Array.from(ids)
  persistSeenNoticeIds()
}
function formatConsultSectionContent(content: string) {
  return content
    .split(/\r?\n/)
    .map((line) =>
      line
        .trim()
        .replace(/^[-*]\s+/, '• ')
        .replace(/^\d+[.、]\s+/, '• ')
        .replace(/\*\*(.*?)\*\*/g, '$1')
    )
    .filter((line) => line.length > 0)
    .join('\n')
}

const consultSectionRows = computed(() => {
  const rows = consultResult.value?.sections
  if (Array.isArray(rows) && rows.length) {
    const order = ['初步评估', '可能原因', '建议就诊科室或医生方向', '参考用药或护理建议', '何时尽快就医', '免责声明']
    return [...rows].map((row: any) => ({
      ...row,
      content: formatConsultSectionContent(row.content || '')
    })).sort((left: any, right: any) => {
      const leftIndex = order.indexOf(left.title)
      const rightIndex = order.indexOf(right.title)
      if (leftIndex === -1 && rightIndex === -1) return 0
      if (leftIndex === -1) return 1
      if (rightIndex === -1) return -1
      return leftIndex - rightIndex
    })
  }
  if (consultResult.value?.answer) {
    return [{ title: 'AI原文', content: formatConsultSectionContent(consultResult.value.answer) }]
  }
  return []
})

const appointmentStatusMap: Record<string, string> = {
  SUBMITTED: '待审核',
  CONFIRMED: '已确认',
  REJECTED: '已拒绝',
  RESCHEDULED: '已改期',
  CANCELLED: '已取消',
  COMPLETED: '已完成',
  NO_SHOW: '爽约'
}

const orderStatusMap: Record<string, string> = {
  PENDING_PAY: '待支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  REFUND_REQUESTED: '退款申请中',
  REFUNDED: '已退款',
  CANCELLED: '已取消'
}

function appointmentStatusLabel(status: string) {
  return appointmentStatusMap[status] || status || '-'
}

function orderStatusLabel(status: string) {
  return orderStatusMap[status] || status || '-'
}

function appointmentStatusTag(status: string) {
  if (['CONFIRMED', 'RESCHEDULED'].includes(status)) return 'success'
  if (['REJECTED', 'CANCELLED', 'NO_SHOW'].includes(status)) return 'danger'
  if (status === 'COMPLETED') return 'info'
  return 'warning'
}

function orderStatusTag(status: string) {
  if (['PAID', 'SHIPPED'].includes(status)) return 'success'
  if (['REFUND_REQUESTED', 'PENDING_PAY'].includes(status)) return 'warning'
  if (['REFUNDED', 'CANCELLED'].includes(status)) return 'danger'
  return 'info'
}

function reminderAlertType(level: string) {
  if (level === 'overdue') return 'error'
  if (level === 'urgent') return 'warning'
  return 'info'
}

async function loadAll() {
  announcements.value = await apiGet('/patient/announcements')
  doctors.value = await apiGet('/patient/doctors')
  medicines.value = await apiGet('/patient/medicines')
  orders.value = await apiGet('/patient/orders')
  appointments.value = await apiGet('/patient/appointments')
  records.value = await apiGet('/patient/records')
  reminderAlerts.value = await apiGet('/patient/reminders/alerts')
  messages.value = await apiGet('/patient/messages')
}

function selectDoctor(id: number) {
  appointmentForm.doctorId = id
  activeTab.value = 'appointment'
}

function canCancel(status: string) {
  return ['SUBMITTED', 'CONFIRMED', 'RESCHEDULED'].includes(status)
}

async function submitOrder() {
  if (!selectedMedicines.value.length) return ElMessage.warning('请先选择药品')
  await apiPost('/patient/orders', {
    deliveryMethod: deliveryMethod.value,
    items: selectedMedicines.value.map((item) => ({ medicineId: item.id, quantity: 1 }))
  })
  ElMessage.success('下单成功')
  activeTab.value = 'orders'
  await loadAll()
}

async function quickBuy(medicine: any) {
  if (!medicine?.id) return ElMessage.warning('药品信息不完整')
  await apiPost('/patient/orders', {
    deliveryMethod: deliveryMethod.value,
    items: [{ medicineId: medicine.id, quantity: 1 }]
  })
  ElMessage.success(`已购买 ${medicine.name}`)
  activeTab.value = 'orders'
  await loadAll()
}

async function createAppointment() {
  await apiPost('/patient/appointments', appointmentForm)
  ElMessage.success('预约已提交，请等待医生确认')
  activeTab.value = 'appointments'
  await loadAll()
}

async function refund(id: number) {
  await apiPut(`/patient/orders/${id}/refund`)
  ElMessage.success('已申请退款')
  await loadAll()
}

async function cancelAppointment(id: number) {
  await apiPut(`/patient/appointments/${id}/cancel`)
  ElMessage.success('预约已取消')
  await loadAll()
}

async function consult() {
  consulting.value = true
  try {
    consultResult.value = await apiPost('/patient/ai/consult', { symptoms: symptomInput.value })
    ElMessage.success('AI问诊建议已生成')
  } finally {
    consulting.value = false
  }
}

function logout() {
  auth.logout()
  router.push('/login')
}

onMounted(async () => {
  restoreSeenNoticeIds()
  await loadAll()
})
</script>

<style scoped>
.patient-layout {
  min-height: 100vh;
  background: #f4f9f9;
  display: flex;
  flex-direction: column;
}

.top-nav {
  background: #ffffff;
  height: 64px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #0f766e;
}

.brand-icon {
  font-size: 24px;
  color: #0d9488;
}

.brand-sub {
  font-size: 12px;
  background: #ccfbf1;
  color: #0f766e;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  margin-left: 6px;
  vertical-align: middle;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-greeting {
  font-size: 14px;
  color: #475569;
}

.hero-section {
  background: linear-gradient(135deg, #0f766e 0%, #0d9488 100%);
  color: #ffffff;
  padding: 48px 24px;
  text-align: center;
}

.hero-container h1 {
  margin: 0 0 12px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.hero-container p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.main-container {
  max-width: 1200px;
  margin: -32px auto 40px;
  padding: 0 24px;
  flex: 1;
  width: 100%;
}

.layout-grid {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 24px;
  align-items: start;
}

.side-nav {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(15, 118, 110, 0.06);
  padding: 20px 12px;
}

.nav-title {
  font-size: 13px;
  font-weight: 600;
  color: #94a3b8;
  margin-bottom: 12px;
  padding: 0 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  color: #475569;
  font-size: 15px;
  transition: all 0.2s ease;
  margin-bottom: 4px;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #0f766e;
}

.nav-item.active {
  background: #f0fdfa;
  color: #0f766e;
  font-weight: 600;
}

.content-panel {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(15, 118, 110, 0.06);
  min-height: 500px;
}

.panel-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.panel-header h2 {
  margin: 0;
  font-size: 20px;
  color: #1e293b;
  font-weight: 600;
}

.panel-body {
  padding: 24px;
}

:deep(.hidden-header-tabs > .el-tabs__header) {
  display: none !important;
}

@media (max-width: 768px) {
  .layout-grid {
    grid-template-columns: 1fr;
  }
  .main-container {
    margin-top: 24px;
  }
}

.narrow-form {
  max-width: 620px;
}
.mb { margin-bottom: 12px; }
.mt-large { margin-top: 24px; }

.notice-carousel {
  border-radius: 8px;
  overflow: hidden;
}
.notice-carousel :deep(.el-alert) {
  height: 100%;
  margin: 0;
  border-radius: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-title::before {
  content: '';
  display: block;
  width: 4px;
  height: 16px;
  background: #0d9488;
  border-radius: 2px;
}

/* Appointments Card */
.appointment-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}
.appointment-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}
.apt-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e2e8f0;
}
.apt-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #0f766e;
  font-size: 15px;
}
.apt-time strong { font-weight: 600; }
.apt-info {
  margin-bottom: 8px;
  font-size: 14px;
  display: flex;
  gap: 8px;
}
.apt-info .label { color: #64748b; white-space: nowrap; }
.apt-info .value { color: #334155; }
.alert-text .value { color: #e11d48; font-weight: 500; }
.apt-footer {
  margin-top: 16px;
  text-align: right;
}

/* Records Timeline */
.record-timeline { padding-top: 8px; }
.record-card { border-radius: 12px; border: 1px solid #e2e8f0; }
.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.diagnosis-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f766e;
}
.doctor-badge {
  background: #f1f5f9;
  color: #475569;
  padding: 4px 10px;
  border-radius: 16px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.record-desc {
  --el-descriptions-table-border: 1px solid #f1f5f9;
  --el-descriptions-item-bordered-label-background: #f8fafc;
}

/* Modern Side Cards */
.modern-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.prescription-card .prescription-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f1f5f9;
}
.prescription-line + .prescription-line {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #f1f5f9;
}
.presc-note {
  color: #0891b2;
  font-size: 13px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.presc-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 4px;
}
.med-name { color: #334155; font-weight: 500; }
.med-usage { color: #64748b; font-size: 13px; }

.mt {
  margin-top: 12px;
}
.reminder-message {
  margin-top: 6px;
  line-height: 1.7;
}

/* Popover Notification Styles */
.notice-bell {
  margin-right: 12px;
  cursor: pointer;
}
.notice-popover-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f1f5f9;
}
.empty-notice {
  color: #94a3b8;
  text-align: center;
  padding: 16px 0;
}
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 280px;
  overflow-y: auto;
}
.notice-item {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  font-size: 13px;
  color: #334155;
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 6px;
}
.notice-icon {
  color: #0ea5e9;
  margin-top: 2px;
}
.notice-text {
  display: flex;
  align-items: center;
  gap: 8px;
  line-height: 1.6;
}
.notice-dot {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: #fee2e2;
  color: #dc2626;
  font-size: 12px;
  font-weight: 600;
}
.result {
  background: #f7faf9;
  border: 1px solid #dbe8e5;
  border-radius: 8px;
  padding: 12px;
  max-height: 320px;
  overflow: auto;
}
.result pre {
  margin: 8px 0 0;
  white-space: pre-wrap;
  font-family: inherit;
  line-height: 1.7;
}
.consult-section-content {
  white-space: pre-wrap;
  line-height: 1.7;
}
.recommend-grid {
  display: grid;
  gap: 12px;
}
.recommend-box {
  border: 1px solid #dfe9eb;
  border-radius: 8px;
  padding: 12px;
  background: #fff;
}
.recommend-box h3 {
  font-size: 15px;
  margin: 0 0 10px;
}
.record-detail {
  padding: 10px 18px;
  background: #fbfdfd;
}
.prescription-line + .prescription-line {
  margin-top: 8px;
}
.prescription-card {
  border: 1px solid #dfe9eb;
  border-radius: 8px;
  padding: 12px;
  background: #fff;
}
.prescription-card + .prescription-card {
  margin-top: 10px;
}
.prescription-title {
  font-weight: 700;
  margin-bottom: 8px;
}
.ai-steps {
  margin-top: 14px;
  display: grid;
  gap: 10px;
  color: #4f6468;
  line-height: 1.7;
}
</style>
