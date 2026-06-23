const teacherService = require("../../services/teacher")
const videoService = require("../../services/video")

Page({
  data: {
    isEdit: false,
    courseId: null,
    chapterId: null,
    form: {
      title: "",
      sort: "1"
    },
    videos: [],
    showVideoForm: false,
    videoForm: {
      title: "",
      videoUrl: "",
      duration: "0",
      sort: "1"
    }
  },

  onLoad(options) {
    this.setData({
      courseId: options.courseId,
      isEdit: !!options.chapterId,
      chapterId: options.chapterId || null
    })

    if (options.chapterId) {
      this.loadChapter(options.chapterId)
    }
  },

  async loadChapter(chapterId) {
    try {
      const res = await videoService.getVideosByChapterId(chapterId)
      this.setData({
        videos: Array.isArray(res.data) ? res.data : []
      })
    } catch (error) {
      console.error("loadChapter error", error)
    }
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [`form.${field}`]: e.detail.value })
  },

  handleVideoInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [`videoForm.${field}`]: e.detail.value })
  },

  async handleSaveChapter() {
    const { form } = this.data
    if (!form.title.trim()) {
      wx.showToast({ title: "请输入章节标题", icon: "none" })
      return
    }

    try {
      const data = {
        title: form.title,
        sort: parseInt(form.sort) || 1,
        courseId: this.data.courseId
      }

      if (this.data.isEdit) {
        data.id = this.data.chapterId
        await teacherService.updateChapter(data)
      } else {
        await teacherService.createChapter(data)
      }

      wx.showToast({ title: "保存成功", icon: "success" })
      setTimeout(() => wx.navigateBack(), 800)
    } catch (error) {
      wx.showToast({ title: "保存失败", icon: "none" })
    }
  },

  toggleVideoForm() {
    this.setData({
      showVideoForm: !this.data.showVideoForm,
      videoForm: { title: "", videoUrl: "", duration: "0", sort: "1" }
    })
  },

  async handleSaveVideo() {
    const { videoForm } = this.data
    if (!videoForm.title.trim()) {
      wx.showToast({ title: "请输入视频标题", icon: "none" })
      return
    }

    try {
      await teacherService.createVideo({
        title: videoForm.title,
        videoUrl: videoForm.videoUrl,
        duration: parseInt(videoForm.duration) || 0,
        sort: parseInt(videoForm.sort) || 1,
        chapterId: this.data.chapterId,
        courseId: this.data.courseId
      })

      wx.showToast({ title: "添加成功", icon: "success" })
      this.setData({ showVideoForm: false })
      this.loadChapter(this.data.chapterId)
    } catch (error) {
      wx.showToast({ title: "添加失败", icon: "none" })
    }
  },

  async handleDeleteVideo(e) {
    const id = e.currentTarget.dataset.id
    const res = await new Promise(resolve => {
      wx.showModal({
        title: "确认删除",
        content: "确定删除该视频吗？",
        confirmColor: "#c0392b",
        success: resolve
      })
    })
    if (!res.confirm) return

    try {
      await teacherService.deleteVideo(id)
      wx.showToast({ title: "已删除", icon: "success" })
      this.loadChapter(this.data.chapterId)
    } catch (error) {
      wx.showToast({ title: "删除失败", icon: "none" })
    }
  }
})
