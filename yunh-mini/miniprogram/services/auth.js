const request = require("../utils/request")

function getCaptcha() {
  return request({
    url: "/api/auth/captcha",
    method: "GET"
  })
}

function login(data) {
  return request({
    url: "/api/auth/login",
    method: "POST",
    data: Object.assign({}, data, {
      clientType: "MINI"
    })
  })
}

function logout() {
  return request({
    url: "/api/auth/logout",
    method: "POST"
  })
}

module.exports = {
  getCaptcha,
  login,
  logout
}
