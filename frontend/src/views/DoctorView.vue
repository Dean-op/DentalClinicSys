<template>
  <div class="shell theme-doctor">
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
          <div class="muted">{{ profile?.name }} · {{ reviewStatusLabel(profile?.reviewStatus) }}</div>
        </div>
        <el-button icon="SwitchButton" @click="logout">退出</el-button>
      </header>

      <section class="panel">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="个人信息" name="profile">
            <el-alert v-if="profile?.reviewStatus !== 'APPROVED'" type="warning" :closable="false" title="医生账号审核通过后，才可以维护排班、处理预约和接诊。" />
            <el-form :model="profileForm" label-width="90px" class="narrow-form mt">
              <el-form-item label="姓名"><el-input v-model="profileForm.name" /></el-form-item>
              <el-form-item label="职称"><el-input v-model="profileForm.title" /></el-form-item>
              <el-form-item label="科室"><el-input v-model="profileForm.department" /></el-form-item>
              <el-form-item label="擅长"><el-input v-model="profileForm.specialty" /></el-form-item>
              <el-form-item label="简介"><el-input v-model="profileForm.introduction" type="textarea" /></el-form-item>
              <el-button type="primary" icon="Check" @click="saveProfile">保存资料</el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="出诊排班" name="schedules">
            <el-alert type="info" :closable="false" title="医生只能维护自己的未来空号；已有预约或历史排班不能修改/删除。" />
            <div class="toolbar mt">
              <el-date-picker v-model="scheduleForm.workDate" value-format="YYYY-MM-DD" />
              <el-time-select v-model="scheduleForm.startTime" start="08:00" step="00:30" end="17:00" />
              <el-time-select v-model="scheduleForm.endTime" start="09:00" step="00:30" end="18:00" />
              <el-input-number v-model="scheduleForm.capacity" :min="1" />
              <el-button type="primary" icon="Plus" @click="saveSchedule">{{ scheduleForm.id ? '保存排班' : '新增排班' }}</el-button>
              <el-button @click="resetSchedule">清空</el-button>
            </div>
            <el-table :data="schedules" stripe>
              <el-table-column prop="workDate" label="日期" />
              <el-table-column prop="startTime" label="开始" />
              <el-table-column prop="endTime" label="结束" />
              <el-table-column prop="capacity" label="容量" />
              <el-table-column prop="bookedCount" label="已约" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button link type="primary" :disabled="row.bookedCount > 0" @click="editSchedule(row)">编辑</el-button>
                  <el-button link type="danger" :disabled="row.bookedCount > 0" @click="deleteSchedule(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="预约审核" name="appointments">
            <el-alert type="info" :closable="false" title="此处只处理患者提交的新预约。确认、拒绝、改期都会写入预约状态，并生成患者端预约通知。" />
            <div class="toolbar mt">
              <el-date-picker v-model="filterDate" value-format="YYYY-MM-DD" placeholder="按日期筛选" />
              <el-button icon="Refresh" @click="loadAppointments">刷新</el-button>
            </div>
            <el-table :data="reviewAppointments" stripe>
              <el-table-column label="患者" prop="patient.name" width="120" />
              <el-table-column label="日期" prop="appointment.visitDate" width="130" />
              <el-table-column label="时间" prop="appointment.timeSlot" width="110" />
              <el-table-column label="症状" prop="appointment.symptoms" />
              <el-table-column label="需求" prop="appointment.demand" />
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="appointmentStatusTag(row.appointment.status)">{{ appointmentStatusLabel(row.appointment.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220">
                <template #default="{ row }">
                  <el-button type="success" link @click="confirmAppointment(row)">确认</el-button>
                  <el-button type="warning" link @click="rescheduleAppointment(row)">改期</el-button>
                  <el-button type="danger" link @click="rejectAppointment(row)">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="待接诊" name="reception">
            <div class="toolbar">
              <el-date-picker v-model="receptionDate" value-format="YYYY-MM-DD" />
              <el-button icon="Refresh" @click="loadAppointments">刷新接诊队列</el-button>
            </div>
            <el-table :data="receptionAppointments" stripe>
              <el-table-column label="顺序" type="index" width="70" />
              <el-table-column label="患者" prop="patient.name" width="120" />
              <el-table-column label="时间" prop="appointment.timeSlot" width="110" />
              <el-table-column label="症状" prop="appointment.symptoms" />
              <el-table-column label="状态" width="130">
                <template #default="{ row }">
                  <el-tag :type="appointmentStatusTag(row.appointment.status)">{{ appointmentStatusLabel(row.appointment.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140">
                <template #default="{ row }">
                  <el-button type="primary" link @click="startReception(row)">进入接诊</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="接诊管理" name="records">
            <el-alert v-if="!selectedEncounter" type="warning" :closable="false" title="请先从“待接诊”选择患者进入接诊。" />
            <div v-else>
              <el-descriptions border :column="3" size="small" class="mb">
                <el-descriptions-item label="患者">{{ selectedEncounter.patient.name }}</el-descriptions-item>
                <el-descriptions-item label="预约日期">{{ selectedEncounter.appointment.visitDate }}</el-descriptions-item>
                <el-descriptions-item label="预约时间">{{ selectedEncounter.appointment.timeSlot }}</el-descriptions-item>
                <el-descriptions-item label="症状">{{ selectedEncounter.appointment.symptoms }}</el-descriptions-item>
                <el-descriptions-item label="需求">{{ selectedEncounter.appointment.demand || '-' }}</el-descriptions-item>
                <el-descriptions-item label="状态">{{ appointmentStatusLabel(selectedEncounter.appointment.status) }}</el-descriptions-item>
              </el-descriptions>

              <el-row :gutter="16">
                <el-col :md="15" :xs="24">
                  <el-form :model="recordForm" label-width="90px">
                    <el-form-item label="主诉"><el-input v-model="recordForm.chiefComplaint" /></el-form-item>
                    <el-form-item label="现病史"><el-input v-model="recordForm.presentIllness" type="textarea" /></el-form-item>
                    <el-form-item label="检查结果"><el-input v-model="recordForm.examination" type="textarea" /></el-form-item>
                    <el-form-item label="诊断意见"><el-input v-model="recordForm.diagnosis" /></el-form-item>
                    <el-form-item label="治疗方案"><el-input v-model="recordForm.treatmentPlan" type="textarea" /></el-form-item>
                    <el-form-item label="检查报告">
                      <el-upload :http-request="uploadReport" :show-file-list="false">
                        <el-button icon="Upload">上传图片/化验单</el-button>
                      </el-upload>
                      <el-link v-if="recordForm.reportImagePath" :href="recordForm.reportImagePath" target="_blank" type="primary" class="upload-link">查看已上传报告</el-link>
                    </el-form-item>
                    <el-button type="primary" icon="DocumentChecked" @click="saveRecord">保存病例并完成接诊</el-button>
                  </el-form>

                </el-col>

                <el-col :md="9" :xs="24">
                  <el-divider content-position="left">患者历史记录</el-divider>
                  <el-alert type="info" :closable="false" title="这里自动显示当前接诊患者的既往病例，用于连续性诊疗参考。" />
                  <el-table :data="patientHistory" size="small" stripe>
                    <el-table-column prop="diagnosis" label="诊断" />
                    <el-table-column prop="treatmentPlan" label="治疗方案" />
                    <el-table-column prop="createdAt" label="时间" width="170" />
                  </el-table>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>

          <el-tab-pane label="电子药单" name="prescriptions">
            <el-alert v-if="!selectedEncounter" type="warning" :closable="false" title="请先从“待接诊”进入患者接诊，再为本次病例开具药单。" />
            <el-alert v-else-if="!prescriptionForm.recordId" type="info" :closable="false" title="请先在“接诊管理”保存病例，系统会把药单关联到该病例。" />
            <div v-else>
              <el-descriptions border :column="3" size="small" class="mb">
                <el-descriptions-item label="患者">{{ selectedEncounter.patient.name }}</el-descriptions-item>
                <el-descriptions-item label="病例编号">{{ prescriptionForm.recordId }}</el-descriptions-item>
                <el-descriptions-item label="预约时间">{{ selectedEncounter.appointment.visitDate }} {{ selectedEncounter.appointment.timeSlot }}</el-descriptions-item>
              </el-descriptions>

              <el-row :gutter="16">
                <el-col :md="11" :xs="24">
                  <el-form label-width="80px">
                    <el-form-item label="药品">
                      <el-select v-model="prescriptionMedicineId" placeholder="选择药品" filterable>
                        <el-option v-for="item in medicines" :key="item.id" :label="`${item.name}（库存 ${item.stock}）`" :value="item.id" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="频次">
                      <el-input v-model="prescriptionItem.frequency" placeholder="如：每日2次" />
                    </el-form-item>
                    <el-form-item label="剂量">
                      <el-input v-model="prescriptionItem.dosage" placeholder="如：每次10ml" />
                    </el-form-item>
                    <el-form-item label="天数">
                      <el-input-number v-model="prescriptionItem.days" :min="1" />
                    </el-form-item>
                    <el-form-item label="医嘱">
                      <el-input v-model="prescriptionForm.note" type="textarea" placeholder="填写注意事项、复诊要求等" />
                    </el-form-item>
                    <el-button type="success" icon="Plus" @click="createPrescription">开具药单</el-button>
                  </el-form>
                </el-col>

                <el-col :md="13" :xs="24">
                  <el-divider content-position="left">药单预览</el-divider>
                  <div class="prescription-preview">
                    <div class="prescription-head">
                      <strong>口腔诊所电子药单</strong>
                      <span>{{ selectedEncounter.patient.name }}</span>
                    </div>
                    <el-table :data="prescriptionPreview" size="small" border>
                      <el-table-column prop="medicineName" label="药品" />
                      <el-table-column prop="frequency" label="频次" width="110" />
                      <el-table-column prop="dosage" label="剂量" width="130" />
                      <el-table-column prop="days" label="天数" width="80" />
                    </el-table>
                    <div class="prescription-note">医嘱：{{ prescriptionForm.note || '暂无' }}</div>
                  </div>

                  <el-divider content-position="left">已开药单</el-divider>
                  <div v-for="prescription in issuedPrescriptions" :key="prescription.prescription.id" class="issued-prescription">
                    <div class="prescription-head">
                      <strong>{{ prescription.prescription.note || '处方医嘱' }}</strong>
                      <span>{{ prescription.prescription.createdAt || '刚刚开具' }}</span>
                    </div>
                    <el-table :data="prescription.items" size="small">
                      <el-table-column prop="medicineName" label="药品" />
                      <el-table-column prop="frequency" label="频次" />
                      <el-table-column prop="dosage" label="剂量" />
                      <el-table-column prop="days" label="天数" width="80" />
                    </el-table>
                  </div>
                  <el-empty v-if="!issuedPrescriptions.length" description="本次接诊暂未开具药单" :image-size="70" />
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>

          <el-tab-pane label="历史查询" name="history">
            <el-alert type="info" :closable="false" title="历史查询用于查看当前医生接触过的患者记录。先从下拉框选择患者，再查询其既往病例。" />
            <div class="toolbar">
              <el-select v-model="historyPatientId" filterable placeholder="选择患者" style="width: 260px">
                <el-option
                  v-for="patient in knownPatients"
                  :key="patient.id"
                  :label="`${patient.name}（ID ${patient.id}）`"
                  :value="patient.id"
                />
              </el-select>
              <el-button type="primary" icon="Search" @click="loadPatientHistory(historyPatientId)">查询患者历史</el-button>
            </div>
            <el-table :data="historyRecords" stripe>
              <el-table-column prop="chiefComplaint" label="主诉" />
              <el-table-column prop="diagnosis" label="诊断" />
              <el-table-column prop="treatmentPlan" label="治疗方案" />
              <el-table-column prop="createdAt" label="时间" width="180" />
            </el-table>
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
              <el-table-column label="操作" width="120">
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { apiDelete, apiGet, apiPost, apiPut, http } from '../api'
import { useAuthStore } from '../store'

const router = useRouter()
const auth = useAuthStore()
const activeTab = ref('profile')
const today = new Date().toISOString().slice(0, 10)
const tabs = [
  { name: 'profile', label: '个人信息', icon: 'User' },
  { name: 'schedules', label: '出诊排班', icon: 'Calendar' },
  { name: 'appointments', label: '预约审核', icon: 'Tickets' },
  { name: 'reception', label: '待接诊', icon: 'FirstAidKit' },
  { name: 'records', label: '接诊管理', icon: 'Document' },
  { name: 'prescriptions', label: '电子药单', icon: 'Memo' },
  { name: 'history', label: '历史查询', icon: 'Search' },
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
const receptionDate = ref(today)
const selectedEncounter = ref<any>(null)
const patientHistory = ref<any[]>([])
const historyRecords = ref<any[]>([])
const historyPatientId = ref<number>()
const issuedPrescriptions = ref<any[]>([])
const scheduleForm = reactive<any>({ id: undefined, workDate: '', startTime: '09:00', endTime: '12:00', capacity: 8, bookedCount: 0 })
const recordForm = reactive<any>({ patientId: undefined, appointmentId: undefined, chiefComplaint: '', presentIllness: '', examination: '', diagnosis: '', treatmentPlan: '', reportImagePath: '' })
const prescriptionForm = reactive<any>({ recordId: undefined, patientId: undefined, note: '' })
const prescriptionItem = reactive<any>({ frequency: '每日2次', dosage: '遵医嘱', days: 3 })
const prescriptionMedicineId = ref<number>()

const appointmentStatusMap: Record<string, string> = {
  SUBMITTED: '待审核',
  CONFIRMED: '已确认',
  REJECTED: '已拒绝',
  RESCHEDULED: '已改期',
  CANCELLED: '已取消',
  COMPLETED: '已完成',
  NO_SHOW: '爽约'
}

const reviewStatusMap: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝'
}

const reviewAppointments = computed(() => appointments.value.filter((row) => row.appointment.status === 'SUBMITTED'))
const receptionAppointments = computed(() =>
  appointments.value.filter((row) =>
    ['CONFIRMED', 'RESCHEDULED'].includes(row.appointment.status) &&
    row.appointment.visitDate === receptionDate.value
  )
)
const knownPatients = computed(() => {
  const map = new Map<number, any>()
  appointments.value.forEach((row) => {
    if (row.patient?.id) map.set(row.patient.id, row.patient)
  })
  return Array.from(map.values())
})
const selectedMedicine = computed(() => medicines.value.find((item) => item.id === prescriptionMedicineId.value))
const prescriptionPreview = computed(() => {
  if (!selectedMedicine.value) return []
  return [{
    medicineName: selectedMedicine.value.name,
    frequency: prescriptionItem.frequency || '-',
    dosage: prescriptionItem.dosage || '-',
    days: prescriptionItem.days || '-'
  }]
})

function appointmentStatusLabel(status: string) {
  return appointmentStatusMap[status] || status || '-'
}

function reviewStatusLabel(status?: string) {
  return status ? reviewStatusMap[status] || status : '-'
}

function appointmentStatusTag(status: string) {
  if (['CONFIRMED', 'RESCHEDULED'].includes(status)) return 'success'
  if (['REJECTED', 'CANCELLED', 'NO_SHOW'].includes(status)) return 'danger'
  if (status === 'COMPLETED') return 'info'
  return 'warning'
}

async function loadAll() {
  profile.value = await apiGet('/doctor/profile')
  Object.assign(profileForm, profile.value)
  schedules.value = await apiGet('/doctor/schedules').catch(() => [])
  medicines.value = await apiGet('/doctor/medicines').catch(() => [])
  await loadAppointments()
  messages.value = await apiGet('/doctor/messages').catch(() => [])
  stats.value = await apiGet('/doctor/stats').catch(() => ({}))
}

async function saveProfile() {
  profile.value = await apiPut('/doctor/profile', profileForm)
  ElMessage.success('资料已保存')
}

function resetSchedule() {
  Object.assign(scheduleForm, { id: undefined, workDate: '', startTime: '09:00', endTime: '12:00', capacity: 8, bookedCount: 0 })
}

function editSchedule(row: any) {
  Object.assign(scheduleForm, row)
}

async function saveSchedule() {
  if (scheduleForm.id) await apiPut(`/doctor/schedules/${scheduleForm.id}`, scheduleForm)
  else await apiPost('/doctor/schedules', scheduleForm)
  ElMessage.success('排班已保存')
  resetSchedule()
  schedules.value = await apiGet('/doctor/schedules')
}

async function deleteSchedule(id: number) {
  await apiDelete(`/doctor/schedules/${id}`)
  ElMessage.success('排班已删除')
  schedules.value = await apiGet('/doctor/schedules')
}

async function loadAppointments() {
  appointments.value = await apiGet('/doctor/appointments', filterDate.value ? { date: filterDate.value } : undefined)
}

async function confirmAppointment(row: any) {
  await apiPut(`/doctor/appointments/${row.appointment.id}`, { status: 'CONFIRMED' })
  ElMessage.success('已确认预约，并通知患者')
  await loadAppointments()
}

async function rejectAppointment(row: any) {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因，会展示给患者', '拒绝预约')
  await apiPut(`/doctor/appointments/${row.appointment.id}`, { status: 'REJECTED', reason: value })
  ElMessage.success('已拒绝预约，并通知患者')
  await loadAppointments()
}

async function rescheduleAppointment(row: any) {
  const { value } = await ElMessageBox.prompt('请输入新时间，格式：YYYY-MM-DD HH:mm', '调整预约时间')
  const [visitDate, timeSlot] = value.split(' ')
  await apiPut(`/doctor/appointments/${row.appointment.id}`, {
    status: 'RESCHEDULED',
    reason: `医生建议改期至 ${visitDate} ${timeSlot}`,
    visitDate,
    timeSlot
  })
  ElMessage.success('已调整预约，并通知患者')
  await loadAppointments()
}

async function startReception(row: any) {
  selectedEncounter.value = row
  Object.assign(recordForm, {
    patientId: row.patient.id,
    appointmentId: row.appointment.id,
    chiefComplaint: row.appointment.symptoms,
    presentIllness: '',
    examination: '',
    diagnosis: '',
    treatmentPlan: '',
    reportImagePath: ''
  })
  Object.assign(prescriptionForm, { recordId: undefined, patientId: row.patient.id, note: '' })
  prescriptionMedicineId.value = undefined
  issuedPrescriptions.value = []
  await loadPatientHistory(row.patient.id, true)
  activeTab.value = 'records'
}

async function uploadReport(options: any) {
  const form = new FormData()
  form.append('file', options.file)
  const response = await http.post('/files/upload', form)
  recordForm.reportImagePath = response.data.data.path
  ElMessage.success('报告已上传')
}

async function saveRecord() {
  if (!selectedEncounter.value) return ElMessage.warning('请先选择接诊患者')
  const saved = await apiPost<any>('/doctor/records', recordForm)
  prescriptionForm.recordId = saved.id
  prescriptionForm.patientId = saved.patientId
  ElMessage.success('病例已保存，预约已标记为完成并通知患者，可继续开具电子药单')
  await loadAppointments()
  activeTab.value = 'prescriptions'
}

async function createPrescription() {
  if (!prescriptionForm.recordId) return ElMessage.warning('请先保存病例')
  if (!prescriptionMedicineId.value) return ElMessage.warning('请选择药品')
  const medicine = selectedMedicine.value
  const item = {
    medicineId: prescriptionMedicineId.value,
    medicineName: medicine?.name || '未命名药品',
    frequency: prescriptionItem.frequency,
    dosage: prescriptionItem.dosage,
    days: prescriptionItem.days
  }
  const saved = await apiPost<any>('/doctor/prescriptions', {
    prescription: prescriptionForm,
    items: [item]
  })
  issuedPrescriptions.value.unshift({
    prescription: saved,
    items: [{ id: `${saved.id}-${Date.now()}`, ...item }]
  })
  ElMessage.success('处方已开具，并生成患者用药提醒')
  prescriptionMedicineId.value = undefined
}

async function loadPatientHistory(patientId?: number, forReception = false) {
  if (!patientId) return ElMessage.warning('请先选择患者')
  const rows = await apiGet(`/doctor/patients/${patientId}/records`)
  if (forReception) patientHistory.value = rows
  else historyRecords.value = rows
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
.mt {
  margin-top: 12px;
}
.mb {
  margin-bottom: 16px;
}
.upload-link {
  margin-left: 12px;
}
.prescription-preview,
.issued-prescription {
  border: 1px solid #dfe9eb;
  border-radius: 8px;
  padding: 12px;
  background: #fff;
}
.issued-prescription + .issued-prescription {
  margin-top: 10px;
}
.prescription-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  color: #2d4c52;
}
.prescription-note {
  margin-top: 10px;
  color: #4f6468;
  line-height: 1.6;
}
</style>
