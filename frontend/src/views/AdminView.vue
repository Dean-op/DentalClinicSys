<template>
  <div class="admin-layout theme-admin">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <div class="logo-box"><el-icon><Monitor /></el-icon></div>
        <div class="brand-info">
          <span class="brand-text">口腔诊所管理系统</span>
          <span class="brand-sub">OS Control Panel</span>
        </div>
      </div>
      
      <div class="sidebar-menu">
        <div 
          v-for="item in tabs" 
          :key="item.name"
          class="menu-item"
          :class="{ active: activeTab === item.name }"
          @click="activeTab = item.name"
        >
          <div class="active-indicator" v-if="activeTab === item.name"></div>
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="admin-info">
          <el-avatar size="small" icon="UserFilled" class="admin-avatar" />
          <div class="admin-text">
            <div class="admin-name">Administrator</div>
            <div class="admin-role">System Admin</div>
          </div>
        </div>
        <el-button link class="logout-btn" @click="logout">退出登录</el-button>
      </div>
    </aside>

    <main class="admin-main">
      <header class="admin-header">
        <div class="header-left">
          <h2 class="current-title">{{ currentTabLabel }}</h2>
          <el-tag effect="dark" round color="#1e293b" style="border:none">全模块统一管理中心</el-tag>
        </div>
        <div class="header-right">
          <div class="time-display"><el-icon><Clock /></el-icon> {{ currentTime }}</div>
        </div>
      </header>

      <div class="admin-content">
        <div class="content-wrapper">
          <el-tabs v-model="activeTab" class="hidden-header-tabs">
          <el-tab-pane label="统计分析" name="stats">
            <div class="dashboard-header">
              <div class="dash-title">核心数据监控墙</div>
              <div class="live-pulse"><span class="pulse-dot"></span> LIVE DATA</div>
            </div>
            <div class="metric-grid modern-metrics">
              <div class="metric-card">
                <div class="m-icon users-icon"><el-icon><User /></el-icon></div>
                <div class="m-info"><span>账户总数</span><strong>{{ stats.users || 0 }}</strong></div>
              </div>
              <div class="metric-card">
                <div class="m-icon docs-icon"><el-icon><FirstAidKit /></el-icon></div>
                <div class="m-info"><span>入驻医生</span><strong>{{ stats.doctors || 0 }}</strong></div>
              </div>
              <div class="metric-card">
                <div class="m-icon pat-icon"><el-icon><Avatar /></el-icon></div>
                <div class="m-info"><span>活跃患者</span><strong>{{ stats.activePatients || 0 }}</strong></div>
              </div>
              <div class="metric-card">
                <div class="m-icon apt-icon"><el-icon><Calendar /></el-icon></div>
                <div class="m-info"><span>预约总量</span><strong>{{ stats.appointments || 0 }}</strong></div>
              </div>
              <div class="metric-card">
                <div class="m-icon order-icon"><el-icon><ShoppingCart /></el-icon></div>
                <div class="m-info"><span>订单总量</span><strong>{{ stats.orders || 0 }}</strong></div>
              </div>
              <div class="metric-card">
                <div class="m-icon record-icon"><el-icon><Document /></el-icon></div>
                <div class="m-info"><span>病历档案</span><strong>{{ stats.records || 0 }}</strong></div>
              </div>
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

          <el-tab-pane label="排班管理" name="schedules">
            <el-alert type="info" :closable="false" title="管理员可以统一维护未来排班；已被预约的排班仅支持查看，不允许修改时间或删除。" />
            <div class="toolbar mt">
              <el-select v-model="scheduleFilters.doctorId" clearable filterable placeholder="按医生筛选" style="width: 220px">
                <el-option
                  v-for="doctor in scheduleDoctorOptions"
                  :key="doctor.id"
                  :label="`${doctor.name} · ${doctor.department}`"
                  :value="doctor.id"
                />
              </el-select>
              <el-date-picker v-model="scheduleFilters.workDate" value-format="YYYY-MM-DD" clearable placeholder="按日期筛选" />
              <el-button type="primary" icon="Search" @click="loadSchedules">查询排班</el-button>
              <el-button @click="resetScheduleFilters">重置筛选</el-button>
            </div>

            <div class="toolbar">
              <el-select v-model="scheduleForm.doctorId" filterable placeholder="选择医生" style="width: 220px">
                <el-option
                  v-for="doctor in scheduleDoctorOptions"
                  :key="doctor.id"
                  :label="`${doctor.name} · ${doctor.department}`"
                  :value="doctor.id"
                />
              </el-select>
              <el-date-picker v-model="scheduleForm.workDate" value-format="YYYY-MM-DD" placeholder="排班日期" />
              <el-time-select v-model="scheduleForm.startTime" start="08:00" step="00:30" end="17:00" />
              <el-time-select v-model="scheduleForm.endTime" start="09:00" step="00:30" end="18:00" />
              <el-input-number v-model="scheduleForm.capacity" :min="1" />
              <el-button type="primary" icon="Plus" @click="saveSchedule">{{ scheduleForm.id ? '保存排班' : '新增排班' }}</el-button>
              <el-button @click="resetScheduleForm">清空</el-button>
            </div>

            <el-table :data="schedules" stripe>
              <el-table-column label="医生" min-width="180">
                <template #default="{ row }">
                  <strong>{{ row.doctor?.name || '-' }}</strong>
                  <div class="muted">{{ row.doctor?.department || '-' }}</div>
                </template>
              </el-table-column>
              <el-table-column label="审核状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="reviewStatusTag(row.doctor?.reviewStatus)">{{ reviewStatusLabel(row.doctor?.reviewStatus) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="schedule.workDate" label="日期" width="130" />
              <el-table-column prop="schedule.startTime" label="开始" width="100" />
              <el-table-column prop="schedule.endTime" label="结束" width="100" />
              <el-table-column prop="schedule.capacity" label="容量" width="90" />
              <el-table-column prop="schedule.bookedCount" label="已约" width="90" />
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button type="primary" link :disabled="!row.editable" @click="editSchedule(row)">编辑</el-button>
                  <el-button type="danger" link :disabled="!row.editable" @click="deleteSchedule(row.schedule.id)">删除</el-button>
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
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { apiDelete, apiGet, apiPost, apiPut } from '../api'
import { useAuthStore } from '../store'

