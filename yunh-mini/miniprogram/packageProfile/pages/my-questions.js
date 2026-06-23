const interactionService = require("../../services/interaction")
const userService = require("../../services/user")
const teacherService = require("../../services/teacher")
const sessionStore = require("../../store/session")

Page({
  data: {
    questions: [],
    loading: true
  },

  onShow() {
    this.loadQuestions()
  },

  async loadQuestions() {
    const session = sessionStore.getSession()
    if (!session.userInfo || !session.userInfo.id) return

    const userId = session.userInfo.id
    const isTeacher = session.userInfo.role === "TEACHER"

    this.setData({ loading: true })
    try {
      // 获取用户的课程列表
      const courseRes = isTeacher
        ? await teacherService.getTeacherCourses(userId)
        : await userService.getUserCourses(userId)

      const courses = Array.isArray(courseRes.data) ? courseRes.data : []

      // 并发获取每门课程的问题（限制前 10 门）
      // 学员端 API 返回 courseId/courseName，教师端返回 id/name
      const targetCourses = courses.slice(0, 10)
      const questionPromises = targetCourses.map(course =>
        interactionService.getQuestionsByCourseId(course.courseId || course.id)
          .then(res => {
            const questions = Array.isArray(res.data) ? res.data : []
            return questions.map(q => ({
              ...q,
              courseName: course.courseName || course.name || "未知课程"
            }))
          })
          .catch(() => [])
      )

      const results = await Promise.all(questionPromises)
      const allQuestions = results.flat()
      allQuestions.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))

      this.setData({ questions: allQuestions, loading: false })
    } catch (error) {
      console.error("loadQuestions error", error)
      this.setData({ loading: false })
    }
  },

  handleView(e) {
    const index = e.currentTarget.dataset.index
    const question = this.data.questions[index]
    if (!question) return

    wx.navigateTo({
      url: "/packageProfile/pages/question-detail",
      success(res) {
        res.eventChannel.emit("questionData", question)
      }
    })
  },

  goCreate() {
    wx.navigateTo({ url: "/packageProfile/pages/question-create" })
  },

  async handleDelete(e) {
    const id = e.currentTarget.dataset.id
    const res = await new Promise(resolve => {
      wx.showModal({
        title: "确认删除",
        content: "确定要删除这个问题吗？",
        confirmColor: "#c0392b",
        success: resolve
      })
    })
    if (!res.confirm) return

    try {
      await interactionService.deleteQuestion(id)
      wx.showToast({ title: "已删除", icon: "success" })
      this.loadQuestions()
    } catch (error) {
      wx.showToast({ title: "删除失败", icon: "none" })
    }
  }
})
