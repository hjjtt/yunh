const TOKEN_KEY = "yunh_token"
const REFRESH_TOKEN_KEY = "yunh_refresh_token"
const USER_INFO_KEY = "yunh_user_info"

function setToken(token) {
  wx.setStorageSync(TOKEN_KEY, token || "")
}

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || ""
}

function clearToken() {
  wx.removeStorageSync(TOKEN_KEY)
}

function setRefreshToken(token) {
  wx.setStorageSync(REFRESH_TOKEN_KEY, token || "")
}

function getRefreshToken() {
  return wx.getStorageSync(REFRESH_TOKEN_KEY) || ""
}

function clearRefreshToken() {
  wx.removeStorageSync(REFRESH_TOKEN_KEY)
}

function setUserInfo(userInfo) {
  wx.setStorageSync(USER_INFO_KEY, userInfo || null)
}

function getUserInfo() {
  return wx.getStorageSync(USER_INFO_KEY) || null
}

function clearUserInfo() {
  wx.removeStorageSync(USER_INFO_KEY)
}

module.exports = {
  setToken,
  getToken,
  clearToken,
  setRefreshToken,
  getRefreshToken,
  clearRefreshToken,
  setUserInfo,
  getUserInfo,
  clearUserInfo
}
