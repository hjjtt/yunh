/**
 * Axios 请求封装
 * 统一处理请求拦截、响应拦截、错误提示
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '',
  timeout: 30000
})

let isRefreshing = false
let pendingRequests = []

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      const refreshTokenValue = localStorage.getItem('refreshToken')
      if (refreshTokenValue && !isRefreshing) {
        isRefreshing = true
        return axios.post('/api/auth/refresh', { refreshToken: refreshTokenValue })
          .then((res) => {
            if (res.data && res.data.code === 200) {
              const data = res.data.data
              localStorage.setItem('token', data.accessToken)
              localStorage.setItem('refreshToken', data.refreshToken)
              error.config.headers.Authorization = 'Bearer ' + data.accessToken
              pendingRequests.forEach((cb) => cb(data.accessToken))
              pendingRequests = []
              return request(error.config)
            } else {
              clearAuth()
              return Promise.reject(error)
            }
          })
          .catch(() => {
            clearAuth()
            return Promise.reject(error)
          })
          .finally(() => {
            isRefreshing = false
          })
      } else if (isRefreshing) {
        return new Promise((resolve) => {
          pendingRequests.push((token) => {
            error.config.headers.Authorization = 'Bearer ' + token
            resolve(request(error.config))
          })
        })
      } else {
        clearAuth()
      }
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

function clearAuth() {
  ElMessage.error('登录已过期，请重新登录')
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

export default request
