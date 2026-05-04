<template>
  <div class="doctor-layout theme-doctor">
    <aside class="doctor-sidebar">
      <div class="sidebar-header">
        <el-icon class="brand-icon"><FirstAidKit /></el-icon>
        <span class="brand-text">口腔诊所管理系统</span>
        <el-tag size="small" effect="dark" round color="rgba(255,255,255,0.2)" class="role-tag">医生端</el-tag>
      </div>
      
      <div class="sidebar-menu">
        <div 
          v-for="item in tabs" 
          :key="item.name"
          class="menu-item"
          :class="{ active: activeTab === item.name }"
          @click="activeTab = item.name"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="doc-info">
          <el-avatar size="small" icon="UserFilled" class="doc-avatar" />
          <div class="doc-text">
            <div class="doc-name">{{ profile?.name || '医生' }}</div>
            <div class="doc-title">{{ profile?.department || '未分配科室' }}</div>
          </div>
        </div>
        <el-button link class="logout-btn" @click="logout">退出登录</el-button>
      </div>
    </aside>

    <main class="doctor-main">
      <header class="doctor-header">
        <div class="header-left">
          <h2 class="current-title">{{ currentTabLabel }}</h2>
        </div>
        <div class="header-right">
          <el-tag :type="profile?.reviewStatus === 'APPROVED' ? 'success' : 'warning'" round effect="light">
            状态: {{ reviewStatusLabel(profile?.reviewStatus) }}
          </el-tag>
        </div>
      </header>

      <div class="doctor-content">
        <div class="content-wrapper">
          <el-tabs v-model="activeTab" class="hidden-header-tabs">
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

          <el-tab-pane label="公告查看" name="announcements">
            <el-alert type="info" :closable="false" title="这里展示管理员已发布公告，方便医生及时查看门诊通知、工作安排和诊所活动。" />
            <el-table :data="announcements" stripe class="mt">
              <el-table-column prop="category" label="分类" width="120" />
              <el-table-column prop="title" label="标题" width="220" />
              <el-table-column prop="content" label="内容" min-width="280" />
              <el-table-column prop="publishAt" label="发布时间" width="180" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="药品查看" name="medicines">
            <el-alert type="info" :closable="false" title="医生可查看当前系统药品信息，用于开具处方前确认名称、功效、说明和库存情况。" />
            <el-table :data="medicines" stripe class="mt">
              <el-table-column prop="name" label="药品" width="180" />
              <el-table-column prop="effect" label="功效" min-width="180" />
              <el-table-column prop="usageInstruction" label="使用说明" min-width="260" />
              <el-table-column prop="price" label="价格" width="90" />
              <el-table-column prop="stock" label="库存" width="90" />
            </el-table>
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
              <el-date-picker v-model="reviewDate" value-format="YYYY-MM-DD" placeholder="按日期筛选" clearable />
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
            <el-alert type="info" :closable="false" title="默认显示当前医生全部待接诊记录，并按日期、时间顺序排序；可按日期筛选。" />
            <div class="toolbar">
              <el-date-picker v-model="receptionDate" value-format="YYYY-MM-DD" placeholder="按日期筛选" clearable />
              <el-button icon="Refresh" @click="loadAppointments">刷新接诊队列</el-button>
            </div>
            <el-table :data="receptionAppointments" stripe>
              <el-table-column label="日期" prop="appointment.visitDate" width="130" />
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
                    <el-button type="primary" icon="DocumentChecked" @click="saveRecord">{{ recordForm.id ? '更新病例' : '保存病例并完成接诊' }}</el-button>
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

          <el-tab-pane label="病例管理" name="history">
            <el-alert type="info" :closable="false" title="这里用于查看和维护当前医生本人创建的病例。可按患者和日期筛选，并回填到接诊表单继续编辑。" />
            <div class="toolbar">
              <el-select v-model="recordFilters.patientId" filterable clearable placeholder="按患者筛选" style="width: 260px">
                <el-option
                  v-for="patient in casePatients"
                  :key="patient.id"
                  :label="`${patient.name}（ID ${patient.id}）`"
                  :value="patient.id"
                />
              </el-select>
              <el-date-picker v-model="recordFilters.dateFrom" value-format="YYYY-MM-DD" clearable placeholder="开始日期" />
              <el-date-picker v-model="recordFilters.dateTo" value-format="YYYY-MM-DD" clearable placeholder="结束日期" />
              <el-button type="primary" icon="Search" @click="loadDoctorRecords">查询病例</el-button>
              <el-button @click="resetRecordFilters">重置</el-button>
            </div>
            <el-table :data="doctorRecords" stripe>
              <el-table-column label="患者" width="120">
                <template #default="{ row }">{{ row.patient?.name || '-' }}</template>
              </el-table-column>
              <el-table-column prop="record.chiefComplaint" label="主诉" min-width="180" />
              <el-table-column prop="record.diagnosis" label="诊断" min-width="160" />
              <el-table-column prop="record.treatmentPlan" label="治疗方案" min-width="220" />
              <el-table-column label="关联预约" width="180">
                <template #default="{ row }">
                  {{ row.appointment?.visitDate || '-' }} {{ row.appointment?.timeSlot || '' }}
                </template>
              </el-table-column>
              <el-table-column prop="record.createdAt" label="记录时间" width="180" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button type="primary" link @click="editDoctorRecord(row.record.id)">编辑病例</el-button>
                </template>
              </el-table-column>
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
                      <el-descriptions-item label="处方数量">{{ row.prescriptions?.length || 0 }}</el-descriptions-item>
                    </el-descriptions>
                  </div>
                </template>
              </el-table-column>
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
        </div>
      </div>
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
const tabs = [
  { name: 'profile', label: '个人信息', icon: 'User' },
  { name: 'announcements', label: '公告查看', icon: 'Bell' },
  { name: 'medicines', label: '药品查看', icon: 'ShoppingBag' },
  { name: 'schedules', label: '出诊排班', icon: 'Calendar' },
  { name: 'appointments', label: '预约审核', icon: 'Tickets' },
  { name: 'reception', label: '待接诊', icon: 'FirstAidKit' },
  { name: 'records', label: '接诊管理', icon: 'Document' },
  { name: 'prescriptions', label: '电子药单', icon: 'Memo' },
  { name: 'history', label: '病例管理', icon: 'Search' },
  { name: 'stats', label: '留言统计', icon: 'DataAnalysis' }
]

