import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { showToast, showLoadingToast, closeToast } from 'vant'
import type { ApiResponse } from '@/types'

class Request {
  private instance: AxiosInstance
  private requestCount = 0

  constructor(config: AxiosRequestConfig) {
    this.instance = axios.create(config)
    this.setupInterceptors()
  }

  private setupInterceptors() {
    // 请求拦截器
    this.instance.interceptors.request.use(
      (config) => {
        // 显示 loading
        if (this.requestCount === 0) {
          showLoadingToast({
            message: '加载中...',
            forbidClick: true,
            duration: 0
          })
        }
        this.requestCount++

        // 添加 token
        const token = localStorage.getItem('token')
        if (token && config.headers) {
          config.headers.Authorization = `Bearer ${token}`
        }

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
        } else if (code === 401) {
          // 未授权,跳转到登录页
          showToast('登录已过期,请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('student')
          setTimeout(() => {
            window.location.href = '/login'
          }, 1500)
          return Promise.reject(new Error(message))
        } else {
          showToast(message || '请求失败')
          return Promise.reject(new Error(message))
        }
      },
      (error) => {
        this.hideLoading()

        // 确保 loading toast 已经关闭后再显示错误
        setTimeout(() => {
          if (error.response) {
            const { status, data } = error.response
            if (status >= 500) {
              showToast('服务器错误,请稍后重试')
            } else if (status === 401) {
              showToast('登录已过期,请重新登录')
              localStorage.removeItem('token')
              localStorage.removeItem('student')
              setTimeout(() => {
                window.location.href = '/login'
              }, 1500)
            } else {
              // 尝试从不同的位置获取错误消息
              const message = data?.message || data?.data?.message || data?.msg || '请求失败'
              showToast(message)
            }
          } else {
            showToast('网络错误,请检查网络连接')
          }
        }, 100)

        return Promise.reject(error)
      }
    )
  }

  private hideLoading() {
    this.requestCount--
    if (this.requestCount <= 0) {
      this.requestCount = 0
      closeToast()
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
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export default request
