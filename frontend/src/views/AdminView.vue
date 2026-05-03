<template>
  <div class="shell">
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
              <div class="metric"><strong>{{ stats.appointments || 0 }}</strong><span>预约量</span></div>
              <div class="metric"><strong>{{ stats.orders || 0 }}</strong><span>订单量</span></div>
              <div class="metric"><strong>{{ stats.records || 0 }}</strong><span>病例数</span></div>
            </div>
            <el-descriptions title="药品销量" border :column="2">
              <el-descriptions-item v-for="(value, key) in stats.medicineSales || {}" :key="key" :label="String(key)">
                {{ value }}
              </el-descriptions-item>
            </el-descriptions>
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
              <el-table-column prop="username" label="账号" />
              <el-table-column label="角色">
                <template #default="{ row }">{{ roleLabel(row.role) }}</template>
              </el-table-column>
              <el-table-column label="状态">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">{{ accountStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button type="warning" link @click="resetPassword(row.id)">重置密码</el-button>
                  <el-button type="danger" link @click="toggleUser(row)">禁/启用</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="医生审核" name="doctors">
            <el-table :data="doctors" stripe>
              <el-table-column prop="name" label="医生" />
              <el-table-column prop="title" label="职称" />
              <el-table-column prop="department" label="科室" />
              <el-table-column label="审核状态">
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
              <el-table-column label="上架">
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
                  <el-option label="发布" value="PUBLISHED" />
                  <el-option label="草稿" value="DRAFT" />
                  <el-option label="下线" value="OFFLINE" />
                </el-select>
              </el-form-item>
              <el-form-item label="内容"><el-input v-model="announcementForm.content" type="textarea" /></el-form-item>
              <el-button type="primary" icon="Promotion" @click="saveAnnouncement">保存公告</el-button>
            </el-form>
            <el-table :data="announcements" stripe @row-click="editAnnouncement">
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="category" label="分类" />
              <el-table-column label="状态">
                <template #default="{ row }">
                  <el-tag :type="announcementStatusTag(row.status)">{{ announcementStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="监督与日志" name="audit">
            <el-divider content-position="left">订单</el-divider>
            <el-table :data="orders" stripe>
              <el-table-column prop="order.orderNo" label="订单号" />
              <el-table-column prop="order.totalAmount" label="金额" />
              <el-table-column label="状态">
                <template #default="{ row }">
                  <el-tag :type="orderStatusTag(row.order.status)">{{ orderStatusLabel(row.order.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button type="success" link @click="updateOrder(row.order.id, 'REFUNDED')">退款通过</el-button>
                  <el-button type="primary" link @click="updateOrder(row.order.id, 'SHIPPED')">发货</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-divider content-position="left">病例质控</el-divider>
            <el-table :data="records" stripe>
              <el-table-column prop="diagnosis" label="诊断" />
              <el-table-column prop="treatmentPlan" label="治疗方案" />
              <el-table-column prop="createdAt" label="记录时间" />
            </el-table>
            <el-divider content-position="left">系统日志</el-divider>
            <el-table :data="logs" stripe>
              <el-table-column label="角色">
                <template #default="{ row }">{{ roleLabel(row.operatorRole) }}</template>
              </el-table-column>
              <el-table-column prop="action" label="操作" />
              <el-table-column prop="detail" label="详情" />
              <el-table-column prop="createdAt" label="时间" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
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
  { name: 'audit', label: '监督日志', icon: 'Files' }
]

const stats = ref<any>({})
const users = ref<any[]>([])
const doctors = ref<any[]>([])
const medicines = ref<any[]>([])
const announcements = ref<any[]>([])
const orders = ref<any[]>([])
const records = ref<any[]>([])
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
const announcementStatusMap: Record<string, string> = {
  DRAFT: '草稿',
  SCHEDULED: '定时发布',
  PUBLISHED: '已发布',
  OFFLINE: '已下线'
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

function roleLabel(role: string) {
  return roleMap[role] || role || '-'
}

function accountStatusLabel(status: string) {
  return accountStatusMap[status] || status || '-'
}

function reviewStatusLabel(status: string) {
  return reviewStatusMap[status] || status || '-'
}

function announcementStatusLabel(status: string) {
  return announcementStatusMap[status] || status || '-'
}

function orderStatusLabel(status: string) {
  return orderStatusMap[status] || status || '-'
}

function reviewStatusTag(status: string) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

function announcementStatusTag(status: string) {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'OFFLINE') return 'info'
  return 'warning'
}

function orderStatusTag(status: string) {
  if (['PAID', 'SHIPPED'].includes(status)) return 'success'
  if (['REFUND_REQUESTED', 'PENDING_PAY'].includes(status)) return 'warning'
  if (['REFUNDED', 'CANCELLED'].includes(status)) return 'danger'
  return 'info'
}

async function loadAll() {
  stats.value = await apiGet('/admin/stats')
  users.value = await apiGet('/admin/users')
  doctors.value = await apiGet('/admin/doctors')
  medicines.value = await apiGet('/admin/medicines')
  announcements.value = await apiGet('/admin/announcements')
  orders.value = await apiGet('/admin/orders')
  records.value = await apiGet('/admin/records')
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
}

async function toggleUser(row: any) {
  await apiPut(`/admin/users/${row.id}/status`, { status: row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED' })
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
</style>
