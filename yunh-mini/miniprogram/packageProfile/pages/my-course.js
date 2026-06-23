const sessionStore = require("../../store/session")
const userService = require("../../services/user")
const format = require("../../utils/format")

Page({
  data: {
    myCourses: [],
    loading: true,
    stats: []
  },

  onShow() {
    this.loadMyCourses()
  },

  async loadMyCourses() {
    const session = sessionStore.getSession()
    const userInfo = session.userInfo

    if (!userInfo || !userInfo.id) {
      this.setData({
        myCourses: [],
        loading: false
      })
      return
    }

    try {
      const res = await userService.getUserCourses(userInfo.id)
      const myCourses = Array.isArray(res.data) ? res.data : []
      this.setData({
        myCourses,
        stats: buildCourseStats(myCourses)
      })
    } catch (error) {
      console.error("loadMyCourses error", error)
    } finally {
      this.setData({
        loading: false
      })
    }
  }
})

function buildCourseStats(courseList) {
  const list = Array.isArray(courseList) ? courseList : []
  const teachers = new Set(list.map(item => item.teacherName).filter(Boolean)).size
  const totalAmount = list.reduce((sum, item) => sum + Number(item.price || 0), 0)

  return [
    { label: "课程数", value: `${list.length}` },
    { label: "讲师数", value: `${teachers}` },
    { label: "课程总价", value: `￥${format.currency(totalAmount)}` }
  ]
}
