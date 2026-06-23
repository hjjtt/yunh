import request from '../utils/request'

export function getList() {
  return request.get('/api/user/list')
}

export function getById(id) {
  return request.get(`/api/user/${id}`)
}

export function login(data) {
  return request.post('/api/auth/login', data)
}

export function getCaptcha() {
  return request.get('/api/auth/captcha')
}

export function refreshToken(refreshToken) {
  return request.post('/api/auth/refresh', { refreshToken })
}

export function authLogout() {
  return request.post('/api/auth/logout')
}

export function queryByUsername(username) {
  return request.get('/api/user/query', { params: { username } })
}

export function register(data) {
  return request.post('/api/user/register', data)
}

export function updateUser(id, data) {
  return request.put(`/api/user/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/api/user/${id}`)
}

export function getUserCourses(userId) {
  return request.get(`/api/user/${userId}/courses`)
}
