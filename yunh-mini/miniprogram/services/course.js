const request = require("../utils/request")

function listCourses() {
  return request({
    url: "/api/course/list",
    method: "GET"
  })
}

function getCourseById(id) {
  return request({
    url: `/api/course/${id}`,
    method: "GET"
  })
}

function getCourseDetail(id) {
  return request({
    url: `/api/course/${id}/detail`,
    method: "GET"
  })
}

module.exports = {
  listCourses,
  getCourseById,
  getCourseDetail
}
