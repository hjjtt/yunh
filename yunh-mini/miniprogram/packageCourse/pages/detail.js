const courseService = require("../../services/course")
const videoService = require("../../services/video")
const format = require("../../utils/format")
const sessionStore = require("../../store/session")

Page({
  data: {
    courseId: "",
    loading: true,
    course: null,
    teacher: null,
    chapterList: [],
    videoList: [],
    totalDurationText: "0 分钟",
    teacherDisplayName: "",
    heroBadges: [],
    previewVideos: [],
    priceText: "0.00",
    stockText: "0 个名额",
    buyButtonText: "立即购买",
    priceLoaded: false
  },

  onLoad(options) {
    this.setData({
      courseId: options.id || ""
    })
    this.loadDetail()
  },

  async loadDetail() {
    if (!this.data.courseId) {
      this.setData({ loading: false })
      return
    }

    try {
      const [detailRes, chapterRes, videoRes] = await Promise.all([
        courseService.getCourseDetail(this.data.courseId),
        videoService.getChaptersByCourseId(this.data.courseId),
        videoService.getVideosByCourseId(this.data.courseId)
      ])

      const course = detailRes.data ? detailRes.data.course : null
      const teacher = detailRes.data ? detailRes.data.teacher : null
      const chapterList = Array.isArray(chapterRes.data) ? chapterRes.data : []
      const videoList = Array.isArray(videoRes.data) ? videoRes.data : []
      const totalDuration = videoList.reduce((sum, item) => sum + Number(item.duration || 0), 0)
      const priceText = format.currency(course && course.price)
      const stockCount = Number(course && course.stock ? course.stock : 0)

      this.setData({
        course,
        teacher,
        chapterList,
        videoList,
        totalDurationText: format.duration(totalDuration),
        teacherDisplayName: course && course.teacherName
          ? course.teacherName
          : teacher
            ? (teacher.nickname || teacher.username || "未分配讲师")
            : "未分配讲师",
        heroBadges: [
          `库存 ${stockCount}`,
          `章节 ${chapterList.length}`,
          `视频 ${videoList.length}`
        ],
        previewVideos: videoList.slice(0, 3),
        priceText,
        stockText: stockCount > 0 ? `剩余 ${stockCount} 个名额` : "已售罄",
        buyButtonText: stockCount > 0 ? `立即购买 | ￥${priceText}` : "暂不可购买"
      })
    } catch (error) {
      console.error("loadDetail error", error)
    } finally {
      this.setData({
        loading: false
      })
    }
  },

  goConfirmOrder() {
    const course = this.data.course
    if (!course || !course.id) {
      return
    }

    if (Number(course.stock || 0) <= 0) {
      wx.showToast({
        title: "课程暂不可购买",
        icon: "none"
      })
      return
    }

    const session = sessionStore.getSession()
    if (!session.token) {
      wx.showModal({
        title: "需要先登录",
        content: "创建订单前请先登录账号。",
        confirmText: "去登录",
        cancelText: "稍后",
        success: (res) => {
          if (res.confirm) {
            wx.reLaunch({ url: "/pages/auth/login" })
          }
        }
      })
      return
    }

    wx.navigateTo({
      url: `/packageOrder/pages/confirm?courseId=${this.data.courseId}`
    })
  },

  goChapter() {
    wx.navigateTo({
      url: `/packageCourse/pages/chapter?id=${this.data.courseId}`
    })
  }
})
