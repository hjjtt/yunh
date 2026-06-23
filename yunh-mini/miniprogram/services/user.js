const request = require("../utils/request")

function queryByUsername(username) {
  return request({
    url: "/api/user/query",
    method: "GET",
    data: {
      username
    },
    authRequired: true
  })
}

function getUserCourses(userId) {
  return request({
    url: `/api/user/${userId}/courses`,
    method: "GET",
    authRequired: true
  })
}

module.exports = {
  queryByUsername,
  getUserCourses
}
