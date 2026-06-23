const interactionService = require("../../services/interaction")

Page({
  data: {
    question: null,
    answers: [],
    answerContent: "",
    submitting: false,
    hasAccepted: false
  },

  onLoad() {
    const eventChannel = this.getOpenerEventChannel()
    eventChannel.on("questionData", (question) => {
      this.setData({ question })
      this.loadAnswers(question.id)
    })
  },

  async loadAnswers(questionId) {
    try {
      const res = await interactionService.getAnswersByQuestionId(questionId)
      const answers = Array.isArray(res.data) ? res.data : []
      answers.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      const hasAccepted = answers.some(a => a.isAccepted === 1)
      this.setData({ answers, hasAccepted })
    } catch (error) {
      console.error("loadAnswers error", error)
    }
  },

  onAnswerInput(e) {
    this.setData({ answerContent: e.detail.value })
  },

  async handleSubmitAnswer() {
    const content = this.data.answerContent.trim()
    if (!content) {
      wx.showToast({ title: "请输入回答内容", icon: "none" })
      return
    }

    const question = this.data.question
    if (!question) return

    this.setData({ submitting: true })
    try {
      await interactionService.createAnswer({
        questionId: question.id,
        content: content
      })
      wx.showToast({ title: "回答成功", icon: "success" })
      this.setData({ answerContent: "" })
      this.loadAnswers(question.id)
    } catch (error) {
      wx.showToast({ title: "回答失败", icon: "none" })
    } finally {
      this.setData({ submitting: false })
    }
  },

  async handleAcceptAnswer(e) {
    const answerId = e.currentTarget.dataset.id
    try {
      await interactionService.acceptAnswer(answerId)
      wx.showToast({ title: "已采纳", icon: "success" })
      const question = this.data.question
      if (question) this.loadAnswers(question.id)
    } catch (error) {
      wx.showToast({ title: "操作失败", icon: "none" })
    }
  },

  async handleDeleteQuestion() {
    const question = this.data.question
    if (!question) return

    const res = await new Promise(resolve => {
      wx.showModal({
        title: "确认删除",
        content: "确定要删除这个问题吗？",
        confirmColor: "#c0392b",
        success: resolve
      })
    })
    if (!res.confirm) return

    try {
      await interactionService.deleteQuestion(question.id)
      wx.showToast({ title: "已删除", icon: "success" })
      setTimeout(() => wx.navigateBack(), 600)
    } catch (error) {
      wx.showToast({ title: "删除失败", icon: "none" })
    }
  }
})
