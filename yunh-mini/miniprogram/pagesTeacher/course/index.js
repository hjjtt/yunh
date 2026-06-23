const teacherService = require("../../services/teacher")
const sessionStore = require("../../store/session")

Page({
  data: {
    courses: [],
    filteredCourses: [],
    filterStatus: "all",
    searchKeyword: "",
    stats: {
      total: 0,
      active: 0,
      totalStock: 0
    },
    loading: true
  },

  onShow() {
    if (typeof this.getTabBar === "function" && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 })
    }
    this.loadCourses()
  },

  async loadCourses() {
    const session = sessionStore.getSession()
    if (!session.userInfo || !session.userInfo.id) return

    this.setData({ loading: true })
    try {
      const res = await teacherService.getTeacherCourses(session.userInfo.id)
      const courses = Array.isArray(res.data) ? res.data : []

      this.setData({
        courses,
        stats: {
          total: courses.length,
          active: courses.filter(c => c.status === 1).length,
          totalStock: courses.reduce((sum, c) => sum + (c.stock || 0), 0)
        },
        loading: false
      })
      this.applyFilter()
    } catch (error) {
      console.error("loadCourses error", error)
      this.setData({ loading: false })
    }
  },

  applyFilter() {
    let list = this.data.courses

    if (this.data.filterStatus === "active") {
      list = list.filter(c => c.status === 1)
    } else if (this.data.filterStatus === "off") {
      list = list.filter(c => c.status !== 1)
    }

    if (this.data.searchKeyword) {
      const keyword = this.data.searchKeyword.toLowerCase()
      list = list.filter(c => (c.name || "").toLowerCase().includes(keyword))
    }

    this.setData({ filteredCourses: list })
  },

  handleFilterTap(e) {
    const status = e.currentTarget.dataset.status
    this.setData({ filterStatus: status })
    this.applyFilter()
  },

  handleSearch(e) {
    this.setData({ searchKeyword: e.detail.value })
    this.applyFilter()
  },

  goToCreateCourse() {
    wx.navigateTo({ url: "/packageTeacher/pages/course-edit" })
  },

  goToEditCourse(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/packageTeacher/pages/course-edit?id=${id}` })
  },

  async handleDeleteCourse(e) {
    const id = e.currentTarget.dataset.id
    const res = await new Promise(resolve => {
      wx.showModal({
        title: "确认删除",
        content: "删除后无法恢复，确定要删除这门课程吗？",
        confirmColor: "#c0392b",
        success: resolve
      })
    })
    if (!res.confirm) return

    try {
      await teacherService.deleteCourse(id)
      wx.showToast({ title: "已删除", icon: "success" })
      this.loadCourses()
    } catch (error) {
      wx.showToast({ title: "删除失败", icon: "none" })
    }
  }
})
