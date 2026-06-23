function isDebugEnabled() {
  try {
    const app = typeof getApp === "function" ? getApp() : null
    const env = app && app.globalData ? app.globalData.env || {} : {}
    const storageValue = typeof wx !== "undefined" && wx.getStorageSync
      ? wx.getStorageSync("yunh_debug_logs")
      : ""

    if (storageValue === true || storageValue === "true") {
      return true
    }

    return !!env.debugLogs
  } catch (error) {
    return false
  }
}

function info() {
  if (isDebugEnabled()) {
    console.info.apply(console, arguments)
  }
}

function warn() {
  if (isDebugEnabled()) {
    console.warn.apply(console, arguments)
  }
}

function error() {
  console.error.apply(console, arguments)
}

module.exports = {
  info,
  warn,
  error
}
