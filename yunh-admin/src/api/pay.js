/**
 * 支付相关 API 接口
 */
import request from '../utils/request'

// 获取支付记录列表
export function getList() {
  return request.get('/api/pay/list')
}

// 根据订单号查询支付状态
export function getByOrderNo(orderNo) {
  return request.get(`/api/pay/status/${orderNo}`)
}

// 申请退款
export function refund(orderNo, reason) {
  return request.post('/api/pay/refund', null, { params: { orderNo, reason } })
}

// 查询退款状态
export function getRefund(orderNo) {
  return request.get(`/api/pay/refund/${orderNo}`)
}
