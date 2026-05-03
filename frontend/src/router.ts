import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './store'
import LoginView from './views/LoginView.vue'
import PatientView from './views/PatientView.vue'
import DoctorView from './views/DoctorView.vue'
import AdminView from './views/AdminView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView },
    { path: '/patient', component: PatientView, meta: { role: 'PATIENT' } },
    { path: '/doctor', component: DoctorView, meta: { role: 'DOCTOR' } },
    { path: '/admin', component: AdminView, meta: { role: 'ADMIN' } }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  const expectedRole = to.meta.role as string | undefined
  if (!expectedRole) return true
  if (!auth.token || !auth.user) return '/login'
  if (auth.user.role !== expectedRole) return roleHome(auth.user.role)
  return true
})

export function roleHome(role: string) {
  if (role === 'ADMIN') return '/admin'
  if (role === 'DOCTOR') return '/doctor'
  return '/patient'
}

export default router
