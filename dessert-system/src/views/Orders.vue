<template>
  <div class="container">
    <h2>📦 我的订单</h2>

    <!-- 只保留状态筛选，去掉刷新按钮 -->
    <div class="tabs">
      <el-button
        v-for="item in statusList"
        :key="item.value"
        :type="currentStatus === item.value ? 'primary' : 'default'"
        @click="switchStatus(item.value)"
      >
        {{ item.label }}
      </el-button>
    </div>

    <!-- 订单列表 -->
    <div v-if="filteredOrders.length > 0" class="order-list">
      <el-card v-for="order in filteredOrders" :key="order.id" class="order-card">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">下单时间：{{ formatTime(order.createTime) }}</span>
          </div>
          <span class="order-status" :style="{ color: getStatusColor(order.status) }">
            {{ getStatusText(order.status) }}
          </span>
        </div>

        <!-- 收货信息 -->
        <div class="order-address">
          <div class="address-title">收货信息</div>
          <div class="address-detail">
            <span>收货人：{{ order.receiverName || '未填写' }}</span>
            <span>手机号：{{ order.receiverPhone || '未填写' }}</span>
            <span>收货地址：{{ order.address || '未填写' }}</span>
            <span v-if="order.remark">备注：{{ order.remark }}</span>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <div class="item-info">
              <div class="item-name">{{ item.dessertName }}</div>
              <div class="item-price">¥{{ item.price }} × {{ item.quantity }}</div>
            </div>
            <div class="item-actions">
              <div class="item-subtotal">¥{{ ((item.price || 0) * (item.quantity || 0)).toFixed(2) }}</div>
              <!-- 评价按钮：已完成且未评价的订单显示 -->
              <el-button
                v-if="order.status === 4 && !isReviewed(order.id, item.dessertId)"
                type="warning"
                size="small"
                @click="openReview(order.id, item.dessertId, item.dessertName)"
              >
                评价
              </el-button>
              <el-tag v-else-if="order.status === 4 && isReviewed(order.id, item.dessertId)" type="success" size="small">
                已评价
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 订单底部 -->
        <div class="order-footer">
          <div class="order-total">
            共 {{ order.items?.length || 0 }} 件商品，实付：¥{{ (order.actualAmount || order.totalAmount || 0).toFixed(2) }}
          </div>
          <div class="order-buttons">
            <!-- 已付款订单，等待发货 -->
            <el-tag v-if="order.status === 2" type="primary" size="small">等待发货</el-tag>

            <!-- 配送中订单，用户可确认收货 -->
            <el-button v-if="order.status === 3" type="success" size="small" @click="confirmOrder(order.id)">确认收货</el-button>

            <!-- 已完成订单 -->
            <el-tag v-if="order.status === 4" type="success" size="small">已完成</el-tag>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty v-else description="暂无订单" />

    <!-- 评价弹窗 -->
    <el-dialog v-model="reviewDialogVisible" title="评价商品" width="500px">
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewFormRef" label-width="80px">
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            rows="4"
            placeholder="请分享您的使用感受..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="reviewLoading">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'
import { reviewApi } from '@/api/review'
import { message } from '@/utils/message'

interface OrderItem {
  id: number
  dessertId: number
  dessertName: string
  price: number
  quantity: number
}

interface Order {
  id: number
  orderNo: string
  totalAmount: number
  actualAmount: number
  status: number
  address: string
  receiverName: string
  receiverPhone: string
  remark: string
  createTime: string
  items: OrderItem[]
}

const orders = ref<Order[]>([])
const currentStatus = ref<number | null>(null)
const loading = ref(false)

const user = JSON.parse(localStorage.getItem('user') || '{}')
const isAdmin = computed(() => user.role === 1)

const reviewDialogVisible = ref(false)
const reviewLoading = ref(false)
const reviewFormRef = ref()
const currentReview = ref({ orderId: 0, dessertId: 0, dessertName: '' })
const reviewedMap = ref<Map<string, boolean>>(new Map())

