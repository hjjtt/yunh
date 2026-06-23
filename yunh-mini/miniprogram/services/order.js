const request = require("../utils/request")

function createOrder(userId, courseId) {
  return request({
    url: `/api/order/create?userId=${userId}&courseId=${courseId}`,
    method: "POST",
    data: {},
    authRequired: true
  })
}

function getOrderById(id) {
  return request({
    url: `/api/order/${id}`,
    method: "GET",
    authRequired: true
  })
}

function getOrdersByUserId(userId) {
  return request({
    url: `/api/order/user/${userId}`,
    method: "GET",
    authRequired: true
  })
}

module.exports = {
  createOrder,
  getOrderById,
  getOrdersByUserId
}
