import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
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
    // 请求拦截器
    this.instance.interceptors.request.use(
      (config) => {
        // 显示loading
        this.loading = ElLoading.service({
          lock: true,
          text: '请求中...',
          background: 'rgba(0, 0, 0, 0.1)'
        })
        return config
      },
      (error) => {
        this.hideLoading()
        return Promise.reject(error)
      }
    )

    // 响应拦截器
    this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResponse>) => {
        this.hideLoading()
        const { code, message, data } = response.data
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
          if (status >= 500) {
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

const request = new Request({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://asdnn.com:45081/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export default request
