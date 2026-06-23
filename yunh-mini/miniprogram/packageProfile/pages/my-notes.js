const interactionService = require("../../services/interaction")
const sessionStore = require("../../store/session")

Page({
  data: {
    notes: [],
    loading: true
  },

  onShow() {
    this.loadNotes()
  },

  async loadNotes() {
    const session = sessionStore.getSession()
    if (!session.userInfo || !session.userInfo.id) return

    this.setData({ loading: true })
    try {
      const res = await interactionService.getNotesByUserId(session.userInfo.id)
      const notes = Array.isArray(res.data) ? res.data : []
      notes.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      this.setData({ notes, loading: false })
    } catch (error) {
      console.error("loadNotes error", error)
      this.setData({ loading: false })
    }
  },

  handleView(e) {
    const index = e.currentTarget.dataset.index
    const note = this.data.notes[index]
    if (!note) return

    wx.navigateTo({
      url: "/packageProfile/pages/note-detail",
      success(res) {
        res.eventChannel.emit("noteData", note)
      }
    })
  },

  async handleDelete(e) {
    const id = e.currentTarget.dataset.id
    const res = await new Promise(resolve => {
      wx.showModal({
        title: "确认删除",
        content: "删除后无法恢复，确定要删除这条笔记吗？",
        confirmColor: "#c0392b",
        success: resolve
      })
    })
    if (!res.confirm) return

    try {
      await interactionService.deleteNote(id)
      wx.showToast({ title: "已删除", icon: "success" })
      this.loadNotes()
    } catch (error) {
      wx.showToast({ title: "删除失败", icon: "none" })
    }
  }
})
