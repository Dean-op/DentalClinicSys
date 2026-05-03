<template>
  <main class="login-page">
    <section class="login-panel">
      <h1 class="login-title">口腔诊所管理系统</h1>
      <p class="login-subtitle">患者预约、医生接诊、诊所运营一体化管理</p>

      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-position="top" @keyup.enter="handleLogin">
            <el-form-item label="账号">
              <el-input v-model="loginForm.username" prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" prefix-icon="Lock" show-password size="large" />
            </el-form-item>
            <el-button type="primary" :loading="loading" class="full login-btn" size="large" @click="handleLogin">
              登 录
            </el-button>
            <div class="demo-tools">
              <el-dropdown trigger="click" @command="applyPreset">
                <span class="demo-link">
                  <el-icon><MagicStick /></el-icon> 演示账号体验
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item 
                      v-for="account in presetAccounts" 
                      :key="account.username" 
                      :command="account.username"
                    >
                      {{ account.label }} ({{ account.username }})
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="患者注册" name="register">
          <el-form :model="registerForm" label-position="top">
            <el-form-item label="账号">
              <el-input v-model="registerForm.username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" show-password />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="registerForm.name" />
            </el-form-item>
            <el-form-item label="联系方式">
              <el-input v-model="registerForm.phone" />
            </el-form-item>
            <el-button type="success" :loading="loading" class="full" @click="handleRegister">
              注册患者账号
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { roleHome } from '../router'
import { useAuthStore } from '../store'

const router = useRouter()
const auth = useAuthStore()
const mode = ref('login')
const loading = ref(false)

const loginForm = reactive({ username: 'patient_li', password: '123456' })
const registerForm = reactive({ username: '', password: '', name: '', phone: '' })

const presetAccounts = [
  { label: '管理员', username: 'admin', password: '123456' },
  { label: '医生 陈洁', username: 'doctor_chen', password: '123456' },
  { label: '医生 王启航', username: 'doctor_wang', password: '123456' },
  { label: '患者 李明', username: 'patient_li', password: '123456' },
  { label: '患者 张晓雨', username: 'patient_zhang', password: '123456' },
  { label: '患者 刘浩', username: 'patient_liu', password: '123456' }
]

function applyPreset(username: string) {
  const account = presetAccounts.find((item) => item.username === username)
  if (!account) return
  loginForm.username = account.username
  loginForm.password = account.password
}

async function handleLogin() {
  loading.value = true
  try {
    await auth.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    await router.push(roleHome(auth.user?.role || 'PATIENT'))
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  loading.value = true
  try {
    await auth.register(registerForm)
    ElMessage.success('注册成功，请登录')
    loginForm.username = registerForm.username
    loginForm.password = registerForm.password
    mode.value = 'login'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.full {
  width: 100%;
}

.login-btn {
  margin-top: 8px;
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 2px;
}

.demo-tools {
  margin-top: 16px;
  text-align: center;
}

.demo-link {
  font-size: 13px;
  color: #94a3b8;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}

.demo-link:hover {
  color: #3b82f6;
}
</style>
