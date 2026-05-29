<template>
  <div class="admin-home">
    <h2>📊 数据概览</h2>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card" @click="goTo('dessert')">
        <div class="stat-icon">🍰</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.dessertCount || 0 }}</div>
          <div class="stat-label">甜点总数</div>
        </div>
      </el-card>

      <el-card class="stat-card" @click="goTo('order')">
        <div class="stat-icon">📦</div>
        <div class="stat-info">
          <div class="stat-value">{{ validOrderCount }}</div>
          <div class="stat-label">订单总数</div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-info">
          <div class="stat-value">¥{{ formatPrice(stats.totalSales) }}</div>
          <div class="stat-label">总销售额</div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.userCount || 0 }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域（新增） -->
    <div class="charts-container">
      <!-- 近7天订单趋势 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>📈 近7天订单趋势</span>
          </div>
        </template>
        <div ref="trendChartRef" class="chart" style="height: 300px;"></div>
      </el-card>

      <!-- 热门甜点排行 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>🔥 热门甜点排行</span>
          </div>
        </template>
        <div ref="rankChartRef" class="chart" style="height: 300px;"></div>
      </el-card>

      <!-- 分类销售占比 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>🥧 分类销售占比</span>
          </div>
        </template>
        <div ref="pieChartRef" class="chart" style="height: 300px;"></div>
      </el-card>
    </div>

    <!-- 最近订单区域（原有） -->
    <div class="recent-section">
      <el-card class="recent-orders">
        <template #header>
          <div class="card-header">
            <span>🕐 最近订单</span>
            <el-button type="primary" link @click="$router.push('/order-manage')">查看全部 →</el-button>
          </div>
        </template>
        <el-table :data="recentOrders" stripe v-loading="loading" style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="totalAmount" label="金额" width="100">
            <template #default="{ row }">¥{{ (row.totalAmount || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="下单时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 订单详情弹窗（原有） -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.receiverName || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentOrder.receiverPhone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.address || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ formatTime(currentOrder.createTime) }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>商品清单</el-divider>
        <el-table :data="currentOrder.items || []" border>
          <el-table-column prop="dessertName" label="商品名称" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥{{ ((row.price || 0) * (row.quantity || 0)).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
        <div class="order-total">实付金额：¥{{ ((currentOrder.actualAmount || currentOrder.totalAmount) || 0).toFixed(2) }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { dashboardApi } from '@/api/dashboard'
import { orderApi } from '@/api/order'
import { dessertApi } from '@/api/dessert'
import { CategoryService } from '@/api/CategoryService'
import request from '@/api/request'
import { message } from '@/utils/message'

const router = useRouter()
const loading = ref(false)
const detailVisible = ref(false)
const currentOrder = ref<any>(null)
const allOrders = ref<any[]>([])      // 存储所有订单（用于图表）
const allDesserts = ref<any[]>([])    // 存储所有甜点（用于排行）
const categoryMap = ref<Record<number, string>>({})  // catId -> 分类名称

const stats = ref({
  dessertCount: 0,
  orderCount: 0,
  totalSales: 0,
  userCount: 0
})

const recentOrders = ref<any[]>([])

// 图表 ref
const trendChartRef = ref<HTMLElement>()
const rankChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()

// ECharts 实例
let trendChart: echarts.ECharts | null = null
let rankChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// 计算有效订单数量（与订单管理页面显示一致）
const validOrderCount = computed(() => {
  return allOrders.value.filter((order: any) => {
    return order.receiverName &&
      order.receiverName.trim() !== '' &&
      order.status !== 1 &&
      order.status !== 5
  }).length
})

const formatPrice = (value: number) => {
  if (value === undefined || value === null) return '0.00'
  return value.toFixed(2)
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ').replace(/\.\d+.*$/, '')
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    1: '待付款',
    2: '已付款',
    3: '配送中',
    4: '已完成',
    5: '已取消'
  }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: Record<number, string> = {
    1: 'danger',
    2: 'primary',
    3: 'warning',
    4: 'success',
    5: 'info'
  }
  return map[status] || 'info'
}

const goTo = (target: string) => {
  if (target === 'dessert') router.push('/dessert')
  if (target === 'order') router.push('/order-manage')
}

const viewDetail = (order: any) => {
  currentOrder.value = order
  detailVisible.value = true
}

// ========== 图表数据计算函数 ==========

// 近7天订单趋势数据
const getTrendData = () => {
  // 生成最近7天日期（YYYY-MM-DD）
  const dates: string[] = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    dates.push(d.toISOString().slice(0, 10))
  }

  const countMap: Record<string, number> = {}
  dates.forEach(date => { countMap[date] = 0 })

  allOrders.value.forEach(order => {
    const createDate = order.createTime?.slice(0, 10)
    if (createDate && countMap[createDate] !== undefined) {
      countMap[createDate]++
    }
  })

  return {
    xAxis: dates.map(d => `${d.slice(5)}`), // 显示 "05-20"
    seriesData: dates.map(d => countMap[d])
  }
}

// 热门甜点排行（取销量前8）
const getRankData = () => {
  const sorted = [...allDesserts.value].sort((a, b) => (b.sales || 0) - (a.sales || 0)).slice(0, 8)
  return {
    names: sorted.map(d => d.name),
    sales: sorted.map(d => d.sales || 0)
  }
}

// 分类销售占比（基于已完成订单）
const getCategorySalesData = () => {
  // 筛选已完成订单
  const completedOrders = allOrders.value.filter(order => order.status === 4)
  // dessertId -> catId 映射
  const dessertCatMap: Record<number, number> = {}
  allDesserts.value.forEach(d => { dessertCatMap[d.id] = d.catId })

  const catSales: Record<number, number> = {}
  const catNameMap: Record<number, string> = {}

  completedOrders.forEach(order => {
    const items = order.items || []
    items.forEach((item: any) => {
      const dessertId = item.dessertId
      const catId = dessertCatMap[dessertId]
      if (catId && categoryMap.value[catId]) {
        const amount = (item.price || 0) * (item.quantity || 0)
        catSales[catId] = (catSales[catId] || 0) + amount
        catNameMap[catId] = categoryMap.value[catId]
      }
    })
  })

  return Object.entries(catSales).map(([catId, sales]) => ({
    name: catNameMap[Number(catId)] || `分类${catId}`,
    value: sales
  }))
}

// ========== 图表初始化函数 ==========
const initTrendChart = () => {
  if (!trendChartRef.value) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)

  const { xAxis, seriesData } = getTrendData()
  trendChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'category', data: xAxis, name: '日期' },
    yAxis: { type: 'value', name: '订单数' },
    series: [{
      data: seriesData,
      type: 'line',
      smooth: true,
      lineStyle: { color: '#667eea', width: 3 },
      areaStyle: { opacity: 0.2, color: '#667eea' },
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: { color: '#667eea' }
    }]
  })
}

