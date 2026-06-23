const storage = require("./storage")
const sessionStore = require("../store/session")
const logger = require("./logger")

let refreshingPromise = null

function decodeBase64Url(value) {
  if (!value || !wx.base64ToArrayBuffer) {
    return ""
  }

  const base64 = value.replace(/-/g, "+").replace(/_/g, "/")
  const padding = (4 - (base64.length % 4)) % 4
  const padded = `${base64}${"=".repeat(padding)}`
  const buffer = wx.base64ToArrayBuffer(padded)
  const bytes = new Uint8Array(buffer)
  let text = ""

  for (let i = 0; i < bytes.length; i += 1) {
    text += String.fromCharCode(bytes[i])
  }

  return text
}

function parseJwtPayload(token) {
  if (!token) {
    return null
  }

  const parts = token.split(".")
  if (parts.length < 2) {
    return null
  }

  try {
    return JSON.parse(decodeBase64Url(parts[1]))
  } catch (error) {
    logger.warn("[yunh-mini] parse jwt payload failed", {
      message: error.message
    })
    return null
  }
}

function isTokenExpiringSoon(token, advanceSeconds) {
  const payload = parseJwtPayload(token)
  if (!payload || !payload.exp) {
    return true
  }

  const nowSeconds = Math.floor(Date.now() / 1000)
  return payload.exp <= nowSeconds + (advanceSeconds || 30)
}

function redirectToLogin(message) {
  sessionStore.clearSession()

  if (message) {
    wx.showToast({
      title: message,
      icon: "none"
    })
  }

  setTimeout(() => {
    wx.reLaunch({ url: "/pages/auth/login" })
  }, 1000)
}

function resolveBaseUrl(baseUrl) {
  if (!baseUrl) {
    return ""
  }

  return baseUrl.replace("://localhost", "://127.0.0.1")
}

function resolveFallbackBaseUrls(env, currentBaseUrl) {
  const fallbackBaseUrls = Array.isArray(env && env.fallbackBaseUrls) ? env.fallbackBaseUrls : []
  return fallbackBaseUrls
    .map((item) => resolveBaseUrl(item))
    .filter((item) => !!item && item !== currentBaseUrl)
}

function normalizeRequestError(error, env) {
  if (!error) {
    return error
  }

  const normalizedBaseUrl = resolveBaseUrl((env && env.baseUrl) || "")
  const errMsg = error.errMsg || error.message || ""
  const isTimeout = errMsg.toLowerCase().includes("timeout")
  const isFetchFailure = errMsg.toLowerCase().includes("failed to fetch")
    || errMsg.toLowerCase().includes("request:fail")
    || errMsg.toLowerCase().includes("network error")

  if (isTimeout) {
    const hostHint = normalizedBaseUrl.includes("127.0.0.1")
      ? "Current dev API points to this machine. For real-device debugging, replace it with your computer LAN IP and ensure the domain is allowed in Mini Program settings."
      : "Check whether the API service is reachable and whether the current domain is allowed in Mini Program request settings."

    return Object.assign({}, error, {
      message: `Request timeout: ${normalizedBaseUrl || "missing baseUrl"}. ${hostHint}`
    })
  }

  if (isFetchFailure) {
    const hostHint = normalizedBaseUrl.includes("127.0.0.1")
      ? "Current dev API points to 127.0.0.1: if the local gateway is not running, Mini Program pages cannot load data. For real-device debugging, replace it with your computer LAN IP."
      : "Check whether the API service is reachable and whether the current domain is allowed in Mini Program request settings."

    return Object.assign({}, error, {
      message: `Request failed: ${normalizedBaseUrl || "missing baseUrl"}. ${hostHint}`
    })
  }

  return error
}

