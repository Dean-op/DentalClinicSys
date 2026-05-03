<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="brand">Dental Clinic</div>
      <div class="role-pill"><el-icon><FirstAidKit /></el-icon> 医生端</div>
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
          <div class="page-title">医生工作台</div>
          <div class="muted">{{ profile?.name }} · {{ profile?.reviewStatus }}</div>
        </div>
        <el-button icon="SwitchButton" @click="logout">退出</el-button>
      </header>

      <section class="panel">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="个人资料" name="profile">
            <el-form :model="profileForm" label-width="90px" class="narrow-form">
              <el-form-item label="姓名"><el-input v-model="profileForm.name" /></el-form-item>
              <el-form-item label="职称"><el-input v-model="profileForm.title" /></el-form-item>
              <el-form-item label="科室"><el-input v-model="profileForm.department" /></el-form-item>
              <el-form-item label="擅长"><el-input v-model="profileForm.specialty" /></el-form-item>
              <el-form-item label="简介"><el-input v-model="profileForm.introduction" type="textarea" /></el-form-item>
              <el-button type="primary" icon="Check" @click="saveProfile">保存资料</el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="排班" name="schedules">
            <div class="toolbar">
              <el-date-picker v-model="scheduleForm.workDate" value-format="YYYY-MM-DD" />
              <el-time-select v-model="scheduleForm.startTime" start="08:00" step="00:30" end="17:00" />
              <el-time-select v-model="scheduleForm.endTime" start="09:00" step="00:30" end="18:00" />
              <el-input-number v-model="scheduleForm.capacity" :min="1" />
              <el-button type="primary" icon="Plus" @click="createSchedule">新增排班</el-button>
            </div>
            <el-table :data="schedules" stripe>
              <el-table-column prop="workDate" label="日期" />
              <el-table-column prop="startTime" label="开始" />
              <el-table-column prop="endTime" label="结束" />
              <el-table-column prop="capacity" label="容量" />
              <el-table-column prop="bookedCount" label="已约" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="预约处理" name="appointments">
            <div class="toolbar">
              <el-date-picker v-model="filterDate" value-format="YYYY-MM-DD" placeholder="按日期筛选" />
              <el-button icon="Refresh" @click="loadAppointments">刷新</el-button>
            </div>
            <el-table :data="appointments" stripe>
              <el-table-column label="患者" prop="patient.name" width="120" />
              <el-table-column label="日期" prop="appointment.visitDate" width="130" />
              <el-table-column label="时间" prop="appointment.timeSlot" width="110" />
              <el-table-column label="症状" prop="appointment.symptoms" />
              <el-table-column label="状态" prop="appointment.status" width="140" />
              <el-table-column label="操作" width="230">
                <template #default="{ row }">
                  <el-button type="success" link @click="updateAppointment(row.appointment.id, 'CONFIRMED')">确认</el-button>
                  <el-button type="warning" link @click="updateAppointment(row.appointment.id, 'REJECTED')">拒绝</el-button>
                  <el-button type="primary" link @click="startRecord(row)">接诊</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="病例处方" name="records">
            <el-form :model="recordForm" label-width="90px">
              <el-form-item label="患者ID"><el-input-number v-model="recordForm.patientId" :min="1" /></el-form-item>
              <el-form-item label="预约ID"><el-input-number v-model="recordForm.appointmentId" :min="1" /></el-form-item>
              <el-form-item label="主诉"><el-input v-model="recordForm.chiefComplaint" /></el-form-item>
              <el-form-item label="现病史"><el-input v-model="recordForm.presentIllness" type="textarea" /></el-form-item>
              <el-form-item label="检查"><el-input v-model="recordForm.examination" type="textarea" /></el-form-item>
              <el-form-item label="诊断"><el-input v-model="recordForm.diagnosis" /></el-form-item>
              <el-form-item label="方案"><el-input v-model="recordForm.treatmentPlan" type="textarea" /></el-form-item>
              <el-button type="primary" icon="DocumentChecked" @click="saveRecord">保存病例</el-button>
            </el-form>

            <el-divider content-position="left">电子处方</el-divider>
            <div class="toolbar">
              <el-input-number v-model="prescriptionForm.recordId" :min="1" placeholder="病例ID" />
              <el-input-number v-model="prescriptionForm.patientId" :min="1" placeholder="患者ID" />
              <el-select v-model="prescriptionMedicineId" placeholder="选择药品" style="width: 220px">
                <el-option v-for="item in medicines" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
              <el-input v-model="prescriptionForm.note" placeholder="医嘱" style="width: 240px" />
              <el-button type="success" icon="Plus" @click="createPrescription">开具药单</el-button>
            </div>
          </el-tab-pane>

          <el-tab-pane label="留言统计" name="stats">
            <div class="metric-grid">
              <div class="metric"><strong>{{ stats.appointmentCount || 0 }}</strong><span>预约量</span></div>
              <div class="metric"><strong>{{ stats.completedCount || 0 }}</strong><span>接诊量</span></div>
              <div class="metric"><strong>{{ stats.noShowCount || 0 }}</strong><span>爽约</span></div>
              <div class="metric"><strong>{{ stats.rating || '-' }}</strong><span>评分</span></div>
            </div>
            <el-table :data="messages" stripe>
              <el-table-column prop="question" label="用户留言" />
              <el-table-column prop="reply" label="回复" />
              <el-table-column label="操作" width="160">
                <template #default="{ row }">
                  <el-button type="primary" link @click="reply(row.id)">回复</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { apiGet, apiPost, apiPut } from '../api'
