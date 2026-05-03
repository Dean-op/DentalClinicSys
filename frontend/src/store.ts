import { defineStore } from 'pinia'
import { apiGet, apiPost } from './api'

export type Role = 'PATIENT' | 'DOCTOR' | 'ADMIN'

export interface SessionUser {
  id: number
  username: string
  role: Role
  profile?: Record<string, unknown>
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null') as SessionUser | null
  }),
  actions: {
    async login(username: string, password: string) {
      const result = await apiPost<{ token: string; user: SessionUser }>('/auth/login', { username, password })
      this.token = result.token
      this.user = result.user
      localStorage.setItem('token', result.token)
      localStorage.setItem('user', JSON.stringify(result.user))
    },
    async register(payload: { username: string; password: string; name?: string; phone?: string }) {
      await apiPost('/auth/register', payload)
    },
    async refreshMe() {
      if (!this.token) return
      this.user = await apiGet<SessionUser>('/me')
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
