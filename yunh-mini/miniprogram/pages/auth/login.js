const authService = require("../../services/auth")
const sessionStore = require("../../store/session")

// 根据角色决定首页路径
function getHomePath(role) {
  return role === "TEACHER" ? "/pagesTeacher/home/index" : "/pages/home/index"
}

Page({
  data: {
    checkingToken: true,
    loading: false,
    showPassword: false,
    form: {
      username: "",
      password: "",
      captchaCode: ""
    },
    captchaKey: "",
    captchaImage: ""
  },

  onLoad() {
    this.checkExistingSession()
  },

  checkExistingSession() {
    if (sessionStore.hasSession()) {
      const session = sessionStore.getSession()
      const role = session.userInfo && session.userInfo.role
      wx.switchTab({
        url: getHomePath(role),
        fail: () => {
          this.setData({ checkingToken: false })
          this.fetchCaptcha()
        }
      })
    } else {
      sessionStore.clearSession()
      this.setData({ checkingToken: false })
      this.fetchCaptcha()
    }
  },

  handleInput(event) {
    const field = event.currentTarget.dataset.field
    this.setData({
      form: Object.assign({}, this.data.form, {
        [field]: event.detail.value
      })
    })
  },

  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword })
  },

  async fetchCaptcha() {
    try {
      const res = await authService.getCaptcha()
      this.setData({
        captchaKey: res.data.captchaKey,
        captchaImage: res.data.captchaImage
      })
    } catch (error) {
      console.error("fetchCaptcha error", error)
    }
  },

  async handleLogin() {
    if (this.data.loading) return

    const { username, password, captchaCode } = this.data.form
    if (!username.trim()) {
      wx.showToast({ title: "请输入用户名", icon: "none" })
      return
    }
    if (!password.trim()) {
      wx.showToast({ title: "请输入密码", icon: "none" })
      return
    }

    this.setData({ loading: true })
    try {
      const res = await authService.login({
        username,
        password,
        captchaKey: this.data.captchaKey,
        captchaCode
      })

      const data = res.data || {}
      sessionStore.setSession({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        userInfo: {
          id: data.userId || null,
          username: data.username || "",
          nickname: data.nickname || "",
          role: data.role || "USER",
          avatar: data.avatar || "",
          createTime: data.createTime || ""
        }
      })

      wx.showToast({ title: "登录成功", icon: "success" })
      setTimeout(() => {
        wx.switchTab({ url: getHomePath(data.role) })
      }, 500)
    } catch (error) {
      console.error("login error", error)
      this.fetchCaptcha()
    } finally {
      this.setData({ loading: false })
    }
  }
})
