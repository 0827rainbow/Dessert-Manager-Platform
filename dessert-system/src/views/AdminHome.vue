<template>
  <div class="admin-home">
    <h2>📊 数据概览</h2>

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

    <!-- 订单详情弹窗 -->
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { dashboardApi } from '@/api/dashboard'
import { orderApi } from '@/api/order'
import { message } from '@/utils/message'

const router = useRouter()
const loading = ref(false)
const detailVisible = ref(false)
const currentOrder = ref<any>(null)
const allOrders = ref<any[]>([])  // 存储所有订单

const stats = ref({
  dessertCount: 0,
  orderCount: 0,
  totalSales: 0,
  userCount: 0
})

const recentOrders = ref<any[]>([])

// 计算有效订单数量（与订单管理页面显示数量一致）
const validOrderCount = computed(() => {
  return allOrders.value.filter((order: any) => {
    // 过滤条件：有收货人姓名、状态不是待付款(1)和已取消(5)
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

// 格式化时间
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

const fetchRecentOrders = async () => {
  loading.value = true
  try {
    const res: any = await orderApi.adminGetAllOrders()
    if (res.code === 200) {
      const orders = res.data || []
      // 存储所有订单用于计算有效订单数量
      allOrders.value = orders

      // 过滤：有收货人姓名、状态不是待付款(1)和已取消(5)
      const validOrders = orders.filter((order: any) =>
        order.receiverName &&
        order.receiverName.trim() !== '' &&
        (order.status === 2 || order.status === 3 || order.status === 4)
      )
      recentOrders.value = validOrders.slice(0, 5)
    }
  } catch (error) {
    console.error('获取订单失败', error)
    message.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStatistics()
  fetchRecentOrders()
})
</script>

<style scoped>
.admin-home { max-width: 1400px; margin: 0 auto; padding: 0 20px; }
h2 { margin-bottom: 20px; color: #333; }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 30px; }
.stat-card { cursor: pointer; transition: all 0.3s; }
.stat-card:hover { transform: translateY(-5px); box-shadow: 0 6px 20px rgba(0,0,0,0.1); }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; padding: 20px; }
.stat-icon { font-size: 48px; margin-right: 20px; }
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: bold; color: #667eea; }
.stat-label { font-size: 14px; color: #999; margin-top: 5px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.recent-section { margin-top: 20px; }
.order-total { text-align: right; font-size: 18px; font-weight: bold; margin-top: 20px; color: #ff6b6b; }
</style>
