<template>
  <div class="interaction-container">
    <div class="page-header">
      <h2>互动管理</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
      <el-tab-pane label="评论管理" name="comment">
        <div class="tab-toolbar">
          <el-select v-model="commentCourseId" placeholder="选择课程筛选" clearable style="width: 260px" @change="fetchComments">
            <el-option v-for="c in courseList" :key="c.id" :label="c.name || c.title" :value="c.id" />
          </el-select>
        </div>
        <el-table :data="commentList" border stripe v-loading="commentLoading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="userId" label="用户ID" width="90" align="center" />
          <el-table-column prop="courseId" label="课程ID" width="90" align="center" />
          <el-table-column prop="content" label="评论内容" min-width="250" show-overflow-tooltip />
          <el-table-column label="评分" width="120" align="center">
            <template #default="{ row }">
              <el-rate :model-value="row.rating || 5" disabled size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="评论时间" width="180" />
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="handleDeleteComment(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!commentLoading && commentList.length === 0" description="暂无评论数据" />
      </el-tab-pane>

      <el-tab-pane label="问答管理" name="question">
        <div class="tab-toolbar">
          <el-select v-model="questionCourseId" placeholder="选择课程筛选" clearable style="width: 260px" @change="fetchQuestions">
            <el-option v-for="c in courseList" :key="c.id" :label="c.name || c.title" :value="c.id" />
          </el-select>
        </div>
        <el-table :data="questionList" border stripe v-loading="questionLoading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="userId" label="用户ID" width="90" align="center" />
          <el-table-column prop="courseId" label="课程ID" width="90" align="center" />
          <el-table-column prop="title" label="问题标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="content" label="问题描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createTime" label="提问时间" width="180" />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleViewAnswers(row)">回答</el-button>
              <el-button type="danger" link size="small" @click="handleDeleteQuestion(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!questionLoading && questionList.length === 0" description="暂无问答数据" />
      </el-tab-pane>

      <el-tab-pane label="笔记管理" name="note">
        <el-table :data="noteList" border stripe v-loading="noteLoading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="userId" label="用户ID" width="90" align="center" />
          <el-table-column prop="title" label="笔记标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="content" label="笔记内容" min-width="250" show-overflow-tooltip />
          <el-table-column label="是否公开" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isPublic === 1 ? 'success' : 'info'" size="small">
                {{ row.isPublic === 1 ? '公开' : '私密' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="handleDeleteNote(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!noteLoading && noteList.length === 0" description="暂无笔记数据" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="answerDialogVisible" title="回答列表" width="700px" destroy-on-close>
      <div style="margin-bottom: 12px">
        <strong>问题：</strong>{{ currentQuestion.title }}
      </div>
      <el-table :data="answerList" border stripe v-loading="answerLoading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="userId" label="回答者ID" width="100" align="center" />
        <el-table-column prop="content" label="回答内容" min-width="250" show-overflow-tooltip />
        <el-table-column label="是否采纳" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.accepted === 1 ? 'success' : 'info'" size="small">
              {{ row.accepted === 1 ? '已采纳' : '未采纳' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="回答时间" width="180" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.accepted !== 1"
              type="success"
              link
              size="small"
              @click="handleAcceptAnswer(row)"
            >
              采纳
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!answerLoading && answerList.length === 0" description="暂无回答" />
      <template #footer>
        <el-button @click="answerDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getComments,
  deleteComment,
  getQuestions,
  deleteQuestion,
  getAnswers,
  acceptAnswer,
  getPublicNotes,
  deleteNote
} from '../api/interaction.js'
import { getList } from '../api/course.js'

const activeTab = ref('comment')
const courseList = ref([])

const commentList = ref([])
const commentLoading = ref(false)
const commentCourseId = ref(null)

const questionList = ref([])
const questionLoading = ref(false)
const questionCourseId = ref(null)

const noteList = ref([])
const noteLoading = ref(false)

const answerDialogVisible = ref(false)
const answerList = ref([])
const answerLoading = ref(false)
const currentQuestion = ref({})

const fetchCourseList = async () => {
  try {
    const res = await getList()
    if (res.code === 200) {
      courseList.value = res.data || []
    }
  } catch (e) {
    console.error('获取课程列表失败', e)
  }
}

const fetchComments = async () => {
  commentLoading.value = true
  try {
    let res
    if (commentCourseId.value) {
      res = await getComments(commentCourseId.value)
    } else {
      const courseRes = await getList()
      if (courseRes.code === 200 && courseRes.data && courseRes.data.length > 0) {
        commentCourseId.value = courseRes.data[0].id
        res = await getComments(commentCourseId.value)
      } else {
        commentList.value = []
        return
      }
    }
    if (res && res.code === 200) {
      commentList.value = res.data || []
    }
  } catch (e) {
    console.error('获取评论失败', e)
  } finally {
    commentLoading.value = false
  }
}

const fetchQuestions = async () => {
  questionLoading.value = true
  try {
    let res
    if (questionCourseId.value) {
      res = await getQuestions(questionCourseId.value)
    } else {
      const courseRes = await getList()
      if (courseRes.code === 200 && courseRes.data && courseRes.data.length > 0) {
        questionCourseId.value = courseRes.data[0].id
        res = await getQuestions(questionCourseId.value)
      } else {
        questionList.value = []
        return
      }
    }
    if (res && res.code === 200) {
      questionList.value = res.data || []
    }
  } catch (e) {
    console.error('获取问答失败', e)
  } finally {
    questionLoading.value = false
  }
}

const fetchNotes = async () => {
  noteLoading.value = true
  try {
    const res = await getPublicNotes()
    if (res.code === 200) {
      noteList.value = res.data || []
    }
  } catch (e) {
    console.error('获取笔记失败', e)
  } finally {
    noteLoading.value = false
  }
}

const handleTabChange = (tab) => {
  if (tab === 'comment') fetchComments()
  else if (tab === 'question') fetchQuestions()
  else if (tab === 'note') fetchNotes()
}

const handleDeleteComment = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteComment(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchComments()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleDeleteQuestion = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该问题吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteQuestion(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchQuestions()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleDeleteNote = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该笔记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteNote(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchNotes()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleViewAnswers = async (row) => {
  currentQuestion.value = row
  answerDialogVisible.value = true
  answerLoading.value = true
  try {
    const res = await getAnswers(row.id)
    if (res.code === 200) {
      answerList.value = res.data || []
    } else {
      answerList.value = []
    }
  } catch (e) {
    answerList.value = []
  } finally {
    answerLoading.value = false
  }
}

const handleAcceptAnswer = async (row) => {
  try {
    const res = await acceptAnswer(row.id)
    if (res.code === 200) {
      ElMessage.success('采纳成功')
      handleViewAnswers(currentQuestion.value)
    } else {
      ElMessage.error(res.message || '采纳失败')
    }
  } catch (e) {
    ElMessage.error('采纳失败')
  }
}

onMounted(async () => {
  await fetchCourseList()
  fetchComments()
})
</script>

<style scoped>
.interaction-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.tab-toolbar {
  margin-bottom: 16px;
}
</style>