function refreshAccessToken() {
  if (refreshingPromise) {
    return refreshingPromise
  }

  const app = getApp()
  const env = app.globalData.env || {}
  const refreshToken = storage.getRefreshToken()
  const baseUrl = resolveBaseUrl(env.baseUrl || "")

  if (!refreshToken) {
    return Promise.reject(new Error("missing refresh token"))
  }

  refreshingPromise = new Promise((resolve, reject) => {
    wx.request({
      url: `${baseUrl}/api/auth/refresh`,
      method: "POST",
      timeout: env.timeout || 12000,
      data: {
        refreshToken
      },
      header: {
        "content-type": "application/json"
      },
      success(res) {
        const payload = res.data || {}
        if (res.statusCode >= 200 && res.statusCode < 300 && payload.code === 200) {
          const currentUser = storage.getUserInfo()
          sessionStore.setSession({
            accessToken: payload.data.accessToken,
            refreshToken: payload.data.refreshToken,
            userInfo: currentUser
          })
          resolve(payload.data.accessToken)
          return
        }
        reject(payload)
      },
      fail(error) {
        reject(normalizeRequestError(error, env))
      },
      complete() {
        refreshingPromise = null
      }
    })
  })

  return refreshingPromise
}

function request(options, retry, preAuthorized) {
  const app = getApp()
  const env = app.globalData.env || {}
  const token = storage.getToken()
  const baseUrl = resolveBaseUrl(options.baseUrl || env.baseUrl || "")
  const requestUrl = `${baseUrl}${options.url}`
  const requestTimeout = options.timeout || env.timeout || 12000
  const fallbackBaseUrls = resolveFallbackBaseUrls(env, baseUrl)

  if (options.authRequired) {
    if (!sessionStore.hasSession()) {
      const authError = new Error("登录已过期，请重新登录")
      redirectToLogin(authError.message)
      return Promise.reject(authError)
    }

    if (!preAuthorized && isTokenExpiringSoon(token)) {
      return refreshAccessToken()
        .then(() => request(options, retry, true))
        .catch((error) => {
          redirectToLogin("登录已过期，请重新登录")
          return Promise.reject(error)
        })
    }
  }

  return new Promise((resolve, reject) => {
    logger.info("[yunh-mini] request start", {
      url: requestUrl,
      method: options.method || "GET",
      retry: !!retry,
      timeout: requestTimeout
    })

    wx.request({
      url: requestUrl,
      method: options.method || "GET",
      data: options.data || {},
      timeout: requestTimeout,
      header: Object.assign(
        {
          "content-type": "application/json"
        },
        token ? { Authorization: `Bearer ${token}` } : {},
        options.header || {}
      ),
      success(res) {
        logger.info("[yunh-mini] request success", {
          url: requestUrl,
          method: options.method || "GET",
          retry: !!retry,
          timeout: requestTimeout,
          statusCode: res.statusCode
        })
        const payload = res.data || {}
        const isUnauthorized = res.statusCode === 401 || payload.code === 401

        if (isUnauthorized && !retry) {
          refreshAccessToken()
            .then(() => resolve(request(options, true, true)))
            .catch((error) => {
              redirectToLogin("登录已过期，请重新登录")
              reject(error)
            })
          return
        }

        if (res.statusCode >= 200 && res.statusCode < 300) {
          if (typeof payload.code !== "undefined" && payload.code !== 200) {
            reject(payload)
            return
          }

          resolve(payload)
          return
        }

        reject(res)
      },
      fail(error) {
        logger.error("[yunh-mini] request fail", {
          url: requestUrl,
          method: options.method || "GET",
          retry: !!retry,
          timeout: requestTimeout,
          error
        })

        if (!retry && error && (error.errMsg || error.message || "").toLowerCase().includes("timeout") && fallbackBaseUrls.length) {
          const nextBaseUrl = fallbackBaseUrls[0]
          logger.warn("[yunh-mini] request retry with fallback baseUrl", {
            from: baseUrl,
            to: nextBaseUrl,
            url: options.url
          })
          resolve(request(Object.assign({}, options, { baseUrl: nextBaseUrl }), true))
          return
        }

        reject(normalizeRequestError(error, env))
      },
      complete(res) {
        logger.info("[yunh-mini] request complete", {
          url: requestUrl,
          method: options.method || "GET",
          retry: !!retry,
          timeout: requestTimeout,
          statusCode: res && typeof res.statusCode !== "undefined" ? res.statusCode : null,
          errMsg: res && res.errMsg ? res.errMsg : ""
        })
      }
    })
  }).catch((error) => {
    if (!options.silent) {
      const message = error.message || error.errMsg || error.data?.message || "Network error, please retry later"
      wx.showToast({
        title: message.length > 18 ? "Request failed, retry later" : message,
        icon: "none"
      })
    }
    return Promise.reject(error)
  })
}

module.exports = request
