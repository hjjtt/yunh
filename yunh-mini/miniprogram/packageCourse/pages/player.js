const videoService = require("../../services/video")

Page({
  data: {
    video: null,
    loading: true
  },

  onLoad(options) {
    if (options.id) {
      this.videoId = options.id
      this.loadVideo(options.id)
    }
  },

  async loadVideo(id) {
    try {
      const res = await videoService.getVideoById(id)
      this.setData({
        video: res.data,
        loading: false
      })
    } catch (error) {
      console.error("loadVideo error", error)
      this.setData({ loading: false })
      wx.showToast({ title: "加载失败", icon: "none" })
    }
  },

  // 视频播放结束，增加播放次数
  onVideoEnded() {
    if (this.videoId) {
      videoService.incrementPlayCount(this.videoId).catch(err => {
        console.error("incrementPlayCount error", err)
      })
    }
  },

  goBack() {
    wx.navigateBack({ delta: 1 })
  },

  // 格式化时长
  formatDuration(seconds) {
    if (!seconds) return "00:00"
    const m = Math.floor(seconds / 60)
    const s = seconds % 60
    return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`
  }
})
