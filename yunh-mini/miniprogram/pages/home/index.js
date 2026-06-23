const courseService = require("../../services/course")
const format = require("../../utils/format")
const sessionStore = require("../../store/session")
const userService = require("../../services/user")
const orderService = require("../../services/order")
const logger = require("../../utils/logger")

function logHomeEvent(event, payload) {
  if (typeof payload === "undefined") {
    logger.info(`[yunh-mini] home ${event}`)
    return
  }

  logger.info(`[yunh-mini] home ${event}`, payload)
}

Page({
  data: {
    loading: true,
    courses: [],
    curatedCourses: [],
    activeCourseCount: 0,
    featuredCourse: null,
    metrics: [],
    userInfo: null,
    greetingTitle: "欢迎来到云课堂",
    greetingSubtitle: "探索精品课程，开启你的学习之旅",
    learningCourse: null,
    latestOrder: null,
    visualCards: []
  },

  onLoad() {
    logHomeEvent("onLoad")
    this.loadHomeData()
  },

  onReady() {
    logHomeEvent("onReady")
  },

  onShow() {
    logHomeEvent("onShow")
    if (typeof this.getTabBar === "function" && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 })
    }
    this.loadUserState()
  },

  onHide() {
    logHomeEvent("onHide")
  },

  onUnload() {
    logHomeEvent("onUnload")
  },

  async loadHomeData() {
    logHomeEvent("loadHomeData:start")
    try {
      const res = await courseService.listCourses()
      const courses = Array.isArray(res.data) ? res.data : []
      const activeCourses = courses.filter((item) => item.status === 1)
      const featuredCourse = activeCourses[0] || courses[0] || null
      const totalStock = courses.reduce((sum, item) => sum + Number(item.stock || 0), 0)
      const averagePrice = courses.length
        ? courses.reduce((sum, item) => sum + Number(item.price || 0), 0) / courses.length
        : 0

      this.setData({
        courses,
        activeCourseCount: activeCourses.length,
        curatedCourses: activeCourses.slice(0, 3).map(item => ({
          id: item.id,
          name: item.name,
          teacherName: item.teacherName,
          cover: item.cover || "",
          priceText: format.currency(item.price),
          stock: item.stock || 0
        })),
        featuredCourse,
        metrics: [
          { label: "在售课程", value: `${activeCourses.length}` },
          { label: "总库存", value: `${totalStock}` },
          { label: "均价", value: `￥${format.currency(averagePrice)}` }
        ],
        visualCards: activeCourses.slice(0, 6).map((item, index) => ({
          id: item.id,
          title: item.name,
          teacherName: item.teacherName,
          description: item.description,
          price: format.currency(item.price),
          cover: item.cover || "",
          stock: item.stock || 0,
          serial: String(index + 1).padStart(2, "0"),
          style: index === 0 ? "hero" : (index < 3 ? "card" : "compact")
        }))
      })

      logHomeEvent("loadHomeData:success", {
        courseCount: courses.length,
        activeCourseCount: activeCourses.length,
        featuredCourseId: featuredCourse ? featuredCourse.id : null
      })
    } catch (error) {
      logger.error("[yunh-mini] home loadHomeData:error", error)
    } finally {
      this.setData({
        loading: false
      })
      logHomeEvent("loadHomeData:complete", {
        loading: false
      })
    }
  },

  async loadUserState() {
    logHomeEvent("loadUserState:start")
    const session = sessionStore.getSession()
    const userInfo = session.userInfo

    if (!sessionStore.hasSession()) {
      this.setData({
        userInfo: null,
        greetingTitle: "欢迎来到云课堂",
        greetingSubtitle: "探索精品课程，开启你的学习之旅",
        learningCourse: null,
        latestOrder: null
      })
      logHomeEvent("loadUserState:skip", {
        reason: "missing or incomplete session"
      })
      return
    }

    this.setData({
      userInfo,
      greetingTitle: `欢迎回来，${resolveLearnerName(userInfo)}`,
      greetingSubtitle: "继续学习，查看你的最新进度"
    })

    try {
      const [courseRes, orderRes] = await Promise.all([
        userService.getUserCourses(userInfo.id),
        orderService.getOrdersByUserId(userInfo.id)
      ])

      const learningCourse = Array.isArray(courseRes.data) && courseRes.data.length ? courseRes.data[0] : null
      const latestOrder = Array.isArray(orderRes.data) && orderRes.data.length ? orderRes.data[0] : null

      this.setData({
        learningCourse,
        latestOrder: latestOrder ? {
          orderNo: latestOrder.orderNo,
          courseName: latestOrder.courseName || "Course",
          statusText: format.orderStatusText(latestOrder.status),
          summaryText: `${latestOrder.orderNo} | ${format.orderStatusText(latestOrder.status)}`
        } : null
      })

      logHomeEvent("loadUserState:success", {
        userId: userInfo.id,
        learningCourseId: learningCourse ? learningCourse.id : null,
        latestOrderId: latestOrder ? latestOrder.id : null
      })
    } catch (error) {
      logger.error("[yunh-mini] home loadUserState:error", error)
    }
  },

  handleCourseTap(event) {
    const id = event.currentTarget.dataset.id
    logHomeEvent("handleCourseTap", { courseId: id || null })
    if (!id) return
    wx.navigateTo({
      url: `/packageCourse/pages/detail?id=${id}`
    })
  },

  goCourseList() {
    logHomeEvent("goCourseList")
    wx.switchTab({
      url: "/pages/course/list"
    })
  },

  goCourseDetail() {
    const course = this.data.featuredCourse
    logHomeEvent("goCourseDetail", {
      courseId: course && course.id ? course.id : null
    })
    if (!course || !course.id) {
      return
    }
    wx.navigateTo({
      url: `/packageCourse/pages/detail?id=${course.id}`
    })
  },

  goLogin() {
    logHomeEvent("goLogin")
    wx.reLaunch({
      url: "/pages/auth/login"
    })
  },

  goMyCourse() {
    logHomeEvent("goMyCourse")
    wx.navigateTo({
      url: "/packageProfile/pages/my-course"
    })
  },

  goMyOrder() {
    logHomeEvent("goMyOrder")
    wx.navigateTo({
      url: "/packageProfile/pages/my-order"
    })
  },

  openVisualCard(event) {
    const id = event.currentTarget.dataset.id
    logHomeEvent("openVisualCard", {
      courseId: id || null
    })
    if (!id) {
      return
    }
    wx.navigateTo({
      url: `/packageCourse/pages/detail?id=${id}`
    })
  }
})

function resolveLearnerName(userInfo) {
  if (!userInfo) {
    return "Learner"
  }

  const nickname = userInfo.nickname || ""
  if (userInfo.role === "ADMIN" || /admin|system/i.test(nickname)) {
    return userInfo.username || "Learner"
  }

  return nickname || userInfo.username || "Learner"
}