const currentTabLabel = computed(() => tabs.find(t => t.name === activeTab.value)?.label || '')

const profile = ref<any>(null)
const profileForm = reactive<any>({})
const announcements = ref<any[]>([])
const schedules = ref<any[]>([])
const appointments = ref<any[]>([])
const messages = ref<any[]>([])
const medicines = ref<any[]>([])
const stats = ref<any>({})
const reviewDate = ref('')
const receptionDate = ref('')
const selectedEncounter = ref<any>(null)
const patientHistory = ref<any[]>([])
const doctorRecords = ref<any[]>([])
const issuedPrescriptions = ref<any[]>([])
const recordFilters = reactive<{ patientId?: number; dateFrom: string; dateTo: string }>({ patientId: undefined, dateFrom: '', dateTo: '' })
const scheduleForm = reactive<any>({ id: undefined, workDate: '', startTime: '09:00', endTime: '12:00', capacity: 8, bookedCount: 0 })
const recordForm = reactive<any>({ id: undefined, patientId: undefined, appointmentId: undefined, chiefComplaint: '', presentIllness: '', examination: '', diagnosis: '', treatmentPlan: '', reportImagePath: '' })
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

const sortedAppointments = computed(() =>
  [...appointments.value].sort((left, right) => {
    const leftKey = `${left.appointment.visitDate || ''} ${left.appointment.timeSlot || ''}`
    const rightKey = `${right.appointment.visitDate || ''} ${right.appointment.timeSlot || ''}`
    return leftKey.localeCompare(rightKey)
  })
)
const reviewAppointments = computed(() =>
  sortedAppointments.value.filter((row) =>
    row.appointment.status === 'SUBMITTED' && matchesDateFilter(row.appointment.visitDate, reviewDate.value)
  )
)
const receptionAppointments = computed(() =>
  sortedAppointments.value.filter((row) =>
    ['CONFIRMED', 'RESCHEDULED'].includes(row.appointment.status) &&
    matchesDateFilter(row.appointment.visitDate, receptionDate.value)
  )
)
const knownPatients = computed(() => {
  const map = new Map<number, any>()
  appointments.value.forEach((row) => {
    if (row.patient?.id) map.set(row.patient.id, row.patient)
  })
  return Array.from(map.values())
})
const casePatients = computed(() => {
  const map = new Map<number, any>()
  knownPatients.value.forEach((patient) => map.set(patient.id, patient))
  doctorRecords.value.forEach((row) => {
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

function matchesDateFilter(visitDate?: string, filterDate?: string) {
  if (!filterDate) return true
  return visitDate === filterDate
}

async function loadAll() {
  profile.value = await apiGet('/doctor/profile')
  Object.assign(profileForm, profile.value)
  announcements.value = await apiGet('/doctor/announcements').catch(() => [])
  schedules.value = await apiGet('/doctor/schedules').catch(() => [])
  medicines.value = await apiGet('/doctor/medicines').catch(() => [])
  await loadAppointments()
  await loadDoctorRecords()
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
  appointments.value = await apiGet('/doctor/appointments')
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
    id: undefined,
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
  const isUpdate = Boolean(recordForm.id)
  const saved = await apiPost<any>('/doctor/records', recordForm)
  prescriptionForm.recordId = saved.id
  prescriptionForm.patientId = saved.patientId
  ElMessage.success(isUpdate ? '病例已更新' : '病例已保存，预约已标记为完成并通知患者，可继续开具电子药单')
  await loadAppointments()
  await loadDoctorRecords()
  if (isUpdate) {
    activeTab.value = 'history'
  } else {
    activeTab.value = 'prescriptions'
  }
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
}

async function loadDoctorRecords() {
  const params: Record<string, unknown> = {}
  if (recordFilters.patientId) params.patientId = recordFilters.patientId
  if (recordFilters.dateFrom) params.dateFrom = recordFilters.dateFrom
  if (recordFilters.dateTo) params.dateTo = recordFilters.dateTo
  doctorRecords.value = await apiGet('/doctor/records', Object.keys(params).length ? params : undefined)
}

function resetRecordFilters() {
  recordFilters.patientId = undefined
  recordFilters.dateFrom = ''
  recordFilters.dateTo = ''
  loadDoctorRecords()
}

async function editDoctorRecord(id: number) {
  const detail = await apiGet(`/doctor/records/${id}`)
  const fallbackAppointment = {
    id: detail.record.appointmentId,
    visitDate: detail.appointment?.visitDate || '-',
    timeSlot: detail.appointment?.timeSlot || '',
    symptoms: detail.appointment?.symptoms || detail.record.chiefComplaint || '',
    demand: detail.appointment?.demand || '',
    status: detail.appointment?.status || 'COMPLETED'
  }
  selectedEncounter.value = {
    patient: detail.patient,
    appointment: fallbackAppointment
  }
  Object.assign(recordForm, {
    id: detail.record.id,
    patientId: detail.record.patientId,
    appointmentId: detail.record.appointmentId,
    chiefComplaint: detail.record.chiefComplaint || '',
    presentIllness: detail.record.presentIllness || '',
    examination: detail.record.examination || '',
    diagnosis: detail.record.diagnosis || '',
    treatmentPlan: detail.record.treatmentPlan || '',
    reportImagePath: detail.record.reportImagePath || ''
  })
  Object.assign(prescriptionForm, { recordId: detail.record.id, patientId: detail.record.patientId, note: '' })
  issuedPrescriptions.value = detail.prescriptions || []
  prescriptionMedicineId.value = undefined
  await loadPatientHistory(detail.patient?.id, true)
  activeTab.value = 'records'
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
.doctor-layout {
  display: flex;
  height: 100vh;
  background: var(--bg-body);
  overflow: hidden;
}

.doctor-sidebar {
  width: 260px;
  background: var(--sidebar-bg);
  color: var(--sidebar-text);
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(0,0,0,0.06);
  z-index: 10;
}

.sidebar-header {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-icon {
  font-size: 24px;
  color: #fff;
}
.brand-text {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  flex: 1;
}
.role-tag {
  border: none;
}

.sidebar-menu {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  cursor: pointer;
  color: var(--sidebar-text);
  font-size: 15px;
  transition: all 0.2s ease;
  margin-bottom: 4px;
}
.menu-item:hover {
  background: rgba(255,255,255,0.08);
  color: #fff;
}
.menu-item.active {
  background: var(--sidebar-active-bg);
  color: var(--sidebar-active-text);
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.menu-item .el-icon {
  font-size: 18px;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.doc-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.doc-avatar {
  background: rgba(255,255,255,0.2);
  color: #fff;
}
.doc-text {
  display: flex;
  flex-direction: column;
}
.doc-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}
.doc-title {
  font-size: 12px;
  color: rgba(255,255,255,0.6);
}
.logout-btn {
  color: rgba(255,255,255,0.6);
}
.logout-btn:hover {
  color: #fff;
}

.doctor-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 100vh;
}

.doctor-header {
  height: 64px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02);
  z-index: 5;
}
.current-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--topbar-title);
}

.doctor-content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}
.content-wrapper {
  background: var(--panel-bg);
  border-radius: 16px;
  padding: 24px 32px;
  box-shadow: var(--panel-shadow);
  border: 1px solid var(--panel-border);
  min-height: calc(100vh - 128px);
}

:deep(.hidden-header-tabs > .el-tabs__header) {
  display: none !important;
}

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
