<template>
  <div class="course-list-container">
    <div class="page-header">
      <h2>课程管理</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="按课程名称搜索"
          clearable
          style="width: 260px"
          :prefix-icon="Search"
        />
        <el-button type="success" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增课程
        </el-button>
      </div>
    </div>

    <el-table :data="filteredCourses" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="name" label="课程名称" width="200" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="teacherName" label="讲师" width="120" />
      <el-table-column label="价格" width="100" align="center">
        <template #default="{ row }">
          <span style="color: #e6a23c; font-weight: 600">
            ¥{{ typeof row.price === 'number' ? row.price.toFixed(2) : '0.00' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '已上架' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleViewDetail(row)">
            详情
          </el-button>
          <el-button type="warning" link size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            删除
          </el-button>
          <el-button type="info" link size="small" @click="handleViewStats(row)">
            统计
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailDialogVisible" title="课程详情" width="600px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="课程ID">{{ currentCourse.id }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ currentCourse.name }}</el-descriptions-item>
        <el-descriptions-item label="讲师">{{ currentCourse.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="价格">
          <span style="color: #e6a23c; font-weight: 600">
            ¥{{ typeof currentCourse.price === 'number' ? currentCourse.price.toFixed(2) : '0.00' }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="库存">{{ currentCourse.stock }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentCourse.status === 1 ? 'success' : 'danger'">
            {{ currentCourse.status === 1 ? '已上架' : '已下架' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentCourse.createTime }}</el-descriptions-item>
        <el-descriptions-item label="课程描述" :span="2">{{ currentCourse.description }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑课程' : '新增课程'"
      width="600px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="讲师姓名" prop="teacherName">
          <el-input v-model="formData.teacherName" placeholder="请输入讲师姓名" />
        </el-form-item>
        <el-form-item label="讲师ID">
          <el-input-number v-model="formData.teacherId" :min="1" placeholder="讲师ID" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="formData.stock" :min="0" />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statsDialogVisible" title="课程统计" width="750px" destroy-on-close>
      <div class="stats-toolbar">
        <el-date-picker
          v-model="statsDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 320px"
        />
        <el-button type="primary" :loading="statsLoading" @click="fetchCourseStats">
          查询
        </el-button>
      </div>
      <div ref="chartRef" style="width: 100%; height: 380px"></div>
      <template #footer>
        <el-button @click="statsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getList, getById, getDetail, createCourse, updateCourse, deleteCourse } from '../api/course.js'
import { getCourseStats } from '../api/statistics.js'

const courseList = ref([])
const loading = ref(false)
const searchKeyword = ref('')

const filteredCourses = computed(() => {
  if (!searchKeyword.value) return courseList.value
  const keyword = searchKeyword.value.toLowerCase()
  return courseList.value.filter(item =>
    item.name && item.name.toLowerCase().includes(keyword)
  )
})

const fetchCourseList = async () => {
  loading.value = true
  try {
    const res = await getList()
    if (res.code === 200) {
      courseList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取课程列表失败')
    }
  } catch (e) {
    ElMessage.error('获取课程列表失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const detailDialogVisible = ref(false)
const currentCourse = ref({})

const handleViewDetail = async (row) => {
  try {
    const res = await getDetail(row.id)
    if (res.code === 200) {
      // 详情接口返回的是DetailVO，课程数据在course字段中
      currentCourse.value = res.data.course || res.data || row
    } else {
      const res2 = await getById(row.id)
      if (res2.code === 200) {
        currentCourse.value = res2.data || row
      } else {
        currentCourse.value = row
      }
    }
  } catch (e) {
    currentCourse.value = row
  }
  detailDialogVisible.value = true
}

const formVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const formData = ref({
  name: '',
  teacherName: '',
  teacherId: 1,
  price: 0,
  stock: 100,
  description: '',
  status: 1
})
const formRules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const handleAdd = () => {
  isEdit.value = false
  formData.value = { name: '', teacherName: '', teacherId: 1, price: 0, stock: 100, description: '', status: 1 }
  formVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.value = { ...row }
  formVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除课程 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteCourse(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchCourseList()
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
      res = await updateCourse(formData.value.id, formData.value)
    } else {
      res = await createCourse(formData.value)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      formVisible.value = false
      fetchCourseList()
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

const statsDialogVisible = ref(false)
const statsLoading = ref(false)
const statsCourseId = ref(null)
const statsDateRange = ref([])
const chartRef = ref(null)
let chartInstance = null

const handleViewStats = (row) => {
  statsCourseId.value = row.id
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  statsDateRange.value = [
    formatDate(start),
    formatDate(end)
  ]
  statsDialogVisible.value = true
}

const formatDate = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const fetchCourseStats = async () => {
  if (!statsDateRange.value || statsDateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  statsLoading.value = true
  try {
    const res = await getCourseStats(
      statsCourseId.value,
      statsDateRange.value[0],
      statsDateRange.value[1]
    )
    if (res.code === 200) {
      await nextTick()
      renderChart(res.data)
    } else {
      ElMessage.error(res.message || '获取统计数据失败')
    }
  } catch (e) {
    ElMessage.error('获取统计数据失败，请检查网络连接')
  } finally {
    statsLoading.value = false
  }
}

const renderChart = (data) => {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  const dates = (data && data.dates) || []
  const orderCounts = (data && data.orderCounts) || []
  const revenueAmounts = (data && data.revenueAmounts) || []
  if ((!dates.length) && data && Array.isArray(data.records)) {
    data.records.forEach(item => {
      dates.push(item.date || item.statDate)
      orderCounts.push(item.orderCount || 0)
      revenueAmounts.push(item.revenue || item.revenueAmount || 0)
    })
  }
  const option = {
    title: { text: '课程统计数据', left: 'center' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '营收金额'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: dates },
    yAxis: [
      { type: 'value', name: '订单数', position: 'left' },
      { type: 'value', name: '营收金额(元)', position: 'right' }
    ],
    series: [
      {
        name: '订单数', type: 'line', data: orderCounts, smooth: true,
        itemStyle: { color: '#409EFF' }, areaStyle: { color: 'rgba(64,158,255,0.15)' }
      },
      {
        name: '营收金额', type: 'line', yAxisIndex: 1, data: revenueAmounts, smooth: true,
        itemStyle: { color: '#67C23A' }, areaStyle: { color: 'rgba(103,194,58,0.15)' }
      }
    ]
  }
  chartInstance.setOption(option, true)
}

watch(statsDialogVisible, (val) => {
  if (!val && chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

onMounted(() => {
  fetchCourseList()
})
</script>

<style scoped>
.course-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stats-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