const router = useRouter()
const auth = useAuthStore()
const activeTab = ref('stats')
const tabs = [
  { name: 'stats', label: '统计分析', icon: 'DataAnalysis' },
  { name: 'users', label: '用户管理', icon: 'UserFilled' },
  { name: 'doctors', label: '医生审核', icon: 'Checked' },
  { name: 'schedules', label: '排班管理', icon: 'Calendar' },
  { name: 'medicines', label: '药品管理', icon: 'ShoppingBag' },
  { name: 'announcements', label: '公告管理', icon: 'Bell' },
  { name: 'records', label: '病例监督', icon: 'Document' },
  { name: 'prescriptions', label: '处方监督', icon: 'Memo' },
  { name: 'orders', label: '订单管理', icon: 'Tickets' },
  { name: 'appointments', label: '预约管理', icon: 'Calendar' },
  { name: 'logs', label: '系统日志', icon: 'Files' }
]

const currentTabLabel = computed(() => tabs.find(t => t.name === activeTab.value)?.label || '')
const currentTime = ref(new Date().toLocaleString('zh-CN', { hour12: false }))
setInterval(() => {
  currentTime.value = new Date().toLocaleString('zh-CN', { hour12: false })
}, 1000)

const stats = ref<any>({})
const users = ref<any[]>([])
const doctors = ref<any[]>([])
const schedules = ref<any[]>([])
const medicines = ref<any[]>([])
const announcements = ref<any[]>([])
const orders = ref<any[]>([])
const records = ref<any[]>([])
const prescriptions = ref<any[]>([])
const appointments = ref<any[]>([])
const logs = ref<any[]>([])

const userForm = reactive({ username: '', name: '', role: 'PATIENT', password: '123456' })
const scheduleFilters = reactive<{ doctorId?: number; workDate: string }>({ doctorId: undefined, workDate: '' })
const scheduleForm = reactive<any>({ id: undefined, doctorId: undefined, workDate: '', startTime: '09:00', endTime: '12:00', capacity: 8, bookedCount: 0 })
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
const scheduleDoctorOptions = computed(() => doctors.value || [])

async function loadAll() {
  stats.value = await apiGet('/admin/stats')
  users.value = await apiGet('/admin/users')
  doctors.value = await apiGet('/admin/doctors')
  await loadSchedules()
  medicines.value = await apiGet('/admin/medicines')
  announcements.value = await apiGet('/admin/announcements')
  orders.value = await apiGet('/admin/orders')
  records.value = await apiGet('/admin/records')
  prescriptions.value = await apiGet('/admin/prescriptions')
  appointments.value = await apiGet('/admin/appointments')
  logs.value = await apiGet('/admin/logs')
}

async function loadSchedules() {
  const params: Record<string, unknown> = {}
  if (scheduleFilters.doctorId) params.doctorId = scheduleFilters.doctorId
  if (scheduleFilters.workDate) params.date = scheduleFilters.workDate
  schedules.value = await apiGet('/admin/schedules', Object.keys(params).length ? params : undefined)
}

