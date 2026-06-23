const request = require("../utils/request")

function createPayment(data) {
  return request({
    url: "/api/pay/create",
    method: "POST",
    data
  })
}

function getPayStatus(orderNo) {
  return request({
    url: `/api/pay/status/${orderNo}`,
    method: "GET"
  })
}

/**
 * 演示支付（模拟支付完成）
 * 代替直接调用 /api/pay/callback（已限制为内部接口）
 */
function demoPay(paymentNo) {
  return request({
    url: "/api/pay/demo-pay",
    method: "POST",
    data: {
      paymentNo
    }
  })
}

module.exports = {
  createPayment,
  getPayStatus,
  demoPay
}