const initRankChart = () => {
  if (!rankChartRef.value) return
  if (rankChart) rankChart.dispose()
  rankChart = echarts.init(rankChartRef.value)

  const { names, sales } = getRankData()
  // 横向柱状图需要反转数据，使最高的在顶部
  const reversedNames = [...names].reverse()
  const reversedSales = [...sales].reverse()
  rankChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { containLabel: true, left: '10%' },
    xAxis: { type: 'value', name: '销量' },
    yAxis: { type: 'category', data: reversedNames, name: '甜点名称', axisLabel: { rotate: 0 } },
    series: [{
      data: reversedSales,
      type: 'bar',
      barWidth: '60%',
      itemStyle: { borderRadius: [0, 4, 4, 0], color: '#ff6b6b' }
    }]
  })
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  if (pieChart) pieChart.dispose()
  pieChart = echarts.init(pieChartRef.value)

  const data = getCategorySalesData()
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: data.map(item => item.name)
    },
    series: [{
      name: '销售额占比',
      type: 'pie',
      radius: '55%',
      center: ['50%', '50%'],
      data: data,
      emphasis: { scale: true },
      label: { show: true, formatter: '{b}: {d}%' }
    }]
  })
}

// 刷新所有图表
const refreshCharts = () => {
  initTrendChart()
  initRankChart()
  initPieChart()
}

// 窗口 resize 响应
const handleResize = () => {
  trendChart?.resize()
  rankChart?.resize()
  pieChart?.resize()
}

// ========== 数据获取 ==========
const fetchStatistics = async () => {
  try {
    const res: any = await dashboardApi.getStatistics()
    if (res.code === 200 && res.data) {
      stats.value = {
        dessertCount: res.data.dessertCount || 0,
        orderCount: res.data.orderCount || 0,
        totalSales: res.data.totalSales || 0,
        userCount: res.data.userCount || 0
      }
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
    message.error('获取统计数据失败')
  }
}

const fetchOrdersAndDesserts = async () => {
  loading.value = true
  try {
    // 获取所有订单
    const orderRes: any = await orderApi.adminGetAllOrders()
    if (orderRes.code === 200) {
      allOrders.value = orderRes.data || []
      // 过滤最近订单（有效订单）
      const validOrders = allOrders.value.filter((order: any) =>
        order.receiverName &&
        order.receiverName.trim() !== '' &&
        (order.status === 2 || order.status === 3 || order.status === 4)
      )
      recentOrders.value = validOrders.slice(0, 5)
    }

    // 获取所有甜点
    const dessertRes: any = await dessertApi.getList({ pageNum: 1, pageSize: 1000 })
    allDesserts.value = dessertRes.rows || []

    // 获取分类映射（用于饼图）
    const catRes: any = await CategoryService.list({ pageNum: 1, pageSize: 100 })
    const categories = catRes.rows || []
    categoryMap.value = {}
    categories.forEach((cat: any) => {
      categoryMap.value[cat.id] = cat.name
    })

    // 数据加载完成后初始化图表
    refreshCharts()
  } catch (error) {
    console.error('获取订单或甜点数据失败', error)
    message.error('获取图表数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStatistics()
  fetchOrdersAndDesserts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  rankChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.admin-home {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}
h2 {
  margin-bottom: 20px;
  color: #333;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}
.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 20px;
}
.stat-icon {
  font-size: 48px;
  margin-right: 20px;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #667eea;
}
.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.recent-section {
  margin-top: 20px;
}
.order-total {
  text-align: right;
  font-size: 18px;
  font-weight: bold;
  margin-top: 20px;
  color: #ff6b6b;
}

/* 新增图表样式 */
.charts-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin: 30px 0;
}
.chart-card {
  border-radius: 16px;
  transition: all 0.3s;
}
.chart-card :deep(.el-card__header) {
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
  font-weight: 500;
}
.chart {
  width: 100%;
}

@media (max-width: 1200px) {
  .charts-container {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style>
