const interactionService = require("../../services/interaction")

Page({
  data: {
    note: null,
    deleting: false
  },

  onLoad() {
    const eventChannel = this.getOpenerEventChannel()
    eventChannel.on("noteData", (note) => {
      this.setData({ note })
      wx.setNavigationBarTitle({
        title: note.title || "笔记详情"
      })
    })
  },

  async handleDelete() {
    if (this.data.deleting) return
    const note = this.data.note
    if (!note) return

    const res = await new Promise(resolve => {
      wx.showModal({
        title: "确认删除",
        content: "删除后无法恢复，确定要删除这条笔记吗？",
        confirmColor: "#c0392b",
        success: resolve
      })
    })
    if (!res.confirm) return

    this.setData({ deleting: true })
    try {
      await interactionService.deleteNote(note.id)
      wx.showToast({ title: "已删除", icon: "success" })
      setTimeout(() => wx.navigateBack(), 600)
    } catch (error) {
      wx.showToast({ title: "删除失败", icon: "none" })
    } finally {
      this.setData({ deleting: false })
    }
  },

  handleBack() {
    wx.navigateBack()
  }
})