function resetScheduleFilters() {
  scheduleFilters.doctorId = undefined
  scheduleFilters.workDate = ''
  loadSchedules()
}

function resetScheduleForm() {
  Object.assign(scheduleForm, { id: undefined, doctorId: undefined, workDate: '', startTime: '09:00', endTime: '12:00', capacity: 8, bookedCount: 0 })
}

function editSchedule(row: any) {
  Object.assign(scheduleForm, row.schedule)
}

async function saveSchedule() {
  if (scheduleForm.id) await apiPut(`/admin/schedules/${scheduleForm.id}`, scheduleForm)
  else await apiPost('/admin/schedules', scheduleForm)
  ElMessage.success('排班已保存')
  resetScheduleForm()
  await loadSchedules()
}

async function deleteSchedule(id: number) {
  await apiDelete(`/admin/schedules/${id}`)
  ElMessage.success('排班已删除')
  await loadSchedules()
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
/* Admin Layout Variables */
.admin-layout {
  display: flex;
  height: 100vh;
  background: #f1f5f9;
  overflow: hidden;
}

.admin-sidebar {
  width: 260px;
  background: #0f172a;
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;
  box-shadow: 4px 0 24px rgba(0,0,0,0.1);
}
.sidebar-header {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.logo-box {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}
.brand-info {
  display: flex;
  flex-direction: column;
}
.brand-info .brand-text {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
}
.brand-info .brand-sub {
  font-size: 11px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.sidebar-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}
.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}
.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.1);
  border-radius: 4px;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  color: #cbd5e1;
  font-size: 14px;
  transition: all 0.2s ease;
  margin-bottom: 6px;
  position: relative;
}
.menu-item:hover {
  background: rgba(255,255,255,0.05);
  color: #fff;
}
.menu-item.active {
  background: #1e293b;
  color: #fff;
  font-weight: 600;
}
.active-indicator {
  position: absolute;
  left: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: #3b82f6;
  border-radius: 0 4px 4px 0;
}
.menu-item .el-icon {
  font-size: 18px;
  color: #64748b;
  transition: color 0.2s;
}
.menu-item.active .el-icon {
  color: #3b82f6;
}
.sidebar-footer {
  padding: 16px;
  background: #0b1120;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.admin-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.admin-avatar {
  background: #1e293b;
  color: #94a3b8;
}
.admin-text {
  display: flex;
  flex-direction: column;
}
.admin-name {
  font-size: 13px;
  font-weight: 600;
  color: #e2e8f0;
}
.admin-role {
  font-size: 11px;
  color: #64748b;
}
.logout-btn {
  color: #64748b;
}
.logout-btn:hover {
  color: #ef4444;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 100vh;
}
.admin-header {
  height: 70px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  z-index: 5;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.current-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}
.time-display {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: monospace;
  font-size: 14px;
  color: #64748b;
  background: #f8fafc;
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
}

.admin-content {
  flex: 1;
  padding: 24px 32px;
  overflow-y: auto;
}
.content-wrapper {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
  border: 1px solid #e2e8f0;
  min-height: calc(100vh - 118px);
}
:deep(.hidden-header-tabs > .el-tabs__header) {
  display: none !important;
}

/* Dashboard Specifics */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}
.dash-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  position: relative;
  padding-left: 12px;
}
.dash-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: #3b82f6;
  border-radius: 2px;
}
.live-pulse {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #10b981;
  letter-spacing: 1px;
}
.pulse-dot {
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); }
  70% { box-shadow: 0 0 0 6px rgba(16, 185, 129, 0); }
  100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}

.modern-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}
.metric-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}
.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0,0,0,0.05);
  border-color: #cbd5e1;
}
.m-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.users-icon { background: #eff6ff; color: #3b82f6; }
.docs-icon { background: #ecfdf5; color: #10b981; }
.pat-icon { background: #fef2f2; color: #ef4444; }
.apt-icon { background: #fffbeb; color: #f59e0b; }
.order-icon { background: #f5f3ff; color: #8b5cf6; }
.record-icon { background: #f0fdfa; color: #14b8a6; }

.m-info {
  display: flex;
  flex-direction: column;
}
.m-info span {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}
.m-info strong {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1;
}
.stat-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}
.stat-card :deep(.el-card__header) {
  font-weight: 600;
  color: #334155;
  background: #f8fafc;
  padding: 12px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.narrow-form {
  max-width: 760px;
  margin-bottom: 18px;
}
.mt {
  margin-top: 12px;
}
.expand-box {
  padding: 8px 4px;
}
</style>
