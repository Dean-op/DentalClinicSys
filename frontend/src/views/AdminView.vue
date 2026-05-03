<template>
  <div class="shell theme-admin">
    <aside class="sidebar">
      <div class="brand">Dental Clinic</div>
      <div class="role-pill"><el-icon><Setting /></el-icon> 管理员端</div>
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
          <div class="page-title">后台管理系统</div>
          <div class="muted">全角色、全模块统一管理</div>
        </div>
        <el-button icon="SwitchButton" @click="logout">退出</el-button>
      </header>

      <section class="panel">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="统计分析" name="stats">
            <div class="metric-grid">
              <div class="metric"><strong>{{ stats.users || 0 }}</strong><span>账户数</span></div>
              <div class="metric"><strong>{{ stats.doctors || 0 }}</strong><span>医生数</span></div>
              <div class="metric"><strong>{{ stats.activePatients || 0 }}</strong><span>活跃患者</span></div>
              <div class="metric"><strong>{{ stats.appointments || 0 }}</strong><span>预约量</span></div>
              <div class="metric"><strong>{{ stats.orders || 0 }}</strong><span>订单量</span></div>
              <div class="metric"><strong>{{ stats.records || 0 }}</strong><span>病例数</span></div>
            </div>

            <el-row :gutter="16" class="mt">
              <el-col :md="12" :xs="24">
                <el-card class="stat-card" shadow="never">
                  <template #header>预约状态分布</template>
                  <el-table :data="appointmentStatusRows" size="small" stripe>
                    <el-table-column prop="label" label="状态" />
                    <el-table-column prop="count" label="数量" width="90" />
                  </el-table>
                </el-card>
              </el-col>
              <el-col :md="12" :xs="24">
                <el-card class="stat-card" shadow="never">
                  <template #header>订单状态分布</template>
                  <el-table :data="orderStatusRows" size="small" stripe>
                    <el-table-column prop="label" label="状态" />
                    <el-table-column prop="count" label="数量" width="90" />
                  </el-table>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="16" class="mt">
              <el-col :md="12" :xs="24">
                <el-card class="stat-card" shadow="never">
                  <template #header>常见病症</template>
                  <el-table :data="commonSymptomRows" size="small" stripe>
                    <el-table-column prop="label" label="病症" />
                    <el-table-column prop="count" label="数量" width="90" />
                  </el-table>
                </el-card>
              </el-col>
              <el-col :md="12" :xs="24">
                <el-card class="stat-card" shadow="never">
                  <template #header>就诊高峰时段</template>
                  <el-table :data="peakHourRows" size="small" stripe>
                    <el-table-column prop="label" label="时段" />
                    <el-table-column prop="count" label="数量" width="90" />
                  </el-table>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="16" class="mt">
              <el-col :md="12" :xs="24">
                <el-card class="stat-card" shadow="never">
                  <template #header>药品销量</template>
                  <el-table :data="medicineSalesRows" size="small" stripe>
                    <el-table-column prop="label" label="药品" />
                    <el-table-column prop="count" label="销量" width="90" />
                  </el-table>
                </el-card>
              </el-col>
              <el-col :md="12" :xs="24">
                <el-card class="stat-card" shadow="never">
                  <template #header>医生工作量</template>
                  <el-table :data="doctorWorkloadRows" size="small" stripe>
                    <el-table-column label="医生">
                      <template #default="{ row }">
                        <strong>{{ row.doctor.name }}</strong>
                        <div class="muted">{{ row.doctor.department }}</div>
                      </template>
                    </el-table-column>
                    <el-table-column prop="appointmentCount" label="预约" width="70" />
                    <el-table-column prop="completedCount" label="接诊" width="70" />
                    <el-table-column prop="noShowCount" label="爽约" width="70" />
                  </el-table>
                </el-card>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="用户管理" name="users">
            <div class="toolbar">
              <el-input v-model="userForm.username" placeholder="账号" style="width: 140px" />
              <el-input v-model="userForm.name" placeholder="姓名" style="width: 140px" />
              <el-select v-model="userForm.role" style="width: 130px">
                <el-option label="患者" value="PATIENT" />
                <el-option label="医生" value="DOCTOR" />
                <el-option label="管理员" value="ADMIN" />
              </el-select>
              <el-button type="primary" icon="Plus" @click="createUser">新增账号</el-button>
            </div>
            <el-table :data="users" stripe>
              <el-table-column prop="account.username" label="账号" width="160" />
              <el-table-column label="角色" width="100">
                <template #default="{ row }">{{ roleLabel(row.account.role) }}</template>
              </el-table-column>
              <el-table-column prop="linkedName" label="关联姓名" width="160" />
              <el-table-column label="资料信息" min-width="200">
                <template #default="{ row }">
                  <div v-if="row.account.role === 'DOCTOR'">
                    {{ row.title }} · {{ row.department }}
                  </div>
                  <div v-else>{{ row.linkedType }}</div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.account.status === 'ENABLED' ? 'success' : 'danger'">
                    {{ accountStatusLabel(row.account.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="account.lastLoginAt" label="最后登录" width="180" />
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button type="warning" link @click="resetPassword(row.account.id)">重置密码</el-button>
                  <el-button type="danger" link @click="toggleUser(row.account)">禁/启用</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="医生审核" name="doctors">
            <el-table :data="doctors" stripe>
              <el-table-column prop="name" label="医生" />
              <el-table-column prop="title" label="职称" />
              <el-table-column prop="department" label="科室" />
              <el-table-column prop="specialty" label="擅长方向" min-width="220" />
              <el-table-column prop="rating" label="评分" width="90" />
              <el-table-column label="审核状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="reviewStatusTag(row.reviewStatus)">{{ reviewStatusLabel(row.reviewStatus) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button type="success" link @click="reviewDoctor(row.id, 'APPROVED')">通过</el-button>
                  <el-button type="danger" link @click="reviewDoctor(row.id, 'REJECTED')">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="药品管理" name="medicines">
            <el-form :inline="true" :model="medicineForm" class="toolbar">
              <el-form-item><el-input v-model="medicineForm.name" placeholder="药品名称" /></el-form-item>
              <el-form-item><el-input v-model="medicineForm.effect" placeholder="功效" /></el-form-item>
              <el-form-item><el-input-number v-model="medicineForm.price" :min="0" /></el-form-item>
              <el-form-item><el-input-number v-model="medicineForm.stock" :min="0" /></el-form-item>
              <el-form-item><el-button type="primary" icon="Plus" @click="saveMedicine">保存药品</el-button></el-form-item>
            </el-form>
            <el-table :data="medicines" stripe @row-click="editMedicine">
              <el-table-column prop="name" label="药品" />
              <el-table-column prop="effect" label="功效" />
              <el-table-column prop="price" label="价格" />
              <el-table-column prop="stock" label="库存" />
              <el-table-column label="上架" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.onShelf ? 'success' : 'info'">{{ row.onShelf ? '已上架' : '已下架' }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="公告管理" name="announcements">
            <el-form :model="announcementForm" label-width="80px" class="narrow-form">
              <el-form-item label="标题"><el-input v-model="announcementForm.title" /></el-form-item>
              <el-form-item label="分类"><el-input v-model="announcementForm.category" /></el-form-item>
              <el-form-item label="状态">
                <el-select v-model="announcementForm.status">
                  <el-option label="草稿" value="DRAFT" />
                  <el-option label="定时发布" value="SCHEDULED" />
                  <el-option label="已发布" value="PUBLISHED" />
                  <el-option label="已下线" value="OFFLINE" />
                </el-select>
              </el-form-item>
              <el-form-item label="内容"><el-input v-model="announcementForm.content" type="textarea" /></el-form-item>
              <el-button type="primary" icon="Promotion" @click="saveAnnouncement">保存公告</el-button>
            </el-form>
            <el-table :data="announcements" stripe @row-click="editAnnouncement">
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="category" label="分类" />
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="announcementStatusTag(row.status)">{{ announcementStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="病例监督" name="records">
            <el-alert type="info" :closable="false" title="管理员仅做只读审核，用于合规检查、质控抽查和病历留档。" />
            <el-table :data="records" stripe>
              <el-table-column label="患者" width="120">
                <template #default="{ row }">{{ row.patient?.name || '-' }}</template>
              </el-table-column>
              <el-table-column label="医生" width="120">
                <template #default="{ row }">{{ row.doctor?.name || '-' }}</template>
              </el-table-column>
              <el-table-column prop="record.chiefComplaint" label="主诉" />
              <el-table-column prop="record.diagnosis" label="诊断" />
              <el-table-column prop="record.treatmentPlan" label="治疗方案" />
              <el-table-column prop="record.createdAt" label="记录时间" width="180" />
              <el-table-column type="expand">
                <template #default="{ row }">
                  <div class="expand-box">
                    <el-descriptions :column="2" border size="small">
                      <el-descriptions-item label="现病史">{{ row.record.presentIllness || '-' }}</el-descriptions-item>
                      <el-descriptions-item label="检查结果">{{ row.record.examination || '-' }}</el-descriptions-item>
                      <el-descriptions-item label="检查报告">
                        <el-link v-if="row.record.reportImagePath" :href="row.record.reportImagePath" target="_blank" type="primary">查看报告</el-link>
                        <span v-else>-</span>
                      </el-descriptions-item>
                      <el-descriptions-item label="关联预约">
                        {{ row.appointment?.visitDate || '-' }} {{ row.appointment?.timeSlot || '' }}
                      </el-descriptions-item>
                    </el-descriptions>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="处方监督" name="prescriptions">
            <el-alert type="info" :closable="false" title="这里只做合理性抽查，不替代医生判断。系统仅对药品种类过多、天数过长等情况做提示。" />
            <el-table :data="prescriptions" stripe>
              <el-table-column label="患者" width="120">
                <template #default="{ row }">{{ row.patient?.name || '-' }}</template>
              </el-table-column>
              <el-table-column label="医生" width="120">
                <template #default="{ row }">{{ row.doctor?.name || '-' }}</template>
              </el-table-column>
              <el-table-column prop="medicineNames" label="药单摘要" min-width="280" />
              <el-table-column label="风险提示" width="140">
                <template #default="{ row }">
                  <el-tag :type="row.riskFlag === '未发现明显异常' ? 'success' : 'warning'">{{ row.riskFlag }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="prescription.createdAt" label="开方时间" width="180" />
              <el-table-column type="expand">
                <template #default="{ row }">
                  <div class="expand-box">
                    <el-descriptions :column="2" border size="small">
                      <el-descriptions-item label="医嘱">{{ row.prescription.note || '-' }}</el-descriptions-item>
                      <el-descriptions-item label="关联病例">{{ row.record?.diagnosis || '-' }}</el-descriptions-item>
                    </el-descriptions>
                    <el-table :data="row.items || []" size="small" class="mt">
                      <el-table-column prop="medicineName" label="药品" />
                      <el-table-column prop="frequency" label="频次" />
                      <el-table-column prop="dosage" label="剂量" />
                      <el-table-column prop="days" label="天数" width="80" />
                    </el-table>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="订单管理" name="orders">
            <el-alert type="info" :closable="false" title="订单页聚焦支付、配送和退款审核，不再把病例逻辑混进来。" />
            <el-table :data="orders" stripe>
              <el-table-column label="患者" width="120">
                <template #default="{ row }">{{ row.patient?.name || '-' }}</template>
              </el-table-column>
              <el-table-column prop="order.orderNo" label="订单号" width="180" />
              <el-table-column prop="medicineNames" label="药品" min-width="220" />
              <el-table-column prop="order.totalAmount" label="金额" width="100" />
              <el-table-column label="支付状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.paymentStatus === '已支付' ? 'success' : row.paymentStatus === '退款申请中' ? 'warning' : 'info'">
                    {{ row.paymentStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="配送状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.deliveryStatus === '已完成' ? 'success' : row.deliveryStatus === '已退款' ? 'danger' : 'warning'">
                    {{ row.deliveryStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="order.deliveryMethod" label="配送方式" width="100" />
              <el-table-column label="订单状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="orderStatusTag(row.order.status)">{{ orderStatusLabel(row.order.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220">
                <template #default="{ row }">
                  <el-button type="primary" link @click="updateOrder(row.order.id, 'SHIPPED')">标记发货</el-button>
                  <el-button type="success" link @click="updateOrder(row.order.id, 'COMPLETED')">完成订单</el-button>
                  <el-button type="warning" link @click="updateOrder(row.order.id, 'REFUNDED')">退款完成</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="预约管理" name="appointments">
            <el-alert type="info" :closable="false" title="预约数据用于排班复盘和爽约统计，不作为直接诊疗操作页。" />
            <el-table :data="appointments" stripe>
              <el-table-column label="患者" width="120">
                <template #default="{ row }">{{ row.patient?.name || '-' }}</template>
              </el-table-column>
              <el-table-column label="医生" width="120">
                <template #default="{ row }">{{ row.doctor?.name || '-' }}</template>
              </el-table-column>
              <el-table-column prop="appointment.visitDate" label="日期" width="130" />
              <el-table-column prop="appointment.timeSlot" label="时间" width="110" />
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="appointmentStatusTag(row.appointment.status)">{{ appointmentStatusLabel(row.appointment.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="appointment.symptoms" label="症状" />
              <el-table-column prop="appointment.statusReason" label="通知/原因" min-width="220" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="系统日志" name="logs">
            <el-alert type="info" :closable="false" title="系统日志当前记录登录、注册、预约、接诊、处方、订单等关键操作，可用于追溯。" />
            <el-table :data="logs" stripe>
              <el-table-column label="角色" width="100">
                <template #default="{ row }">{{ roleLabel(row.operatorRole) }}</template>
              </el-table-column>
              <el-table-column prop="action" label="操作" width="160" />
              <el-table-column prop="detail" label="详情" min-width="260" />
              <el-table-column prop="createdAt" label="时间" width="180" />
            </el-table>
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
const activeTab = ref('stats')
const tabs = [
  { name: 'stats', label: '统计分析', icon: 'DataAnalysis' },
  { name: 'users', label: '用户管理', icon: 'UserFilled' },
  { name: 'doctors', label: '医生审核', icon: 'Checked' },
  { name: 'medicines', label: '药品管理', icon: 'ShoppingBag' },
  { name: 'announcements', label: '公告管理', icon: 'Bell' },
  { name: 'records', label: '病例监督', icon: 'Document' },
  { name: 'prescriptions', label: '处方监督', icon: 'Memo' },
  { name: 'orders', label: '订单管理', icon: 'Tickets' },
  { name: 'appointments', label: '预约管理', icon: 'Calendar' },
  { name: 'logs', label: '系统日志', icon: 'Files' }
]

const stats = ref<any>({})
const users = ref<any[]>([])
const doctors = ref<any[]>([])
const medicines = ref<any[]>([])
const announcements = ref<any[]>([])
const orders = ref<any[]>([])
const records = ref<any[]>([])
const prescriptions = ref<any[]>([])
const appointments = ref<any[]>([])
const logs = ref<any[]>([])

const userForm = reactive({ username: '', name: '', role: 'PATIENT', password: '123456' })
const medicineForm = reactive<any>({ id: undefined, name: '', effect: '', usageInstruction: '', price: 0, stock: 0, onShelf: true })
const announcementForm = reactive<any>({ id: undefined, title: '', category: '通知', content: '', status: 'PUBLISHED' })

const roleMap: Record<string, string> = {
  PATIENT: '患者',
  DOCTOR: '医生',
  ADMIN: '管理员'
}
const accountStatusMap: Record<string, string> = {
  ENABLED: '启用',
  DISABLED: '禁用'
}
const reviewStatusMap: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝'
}
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
const announcementStatusMap: Record<string, string> = {
  DRAFT: '草稿',
  SCHEDULED: '定时发布',
  PUBLISHED: '已发布',
  OFFLINE: '已下线'
}

function roleLabel(role: string) {
  return roleMap[role] || role || '-'
}

function accountStatusLabel(status: string) {
  return accountStatusMap[status] || status || '-'
}

function reviewStatusLabel(status: string) {
  return reviewStatusMap[status] || status || '-'
}

function reviewStatusTag(status: string) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

function appointmentStatusLabel(status: string) {
  return appointmentStatusMap[status] || status || '-'
}

function appointmentStatusTag(status: string) {
  if (['CONFIRMED', 'RESCHEDULED'].includes(status)) return 'success'
  if (['REJECTED', 'CANCELLED', 'NO_SHOW'].includes(status)) return 'danger'
  if (status === 'COMPLETED') return 'info'
  return 'warning'
}

function orderStatusLabel(status: string) {
  return orderStatusMap[status] || status || '-'
}

function orderStatusTag(status: string) {
  if (['PAID', 'SHIPPED'].includes(status)) return 'success'
  if (['REFUND_REQUESTED', 'PENDING_PAY'].includes(status)) return 'warning'
  if (['REFUNDED', 'CANCELLED'].includes(status)) return 'danger'
  return 'info'
}

function announcementStatusLabel(status: string) {
  return announcementStatusMap[status] || status || '-'
}

function announcementStatusTag(status: string) {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'OFFLINE') return 'info'
  return 'warning'
}

const appointmentStatusRows = computed(() =>
  Object.entries(stats.value.appointmentStatusCounts || {}).map(([key, value]) => ({
    label: appointmentStatusLabel(key),
    count: value
  }))
)
const orderStatusRows = computed(() =>
  Object.entries(stats.value.orderStatusCounts || {}).map(([key, value]) => ({
    label: orderStatusLabel(key),
    count: value
  }))
)
const commonSymptomRows = computed(() =>
  Object.entries(stats.value.commonSymptoms || {}).map(([key, value]) => ({
    label: key,
    count: value
  }))
)
const peakHourRows = computed(() =>
  Object.entries(stats.value.peakHours || {}).map(([key, value]) => ({
    label: key,
    count: value
  }))
)
const medicineSalesRows = computed(() =>
  Object.entries(stats.value.medicineSales || {}).map(([key, value]) => ({
    label: key,
    count: value
  }))
)
const doctorWorkloadRows = computed(() => stats.value.doctorWorkload || [])

async function loadAll() {
  stats.value = await apiGet('/admin/stats')
  users.value = await apiGet('/admin/users')
  doctors.value = await apiGet('/admin/doctors')
  medicines.value = await apiGet('/admin/medicines')
  announcements.value = await apiGet('/admin/announcements')
  orders.value = await apiGet('/admin/orders')
  records.value = await apiGet('/admin/records')
  prescriptions.value = await apiGet('/admin/prescriptions')
  appointments.value = await apiGet('/admin/appointments')
  logs.value = await apiGet('/admin/logs')
}

async function createUser() {
  await apiPost('/admin/users', userForm)
  ElMessage.success('账号已新增')
  await loadAll()
}

async function resetPassword(id: number) {
  await apiPut(`/admin/users/${id}/password`, { password: '123456' })
  ElMessage.success('密码已重置为 123456')
  await loadAll()
}

async function toggleUser(row: any) {
  await apiPut(`/admin/users/${row.id}/status`, { status: row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED' })
  ElMessage.success('账号状态已更新')
  await loadAll()
}

async function reviewDoctor(id: number, status: string) {
  await apiPut(`/admin/doctors/${id}/review`, { status, comment: status === 'APPROVED' ? '审核通过' : '资质不足' })
  ElMessage.success('审核状态已更新')
  await loadAll()
}

function editMedicine(row: any) {
  Object.assign(medicineForm, row)
}

async function saveMedicine() {
  if (medicineForm.id) await apiPut(`/admin/medicines/${medicineForm.id}`, medicineForm)
  else await apiPost('/admin/medicines', medicineForm)
  ElMessage.success('药品已保存')
  Object.assign(medicineForm, { id: undefined, name: '', effect: '', usageInstruction: '', price: 0, stock: 0, onShelf: true })
  await loadAll()
}

function editAnnouncement(row: any) {
  Object.assign(announcementForm, row)
}

async function saveAnnouncement() {
  if (announcementForm.id) await apiPut(`/admin/announcements/${announcementForm.id}`, announcementForm)
  else await apiPost('/admin/announcements', announcementForm)
  ElMessage.success('公告已保存')
  Object.assign(announcementForm, { id: undefined, title: '', category: '通知', content: '', status: 'PUBLISHED' })
  await loadAll()
}

async function updateOrder(id: number, status: string) {
  await apiPut(`/admin/orders/${id}/status`, { status })
  ElMessage.success('订单状态已更新')
  await loadAll()
}

function logout() {
  auth.logout()
  router.push('/login')
}

onMounted(loadAll)
</script>

<style scoped>
.narrow-form {
  max-width: 760px;
  margin-bottom: 18px;
}

.mt {
  margin-top: 12px;
}

.stat-card {
  border: 1px solid #dfe9eb;
  border-radius: 8px;
}

.expand-box {
  padding: 8px 4px;
}
</style>
