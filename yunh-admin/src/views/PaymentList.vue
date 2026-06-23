<template>
  <div class="payment-container">
    <div class="page-header">
      <h2>支付记录</h2>
    </div>

    <el-table :data="paymentList" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="paymentNo" label="支付单号" width="180" />
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="userId" label="用户ID" width="100" align="center" />
      <el-table-column label="金额" width="120" align="center">
        <template #default="{ row }">
          <span class="amount-text">{{ formatAmount(row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.payType === 1 ? 'success' : 'primary'" size="small">
            {{ row.payType === 1 ? '微信' : '支付宝' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payTime" label="支付时间" width="180" align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleViewRefund(row)">
            查看退款
          </el-button>
          <el-button
            v-if="row.status === 1"
            type="warning"
            link
            size="small"
            @click="handleRefund(row)"
          >
            退款
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="refundDialogVisible" title="退款记录" width="500px">
      <div v-loading="refundLoading">
        <template v-if="refundInfo">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="订单号">{{ refundInfo.orderNo || currentRow.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="退款金额">{{ formatAmount(refundInfo.refundAmount) }}</el-descriptions-item>
            <el-descriptions-item label="退款状态">
              <el-tag :type="refundInfo.status === 1 ? 'success' : refundInfo.status === 0 ? 'warning' : 'danger'" size="small">
                {{ refundInfo.status === 1 ? '退款成功' : refundInfo.status === 0 ? '退款中' : '退款失败' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="退款时间">{{ refundInfo.refundTime || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="退款原因">{{ refundInfo.reason || '暂无' }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <template v-else>
          <el-empty description="暂无退款记录" />
        </template>
      </div>
    </el-dialog>

    <el-dialog v-model="refundFormVisible" title="申请退款" width="450px" destroy-on-close>
      <el-form :model="refundForm" label-width="80px">
        <el-form-item label="订单号">
          <el-input :model-value="refundForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input
            v-model="refundForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入退款原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="refundLoading" @click="submitRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getList, getRefund, refund } from '../api/pay.js'

const paymentList = ref([])
const loading = ref(false)
const refundDialogVisible = ref(false)
const refundFormVisible = ref(false)
const refundLoading = ref(false)
const refundInfo = ref(null)
const currentRow = ref(null)
const refundForm = ref({ orderNo: '', reason: '' })

const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '¥0.00'
  return '¥' + Number(amount).toFixed(2)
}

const statusTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = { 0: '待支付', 1: '已支付', 2: '失败' }
  return map[status] || '未知'
}

const fetchPaymentList = async () => {
  loading.value = true
  try {
    const res = await getList()
    if (res.code === 200) {
      paymentList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取支付记录失败')
    }
  } catch (e) {
    ElMessage.error('获取支付记录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const handleViewRefund = async (row) => {
  currentRow.value = row
  refundInfo.value = null
  refundDialogVisible.value = true
  refundLoading.value = true
  try {
    const res = await getRefund(row.orderNo)
    if (res.code === 200) {
      refundInfo.value = res.data || null
    } else {
      ElMessage.error(res.message || '获取退款记录失败')
    }
  } catch (e) {
    ElMessage.error('获取退款记录失败，请检查网络连接')
  } finally {
    refundLoading.value = false
  }
}

const handleRefund = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要对订单 "${row.orderNo}" 申请退款吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    refundForm.value = { orderNo: row.orderNo, reason: '' }
    refundFormVisible.value = true
  } catch (e) {
    // 用户取消
  }
}

const submitRefund = async () => {
  if (!refundForm.value.reason.trim()) {
    ElMessage.warning('请输入退款原因')
    return
  }
  refundLoading.value = true
  try {
    const res = await refund(refundForm.value.orderNo, refundForm.value.reason)
    if (res.code === 200) {
      ElMessage.success('退款申请已提交')
      refundFormVisible.value = false
      fetchPaymentList()
    } else {
      ElMessage.error(res.message || '退款申请失败')
    }
  } catch (e) {
    ElMessage.error('退款申请失败')
  } finally {
    refundLoading.value = false
  }
}

onMounted(() => {
  fetchPaymentList()
})
</script>

<style scoped>
.payment-container {
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

.amount-text {
  color: #e6a23c;
  font-weight: 600;
}
</style>
