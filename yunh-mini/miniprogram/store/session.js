const storage = require("../utils/storage")

const SAFE_USER_FIELDS = [
  "id",
  "username",
  "nickname",
  "avatar",
  "status",
  "role",
  "createTime",
  "updateTime"
]

function syncAppSession(partialState) {
  const app = getApp()
  app.globalData = Object.assign({}, app.globalData, partialState)
}

function sanitizeUserInfo(userInfo) {
  if (!userInfo || typeof userInfo !== "object") {
    return null
  }

  const sanitized = {}
  SAFE_USER_FIELDS.forEach((field) => {
    if (typeof userInfo[field] !== "undefined") {
      sanitized[field] = userInfo[field]
    }
  })

  if (!sanitized.id) {
    return null
  }

  return sanitized
}

function hasUnsafeUserFields(userInfo) {
  if (!userInfo || typeof userInfo !== "object") {
    return false
  }

  return ["password", "email", "phone"].some((field) =>
    Object.prototype.hasOwnProperty.call(userInfo, field)
  )
}

function setSession(payload) {
  const token = payload.accessToken || payload.token || ""
  const refreshToken = payload.refreshToken || ""
  const userInfo = sanitizeUserInfo(payload.userInfo)

  storage.setToken(token)
  storage.setRefreshToken(refreshToken)
  storage.setUserInfo(userInfo)

  syncAppSession({
    token,
    refreshToken,
    userInfo
  })
}

function clearSession() {
  storage.clearToken()
  storage.clearRefreshToken()
  storage.clearUserInfo()

  syncAppSession({
    token: "",
    refreshToken: "",
    userInfo: null
  })
}

function getSession() {
  const rawUserInfo = storage.getUserInfo()
  const userInfo = sanitizeUserInfo(rawUserInfo)

  if (hasUnsafeUserFields(rawUserInfo)) {
    storage.setUserInfo(userInfo)
  }

  return {
    token: storage.getToken(),
    refreshToken: storage.getRefreshToken(),
    userInfo
  }
}

function hasSession() {
  const session = getSession()
  return !!(session.token && session.refreshToken && session.userInfo && session.userInfo.id)
}

module.exports = {
  setSession,
  clearSession,
  getSession,
  hasSession
}
