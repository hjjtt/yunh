const interactionService = require("../../services/interaction")
const userService = require("../../services/user")
const teacherService = require("../../services/teacher")
const sessionStore = require("../../store/session")

Page({
  data: {
    courses: [],
    courseIndex: 0,
    courseNames: [],
    title: "",
    content: "",
    submitting: false
  },

  onLoad() {
    this.loadCourses()
  },

  async loadCourses() {
    const session = sessionStore.getSession()
    if (!session.userInfo || !session.userInfo.id) return

    const userId = session.userInfo.id
    const isTeacher = session.userInfo.role === "TEACHER"

    try {
      const courseRes = isTeacher
        ? await teacherService.getTeacherCourses(userId)
        : await userService.getUserCourses(userId)

      const courses = Array.isArray(courseRes.data) ? courseRes.data : []
      const courseNames = courses.map(c => c.courseName || c.name || "未知课程")

      this.setData({ courses, courseNames })
    } catch (error) {
      console.error("loadCourses error", error)
    }
  },

  onCourseChange(e) {
    this.setData({ courseIndex: Number(e.detail.value) })
  },

  onTitleInput(e) {
    this.setData({ title: e.detail.value })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  async handleSubmit() {
    if (this.data.submitting || !this.data.courseNames.length) {
      return
    }

    const title = this.data.title.trim()
    const content = this.data.content.trim()

    if (!title) {
      wx.showToast({ title: "请输入问题标题", icon: "none" })
      return
    }
    if (!content) {
      wx.showToast({ title: "请输入问题描述", icon: "none" })
      return
    }

    const course = this.data.courses[this.data.courseIndex]
    if (!course) {
      wx.showToast({ title: "请选择课程", icon: "none" })
      return
    }

    const courseId = course.courseId || course.id

    this.setData({ submitting: true })
    try {
      await interactionService.createQuestion({
        courseId: courseId,
        title: title,
        content: content
      })
      wx.showToast({ title: "提问成功", icon: "success" })
      setTimeout(() => wx.navigateBack(), 600)
    } catch (error) {
      wx.showToast({ title: "提问失败", icon: "none" })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
