/**
 * 统计相关 API 接口
 */
import request from '../utils/request'

// 获取课程统计数据
export function getCourseStats(courseId, startDate, endDate) {
  return request.get(`/api/statistics/course/${courseId}`, { params: { startDate, endDate } })
}

// 获取平台统计数据
export function getPlatformStats(startDate, endDate) {
  return request.get('/api/statistics/platform', { params: { startDate, endDate } })
}

// 生成每日统计报表
export function generateDaily() {
  return request.post('/api/statistics/daily')
}
