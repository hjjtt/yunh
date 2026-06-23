const request = require("../utils/request")
const session = require("../store/session")

// 获取当前用户购物车列表
function getCartList() {
  return request({
    url: "/api/cart/list",
    method: "GET"
  })
}

// 添加课程到购物车
function addToCart(courseId) {
  return request({
    url: `/api/cart/add?courseId=${courseId}`,
    method: "POST"
  })
}

// 从购物车移除课程
function removeFromCart(courseId) {
  return request({
    url: `/api/cart/remove?courseId=${courseId}`,
    method: "DELETE"
  })
}

// 清空购物车
function clearCart() {
  return request({
    url: "/api/cart/clear",
    method: "DELETE"
  })
}

module.exports = {
  getCartList,
  addToCart,
  removeFromCart,
  clearCart
}
