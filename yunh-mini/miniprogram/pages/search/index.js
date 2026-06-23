const courseService = require("../../services/course")
const searchService = require("../../services/search")
const format = require("../../utils/format")

Page({
  data: {
    keyword: "",
    searching: false,
    searchResults: [],
    courseList: [],
    filteredCourses: [],
    featuredCourse: null,
    hotCourses: [],
    teachers: [],
    teacherCount: 0,
    categories: [],
    activeCategory: "all"
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 })
    }
    this.loadData()
  },

  async loadData() {
    try {
      const res = await courseService.listCourses()
      const courseList = Array.isArray(res.data) ? res.data : []
      const activeCourses = courseList.filter(c => c.status === 1)

      const featuredCourse = activeCourses.length ? activeCourses[0] : null

      const hotCourses = activeCourses
        .slice()
        .sort((a, b) => Number(b.price) - Number(a.price))
        .slice(0, 5)

      const teacherMap = {}
      activeCourses.forEach(c => {
        const name = c.teacherName || "未知"
        if (!teacherMap[name]) {
          teacherMap[name] = { name, courses: [], totalPrice: 0 }
        }
        teacherMap[name].courses.push(c)
        teacherMap[name].totalPrice += Number(c.price || 0)
      })
      const teachers = Object.values(teacherMap).map(t => ({
        name: t.name,
        courseCount: t.courses.length,
        avgPrice: format.currency(t.totalPrice / t.courses.length)
      }))

      const categoryDefs = [
        { key: "all", label: "全部", icon: "📚", keywords: [] },
        { key: "java", label: "Java", icon: "☕", keywords: ["java", "设计模式"] },
        { key: "cloud", label: "微服务", icon: "☁️", keywords: ["spring cloud", "微服务", "nacos"] },
        { key: "frontend", label: "前端", icon: "🎨", keywords: ["vue", "前端", "react"] },
        { key: "database", label: "数据库", icon: "🗄️", keywords: ["mysql", "redis", "数据库", "索引"] }
      ]
      const categories = categoryDefs.map(cat => {
        const count = cat.key === "all"
          ? activeCourses.length
          : activeCourses.filter(c => {
              const text = (c.name + " " + (c.description || "")).toLowerCase()
              return cat.keywords.some(kw => text.includes(kw))
            }).length
        return { ...cat, count }
      })

      this.setData({
        courseList,
        filteredCourses: activeCourses,
        featuredCourse,
        hotCourses,
        teachers,
        teacherCount: teachers.length,
        categories,
        activeCategory: "all"
      })
    } catch (error) {
      console.error("discover load error", error)
    }
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  async doSearch() {
    const keyword = this.data.keyword.trim()
    if (!keyword) {
      this.setData({ searching: false, searchResults: [] })
      return
    }

    this.setData({ searching: true })

    try {
      // 先尝试后端搜索 API
      const res = await searchService.searchCourses(keyword, 0, 20)
      let results = Array.isArray(res.data) ? res.data : []

      // 后端 ES 未实现时返回空，回退到本地过滤
      if (!results.length) {
        const activeCourses = this.data.courseList.filter(c => c.status === 1)
        const kw = keyword.toLowerCase()
        results = activeCourses.filter(c => {
          const text = (c.name + " " + (c.description || "") + " " + (c.teacherName || "")).toLowerCase()
          return text.includes(kw)
        })
      }

      this.setData({ searchResults: results })
    } catch (error) {
      // 搜索 API 不可用时，使用本地过滤
      const activeCourses = this.data.courseList.filter(c => c.status === 1)
      const kw = keyword.toLowerCase()
      const results = activeCourses.filter(c => {
        const text = (c.name + " " + (c.description || "") + " " + (c.teacherName || "")).toLowerCase()
        return text.includes(kw)
      })
      this.setData({ searchResults: results })
    }
  },

  clearSearch() {
    this.setData({ keyword: "", searching: false, searchResults: [] })
  },

  switchCategory(event) {
    const key = event.currentTarget.dataset.key
    const activeCourses = this.data.courseList.filter(c => c.status === 1)

    let filteredCourses
    if (key === "all") {
      filteredCourses = activeCourses
    } else {
      const cat = this.data.categories.find(c => c.key === key)
      const keywords = cat ? cat.keywords : []
      filteredCourses = activeCourses.filter(c => {
        const text = (c.name + " " + (c.description || "")).toLowerCase()
        return keywords.some(kw => text.includes(kw))
      })
    }

    this.setData({
      activeCategory: key,
      filteredCourses
    })
  },

  goDetail(event) {
    const id = event.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/packageCourse/pages/detail?id=${id}`
    })
  }
})
