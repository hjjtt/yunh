const request = require("../utils/request")

function getChaptersByCourseId(courseId) {
  return request({
    url: `/api/chapter/course/${courseId}`,
    method: "GET"
  })
}

function getVideosByCourseId(courseId) {
  return request({
    url: `/api/video/course/${courseId}`,
    method: "GET"
  })
}

function getVideosByChapterId(chapterId) {
  return request({
    url: `/api/video/chapter/${chapterId}`,
    method: "GET"
  })
}

function getVideoById(id) {
  return request({
    url: `/api/video/${id}`,
    method: "GET"
  })
}

function incrementPlayCount(id) {
  return request({
    url: `/api/video/play/${id}`,
    method: "POST"
  })
}

module.exports = {
  getChaptersByCourseId,
  getVideosByCourseId,
  getVideosByChapterId,
  getVideoById,
  incrementPlayCount
}
