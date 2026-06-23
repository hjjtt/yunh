const courseService = require("../../services/course")
const format = require("../../utils/format")

Page({
  data: {
    loading: false,
    courseList: [],
    filteredList: [],
    keyword: "",
    activeStatus: "all",
    stats: {
      total: 0,
      teachers: 0,
      avgPrice: "0.00"
    }
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 })
    }
    this.loadCourses()
  },

  async loadCourses() {
    this.setData({ loading: true })
    try {
      const res = await courseService.listCourses()
      const courseList = Array.isArray(res.data) ? res.data : []
      this.setData({ courseList })
      this.applyFilters()
    } catch (error) {
      console.error("loadCourses error", error)
    } finally {
      this.setData({ loading: false })
    }
  },

  handleCourseTap(event) {
    const id = event.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/packageCourse/pages/detail?id=${id}`
    })
  },

  handleKeywordInput(event) {
    this.setData({ keyword: event.detail.value })
    this.applyFilters()
  },

  switchStatus(event) {
    this.setData({ activeStatus: event.currentTarget.dataset.status })
    this.applyFilters()
  },

  applyFilters() {
    const keyword = (this.data.keyword || "").trim().toLowerCase()
    const activeStatus = this.data.activeStatus

    const filteredList = this.data.courseList.filter(item => {
      const name = (item.name || "").toLowerCase()
      const teacher = (item.teacherName || "").toLowerCase()
      const matchKeyword = !keyword || name.includes(keyword) || teacher.includes(keyword)
      const matchStatus = activeStatus === "all" || String(item.status) === activeStatus
      return matchKeyword && matchStatus
    })

    // 计算统计
    const total = filteredList.length
    const avgPrice = total ? filteredList.reduce((sum, item) => sum + Number(item.price || 0), 0) / total : 0
    const teachers = total ? new Set(filteredList.map(item => item.teacherName).filter(Boolean)).size : 0

    this.setData({
      filteredList,
      stats: {
        total,
        teachers,
        avgPrice: format.currency(avgPrice)
      }
    })
  }
})
