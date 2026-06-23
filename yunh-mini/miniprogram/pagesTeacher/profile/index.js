const sessionStore = require("../../store/session")
const authService = require("../../services/auth")
const teacherService = require("../../services/teacher")

Page({
  data: {
    userInfo: null,
    avatarLetter: "?",
    profileTitle: "Not logged in",
    stats: {
      courseCount: 0,
      studentCount: 0,
      teachingDays: 0
    }
  },

  async onShow() {
    if (typeof this.getTabBar === "function" && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 })
    }

    const session = sessionStore.getSession()
    const userInfo = session.userInfo

    if (!userInfo || !userInfo.id || !session.token) {
      this.setData({
        userInfo: null,
        avatarLetter: "?",
        profileTitle: "Not logged in"
      })
      return
    }

    const name = userInfo.nickname || userInfo.username || "Teacher"
    const avatarLetter = (name[0] || "?").toUpperCase()

    let teachingDays = 0
    if (userInfo.createTime) {
      const createDate = new Date(userInfo.createTime)
      teachingDays = Math.floor((Date.now() - createDate.getTime()) / (24 * 3600 * 1000))
    }

    this.setData({
      userInfo,
      avatarLetter,
      profileTitle: name,
      stats: {
        courseCount: 0,
        studentCount: 0,
        teachingDays
      }
    })

    this.loadStats(userInfo.id)
  },

  async loadStats(teacherId) {
    try {
      const res = await teacherService.getTeacherCourses(teacherId)
      const courses = Array.isArray(res.data) ? res.data : []
      const courseCount = courses.length

      let studentCount = 0
      const endDate = this.formatDate(new Date())
      const startDate = this.formatDate(new Date(Date.now() - 90 * 24 * 3600 * 1000))

      if (courses.length > 0) {
        const statPromises = courses.map((course) =>
          teacherService.getCourseStatistics(course.id, startDate, endDate)
            .then((statRes) => {
              const stats = Array.isArray(statRes.data) ? statRes.data : []
              let count = 0
              stats.forEach((item) => {
                count += item.studentCount || 0
              })
              return count
            })
            .catch(() => 0)
        )
        const counts = await Promise.all(statPromises)
        studentCount = counts.reduce((sum, count) => sum + count, 0)
      }

      this.setData({
        "stats.courseCount": courseCount,
        "stats.studentCount": studentCount
      })
    } catch (error) {
      console.error("profile loadStats error", error)
    }
  },

  formatDate(date) {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, "0")
    const d = String(date.getDate()).padStart(2, "0")
    return `${y}-${m}-${d}`
  },

  goToCourseManage() {
    wx.switchTab({ url: "/pagesTeacher/course/index" })
  },

  goToStats() {
    wx.switchTab({ url: "/pagesTeacher/stats/index" })
  },

  goToCreateCourse() {
    wx.navigateTo({ url: "/packageTeacher/pages/course-edit" })
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
    const res = await new Promise((resolve) => {
      wx.showModal({
        title: "确认退出",
        content: "退出后需要重新登录才能使用教师功能。",
        confirmColor: "#c0392b",
        success: resolve
      })
    })

    if (!res.confirm) return

    try {
      await authService.logout()
    } catch (error) {
      // Ignore logout API failures and clear local session anyway.
    }

    sessionStore.clearSession()
    wx.reLaunch({ url: "/pages/auth/login" })
  }
})
