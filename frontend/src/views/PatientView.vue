<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="brand">Dental Clinic</div>
      <div class="role-pill"><el-icon><User /></el-icon> 患者端</div>
      <el-menu :default-active="activeTab" background-color="#113c49" text-color="#d9eff2" active-text-color="#ffffff" @select="activeTab = $event">
        <el-menu-item v-for="item in tabs" :key="item.name" :index="item.name">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <main class="main">
      <header class="topbar">
        <div>
          <div class="page-title">患者工作台</div>
          <div class="muted">欢迎，{{ auth.user?.profile?.name || auth.user?.username }}</div>
        </div>
        <el-button icon="SwitchButton" @click="logout">退出</el-button>
      </header>

      <section class="panel">
        <el-tabs v-model="activeTab">
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
            <el-alert
              v-for="notice in appointmentNotices"
              :key="notice.id"
              class="mt"
              type="info"
              :closable="false"
              :title="notice.question"
            />
            <el-table :data="appointments" stripe>
              <el-table-column label="医生" prop="doctor.name" width="120" />
              <el-table-column label="日期" prop="appointment.visitDate" width="130" />
              <el-table-column label="时间" prop="appointment.timeSlot" width="110" />
              <el-table-column label="状态" width="140">
                <template #default="{ row }">
                  <el-tag :type="appointmentStatusTag(row.appointment.status)">{{ appointmentStatusLabel(row.appointment.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="通知/原因" prop="appointment.statusReason" min-width="220" />
              <el-table-column label="症状" prop="appointment.symptoms" />
              <el-table-column label="操作" width="110">
                <template #default="{ row }">
                  <el-button type="danger" link :disabled="!canCancel(row.appointment.status)" @click="cancelAppointment(row.appointment.id)">
                    取消
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="病例查看" name="records">
            <el-row :gutter="16">
              <el-col :md="15" :xs="24">
                <el-table :data="records" stripe>
                  <el-table-column type="expand">
                    <template #default="{ row }">
                      <div class="record-detail">
                        <el-descriptions :column="1" border size="small">
                          <el-descriptions-item label="主诉">{{ row.record.chiefComplaint || '-' }}</el-descriptions-item>
                          <el-descriptions-item label="现病史">{{ row.record.presentIllness || '-' }}</el-descriptions-item>
                          <el-descriptions-item label="检查结果">{{ row.record.examination || '-' }}</el-descriptions-item>
                          <el-descriptions-item label="检查报告">
                            <el-link v-if="row.record.reportImagePath" :href="row.record.reportImagePath" target="_blank" type="primary">查看报告</el-link>
                            <span v-else>-</span>
                          </el-descriptions-item>
                        </el-descriptions>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="doctor.name" label="医生" width="110" />
                  <el-table-column prop="record.diagnosis" label="诊断" />
                  <el-table-column prop="record.treatmentPlan" label="治疗方案" />
                  <el-table-column prop="record.createdAt" label="时间" width="180" />
                </el-table>
              </el-col>

              <el-col :md="9" :xs="24">
                <el-divider content-position="left">用药记录</el-divider>
                <div v-for="record in records" :key="record.record.id" class="prescription-card">
                  <div class="prescription-title">{{ record.record.diagnosis || '未填写诊断' }}</div>
                  <div v-if="record.prescriptions?.length">
                    <div v-for="prescription in record.prescriptions" :key="prescription.prescription.id" class="prescription-line">
                      <strong>{{ prescription.prescription.note || '处方医嘱' }}</strong>
                      <div v-for="item in prescription.items" :key="item.id">
                        {{ item.medicineName }}，{{ item.frequency || '-' }}，{{ item.dosage || '-' }}，{{ item.days || '-' }} 天
                      </div>
                    </div>
                  </div>
                  <div v-else class="muted">本次病例暂无处方药单</div>
                </div>
                <el-empty v-if="!records.length" description="暂无病例记录" :image-size="70" />

                <el-divider content-position="left">用药提醒</el-divider>
                <el-alert
                  v-for="alert in reminderAlerts"
                  :key="alert.reminder.id"
                  class="mt"
                  type="error"
                  :closable="false"
                  :title="alert.message"
                >
                  <template #default>
                    <el-button size="small" type="primary" @click="quickBuy(alert.medicine)">立即购药</el-button>
                  </template>
                </el-alert>
                <el-empty v-if="!reminderAlerts.length" description="暂无即将用完的药品" :image-size="70" />
                <el-table :data="reminders" stripe class="mt">
                  <el-table-column prop="medicineName" label="药品" />
                  <el-table-column prop="expectedRunOutDate" label="预计用完" />
                  <el-table-column label="状态" width="90">
                    <template #default="{ row }">
                      <el-tag :type="row.warned ? 'danger' : 'success'">{{ row.warned ? '预警' : '正常' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="90">
                    <template #default="{ row }">
                      <el-button type="primary" link @click="buyReminder(row)">购药</el-button>
                    </template>
                  </el-table-column>
                </el-table>
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
      </section>
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

const announcements = ref<any[]>([])
const doctors = ref<any[]>([])
const medicines = ref<any[]>([])
const selectedMedicines = ref<any[]>([])
const orders = ref<any[]>([])
const appointments = ref<any[]>([])
const records = ref<any[]>([])
const reminders = ref<any[]>([])
const reminderAlerts = ref<any[]>([])
const messages = ref<any[]>([])
const deliveryMethod = ref('到店自取')
const symptomInput = ref('')
const consultResult = ref<any>(null)
const consulting = ref(false)
const appointmentForm = reactive({ doctorId: undefined as number | undefined, visitDate: '', timeSlot: '09:00', symptoms: '', demand: '' })

const appointmentNotices = computed(() => messages.value.filter((item) => item.question?.startsWith('【预约通知】')).slice(0, 5))
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

async function loadAll() {
  announcements.value = await apiGet('/patient/announcements')
  doctors.value = await apiGet('/patient/doctors')
  medicines.value = await apiGet('/patient/medicines')
  orders.value = await apiGet('/patient/orders')
  appointments.value = await apiGet('/patient/appointments')
  records.value = await apiGet('/patient/records')
  reminders.value = await apiGet('/patient/reminders')
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

async function buyReminder(reminder: any) {
  const medicine = medicines.value.find((item) => item.id === reminder.medicineId)
  if (medicine) return quickBuy(medicine)
  return quickBuy({ id: reminder.medicineId, name: reminder.medicineName })
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

onMounted(loadAll)
</script>

<style scoped>
.narrow-form {
  max-width: 620px;
}
.mt {
  margin-top: 12px;
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
