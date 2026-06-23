const orderService = require("../../services/order")
const payService = require("../../services/pay")
const format = require("../../utils/format")

Page({
  data: {
    order: null,
    payment: null,
    statusText: "",
    createTimeText: "--",
    paymentStatusText: "",
    paymentNo: "",
    orderNo: "",
    paying: false,
    polling: false,
    pollCount: 0,
    pollHint: "正在等待支付状态同步",
    amountText: "0.00",
    courseName: "",
    payDone: false,
    paySuccess: false,
    orderId: "",
    courseId: ""
  },

  onLoad(options) {
    const amountText = options.amount || "0.00"
    const courseName = decodeURIComponent(options.courseName || "")
    this.setData({
      paymentNo: options.paymentNo || "",
      orderNo: options.orderNo || "",
      orderId: options.id || "",
      courseId: options.courseId || "",
      amountText,
      courseName
    })
    this.loadAll(options.id, options.orderNo)
  },

  onUnload() {
    this.stopPolling()
  },

  async loadAll(id, orderNo) {
    if (!id) {
      return
    }

    try {
      const orderPromise = orderService.getOrderById(id)
      const paymentPromise = orderNo
        ? payService.getPayStatus(orderNo)
        : Promise.resolve({ data: null })

      const [orderRes, paymentRes] = await Promise.all([orderPromise, paymentPromise])

      const order = orderRes.data || null
      const payment = paymentRes.data || null

      const updateData = {
        order,
        statusText: format.orderStatusText(order ? order.status : null),
        createTimeText: format.formatDateTime(order ? order.createTime : ""),
        payment,
        paymentNo: payment ? payment.paymentNo : this.data.paymentNo,
        paymentStatusText: format.paymentStatusText(payment ? payment.status : null)
      }

      if (order) {
        const orderAmount = order.totalAmount || order.amount || order.price
        if (orderAmount) {
          updateData.amountText = format.currency(orderAmount)
        }
        if (order.courseName && !this.data.courseName) {
          updateData.courseName = order.courseName
        }
        if (order.courseId && !this.data.courseId) {
          updateData.courseId = order.courseId
        }
      }

      if (payment && payment.status === 1) {
        updateData.payDone = true
        updateData.paySuccess = true
        updateData.pollHint = "支付成功"
      }

      this.setData(updateData)
      this.updatePollingState()
    } catch (error) {
      console.error("loadAll error", error)
    }
  },

  async handleSimPay() {
    if (!this.data.paymentNo || this.data.paying) {
      return
    }

    this.setData({
      paying: true
    })

    try {
      await payService.demoPay(this.data.paymentNo)

      this.setData({
        payDone: true,
        paySuccess: true
      })
      wx.showToast({
        title: "支付成功",
        icon: "success"
      })
      await this.loadAll(this.data.orderId, this.data.orderNo)
    } catch (error) {
      console.error("handleSimPay error", error)
      this.setData({
        payDone: true,
        paySuccess: false
      })
      wx.showToast({
        title: "支付失败",
        icon: "none"
      })
    } finally {
      this.setData({
        paying: false
      })
    }
  },

  updatePollingState() {
    const shouldPoll = Boolean(this.data.order && this.data.order.status === 0 && this.data.paymentNo && !this.data.payDone)
    if (shouldPoll) {
      this.startPolling()
      return
    }
    this.stopPolling()
  },

  startPolling() {
    if (this.pollTimer) {
      return
    }

    this.setData({
      polling: true,
      pollHint: "正在自动刷新支付状态"
    })

    this.pollTimer = setInterval(async () => {
      if (!this.data.orderId) {
        this.stopPolling()
        return
      }

      const nextPollCount = this.data.pollCount + 1
      this.setData({
        pollCount: nextPollCount
      })

      try {
        await this.loadAll(this.data.orderId, this.data.orderNo)
      } catch (error) {
        console.error("polling loadAll error", error)
      }

      if (!this.data.order || this.data.order.status !== 0) {
        this.stopPolling()
        return
      }

      if (nextPollCount >= 12) {
        this.stopPolling("自动刷新已暂停，可手动确认支付")
      }
    }, 3000)
  },

  stopPolling(message) {
    if (this.pollTimer) {
      clearInterval(this.pollTimer)
      this.pollTimer = null
    }

    this.setData({
      polling: false,
      pollCount: 0,
      pollHint: message || (this.data.order && this.data.order.status === 0 ? "请确认支付" : "支付状态已同步")
    })
  },

  refreshStatus() {
    if (this.data.orderId) {
      this.loadAll(this.data.orderId, this.data.orderNo)
    }
  },

  goMyCourse() {
    wx.switchTab({
      url: "/pages/profile/index"
    })
  },

  goHome() {
    wx.switchTab({
      url: "/pages/home/index"
    })
  }
})
