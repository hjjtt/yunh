import request from '../utils/request'

export function getList() {
  return request.get('/api/course/list')
}

export function getById(id) {
  return request.get(`/api/course/${id}`)
}

export function getDetail(id) {
  return request.get(`/api/course/${id}/detail`)
}

export function getByTeacher(teacherId) {
  return request.get(`/api/course/teacher/${teacherId}`)
}

export function createCourse(data) {
  return request.post('/api/course/create', data)
}

export function updateCourse(id, data) {
  return request.put(`/api/course/${id}`, data)
}

export function deleteCourse(id) {
  return request.delete(`/api/course/${id}`)
}
