const courseService = require("../../services/course")
const orderService = require("../../services/order")
const payService = require("../../services/pay")
const sessionStore = require("../../store/session")
const format = require("../../utils/format")

Page({
  data: {
    courseId: "",
    course: null,
    userInfo: null,
    submitting: false,
    userDisplayName: "未登录",
    payType: 1,
    payTypeText: "微信支付",
    priceText: "0.00",
    payTypeOptions: [
      { label: "微信支付", value: 1, caption: "适合移动端快速完成支付" },
      { label: "支付宝", value: 2, caption: "提供另一种钱包支付方式" }
    ]
  },

  onLoad(options) {
    this.setData({
      courseId: options.courseId || ""
    })
    this.loadPageData()
  },

  async loadPageData() {
    const session = sessionStore.getSession()
    this.setData({
      userInfo: session.userInfo,
      userDisplayName: resolveLearnerName(session.userInfo)
    })

    try {
      const res = await courseService.getCourseById(this.data.courseId)
      const course = res.data || null
      this.setData({
        course,
        priceText: format.currency(course && course.price)
      })
    } catch (error) {
      console.error("loadPageData error", error)
    }
  },

  async submitOrder() {
    if (this.data.submitting) {
      return
    }

    if (!this.data.userInfo || !this.data.userInfo.id) {
      wx.showToast({
        title: "请先登录",
        icon: "none"
      })
      return
    }

    if (!this.data.course || !this.data.course.id) {
      wx.showToast({
        title: "课程信息未就绪",
        icon: "none"
      })
      return
    }

    this.setData({
      submitting: true
    })

    try {
      const res = await orderService.createOrder(this.data.userInfo.id, this.data.courseId)
      const order = res.data || {}
      const paymentRes = await payService.createPayment({
        orderNo: order.orderNo,
        amount: this.data.course.price,
        payType: this.data.payType
      })
      const payment = paymentRes.data || {}
      const amount = this.data.priceText || format.currency(this.data.course.price)
      const courseName = encodeURIComponent(this.data.course.name || "")
      const courseId = this.data.courseId || ""
      wx.navigateTo({
        url: `/packageOrder/pages/result?id=${order.id || ""}&orderNo=${order.orderNo || ""}&paymentNo=${payment.paymentNo || ""}&amount=${amount}&courseName=${courseName}&courseId=${courseId}`
      })
    } catch (error) {
      console.error("submitOrder error", error)
    } finally {
      this.setData({
        submitting: false
      })
    }
  },

  choosePayType(event) {
    const value = Number(event.currentTarget.dataset.value)
    const current = this.data.payTypeOptions.find((item) => item.value === value)
    this.setData({
      payType: value,
      payTypeText: current ? current.label : "微信支付"
    })
  }
})

function resolveLearnerName(userInfo) {
  if (!userInfo) {
    return "未登录"
  }

  const nickname = userInfo.nickname || ""
  if (userInfo.role === "ADMIN" || /admin|system/i.test(nickname)) {
    return userInfo.username || "当前用户"
  }

  return nickname || userInfo.username || "当前用户"
}
