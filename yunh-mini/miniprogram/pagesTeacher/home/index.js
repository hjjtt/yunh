const teacherService = require("../../services/teacher")
const sessionStore = require("../../store/session")

Page({
  data: {
    userInfo: null,
    avatarLetter: "",
    greetingTitle: "Welcome back",
    stats: {
      courseCount: 0,
      activeCount: 0,
      totalStudents: 0,
      totalIncome: "0.00"
    },
    recentCourses: [],
    loading: true
  },

  onShow() {
    if (typeof this.getTabBar === "function" && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 })
    }
    this.loadData()
  },

  async loadData() {
    const session = sessionStore.getSession()
    if (!session.userInfo || !session.userInfo.id) return

    const userInfo = session.userInfo
    const displayName = userInfo.nickname || userInfo.username || "Teacher"
    const avatarLetter = (displayName[0] || "T").toUpperCase()

    this.setData({
      userInfo,
      avatarLetter,
      greetingTitle: `Welcome back, ${displayName}`,
      loading: true
    })

    try {
      const res = await teacherService.getTeacherCourses(userInfo.id)
      const courses = Array.isArray(res.data) ? res.data : []
      const activeCount = courses.filter((course) => course.status === 1).length

      let totalStudents = 0
      let totalIncome = 0
      const endDate = this.formatDate(new Date())
      const startDate = this.formatDate(new Date(Date.now() - 30 * 24 * 3600 * 1000))

      const statPromises = courses.map((course) =>
        teacherService.getCourseStatistics(course.id, startDate, endDate)
          .then((statRes) => {
            const stats = Array.isArray(statRes.data) ? statRes.data : []
            stats.forEach((item) => {
              totalStudents += item.studentCount || 0
              totalIncome += parseFloat(item.income) || 0
            })
          })
          .catch(() => {})
      )

      await Promise.all(statPromises)

      this.setData({
        stats: {
          courseCount: courses.length,
          activeCount,
          totalStudents,
          totalIncome: totalIncome.toFixed(2)
        },
        recentCourses: courses.slice(0, 3),
        loading: false
      })
    } catch (error) {
      console.error("teacher home loadData error", error)
      this.setData({ loading: false })
    }
  },

  formatDate(date) {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, "0")
    const d = String(date.getDate()).padStart(2, "0")
    return `${y}-${m}-${d}`
  },

  goToCreateCourse() {
    wx.navigateTo({ url: "/packageTeacher/pages/course-edit" })
  },

  goToCourseManage() {
    wx.switchTab({ url: "/pagesTeacher/course/index" })
  },

  goToStats() {
    wx.switchTab({ url: "/pagesTeacher/stats/index" })
  },

  goToProfile() {
    wx.switchTab({ url: "/pagesTeacher/profile/index" })
  },

  goToCourseDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/packageCourse/pages/detail?id=${id}` })
  }
})
