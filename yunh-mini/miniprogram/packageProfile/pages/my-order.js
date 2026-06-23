const sessionStore = require("../../store/session")
const orderService = require("../../services/order")
const format = require("../../utils/format")

Page({
  data: {
    loading: true,
    orderList: [],
    stats: []
  },

  onShow() {
    this.loadOrders()
  },

  async loadOrders() {
    const session = sessionStore.getSession()
    const userInfo = session.userInfo

    if (!userInfo || !userInfo.id) {
      this.setData({
        orderList: [],
        loading: false
      })
      return
    }

    try {
      const res = await orderService.getOrdersByUserId(userInfo.id)
      const orderList = Array.isArray(res.data) ? res.data : []
      this.setData({
        orderList: orderList.map(item => ({
          id: item.id,
          orderNo: item.orderNo,
          courseName: item.courseName || "待补全课程名",
          userName: item.userName || "待补全用户名",
          amountText: `￥${format.currency(item.payAmount || item.originalPrice)}`,
          statusText: format.orderStatusText(item.status),
          createTimeText: format.formatDateTime(item.createTime)
        })),
        stats: buildOrderStats(orderList)
      })
    } catch (error) {
      console.error("loadOrders error", error)
    } finally {
      this.setData({
        loading: false
      })
    }
  }
})

function buildOrderStats(orderList) {
  const list = Array.isArray(orderList) ? orderList : []
  const paidCount = list.filter(item => item.status === 1).length
  const pendingCount = list.filter(item => item.status === 0).length
  const totalAmount = list.reduce((sum, item) => sum + Number(item.payAmount || item.originalPrice || 0), 0)

  return [
    { label: "订单总数", value: `${list.length}` },
    { label: "待支付", value: `${pendingCount}` },
    { label: "已支付", value: `${paidCount}` },
    { label: "累计金额", value: `￥${format.currency(totalAmount)}` }
  ]
}
