<template>
  <div class="order-list-container">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchForm.orderNo"
        placeholder="请输入订单号"
        clearable
        style="width: 220px"
      />
      <el-input
        v-model="searchForm.userName"
        placeholder="请输入用户名"
        clearable
        style="width: 180px"
      />
      <el-select v-model="searchForm.status" placeholder="订单状态" clearable style="width: 140px">
        <el-option label="全部" :value="''" />
        <el-option label="待支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="已完成" :value="2" />
        <el-option label="已取消" :value="3" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      <el-button type="success" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建订单
      </el-button>
    </div>

    <el-table :data="pagedOrders" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="userName" label="用户名" width="120" />
      <el-table-column prop="courseName" label="课程名" show-overflow-tooltip />
      <el-table-column label="原价" width="100" align="center">
        <template #default="{ row }">
          ¥{{ typeof row.originalPrice === 'number' ? row.originalPrice.toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="实付" width="100" align="center">
        <template #default="{ row }">
          <span style="color: #e6a23c; font-weight: 700">
            ¥{{ typeof row.payAmount === 'number' ? row.payAmount.toFixed(2) : '0.00' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="110" align="center">
        <template #default="{ row }">
          <template v-if="row.payType === 1">
            <el-tag type="success">微信</el-tag>
          </template>
          <template v-else-if="row.payType === 2">
            <el-tag type="primary">支付宝</el-tag>
          </template>
          <template v-else>
            <span style="color: #909399">--</span>
          </template>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleViewDetail(row)">
            详情
          </el-button>
          <el-button
            v-if="row.status === 0"
            type="success"
            link
            size="small"
            @click="handlePay(row)"
          >
            支付
          </el-button>
          <el-button
            v-if="row.status === 0"
            type="warning"
            link
            size="small"
            @click="handleCancel(row)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="filteredOrders.length"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>

    <el-dialog v-model="detailDialogVisible" title="订单详情" width="620px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号" :span="2">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentOrder.userName }}</el-descriptions-item>
        <el-descriptions-item label="课程名">{{ currentOrder.courseName }}</el-descriptions-item>
        <el-descriptions-item label="原价">
          ¥{{ typeof currentOrder.originalPrice === 'number' ? currentOrder.originalPrice.toFixed(2) : '0.00' }}
        </el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span style="color: #e6a23c; font-weight: 700">
            ¥{{ typeof currentOrder.payAmount === 'number' ? currentOrder.payAmount.toFixed(2) : '0.00' }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusTagType(currentOrder.status)">
            {{ statusText(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <template v-if="currentOrder.payType === 1">
            <el-tag type="success">微信</el-tag>
          </template>
          <template v-else-if="currentOrder.payType === 2">
            <el-tag type="primary">支付宝</el-tag>
          </template>
          <template v-else>
            <span style="color: #909399">未支付</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.payTime" label="支付时间" :span="2">
          {{ currentOrder.payTime }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="createDialogVisible"
      title="创建订单"
      width="450px"
      destroy-on-close
      @close="resetCreateForm"
    >
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="createForm.userId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="课程ID" prop="courseId">
          <el-input-number v-model="createForm.courseId" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="payDialogVisible" title="订单支付" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="订单号">
          <el-input :model-value="payOrderNo" disabled />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="payType">
            <el-radio :value="1">微信支付</el-radio>
            <el-radio :value="2">支付宝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="payLoading" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getList, getByOrderNo, createOrder, cancelOrder, payOrder } from '../api/order.js'

const searchForm = reactive({
  orderNo: '',
  userName: '',
  status: ''
})

const orderList = ref([])
const loading = ref(false)

const filteredOrders = computed(() => {
  return orderList.value.filter(item => {
    if (searchForm.orderNo && !item.orderNo.includes(searchForm.orderNo)) return false
    if (searchForm.userName && !(item.userName && item.userName.includes(searchForm.userName))) return false
    if (searchForm.status !== '' && searchForm.status !== null && searchForm.status !== undefined) {
      if (item.status !== searchForm.status) return false
    }
    return true
  })
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

const pagedOrders = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredOrders.value.slice(start, end)
})

const statusTagType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = { 0: '待支付', 1: '已支付', 2: '已完成', 3: '已取消' }
  return map[status] || '未知'
}

const handleSearch = () => {
  pagination.currentPage = 1
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.userName = ''
  searchForm.status = ''
  pagination.currentPage = 1
}

const detailDialogVisible = ref(false)
const currentOrder = ref({})

const handleViewDetail = async (row) => {
  try {
    const res = await getByOrderNo(row.orderNo)
    if (res.code === 200) {
      currentOrder.value = res.data || row
    } else {
      currentOrder.value = row
    }
  } catch (e) {
    currentOrder.value = row
  }
  detailDialogVisible.value = true
}

const createDialogVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref(null)
const createForm = ref({ userId: 1, courseId: 1 })
const createRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  courseId: [{ required: true, message: '请输入课程ID', trigger: 'blur' }]
}

const handleCreate = () => {
  createForm.value = { userId: 1, courseId: 1 }
  createDialogVisible.value = true
}

const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate()
  createLoading.value = true
  try {
    const res = await createOrder(createForm.value.userId, createForm.value.courseId)
    if (res.code === 200) {
      ElMessage.success('创建订单成功')
      createDialogVisible.value = false
      fetchOrderList()
    } else {
      ElMessage.error(res.message || '创建订单失败')
    }
  } catch (e) {
    ElMessage.error('创建订单失败')
  } finally {
    createLoading.value = false
  }
}

const resetCreateForm = () => {
  if (createFormRef.value) createFormRef.value.resetFields()
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要取消订单 "${row.orderNo}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await cancelOrder(row.orderNo)
    if (res.code === 200) {
      ElMessage.success('取消订单成功')
      fetchOrderList()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

const payDialogVisible = ref(false)
const payLoading = ref(false)
const payOrderNo = ref('')
const payType = ref(1)

const handlePay = (row) => {
  payOrderNo.value = row.orderNo
  payType.value = 1
  payDialogVisible.value = true
}

const confirmPay = async () => {
  payLoading.value = true
  try {
    const res = await payOrder(payOrderNo.value, payType.value)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      fetchOrderList()
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch (e) {
    ElMessage.error('支付失败')
  } finally {
    payLoading.value = false
  }
}

const fetchOrderList = async () => {
  loading.value = true
  try {
    const res = await getList()
    if (res.code === 200) {
      orderList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取订单列表失败')
    }
  } catch (e) {
    ElMessage.error('获取订单列表失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrderList()
})
</script>

<style scoped>
.order-list-container {
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

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
