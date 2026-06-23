<template>
  <div class="video-container">
    <div class="page-header">
      <h2>视频管理</h2>
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
        新增视频
      </el-button>
    </div>

    <el-empty v-if="!selectedCourseId" description="请选择课程查看视频" />

    <el-table
      v-else
      :data="videoList"
      border
      stripe
      v-loading="loading"
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="title" label="视频标题" min-width="180" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="时长" width="100" align="center">
        <template #default="{ row }">
          {{ formatDuration(row.duration) }}
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="playCount" label="播放次数" width="100" align="center" />
      <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑视频' : '新增视频'"
      width="550px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="视频标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入视频标题" />
        </el-form-item>
        <el-form-item label="视频描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="时长(秒)">
          <el-input-number v-model="formData.duration" :min="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
        <el-form-item label="视频地址">
          <el-input v-model="formData.url" placeholder="请输入视频URL" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="正常"
            inactive-text="禁用"
          />
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
import { getByCourseId, upload, update, remove } from '../api/video.js'
import { getList } from '../api/course.js'

const courseList = ref([])
const selectedCourseId = ref(null)
const videoList = ref([])
const loading = ref(false)

const formVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const formData = ref({
  title: '',
  description: '',
  duration: 0,
  sort: 0,
  url: '',
  status: 1,
  courseId: null
})
const formRules = {
  title: [{ required: true, message: '请输入视频标题', trigger: 'blur' }]
}

const formatDuration = (seconds) => {
  if (!seconds && seconds !== 0) return '00:00'
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return String(minutes).padStart(2, '0') + ':' + String(secs).padStart(2, '0')
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

const fetchVideoList = async () => {
  if (!selectedCourseId.value) {
    videoList.value = []
    return
  }
  loading.value = true
  try {
    const res = await getByCourseId(selectedCourseId.value)
    if (res.code === 200) {
      videoList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取视频列表失败')
    }
  } catch (e) {
    ElMessage.error('获取视频列表失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const handleCourseChange = () => {
  fetchVideoList()
}

const handleAdd = () => {
  isEdit.value = false
  formData.value = {
    title: '',
    description: '',
    duration: 0,
    sort: 0,
    url: '',
    status: 1,
    courseId: selectedCourseId.value
  }
  formVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.value = { ...row, courseId: selectedCourseId.value }
  formVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除视频 "${row.title}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await remove(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchVideoList()
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
      res = await upload(formData.value)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      formVisible.value = false
      fetchVideoList()
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
.video-container {
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
