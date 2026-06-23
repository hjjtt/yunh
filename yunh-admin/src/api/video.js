/**
 * 视频相关 API 接口
 */
import request from '../utils/request'

// 根据课程 ID 获取视频列表
export function getByCourseId(courseId) {
  return request.get(`/api/video/course/${courseId}`)
}

// 根据 ID 获取视频详情
export function getById(id) {
  return request.get(`/api/video/${id}`)
}

// 上传视频
export function upload(data) {
  return request.post('/api/video/upload', data)
}

// 更新视频信息
export function update(data) {
  return request.put('/api/video/update', data)
}

// 删除视频
export function remove(id) {
  return request.delete(`/api/video/${id}`)
}
