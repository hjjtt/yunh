const videoService = require("../../services/video")
const sessionStore = require("../../store/session")

Page({
  data: {
    chapterList: [],
    // 每个章节的展开状态，{ chapterId: true/false }
    expandedMap: {}
  },

  onLoad(options) {
    this.courseId = options.id
    this.loadData(options.id)
  },

  async loadData(courseId) {
    if (!courseId) return

    const session = sessionStore.getSession()
    if (!session.token) {
      wx.showModal({
        title: "提示",
        content: "查看章节列表需要登录，是否前往登录？",
        confirmText: "去登录",
        cancelText: "返回",
        success: (res) => {
          if (res.confirm) {
            wx.reLaunch({ url: "/pages/auth/login" })
          } else {
            wx.navigateBack({ delta: 1 })
          }
        }
      })
      return
    }

    try {
      const [chapterRes, videoRes] = await Promise.all([
        videoService.getChaptersByCourseId(courseId),
        videoService.getVideosByCourseId(courseId)
      ])

      const chapters = Array.isArray(chapterRes.data) ? chapterRes.data : []
      const videos = Array.isArray(videoRes.data) ? videoRes.data : []

      // 按 chapterId 分组视频
      const videoMap = {}
      videos.forEach(v => {
        const key = v.chapterId
        if (!videoMap[key]) videoMap[key] = []
        videoMap[key].push(v)
      })

      // 把视频列表挂到每个章节上
      const chapterList = chapters.map(ch => ({
        ...ch,
        videos: videoMap[ch.id] || []
      }))

      this.setData({ chapterList })
    } catch (error) {
      console.error("loadData error", error)
    }
  },

  // 点击章节：展开/收起
  toggleChapter(e) {
    const id = e.currentTarget.dataset.id
    const key = `expandedMap.${id}`
    const current = this.data.expandedMap[id]
    this.setData({ [key]: !current })
  },

  // 点击视频：跳转播放页
  goPlayer(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/packageCourse/pages/player?id=${id}`
    })
  },

  // 格式化时长（秒 → mm:ss）
  formatDuration(seconds) {
    if (!seconds) return "00:00"
    const m = Math.floor(seconds / 60)
    const s = seconds % 60
    return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`
  }
})
