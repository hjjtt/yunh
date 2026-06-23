<template>
  <div class="statistics-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>数据统计</h2>
    </div>

    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 360px"
      />
      <el-button type="primary" @click="fetchPlatformStats" style="margin-left: 12px">
        查询统计
      </el-button>
      <el-button type="success" @click="handleGenerateDaily">
        生成每日统计
      </el-button>
    </div>

    <!-- 平台统计概览卡片 -->
    <el-row :gutter="20" class="stats-cards" v-loading="statsLoading">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ statsData.newUserCount || 0 }}</div>
          <div class="stat-label">新增用户数</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ statsData.activeUserCount || 0 }}</div>
          <div class="stat-label">活跃用户数</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ statsData.newOrderCount || 0 }}</div>
          <div class="stat-label">新增订单数</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value amount-highlight">{{ formatIncome(statsData.totalIncome) }}</div>
          <div class="stat-label">总收入</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ statsData.newCourseCount || 0 }}</div>
          <div class="stat-label">新增课程数</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ statsData.totalVideoViews || 0 }}</div>
          <div class="stat-label">视频播放总数</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 平台趋势图表 -->
    <el-card shadow="hover" class="chart-card">
      <template #header>
        <span class="chart-title">平台趋势</span>
      </template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getPlatformStats, generateDaily } from '../api/statistics.js'

// 日期范围，默认最近7天
const dateRange = ref([])
// 统计数据
const statsData = reactive({
  newUserCount: 0,
  activeUserCount: 0,
  newOrderCount: 0,
  totalIncome: 0,
  newCourseCount: 0,
  totalVideoViews: 0
})
// 加载状态
const statsLoading = ref(false)
// 图表DOM引用
const chartRef = ref(null)
// ECharts实例
let chartInstance = null

/**
 * 初始化默认日期范围为最近7天
 */
const initDateRange = () => {
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 6 * 24 * 3600 * 1000)
  const formatDate = (date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
  dateRange.value = [formatDate(start), formatDate(end)]
}

/**
 * 格式化收入为人民币格式
 * @param {number} amount 金额
 * @returns {string} 格式化后的金额字符串
 */
const formatIncome = (amount) => {
  if (amount === null || amount === undefined) return '¥0.00'
  return '¥' + Number(amount).toFixed(2)
}

/**
 * 获取平台统计数据
 */
const fetchPlatformStats = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  statsLoading.value = true
  try {
    const res = await getPlatformStats(dateRange.value[0], dateRange.value[1])
    if (res.code === 200) {
      const data = res.data || {}
      // 更新统计数据
      statsData.newUserCount = data.newUserCount || 0
      statsData.activeUserCount = data.activeUserCount || 0
      statsData.newOrderCount = data.newOrderCount || 0
      statsData.totalIncome = data.totalIncome || 0
      statsData.newCourseCount = data.newCourseCount || 0
      statsData.totalVideoViews = data.totalVideoViews || 0
      // 更新图表
      updateChart(data)
    } else {
      ElMessage.error(res.message || '获取统计数据失败')
    }
  } catch (e) {
    ElMessage.error('获取统计数据失败，请检查网络连接')
  } finally {
    statsLoading.value = false
  }
}

/**
 * 生成每日统计报表
 */
const handleGenerateDaily = async () => {
  try {
    const res = await generateDaily()
    if (res.code === 200) {
      ElMessage.success('每日统计生成成功')
      // 生成后重新查询数据
      fetchPlatformStats()
    } else {
      ElMessage.error(res.message || '生成每日统计失败')
    }
  } catch (e) {
    ElMessage.error('生成每日统计失败，请检查网络连接')
  }
}

/**
 * 初始化 ECharts 图表实例
 */
const initChart = () => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  // 窗口大小变化时自适应
  window.addEventListener('resize', handleResize)
}

/**
 * 更新图表数据（双Y轴折线图：新增用户 + 新增订单趋势）
 * @param {Object} data 统计数据，包含 trendDaily 列表
 */
const updateChart = (data) => {
  if (!chartInstance) return
  const trendList = data.trendDaily || []
  // 提取日期、新增用户数、新增订单数
  const dates = trendList.map(item => item.date)
  const newUsers = trendList.map(item => item.newUserCount || 0)
  const newOrders = trendList.map(item => item.newOrderCount || 0)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['新增用户', '新增订单'],
      top: 10,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: {
        fontSize: 13
      }
    },
    grid: {
      left: '5%',
      right: '8%',
      top: '15%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        rotate: 45,
        interval: 'auto',
        fontSize: 12,
        margin: 10
      },
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '新增用户',
        position: 'left',
        nameTextStyle: {
          fontSize: 13,
          padding: [0, 0, 0, 0]
        },
        axisLabel: {
          fontSize: 12,
          margin: 8
        },
        splitLine: {
          lineStyle: {
            color: '#f2f6fc',
            type: 'dashed'
          }
        }
      },
      {
        type: 'value',
        name: '新增订单',
        position: 'right',
        nameTextStyle: {
          fontSize: 13,
          padding: [0, 0, 0, 0]
        },
        axisLabel: {
          fontSize: 12,
          margin: 8
        },
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      {
        name: '新增用户',
        type: 'line',
        yAxisIndex: 0,
        data: newUsers,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#409EFF' },
        lineStyle: {
          width: 3,
          shadowColor: 'rgba(64,158,255,0.3)',
          shadowBlur: 10,
          shadowOffsetY: 5
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.3)' },
            { offset: 1, color: 'rgba(64,158,255,0.05)' }
          ])
        }
      },
      {
        name: '新增订单',
        type: 'line',
        yAxisIndex: 1,
        data: newOrders,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#67C23A' },
        lineStyle: {
          width: 3,
          shadowColor: 'rgba(103,194,58,0.3)',
          shadowBlur: 10,
          shadowOffsetY: 5
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103,194,58,0.3)' },
            { offset: 1, color: 'rgba(103,194,58,0.05)' }
          ])
        }
      }
    ]
  }
  chartInstance.setOption(option, true)
}

/**
 * 窗口大小变化处理
 */
const handleResize = () => {
  chartInstance && chartInstance.resize()
}

// 页面加载时自动初始化
onMounted(async () => {
  initDateRange()
  await nextTick()
  initChart()
  fetchPlatformStats()
})

// 组件卸载时清理
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.statistics-container {
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

/* 统计概览卡片样式 */
.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 10px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.amount-highlight {
  color: #e6a23c;
}

/* 图表样式 */
.chart-card {
  margin-top: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 550px;
}
</style>
