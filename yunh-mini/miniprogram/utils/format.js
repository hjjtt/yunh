function currency(value) {
  const num = Number(value || 0)
  if (Number.isNaN(num)) {
    return "0.00"
  }
  return num.toFixed(2)
}

function duration(seconds) {
  const total = Number(seconds || 0)
  if (!total) {
    return "0 分钟"
  }
  const minute = Math.floor(total / 60)
  if (minute < 60) {
    return `${minute} 分钟`
  }
  const hour = Math.floor(minute / 60)
  const remain = minute % 60
  return `${hour} 小时 ${remain} 分钟`
}

function formatDateTime(value) {
  if (!value) {
    return "--"
  }

  const text = String(value).replace("T", " ")
  return text.length > 19 ? text.slice(0, 19) : text
}

function orderStatusText(status) {
  const map = {
    0: "待支付",
    1: "已支付",
    2: "已完成",
    3: "已取消"
  }
  return map[status] || "未知状态"
}

function paymentStatusText(status) {
  const map = {
    0: "待支付",
    1: "支付成功",
    2: "支付失败"
  }
  return map[status] || "未知状态"
}

function safeArray(value) {
  return Array.isArray(value) ? value : []
}

module.exports = {
  currency,
  duration,
  formatDateTime,
  orderStatusText,
  paymentStatusText,
  safeArray
}
