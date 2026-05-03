<template>
  <main class="login-page">
    <section class="login-panel">
      <h1 class="login-title">口腔诊所管理系统</h1>
      <p class="login-subtitle">患者预约、医生接诊、诊所运营一体化管理</p>

      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-position="top" @keyup.enter="handleLogin">
            <el-form-item label="账号">
              <el-input v-model="loginForm.username" prefix-icon="User" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" prefix-icon="Lock" show-password />
            </el-form-item>
            <el-button type="primary" :loading="loading" class="full" @click="handleLogin">
              登录系统
            </el-button>
          </el-form>
          <el-divider />
          <el-space wrap>
            <el-button text @click="fill('admin')">管理员</el-button>
            <el-button text @click="fill('doctor_chen')">医生</el-button>
            <el-button text @click="fill('patient_li')">患者</el-button>
          </el-space>
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

function fill(username: string) {
  loginForm.username = username
  loginForm.password = '123456'
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
</style>
