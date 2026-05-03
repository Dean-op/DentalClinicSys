import axios from 'axios'
import { ElMessage } from 'element-plus'

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>
    if (body && body.success === false) {
      ElMessage.error(body.message || '操作失败')
      return Promise.reject(new Error(body.message))
    }
    return response
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export async function apiGet<T = any>(url: string, params?: Record<string, unknown>) {
  const response = await http.get<ApiResponse<T>>(url, { params })
  return response.data.data
}

export async function apiPost<T = any>(url: string, data?: unknown) {
  const response = await http.post<ApiResponse<T>>(url, data)
  return response.data.data
}

export async function apiPut<T = any>(url: string, data?: unknown) {
  const response = await http.put<ApiResponse<T>>(url, data)
  return response.data.data
}

export async function apiDelete<T = any>(url: string) {
  const response = await http.delete<ApiResponse<T>>(url)
  return response.data.data
}
