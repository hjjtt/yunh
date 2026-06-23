<template>
  <div class="dashboard-container">
    <!-- 顶部统计卡片区域 -->
    <el-row :gutter="20" class="stat-cards">
      <!-- 用户总数卡片 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">用户总数</p>
              <p class="stat-value">{{ statData.userCount }}</p>
            </div>
            <div class="stat-icon user-icon">
              <el-icon :size="36"><User /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <!-- 课程总数卡片 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">课程总数</p>
              <p class="stat-value">{{ statData.courseCount }}</p>
            </div>
            <div class="stat-icon course-icon">
              <el-icon :size="36"><Reading /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <!-- 订单总数卡片 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">订单总数</p>
              <p class="stat-value">{{ statData.orderCount }}</p>
            </div>
            <div class="stat-icon order-icon">
              <el-icon :size="36"><List /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <!-- 总收入卡片 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">总收入</p>
              <p class="stat-value">&yen;{{ statData.totalIncome.toFixed(2) }}</p>
            </div>
            <div class="stat-icon income-icon">
              <el-icon :size="36"><Wallet /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 中间区域：饼图 + 最近订单 -->
    <el-row :gutter="20" class="middle-section">
      <!-- 左侧：订单状态分布饼图 -->
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">订单状态分布</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <!-- 右侧：最近订单列表 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">最近订单</span>
          </template>
          <el-table :data="recentOrders" stripe style="width: 100%" max-height="360">
            <el-table-column prop="orderNo" label="订单号" min-width="160" show-overflow-tooltip />
            <el-table-column prop="username" label="用户名" min-width="100" />
            <el-table-column prop="courseName" label="课程名" min-width="140" show-overflow-tooltip />
            <el-table-column prop="payAmount" label="金额" min-width="90">
              <template #default="{ row }">
                <span>&yen;{{ row.payAmount?.toFixed(2) ?? '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" min-width="90">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" min-width="160">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部：平台统计趋势折线图 -->
    <el-row :gutter="20" class="bottom-section">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">平台统计趋势（最近7天）</span>
            </div>
          </template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { User, Reading, List, Wallet } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getList as getUserList } from '../api/user.js'
import { getList as getCourseList } from '../api/course.js'
import { getList as getOrderList } from '../api/order.js'
import { getPlatformStats } from '../api/statistics.js'

// ==================== 响应式数据 ====================

// 统计数据
const statData = reactive({
  userCount: 0,
  courseCount: 0,
  orderCount: 0,
  totalIncome: 0
})

// 最近订单列表
const recentOrders = ref([])

// 全部订单列表（用于饼图统计）
const allOrders = ref([])

// 图表 DOM 引用
const pieChartRef = ref(null)
const lineChartRef = ref(null)

// 图表实例
let pieChart = null
let lineChart = null

// ==================== 工具函数 ====================

/**
 * 格式化时间字符串
 * @param {string} time - 时间字符串
 * @returns {string} 格式化后的时间
 */
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

/**
 * 获取订单状态标签文本
 * @param {number} status - 订单状态码
 * @returns {string} 状态文本
 */
const statusLabel = (status) => {
  const map = { 0: '待支付', 1: '已支付', 2: '已完成', 3: '已取消' }
  return map[status] ?? '未知'
}

/**
 * 获取订单状态标签类型
 * @param {number} status - 订单状态码
 * @returns {string} Element Plus Tag 类型
 */
const statusTagType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return map[status] ?? 'info'
}

/**
 * 获取指定日期前 N 天的日期字符串
 * @param {number} days - 往前推的天数
 * @returns {string} 日期字符串 YYYY-MM-DD
 */
