import axios, { AxiosInstance, AxiosRequestConfig } from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import type { ApiResponse } from '@/types'

class Request {
  private instance: AxiosInstance
  private loading: any

  constructor(config: AxiosRequestConfig) {
    this.instance = axios.create(config)
    this.setupInterceptors()
  }

  private setupInterceptors() {
    this.instance.interceptors.request.use(
      (config) => {
        this.loading = ElLoading.service({
          lock: true,
          text: '加载中...',
          background: 'rgba(0, 0, 0, 0.1)'
        })

        // 添加token到请求头
        const token = localStorage.getItem('token') || sessionStorage.getItem('token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }

        return config
      },
      (error) => {
        this.hideLoading()
        return Promise.reject(error)
      }
    )

    this.instance.interceptors.response.use(
      (response) => {
        this.hideLoading()
        const { code, message, data } = response.data as ApiResponse
        
        if (code === 200) {
          return data
        } else {
          ElMessage.error(message || '请求失败')
          return Promise.reject(new Error(message))
        }
      },
      (error) => {
        this.hideLoading()

        if (error.response) {
          const { status, data } = error.response

          if (status === 401) {
            // 未授权，清除token并跳转到登录页
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            sessionStorage.removeItem('token')
            sessionStorage.removeItem('userInfo')

            if (window.location.pathname !== '/login') {
              window.location.href = '/login'
            }
          } else if (status >= 500) {
            ElMessage.error('服务器错误，请稍后重试')
          } else {
            ElMessage.error(data?.message || '请求失败')
          }
        } else {
          ElMessage.error('网络错误，请检查网络连接')
        }

        return Promise.reject(error)
      }
    )
  }

  private hideLoading() {
    if (this.loading) {
      this.loading.close()
      this.loading = null
    }
  }

  public get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.get(url, config)
  }

  public post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.post(url, data, config)
  }

  public put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.put(url, data, config)
  }

  public delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.delete(url, config)
  }
}

// Use Vite environment variable VITE_API_BASE when available so we can switch
// between using the dev server proxy (e.g. "/api") and a direct backend URL
// (e.g. "http://localhost:45082/api"). Falls back to "/api" for compatibility.
const baseURL = (import.meta.env && (import.meta.env.VITE_API_BASE as string)) || '/api'

const request = new Request({
  baseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export default request
