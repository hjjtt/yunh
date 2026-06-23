/**
 * 订单相关 API 接口
 */
import request from '../utils/request'

// 获取订单列表
export function getList() {
  return request.get('/api/order/list')
}

// 根据 ID 获取订单详情
export function getById(id) {
  return request.get(`/api/order/${id}`)
}

// 根据订单号获取订单
export function getByOrderNo(orderNo) {
  return request.get(`/api/order/no/${orderNo}`)
}

// 根据用户 ID 获取订单列表
export function getByUserId(userId) {
  return request.get(`/api/order/user/${userId}`)
}

// 创建订单
export function createOrder(userId, courseId) {
  return request.post('/api/order/create', null, { params: { userId, courseId } })
}

// 取消订单
export function cancelOrder(orderNo) {
  return request.post(`/api/order/cancel/${orderNo}`)
}

// 支付订单
export function payOrder(orderNo, payType) {
  return request.post(`/api/order/pay/${orderNo}`, null, { params: { payType } })
}
