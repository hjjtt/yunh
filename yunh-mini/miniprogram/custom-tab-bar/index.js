const sessionStore = require("../store/session")

const studentTabs = [
  { pagePath: "/pages/home/index", text: "首页" },
  { pagePath: "/pages/course/list", text: "课程" },
  { pagePath: "/pages/search/index", text: "发现" },
  { pagePath: "/pages/profile/index", text: "我的" }
]

const teacherTabs = [
  { pagePath: "/pagesTeacher/home/index", text: "教学台" },
  { pagePath: "/pagesTeacher/course/index", text: "课程管理" },
  { pagePath: "/pagesTeacher/stats/index", text: "数据" },
  { pagePath: "/pagesTeacher/profile/index", text: "我的" }
]

Component({
  data: {
    selected: 0,
    isTeacher: false,
    list: studentTabs
  },
  attached() {
    const session = sessionStore.getSession()
    const isTeacher = session.userInfo && session.userInfo.role === "TEACHER"
    const list = isTeacher ? teacherTabs : studentTabs

    this.setData({ isTeacher, list })

    const pages = getCurrentPages()
    if (pages.length === 0) return
    const currentPage = pages[pages.length - 1]
    if (!currentPage || !currentPage.route) return
    const currentPath = `/${currentPage.route}`
    const index = list.findIndex(item => item.pagePath === currentPath)
    if (index !== -1) {
      this.setData({ selected: index })
    }
  },
  methods: {
    switchTab(e) {
      const data = e.currentTarget.dataset
      const url = data.path
      wx.switchTab({ url })
      this.setData({
        selected: data.index
      })
    }
  }
})
