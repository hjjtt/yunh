const request = require("../utils/request")

// ===== 课程搜索 =====
function searchCourses(keyword, page, size) {
  return request({
    url: `/api/search/course?keyword=${encodeURIComponent(keyword)}&page=${page || 0}&size=${size || 10}`,
    method: "GET"
  })
}

function syncIndex() {
  return request({ url: "/api/search/sync", method: "POST" })
}

module.exports = {
  searchCourses,
  syncIndex
}