const getDateString = (days) => {
  const date = new Date()
  date.setDate(date.getDate() - days)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// ==================== 数据获取 ====================

/**
 * 获取用户总数
 */
const fetchUserCount = async () => {
  try {
    const res = await getUserList()
    if (res.code === 200) {
      statData.userCount = Array.isArray(res.data) ? res.data.length : 0
    }
  } catch (e) {
    console.error('获取用户列表失败：', e)
  }
}

/**
 * 获取课程总数
 */
const fetchCourseCount = async () => {
  try {
    const res = await getCourseList()
    if (res.code === 200) {
      statData.courseCount = Array.isArray(res.data) ? res.data.length : 0
    }
  } catch (e) {
    console.error('获取课程列表失败：', e)
  }
}

/**
 * 获取订单相关数据（订单总数、总收入、饼图、最近订单）
 */
const fetchOrderData = async () => {
  try {
    const res = await getOrderList()
    if (res.code === 200) {
      const list = Array.isArray(res.data) ? res.data : []
      allOrders.value = list
      // 订单总数
      statData.orderCount = list.length
      // 累加所有已支付订单的 payAmount（状态为 1-已支付 或 2-已完成）
      statData.totalIncome = list
        .filter((o) => o.status === 1 || o.status === 2)
        .reduce((sum, o) => sum + (Number(o.payAmount) || 0), 0)
      // 最近订单：按创建时间倒序取前 10 条
      recentOrders.value = [...list]
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
        .slice(0, 10)
      // 初始化饼图
      initPieChart()
    }
  } catch (e) {
    console.error('获取订单列表失败：', e)
  }
}

/**
 * 获取平台统计数据并初始化折线图
 */
const fetchPlatformStats = async () => {
  try {
    const startDate = getDateString(6) // 7 天前
    const endDate = getDateString(0) // 今天
    const res = await getPlatformStats(startDate, endDate)
    if (res.code === 200) {
      initLineChart(res.data)
    }
  } catch (e) {
    console.error('获取平台统计失败：', e)
  }
}

// ==================== 图表初始化 ====================

/**
 * 初始化订单状态分布饼图
 */
const initPieChart = () => {
  if (!pieChartRef.value) return
  // 统计各状态订单数量
  const statusMap = { 0: '待支付', 1: '已支付', 2: '已完成', 3: '已取消' }
  const countMap = { 0: 0, 1: 0, 2: 0, 3: 0 }
  allOrders.value.forEach((o) => {
    if (countMap[o.status] !== undefined) {
      countMap[o.status]++
    }
  })
  const pieData = Object.keys(countMap).map((key) => ({
    name: statusMap[key],
    value: countMap[key]
  }))

  // 创建或复用图表实例
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'horizontal', bottom: 0 },
    color: ['#E6A23C', '#409EFF', '#67C23A', '#909399'],
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}单' },
        emphasis: {
          label: { show: true, fontSize: 16, fontWeight: 'bold' }
        },
        data: pieData
      }
    ]
  })
}

/**
 * 初始化平台统计趋势折线图
 * @param {Array|Object} data - 平台统计数据
 */
const initLineChart = (data) => {
  if (!lineChartRef.value) return
  // 生成最近 7 天日期标签
  const dates = []
  for (let i = 6; i >= 0; i--) {
    dates.push(getDateString(i))
  }

  // 解析后端数据，兼容数组和对象格式
  let userTrend = []
  let orderTrend = []
  if (Array.isArray(data)) {
    // 如果后端返回数组，按日期匹配
    const dateDataMap = {}
    data.forEach((item) => {
      if (item.date) dateDataMap[item.date] = item
    })
    dates.forEach((d) => {
      const item = dateDataMap[d] || {}
      userTrend.push(item.newUserCount ?? item.userCount ?? 0)
      orderTrend.push(item.orderCount ?? 0)
    })
  } else if (data && typeof data === 'object') {
    // 如果后端返回对象格式，直接取数组
    userTrend = data.newUserList || data.userList || dates.map(() => 0)
    orderTrend = data.orderList || dates.map(() => 0)
  } else {
    userTrend = dates.map(() => 0)
    orderTrend = dates.map(() => 0)
  }

  // 简化日期标签显示（只显示 MM-DD）
  const labels = dates.map((d) => d.substring(5))

  if (!lineChart) {
    lineChart = echarts.init(lineChartRef.value)
  }
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '新增订单'], top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.3)' },
            { offset: 1, color: 'rgba(64,158,255,0.02)' }
          ])
        },
        data: userTrend
      },
      {
        name: '新增订单',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#E6A23C' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230,162,60,0.3)' },
            { offset: 1, color: 'rgba(230,162,60,0.02)' }
          ])
        },
        data: orderTrend
      }
    ]
  })
}

// ==================== 窗口自适应 ====================

/**
 * 监听窗口大小变化，自动调整图表尺寸
 */
const handleResize = () => {
  pieChart?.resize()
  lineChart?.resize()
}

// ==================== 生命周期 ====================

onMounted(async () => {
  // 并行请求所有数据
  await Promise.all([fetchUserCount(), fetchCourseCount(), fetchOrderData(), fetchPlatformStats()])
  // 监听窗口变化
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  // 销毁图表实例，防止内存泄漏
  pieChart?.dispose()
  lineChart?.dispose()
  pieChart = null
  lineChart = null
  // 移除窗口监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100%;
}

/* 统计卡片 */
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin: 0 0 8px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

/* 不同颜色的图标背景 */
.user-icon {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.course-icon {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.order-icon {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.income-icon {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

/* 中间区域 */
.middle-section {
  margin-bottom: 20px;
}

/* 底部区域 */
.bottom-section {
  margin-bottom: 20px;
}

/* 图表容器 */
.chart-container {
  width: 100%;
  height: 360px;
}

/* 卡片标题 */
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 卡片头部行 */
.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
