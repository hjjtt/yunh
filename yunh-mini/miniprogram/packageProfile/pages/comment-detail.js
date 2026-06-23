const interactionService = require("../../services/interaction")

Page({
  data: {
    comment: null,
    replyContent: "",
    submitting: false
  },

  onLoad() {
    const eventChannel = this.getOpenerEventChannel()
    eventChannel.on("commentData", (comment) => {
      this.setData({ comment })
    })
  },

  onReplyInput(e) {
    this.setData({ replyContent: e.detail.value })
  },

  async handleReply() {
    const content = this.data.replyContent.trim()
    if (!content) {
      wx.showToast({ title: "请输入回复内容", icon: "none" })
      return
    }

    const comment = this.data.comment
    if (!comment) return

    this.setData({ submitting: true })
    try {
      await interactionService.createComment({
        courseId: comment.courseId,
        content: content,
        parentId: comment.id
      })
      wx.showToast({ title: "回复成功", icon: "success" })
      this.setData({ replyContent: "" })
    } catch (error) {
      wx.showToast({ title: "回复失败", icon: "none" })
    } finally {
      this.setData({ submitting: false })
    }
  },

  async handleDelete() {
    const comment = this.data.comment
    if (!comment) return

    const res = await new Promise(resolve => {
      wx.showModal({
        title: "确认删除",
        content: "确定要删除这条评论吗？",
        confirmColor: "#c0392b",
        success: resolve
      })
    })
    if (!res.confirm) return

    try {
      await interactionService.deleteComment(comment.id)
      wx.showToast({ title: "已删除", icon: "success" })
      setTimeout(() => wx.navigateBack(), 600)
    } catch (error) {
      wx.showToast({ title: "删除失败", icon: "none" })
    }
  }
})
