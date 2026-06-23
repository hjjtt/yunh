/**
 * 互动相关 API 接口
 * 包含评论、问答、笔记等互动功能
 */
import request from '../utils/request'

// 获取课程评论列表
export function getComments(courseId) {
  return request.get(`/api/comment/course/${courseId}`)
}

// 删除评论
export function deleteComment(id) {
  return request.delete(`/api/comment/${id}`)
}

// 获取课程问题列表
export function getQuestions(courseId) {
  return request.get(`/api/question/course/${courseId}`)
}

// 删除问题
export function deleteQuestion(id) {
  return request.delete(`/api/question/${id}`)
}

// 获取问题的回答列表
export function getAnswers(questionId) {
  return request.get(`/api/answer/question/${questionId}`)
}

// 采纳回答
export function acceptAnswer(id) {
  return request.post(`/api/answer/accept/${id}`)
}

// 获取公开笔记列表
export function getPublicNotes() {
  return request.get('/api/note/public')
}

// 删除笔记
export function deleteNote(id) {
  return request.delete(`/api/note/${id}`)
}
