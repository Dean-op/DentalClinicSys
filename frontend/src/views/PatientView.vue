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
          <el-tab-pane label="公告" name="announcements">
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
              <el-table-column label="操作" width="140">
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

          <el-tab-pane label="订单/预约" name="orders">
            <el-divider content-position="left">药品订单</el-divider>
            <el-table :data="orders" stripe>
              <el-table-column label="订单号" prop="order.orderNo" width="180" />
              <el-table-column label="金额" prop="order.totalAmount" width="100" />
              <el-table-column label="配送" prop="order.deliveryMethod" width="110" />
              <el-table-column label="状态" prop="order.status" width="140" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button type="warning" link @click="refund(row.order.id)">退款</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-divider content-position="left">预约记录</el-divider>
            <el-table :data="appointments" stripe>
              <el-table-column label="医生" prop="doctor.name" width="120" />
              <el-table-column label="日期" prop="appointment.visitDate" width="130" />
              <el-table-column label="时间" prop="appointment.timeSlot" width="110" />
              <el-table-column label="状态" prop="appointment.status" width="140" />
              <el-table-column label="症状" prop="appointment.symptoms" />
              <el-table-column label="操作" width="110">
                <template #default="{ row }">
                  <el-button type="danger" link @click="cancelAppointment(row.appointment.id)">取消</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="病例/AI" name="ai">
            <el-row :gutter="16">
              <el-col :md="12" :xs="24">
                <el-input v-model="symptomInput" type="textarea" :rows="5" placeholder="请输入牙痛、牙龈出血、口臭等症状" />
                <el-button type="primary" icon="MagicStick" style="margin-top: 10px" @click="consult">AI牙医初评</el-button>
                <el-alert v-if="consultResult" class="mt" type="warning" :closable="false" :title="consultResult.disclaimer" />
                <div v-if="consultResult" class="result">
                  <div class="muted">模型：{{ consultResult.model }} · {{ consultResult.provider }}</div>
                  <pre>{{ consultResult.answer }}</pre>
                </div>
              </el-col>
              <el-col :md="12" :xs="24">
                <el-divider content-position="left">既往病例</el-divider>
                <el-table :data="records" stripe>
                  <el-table-column prop="diagnosis" label="诊断" />
                  <el-table-column prop="treatmentPlan" label="治疗方案" />
                  <el-table-column prop="createdAt" label="时间" width="180" />
                </el-table>
                <el-divider content-position="left">用药提醒</el-divider>
                <el-table :data="reminders" stripe>
                  <el-table-column prop="medicineName" label="药品" />
                  <el-table-column prop="expectedRunOutDate" label="预计用完" />
                </el-table>
              </el-col>
            </el-row>
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
const activeTab = ref('announcements')
const tabs = [
  { name: 'announcements', label: '公告浏览', icon: 'Bell' },
  { name: 'doctors', label: '医生查询', icon: 'Avatar' },
  { name: 'medicines', label: '药品购买', icon: 'ShoppingBag' },
  { name: 'appointment', label: '在线预约', icon: 'Calendar' },
  { name: 'orders', label: '订单预约', icon: 'Tickets' },
  { name: 'ai', label: '病例与AI', icon: 'MagicStick' }
]

const announcements = ref<any[]>([])
const doctors = ref<any[]>([])
const medicines = ref<any[]>([])
const selectedMedicines = ref<any[]>([])
const orders = ref<any[]>([])
const appointments = ref<any[]>([])
const records = ref<any[]>([])
const reminders = ref<any[]>([])
const deliveryMethod = ref('到店自取')
const symptomInput = ref('')
const consultResult = ref<any>(null)
const appointmentForm = reactive({ doctorId: undefined as number | undefined, visitDate: '', timeSlot: '09:00', symptoms: '', demand: '' })

async function loadAll() {
  announcements.value = await apiGet('/patient/announcements')
  doctors.value = await apiGet('/patient/doctors')
  medicines.value = await apiGet('/patient/medicines')
  orders.value = await apiGet('/patient/orders')
  appointments.value = await apiGet('/patient/appointments')
  records.value = await apiGet('/patient/records')
  reminders.value = await apiGet('/patient/reminders')
}

function selectDoctor(id: number) {
  appointmentForm.doctorId = id
  activeTab.value = 'appointment'
}

async function submitOrder() {
  if (!selectedMedicines.value.length) return ElMessage.warning('请先选择药品')
  await apiPost('/patient/orders', {
    deliveryMethod: deliveryMethod.value,
    items: selectedMedicines.value.map((item) => ({ medicineId: item.id, quantity: 1 }))
  })
  ElMessage.success('下单成功')
  await loadAll()
}

async function createAppointment() {
  await apiPost('/patient/appointments', appointmentForm)
  ElMessage.success('预约已提交')
  activeTab.value = 'orders'
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
  consultResult.value = await apiPost('/patient/ai/consult', { symptoms: symptomInput.value })
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
</style>