const reviewForm = ref({
  content: ''
})

const reviewRules = {
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}

// 普通用户：去掉待付款、已取消，只显示已付款、配送中、已完成
const statusList = [
  { label: '全部', value: null },
  { label: '已付款', value: 2 },
  { label: '配送中', value: 3 },
  { label: '已完成', value: 4 }
]

const filteredOrders = computed(() => {
  if (currentStatus.value === null) return orders.value
  return orders.value.filter(order => order.status === currentStatus.value)
})

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

const getStatusColor = (status: number) => {
  const map: Record<number, string> = {
    1: '#f56c6c',
    2: '#409eff',
    3: '#e6a23c',
    4: '#67c23a',
    5: '#909399'
  }
  return map[status] || '#909399'
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ').replace(/\.\d+.*$/, '')
}

const checkReviewedStatus = async () => {
  for (const order of orders.value) {
    if (order.status === 4 && order.items) {
      for (const item of order.items) {
        try {
          const res: any = await reviewApi.checkReviewed(order.id, item.dessertId)
          if (res.code === 200 && res.data) {
            reviewedMap.value.set(`${order.id}-${item.dessertId}`, true)
          }
        } catch (error) {
          console.error('检查评价状态失败', error)
        }
      }
    }
  }
}

const isReviewed = (orderId: number, dessertId: number) => {
  return reviewedMap.value.get(`${orderId}-${dessertId}`) || false
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res: any = await orderApi.getList()
    if (res.code === 200) {
      orders.value = res.data || res.rows || res.list || []
      await checkReviewedStatus()
    } else {
      message.error(res.message || '获取订单失败')
    }
  } catch (error) {
    console.error('获取订单失败:', error)
    message.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

const switchStatus = (status: number | null) => {
  currentStatus.value = status
}

const confirmOrder = async (orderId: number) => {
  ElMessageBox.confirm('确认已收到商品吗？', '提示', { type: 'info' }).then(async () => {
    try {
      const res: any = await orderApi.confirm(orderId)
      if (res.code === 200) {
        message.success('确认收货成功')
        await fetchOrders()
      } else {
        message.error(res.message || '确认收货失败')
      }
    } catch {
      message.error('确认收货失败')
    }
  }).catch(() => {})
}

const openReview = (orderId: number, dessertId: number, dessertName: string) => {
  currentReview.value = { orderId, dessertId, dessertName }
  reviewForm.value = { content: '' }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  try {
    await reviewFormRef.value?.validate()
    reviewLoading.value = true

    const res: any = await reviewApi.addReview({
      orderId: currentReview.value.orderId,
      dessertId: currentReview.value.dessertId,
      rating: 5,
      content: reviewForm.value.content
    })

    if (res.code === 200) {
      message.success('评价成功，感谢您的分享！')
      reviewDialogVisible.value = false
      reviewedMap.value.set(`${currentReview.value.orderId}-${currentReview.value.dessertId}`, true)
      await fetchOrders()
    } else {
      message.error(res.message || '评价失败')
    }
  } catch (error) {
    message.error('评价失败')
  } finally {
    reviewLoading.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  border-radius: 12px;
  overflow: hidden;
}

.order-header {
  padding: 15px 20px;
  background: #f8f9fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-no {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.order-time {
  color: #999;
  font-size: 12px;
}

.order-status {
  font-weight: 500;
  font-size: 16px;
}

.order-address {
  padding: 15px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #eee;
}

.address-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.address-detail {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 13px;
  color: #666;
}

.address-detail span {
  display: inline-flex;
  align-items: center;
}

.order-items {
  padding: 15px 20px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.item-price {
  font-size: 12px;
  color: #999;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item-subtotal {
  font-weight: 500;
  color: #ff6b6b;
}

.order-footer {
  padding: 15px 20px;
  background: #fafafa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #eee;
}

.order-total {
  font-size: 14px;
  color: #666;
}

.order-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
