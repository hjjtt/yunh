const request = require("../utils/request")

// ===== 课程管理 =====
function getTeacherCourses(teacherId) {
  return request({
    url: `/api/course/teacher/${teacherId}`,
    method: "GET",
    authRequired: true
  })
}

function createCourse(data) {
  return request({
    url: "/api/course/create",
    method: "POST",
    data,
    authRequired: true
  })
}

function updateCourse(id, data) {
  return request({
    url: `/api/course/${id}`,
    method: "PUT",
    data,
    authRequired: true
  })
}

function deleteCourse(id) {
  return request({
    url: `/api/course/${id}`,
    method: "DELETE",
    authRequired: true
  })
}

// ===== 章节管理 =====
function createChapter(data) {
  return request({
    url: "/api/chapter/create",
    method: "POST",
    data,
    authRequired: true
  })
}

function updateChapter(data) {
  return request({
    url: "/api/chapter/update",
    method: "PUT",
    data,
    authRequired: true
  })
}

function deleteChapter(id) {
  return request({
    url: `/api/chapter/${id}`,
    method: "DELETE",
    authRequired: true
  })
}

// ===== 视频管理 =====
function createVideo(data) {
  return request({
    url: "/api/video/upload",
    method: "POST",
    data,
    authRequired: true
  })
}

function updateVideo(data) {
  return request({
    url: "/api/video/update",
    method: "PUT",
    data,
    authRequired: true
  })
}

function deleteVideo(id) {
  return request({
    url: `/api/video/${id}`,
    method: "DELETE",
    authRequired: true
  })
}

// ===== 统计数据 =====
function getCourseStatistics(courseId, startDate, endDate) {
  return request({
    url: `/api/statistics/course/${courseId}?startDate=${startDate}&endDate=${endDate}`,
    method: "GET",
    authRequired: true
  })
}

function getPlatformStatistics(startDate, endDate) {
  return request({
    url: `/api/statistics/platform?startDate=${startDate}&endDate=${endDate}`,
    method: "GET",
    authRequired: true
  })
}

module.exports = {
  getTeacherCourses,
  createCourse,
  updateCourse,
  deleteCourse,
  createChapter,
  updateChapter,
  deleteChapter,
  createVideo,
  updateVideo,
  deleteVideo,
  getCourseStatistics,
  getPlatformStatistics
}
