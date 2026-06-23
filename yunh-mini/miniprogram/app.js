const envMap = {
  develop: require("./env/dev"),
  trial: require("./env/test"),
  release: require("./env/prod")
}
const logger = require("./utils/logger")

function logAppEvent(event, payload) {
  if (typeof payload === "undefined") {
    logger.info(`[yunh-mini] app ${event}`)
    return
  }

  logger.info(`[yunh-mini] app ${event}`, payload)
}

function resolveRuntimeEnv() {
  const accountInfo = wx.getAccountInfoSync ? wx.getAccountInfoSync() : null
  const envVersion = accountInfo && accountInfo.miniProgram ? accountInfo.miniProgram.envVersion : "develop"
  const systemInfo = wx.getSystemInfoSync ? wx.getSystemInfoSync() : null
  const platform = systemInfo ? systemInfo.platform : ""
  const baseEnv = envMap[envVersion] || envMap.develop
  const baseUrlOverride = wx.getStorageSync("yunh_base_url_override") || ""

  // WeChat DevTools often runs preview/trial packages locally. Force local dev API there.
  const resolvedEnv = platform === "devtools" ? Object.assign({}, envMap.develop) : Object.assign({}, baseEnv)

  if (baseUrlOverride) {
    resolvedEnv.baseUrl = baseUrlOverride
  }

  logger.info("[yunh-mini] runtime env", {
    envVersion,
    platform,
    envName: resolvedEnv.name,
    baseUrl: resolvedEnv.baseUrl,
    timeout: resolvedEnv.timeout
  })

  return resolvedEnv
}

App({
  globalData: {
    env: {},
    userInfo: null,
    token: "",
    refreshToken: "",
    courseDraft: null
  },

  onLaunch() {
    logAppEvent("onLaunch:start")
    const currentEnv = resolveRuntimeEnv()
    const token = wx.getStorageSync("yunh_token") || ""
    const refreshToken = wx.getStorageSync("yunh_refresh_token") || ""
    const userInfo = wx.getStorageSync("yunh_user_info") || null

    this.globalData.env = currentEnv
    this.globalData.token = token
    this.globalData.refreshToken = refreshToken
    this.globalData.userInfo = userInfo

    logAppEvent("onLaunch:complete", {
      hasToken: !!token,
      hasRefreshToken: !!refreshToken,
      hasUserInfo: !!userInfo
    })
  },

  onShow(options) {
    logAppEvent("onShow", options || {})
  },

  onHide() {
    logAppEvent("onHide")
  },

  onError(error) {
    logger.error("[yunh-mini] app onError", {
      error
    })
  },

  onUnhandledRejection(payload) {
    logger.error("[yunh-mini] app onUnhandledRejection", payload || {})
  },

  onPageNotFound(payload) {
    logger.error("[yunh-mini] app onPageNotFound", payload || {})
  }
})
