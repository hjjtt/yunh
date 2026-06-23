const sessionStore = require("../../store/session")
const orderService = require("../../services/order")
const userService = require("../../services/user")
const authService = require("../../services/auth")
const format = require("../../utils/format")

Page({
  data: {
    userInfo: null,
    avatarLetter: "?",
    myCourses: [],
    orderList: [],
    profileTitle: "还未登录",
    profileSubtitle: "登录后查看已购课程和订单信息",
    profileStats: []
  },

  async onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 })
    }
    const session = sessionStore.getSession()
    const userInfo = session.userInfo

    if (!sessionStore.hasSession()) {
      this.setData({
        userInfo: null,
        avatarLetter: "?",
        profileTitle: "还未登录",
        profileSubtitle: "登录后查看已购课程和订单信息",
        myCourses: [],
        orderList: [],
        profileStats: []
      })
      return
    }

    const profileTitle = resolveProfileTitle(userInfo)
    const profileSubtitle = resolveProfileSubtitle(userInfo)
    const avatarLetter = (profileTitle[0] || "?").toUpperCase()

    this.setData({
      userInfo,
      avatarLetter,
      profileTitle,
      profileSubtitle
    })

    try {
      const [courseRes, orderRes] = await Promise.all([
        userService.getUserCourses(userInfo.id),
        orderService.getOrdersByUserId(userInfo.id)
      ])
      this.setData({
        myCourses: Array.isArray(courseRes.data) ? courseRes.data.slice(0, 3) : [],
        orderList: (Array.isArray(orderRes.data) ? orderRes.data : []).slice(0, 3).map(item => ({
          id: item.id,
          orderNo: item.orderNo,
          courseName: item.courseName || "待补全课程名",
          statusText: format.orderStatusText(item.status)
        })),
        profileStats: [
          { label: "已购课程", value: `${Array.isArray(courseRes.data) ? courseRes.data.length : 0}` },
          { label: "订单数", value: `${Array.isArray(orderRes.data) ? orderRes.data.length : 0}` },
          { label: "账号状态", value: "正常" }
        ]
      })
    } catch (error) {
      console.error("profile onShow error", error)
    }
  },

  goLogin() {
    wx.reLaunch({ url: "/pages/auth/login" })
  },

  goMyCourse() {
    wx.navigateTo({ url: "/packageProfile/pages/my-course" })
  },

  goMyOrder() {
    wx.navigateTo({ url: "/packageProfile/pages/my-order" })
  },

  goCourseList() {
    wx.switchTab({ url: "/pages/course/list" })
  },

  goMyNotes() {
    wx.navigateTo({ url: "/packageProfile/pages/my-notes" })
  },

  goMyComments() {
    wx.navigateTo({ url: "/packageProfile/pages/my-comments" })
  },

  goMyQuestions() {
    wx.navigateTo({ url: "/packageProfile/pages/my-questions" })
  },

  async handleLogout() {
    wx.showModal({
      title: "退出登录",
      content: "确定要退出当前账号吗？",
      confirmText: "退出",
      confirmColor: "#c0392b",
      success: async (res) => {
        if (!res.confirm) return
        try {
          await authService.logout()
        } catch (error) {
          console.error("logout error", error)
        } finally {
          sessionStore.clearSession()
          wx.showToast({ title: "已退出登录", icon: "none" })
          setTimeout(() => {
            wx.reLaunch({ url: "/pages/auth/login" })
          }, 500)
        }
      }
    })
  }
})

function resolveProfileTitle(userInfo) {
  if (!userInfo) return "还未登录"
  const nickname = userInfo.nickname || ""
  if (userInfo.role === "ADMIN" || /管理员|系统/.test(nickname)) {
    return userInfo.username || "当前用户"
  }
  return nickname || userInfo.username || "当前用户"
}

function resolveProfileSubtitle(userInfo) {
  if (!userInfo) return "登录后查看已购课程和订单信息"
  if (userInfo.role === "TEACHER") {
    return "欢迎回到教学空间"
  }
  return "欢迎来到学习空间"
}
