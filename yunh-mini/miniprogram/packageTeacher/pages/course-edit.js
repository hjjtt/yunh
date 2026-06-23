const teacherService = require("../../services/teacher")
const videoService = require("../../services/video")
const sessionStore = require("../../store/session")

Page({
  data: {
    isEdit: false,
    courseId: null,
    loading: false,
    form: {
      name: "",
      description: "",
      teacherName: "",
      price: "",
      stock: "100",
      cover: "",
      status: 1
    },
    chapters: []
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ isEdit: true, courseId: options.id })
      this.loadCourse(options.id)
    } else {
      const session = sessionStore.getSession()
      this.setData({
        "form.teacherName": session.userInfo ? session.userInfo.nickname || session.userInfo.username : ""
      })
    }
  },

  async loadCourse(id) {
    this.setData({ loading: true })
    try {
      const res = await teacherService.getTeacherCourses(sessionStore.getSession().userInfo.id)
      const courses = Array.isArray(res.data) ? res.data : []
      const course = courses.find(c => c.id == id)

      if (course) {
        this.setData({
          form: {
            name: course.name || "",
            description: course.description || "",
            teacherName: course.teacherName || "",
            price: course.price != null ? String(course.price) : "",
            stock: course.stock != null ? String(course.stock) : "100",
            cover: course.cover || "",
            status: course.status != null ? course.status : 1
          }
        })
      }

      // 加载章节
      const chapterRes = await videoService.getChaptersByCourseId(id)
      this.setData({
        chapters: Array.isArray(chapterRes.data) ? chapterRes.data : [],
        loading: false
      })
    } catch (error) {
      console.error("loadCourse error", error)
      this.setData({ loading: false })
    }
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [`form.${field}`]: e.detail.value })
  },

  toggleStatus() {
    this.setData({ "form.status": this.data.form.status === 1 ? 0 : 1 })
  },

  async handleSave() {
    const { form } = this.data
    if (!form.name.trim()) {
      wx.showToast({ title: "请输入课程名称", icon: "none" })
      return
    }

    this.setData({ loading: true })
    try {
      const data = {
        name: form.name,
        description: form.description,
        teacherName: form.teacherName,
        price: parseFloat(form.price) || 0,
        stock: parseInt(form.stock) || 100,
        cover: form.cover,
        status: form.status
      }

      if (this.data.isEdit) {
        await teacherService.updateCourse(this.data.courseId, data)
        wx.showToast({ title: "保存成功", icon: "success" })
      } else {
        const session = sessionStore.getSession()
        data.teacherId = session.userInfo.id
        await teacherService.createCourse(data)
        wx.showToast({ title: "创建成功", icon: "success" })
      }

      setTimeout(() => wx.navigateBack(), 800)
    } catch (error) {
      wx.showToast({ title: "保存失败", icon: "none" })
      console.error("handleSave error", error)
    } finally {
      this.setData({ loading: false })
    }
  },

  async handlePublish() {
    const { form } = this.data
    if (!form.name.trim()) {
      wx.showToast({ title: "请输入课程名称", icon: "none" })
      return
    }

    this.setData({ loading: true })
    try {
      const data = {
        name: form.name,
        description: form.description,
        teacherName: form.teacherName,
        price: parseFloat(form.price) || 0,
        stock: parseInt(form.stock) || 100,
        cover: form.cover,
        status: 1
      }

      if (this.data.isEdit) {
        await teacherService.updateCourse(this.data.courseId, data)
        wx.showToast({ title: "发布成功", icon: "success" })
      } else {
        const session = sessionStore.getSession()
        data.teacherId = session.userInfo.id
        await teacherService.createCourse(data)
        wx.showToast({ title: "发布成功", icon: "success" })
      }

      this.setData({ "form.status": 1 })
      setTimeout(() => wx.navigateBack(), 800)
    } catch (error) {
      wx.showToast({ title: "发布失败", icon: "none" })
      console.error("handlePublish error", error)
    } finally {
      this.setData({ loading: false })
    }
  },

  async handleDelete() {
    if (!this.data.isEdit) return

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
      await teacherService.deleteCourse(this.data.courseId)
      wx.showToast({ title: "已删除", icon: "success" })
      setTimeout(() => wx.navigateBack(), 800)
    } catch (error) {
      wx.showToast({ title: "删除失败", icon: "none" })
    }
  },

  goToChapterEdit(e) {
    const chapterId = e.currentTarget.dataset.id
    const courseId = this.data.courseId
    wx.navigateTo({
      url: `/packageTeacher/pages/chapter-edit?courseId=${courseId}&chapterId=${chapterId}`
    })
  },

  goToAddChapter() {
    wx.navigateTo({
      url: `/packageTeacher/pages/chapter-edit?courseId=${this.data.courseId}`
    })
  }
})
