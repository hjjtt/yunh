const request = require("../utils/request")

// ===== 评论 =====
function getCommentsByCourseId(courseId) {
  return request({ url: `/api/comment/course/${courseId}`, method: "GET", authRequired: true })
}

function createComment(data) {
  return request({ url: "/api/comment/create", method: "POST", data, authRequired: true })
}

function deleteComment(id) {
  return request({ url: `/api/comment/${id}`, method: "DELETE", authRequired: true })
}

// ===== 问答 =====
function getQuestionsByCourseId(courseId) {
  return request({ url: `/api/question/course/${courseId}`, method: "GET", authRequired: true })
}

function createQuestion(data) {
  return request({ url: "/api/question/create", method: "POST", data, authRequired: true })
}

function deleteQuestion(id) {
  return request({ url: `/api/question/${id}`, method: "DELETE", authRequired: true })
}

// ===== 回答 =====
function getAnswersByQuestionId(questionId) {
  return request({ url: `/api/answer/question/${questionId}`, method: "GET", authRequired: true })
}

function createAnswer(data) {
  return request({ url: "/api/answer/create", method: "POST", data, authRequired: true })
}

function acceptAnswer(id) {
  return request({ url: `/api/answer/accept/${id}`, method: "POST", authRequired: true })
}

// ===== 笔记 =====
function getNotesByUserId(userId) {
  return request({ url: `/api/note/user/${userId}`, method: "GET", authRequired: true })
}

function getPublicNotes() {
  return request({ url: "/api/note/public", method: "GET", authRequired: true })
}

function createNote(data) {
  return request({ url: "/api/note/create", method: "POST", data, authRequired: true })
}

function deleteNote(id) {
  return request({ url: `/api/note/${id}`, method: "DELETE", authRequired: true })
}

module.exports = {
  getCommentsByCourseId, createComment, deleteComment,
  getQuestionsByCourseId, createQuestion, deleteQuestion,
  getAnswersByQuestionId, createAnswer, acceptAnswer,
  getNotesByUserId, getPublicNotes, createNote, deleteNote
}
