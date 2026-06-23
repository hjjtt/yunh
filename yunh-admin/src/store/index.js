/**
 * Pinia 状态管理
 * 用户状态管理模块
 */
import { defineStore } from 'pinia'
import router from '../router'
import { authLogout } from '../api/user.js'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),

  getters: {
    isLoggedIn: (state) => !!state.token
  },

  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    setRefreshToken(refreshToken) {
      this.refreshToken = refreshToken
      localStorage.setItem('refreshToken', refreshToken)
    },

    setUser(user) {
      this.userInfo = user
      localStorage.setItem('userInfo', JSON.stringify(user))
    },

    async logout() {
      try {
        if (this.token) {
          await authLogout()
        }
      } catch (e) {
        // 忽略登出接口错误
      }
      this.token = ''
      this.refreshToken = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
  }
})
