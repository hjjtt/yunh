const teacherService = require("../../services/teacher")
const sessionStore = require("../../store/session")

Page({
  data: {
    dateRange: 7,
    datePresets: [
      { label: "近7天", value: 7 },
      { label: "近30天", value: 30 },
      { label: "近90天", value: 90 }
    ],
    summary: {
      totalOrders: 0,
      totalIncome: "0.00",
      totalStudents: 0,
      totalViews: 0
    },
    courseStats: [],
    chartData: [],
    chartMaxLabel: "",
    chartMidLabel: "",
    loading: true
  },

  onShow() {
    if (typeof this.getTabBar === "function" && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 })
    }
    this.loadData()
  },

  formatDate(date) {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, "0")
    const d = String(date.getDate()).padStart(2, "0")
    return `${y}-${m}-${d}`
  },

  handleDatePreset(e) {
    const value = e.currentTarget.dataset.value
    this.setData({ dateRange: value })
    this.loadData()
  },

  async loadData() {
    const session = sessionStore.getSession()
    if (!session.userInfo || !session.userInfo.id) return

    this.setData({ loading: true })

    try {
      const courseRes = await teacherService.getTeacherCourses(session.userInfo.id)
      const courses = Array.isArray(courseRes.data) ? courseRes.data : []

      if (courses.length === 0) {
        this.setData({
          summary: { totalOrders: 0, totalIncome: "0.00", totalStudents: 0, totalViews: 0 },
          courseStats: [],
          loading: false
        })
        return
      }

      const endDate = this.formatDate(new Date())
      const startDate = this.formatDate(new Date(Date.now() - this.data.dateRange * 24 * 3600 * 1000))

      let totalOrders = 0
      let totalIncome = 0
      let totalStudents = 0
      let totalViews = 0
      let allRawStats = []

      const statPromises = courses.map(course =>
        teacherService.getCourseStatistics(course.id, startDate, endDate)
          .then(statRes => {
            const stats = Array.isArray(statRes.data) ? statRes.data : []
            allRawStats.push(...stats)
            let buyCount = 0
            let income = 0
            let studentCount = 0
            let viewCount = 0

            stats.forEach(s => {
              buyCount += s.buyCount || 0
              income += parseFloat(s.income) || 0
              studentCount += s.studentCount || 0
              viewCount += s.viewCount || 0
            })

            totalOrders += buyCount
            totalIncome += income
            totalStudents += studentCount
            totalViews += viewCount

            return {
              courseId: course.id,
              courseName: course.name,
              status: course.status,
              buyCount,
              income: income.toFixed(2),
              studentCount,
              viewCount
            }
          })
          .catch(() => ({
            courseId: course.id,
            courseName: course.name,
            status: course.status,
            buyCount: 0,
            income: "0.00",
            studentCount: 0,
            viewCount: 0
          }))
      )

      const courseStats = await Promise.all(statPromises)

      // 按收入降序
      courseStats.sort((a, b) => parseFloat(b.income) - parseFloat(a.income))

      // 计算趋势图数据
      const chartResult = this.computeChartData(allRawStats)

      this.setData({
        summary: {
          totalOrders,
          totalIncome: totalIncome.toFixed(2),
          totalStudents,
          totalViews
        },
        courseStats,
        chartData: chartResult.bars,
        chartMaxLabel: chartResult.maxLabel,
        chartMidLabel: chartResult.midLabel,
        loading: false
      })

      // Canvas 绘图需要等 DOM 渲染完毕
      if (chartResult.bars.length > 0) {
        setTimeout(() => this.drawChart(), 300)
      }
    } catch (error) {
      console.error("stats loadData error", error)
      this.setData({ loading: false })
    }
  },

  computeChartData(rawStats) {
    const range = this.data.dateRange

    // 构建日期->收入 映射，填充所有日期
    const dailyMap = {}
    for (let i = range - 1; i >= 0; i--) {
      const d = new Date(Date.now() - i * 24 * 3600 * 1000)
      dailyMap[this.formatDate(d)] = 0
    }

    // 聚合收入：statDate 可能是字符串 "2026-04-15" 或数组 [2026,4,15]
    rawStats.forEach(s => {
      let key = s.statDate
      if (Array.isArray(key)) {
        const [y, m, d] = key
        key = `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`
      } else if (typeof key === "number") {
        key = this.formatDate(new Date(key))
      }
      if (key && dailyMap[key] !== undefined) {
        dailyMap[key] += parseFloat(s.income) || 0
      }
    })

    // 按日期排序
    const sortedDates = Object.keys(dailyMap).sort()

    // 分桶：最多 10 根柱子
    const maxBars = 10
    const bucketSize = Math.max(1, Math.ceil(sortedDates.length / maxBars))
    const bars = []

    for (let i = 0; i < sortedDates.length; i += bucketSize) {
      const bucket = sortedDates.slice(i, i + bucketSize)
      const total = bucket.reduce((sum, d) => sum + dailyMap[d], 0)
      const lastDate = bucket[bucket.length - 1]
      bars.push({
        date: lastDate,
        label: lastDate.slice(5).replace("-", "/"),
        income: total,
        incomeText: total.toFixed(2),
        shortVal: total >= 10000
          ? (total / 10000).toFixed(1) + "w"
          : total >= 1000
            ? (total / 1000).toFixed(1) + "k"
            : total.toFixed(0),
        height: 0,
        width: 0,
        isMax: false
      })
    }

    const maxIncome = Math.max(...bars.map(d => d.income), 1)
    bars.forEach(d => {
      // 使用 rpx 单位，最大 240rpx
      d.height = Math.round((d.income / maxIncome) * 240)
      d.width = Math.max(d.income > 0 ? 8 : 0, Math.round((d.income / maxIncome) * 100))
      if (d.income >= maxIncome) d.isMax = true
    })

    // 坐标轴标签
    const fmtMoney = v => v >= 10000
      ? "¥" + (v / 10000).toFixed(1) + "w"
      : v >= 1000
        ? "¥" + (v / 1000).toFixed(1) + "k"
        : "¥" + v.toFixed(0)

    return {
      bars,
      maxLabel: fmtMoney(maxIncome),
      midLabel: fmtMoney(maxIncome / 2)
    }
  },

  drawChart() {
    const query = wx.createSelectorQuery().in(this)
    query.select("#trendChart").fields({ node: true, size: true }).exec(res => {
      if (!res || !res[0] || !res[0].node) return

      const canvas = res[0].node
      const ctx = canvas.getContext("2d")
      const dpr = wx.getWindowInfo().pixelRatio
      const w = res[0].width
      const h = res[0].height

      canvas.width = w * dpr
      canvas.height = h * dpr
      ctx.scale(dpr, dpr)

      const data = this.data.chartData
      if (!data || data.length === 0) return

      // 布局边距
      const pad = { top: 28, right: 12, bottom: 40, left: 52 }
      const cw = w - pad.left - pad.right
      const ch = h - pad.top - pad.bottom
      const maxVal = Math.max(...data.map(d => d.income), 1)

      ctx.clearRect(0, 0, w, h)

      // 格式化轴标签
      const fmtV = v => v >= 10000 ? (v / 10000).toFixed(1) + "w"
        : v >= 1000 ? (v / 1000).toFixed(1) + "k"
        : v.toFixed(0)

      // 网格线（虚线）
      ctx.strokeStyle = "rgba(0,0,0,0.06)"
      ctx.lineWidth = 1
      ctx.setLineDash([4, 3])
      for (let i = 0; i <= 2; i++) {
        const y = pad.top + ch * i / 2
        ctx.beginPath()
        ctx.moveTo(pad.left, y)
        ctx.lineTo(pad.left + cw, y)
        ctx.stroke()
      }
      ctx.setLineDash([])

      // X 轴实线
      ctx.strokeStyle = "rgba(0,0,0,0.12)"
      ctx.beginPath()
      ctx.moveTo(pad.left, pad.top + ch)
      ctx.lineTo(pad.left + cw, pad.top + ch)
      ctx.stroke()

      // Y 轴标签
      ctx.fillStyle = "#aaa"
      ctx.font = "10px sans-serif"
      ctx.textAlign = "right"
      ctx.textBaseline = "middle"
      ctx.fillText(fmtV(maxVal), pad.left - 8, pad.top)
      ctx.fillText(fmtV(maxVal / 2), pad.left - 8, pad.top + ch / 2)
      ctx.fillText("0", pad.left - 8, pad.top + ch)

      // 计算柱子尺寸
      const n = data.length
      const gap = Math.max(4, cw / n * 0.25)
      const barW = (cw - gap * (n + 1)) / n

      data.forEach((d, i) => {
        const x = pad.left + gap + i * (barW + gap)
        const barH = Math.max(0, (d.income / maxVal) * ch)
        const y = pad.top + ch - barH

        // 渐变填充
        const grad = ctx.createLinearGradient(x, y, x, pad.top + ch)
        if (d.isMax) {
          grad.addColorStop(0, "#c0392b")
          grad.addColorStop(1, "rgba(192,57,43,0.25)")
        } else if (d.income > 0) {
          grad.addColorStop(0, "#2c3e50")
          grad.addColorStop(1, "rgba(44,62,80,0.2)")
        }

        // 圆角柱子
        const r = Math.min(4, barW / 2)
        ctx.fillStyle = grad
        if (barH > r * 2) {
          ctx.beginPath()
          ctx.moveTo(x, pad.top + ch)
          ctx.lineTo(x, y + r)
          ctx.arcTo(x, y, x + r, y, r)
          ctx.arcTo(x + barW, y, x + barW, y + r, r)
          ctx.lineTo(x + barW, pad.top + ch)
          ctx.closePath()
          ctx.fill()
        } else if (barH > 0) {
          ctx.fillRect(x, y, barW, barH)
        } else {
          ctx.fillStyle = "rgba(0,0,0,0.04)"
          ctx.fillRect(x, pad.top + ch - 2, barW, 2)
        }

        // 峰值标注
        if (d.isMax && barH > 0) {
          ctx.fillStyle = "#c0392b"
          ctx.font = "bold 10px sans-serif"
          ctx.textAlign = "center"
          ctx.textBaseline = "bottom"
          ctx.fillText("¥" + d.shortVal, x + barW / 2, y - 6)
        }

        // X 轴日期
        ctx.fillStyle = "#aaa"
        ctx.font = "9px sans-serif"
        ctx.textAlign = "center"
        ctx.textBaseline = "top"
        ctx.fillText(d.label, x + barW / 2, pad.top + ch + 8)
      })
    })
  }
})
