/**
 * 章节相关 API 接口
 */
import request from '../utils/request'

// 根据课程 ID 获取章节列表
export function getByCourseId(courseId) {
  return request.get(`/api/chapter/course/${courseId}`)
}

// 创建章节
export function create(data) {
  return request.post('/api/chapter/create', data)
}

// 更新章节信息
export function update(data) {
  return request.put('/api/chapter/update', data)
}

// 删除章节
export function remove(id) {
  return request.delete(`/api/chapter/${id}`)
}
