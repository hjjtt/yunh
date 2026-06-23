<template>
  <div class="chapter-container">
    <div class="page-header">
      <h2>章节管理</h2>
    </div>

    <div class="toolbar">
      <el-select
        v-model="selectedCourseId"
        placeholder="请选择课程"
        clearable
        style="width: 300px"
        @change="handleCourseChange"
      >
        <el-option
          v-for="course in courseList"
          :key="course.id"
          :label="course.name || course.title"
          :value="course.id"
        />
      </el-select>
      <el-button type="success" :disabled="!selectedCourseId" @click="handleAdd" style="margin-left: 12px">
        <el-icon><Plus /></el-icon>
        新增章节
      </el-button>
    </div>

    <el-empty v-if="!selectedCourseId" description="请选择课程查看章节" />

    <el-table
      v-else
      :data="chapterList"
      border
      stripe
      v-loading="loading"
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="title" label="章节标题" min-width="250" />
      <el-table-column prop="sort" label="排序" width="100" align="center" />
      <el-table-column prop="createTime" label="创建时间" width="200" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑章节' : '新增章节'"
      width="500px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="章节标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入章节标题" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
        <el-form-item label="章节描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getByCourseId, create, update, remove } from '../api/chapter.js'
import { getList } from '../api/course.js'

const courseList = ref([])
const selectedCourseId = ref(null)
const chapterList = ref([])
const loading = ref(false)

const formVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const formData = ref({
  title: '',
  sort: 0,
  description: '',
  courseId: null
})
const formRules = {
  title: [{ required: true, message: '请输入章节标题', trigger: 'blur' }]
}

const fetchCourseList = async () => {
  try {
    const res = await getList()
    if (res.code === 200) {
      courseList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取课程列表失败')
    }
  } catch (e) {
    ElMessage.error('获取课程列表失败，请检查网络连接')
  }
}

const fetchChapterList = async () => {
  if (!selectedCourseId.value) {
    chapterList.value = []
    return
  }
  loading.value = true
  try {
    const res = await getByCourseId(selectedCourseId.value)
    if (res.code === 200) {
      chapterList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取章节列表失败')
    }
  } catch (e) {
    ElMessage.error('获取章节列表失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const handleCourseChange = () => {
  fetchChapterList()
}

const handleAdd = () => {
  isEdit.value = false
  formData.value = { title: '', sort: 0, description: '', courseId: selectedCourseId.value }
  formVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.value = { ...row, courseId: selectedCourseId.value }
  formVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除章节 "${row.title}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await remove(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchChapterList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    let res
    if (isEdit.value) {
      res = await update(formData.value)
    } else {
      res = await create(formData.value)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      formVisible.value = false
      fetchChapterList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
}

onMounted(() => {
  fetchCourseList()
})
</script>

<style scoped>
.chapter-container {
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

.toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
</style>