import { useAuthStore } from '../store'

const router = useRouter()
const auth = useAuthStore()
const activeTab = ref('profile')
const tabs = [
  { name: 'profile', label: '个人信息', icon: 'User' },
  { name: 'schedules', label: '出诊排班', icon: 'Calendar' },
  { name: 'appointments', label: '预约处理', icon: 'Tickets' },
  { name: 'records', label: '病例处方', icon: 'Document' },
  { name: 'stats', label: '留言统计', icon: 'DataAnalysis' }
]

const profile = ref<any>(null)
const profileForm = reactive<any>({})
const schedules = ref<any[]>([])
const appointments = ref<any[]>([])
const messages = ref<any[]>([])
const medicines = ref<any[]>([])
const stats = ref<any>({})
const filterDate = ref('')
const scheduleForm = reactive({ workDate: '', startTime: '09:00', endTime: '12:00', capacity: 8, bookedCount: 0 })
const recordForm = reactive<any>({ patientId: 1, appointmentId: undefined, chiefComplaint: '', presentIllness: '', examination: '', diagnosis: '', treatmentPlan: '' })
const prescriptionForm = reactive<any>({ recordId: undefined, patientId: 1, note: '' })
const prescriptionMedicineId = ref<number>()

async function loadAll() {
  profile.value = await apiGet('/doctor/profile')
  Object.assign(profileForm, profile.value)
  schedules.value = await apiGet('/doctor/schedules')
  medicines.value = await apiGet('/doctor/medicines').catch(() => [])
  await loadAppointments()
  messages.value = await apiGet('/doctor/messages').catch(() => [])
  stats.value = await apiGet('/doctor/stats').catch(() => ({}))
}

async function saveProfile() {
  profile.value = await apiPut('/doctor/profile', profileForm)
  ElMessage.success('资料已保存')
}

async function createSchedule() {
  await apiPost('/doctor/schedules', scheduleForm)
  ElMessage.success('排班已新增')
  schedules.value = await apiGet('/doctor/schedules')
}

async function loadAppointments() {
  appointments.value = await apiGet('/doctor/appointments', filterDate.value ? { date: filterDate.value } : undefined)
}

async function updateAppointment(id: number, status: string) {
  await apiPut(`/doctor/appointments/${id}`, { status })
  ElMessage.success('预约状态已更新')
  await loadAppointments()
}

function startRecord(row: any) {
  recordForm.patientId = row.patient.id
  recordForm.appointmentId = row.appointment.id
  recordForm.chiefComplaint = row.appointment.symptoms
  activeTab.value = 'records'
}

async function saveRecord() {
  const saved = await apiPost<any>('/doctor/records', recordForm)
  prescriptionForm.recordId = saved.id
  prescriptionForm.patientId = saved.patientId
  ElMessage.success('病例已保存')
}

async function createPrescription() {
  if (!prescriptionMedicineId.value) return ElMessage.warning('请选择药品')
  await apiPost('/doctor/prescriptions', {
    prescription: prescriptionForm,
    items: [{ medicineId: prescriptionMedicineId.value, frequency: '每日2次', dosage: '遵医嘱', days: 3 }]
  })
  ElMessage.success('处方已开具，并生成用药提醒')
}

async function reply(id: number) {
  const { value } = await ElMessageBox.prompt('请输入回复内容', '留言回复')
  await apiPut(`/doctor/messages/${id}/reply`, { reply: value })
  ElMessage.success('已回复')
  messages.value = await apiGet('/doctor/messages')
}

function logout() {
  auth.logout()
  router.push('/login')
}

onMounted(loadAll)
</script>

<style scoped>
.narrow-form {
  max-width: 680px;
}
</style>
