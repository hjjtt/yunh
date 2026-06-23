<template>
  <div class="user-list-container">
    <el-card shadow="never" class="search-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="6">
          <el-input
            v-model="searchUsername"
            placeholder="请输入用户名搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :span="18">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="pagedUsers" stripe border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="130" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="warning" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredUsers.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="用户详情" width="700px" destroy-on-close>
      <template v-if="currentUser">
        <el-descriptions :column="2" border class="user-descriptions">
          <el-descriptions-item label="ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
              {{ currentUser.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTime(currentUser.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="头像">
            <el-avatar v-if="currentUser.avatar" :src="currentUser.avatar" :size="32" />
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="course-section">
          <h4 class="section-title">已购课程</h4>
          <el-table :data="userCourses" stripe border v-loading="courseLoading" max-height="280">
            <el-table-column prop="id" label="课程ID" width="80" align="center" />
            <el-table-column prop="title" label="课程名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="price" label="价格" width="100" align="center">
              <template #default="{ row }">
                <span>&yen;{{ row.price?.toFixed(2) ?? '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '已上架' : '未上架' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!courseLoading && userCourses.length === 0" description="暂无已购课程" />
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="550px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Edit, Delete, Plus } from '@element-plus/icons-vue'
import { getList, queryByUsername, getUserCourses, register, updateUser, deleteUser } from '../api/user.js'

const searchUsername = ref('')
const allUsers = ref([])
const filteredUsers = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const currentUser = ref(null)
const userCourses = ref([])
const courseLoading = ref(false)

const formVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const formData = ref({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: '',
  status: 1
})
const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const pagedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}:${s}`
}

const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await getList()
    if (res.code === 200) {
      allUsers.value = Array.isArray(res.data) ? res.data : []
      filteredUsers.value = [...allUsers.value]
    }
  } catch (e) {
    console.error('获取用户列表失败：', e)
  } finally {
    loading.value = false
  }
}

const fetchUserCourses = async (userId) => {
  courseLoading.value = true
  userCourses.value = []
  try {
    const res = await getUserCourses(userId)
    if (res.code === 200) {
      userCourses.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (e) {
    console.error('获取用户课程失败：', e)
  } finally {
    courseLoading.value = false
  }
}

const handleSearch = async () => {
  const keyword = searchUsername.value.trim()
  if (!keyword) {
    filteredUsers.value = [...allUsers.value]
  } else {
    try {
      const res = await queryByUsername(keyword)
      if (res.code === 200) {
        if (Array.isArray(res.data)) {
          filteredUsers.value = res.data
        } else if (res.data) {
          filteredUsers.value = [res.data]
        } else {
          filteredUsers.value = []
        }
      }
    } catch (e) {
      filteredUsers.value = allUsers.value.filter((u) =>
        u.username?.toLowerCase().includes(keyword.toLowerCase())
      )
    }
  }
  currentPage.value = 1
}

const handleReset = () => {
  searchUsername.value = ''
  filteredUsers.value = [...allUsers.value]
  currentPage.value = 1
}

const handleViewDetail = (row) => {
  currentUser.value = { ...row }
  detailVisible.value = true
  fetchUserCourses(row.id)
}

const handleAdd = () => {
  isEdit.value = false
  formData.value = { username: '', nickname: '', email: '', phone: '', password: '', status: 1 }
  formVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.value = { ...row }
  formVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchUserList()
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
      res = await updateUser(formData.value.id, formData.value)
    } else {
      res = await register(formData.value)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      formVisible.value = false
      fetchUserList()
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
  fetchUserList()
})
</script>

<style scoped>
.user-list-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100%;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.user-descriptions {
  margin-bottom: 24px;
}

.course-section {
  margin-top: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}
</style>
