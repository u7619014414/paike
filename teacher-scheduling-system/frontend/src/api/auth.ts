import request from './request'

interface LoginParams {
  username: string
  password: string
  rememberMe?: boolean
}

interface LoginResponse {
  token: string
  userInfo: {
    id: number
    username: string
    name: string
    role: string
  }
}

export const authApi = {
  // 登录
  login: (data: LoginParams) =>
    request.post<LoginResponse>('/auth/login', data),

  // 登出
  logout: () =>
    request.post('/auth/logout'),

  // 获取当前用户信息
  getCurrentUser: () =>
    request.get('/auth/current-user')
}
