<template>
  <div class="order-manage">
    <h2>📋 订单管理</h2>

    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="收货人">
          <el-input v-model="searchForm.receiverName" placeholder="请输入收货人" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="已付款" :value="2" />
            <el-option label="配送中" :value="3" />
            <el-option label="已完成" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchOrders">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表（分页展示） -->
    <el-table :data="paginatedOrderList" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="receiverPhone" label="手机号" width="120" />
      <el-table-column prop="address" label="收货地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="totalAmount" label="总金额" width="100">
        <template #default="{ row }">¥{{ (row.totalAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 2" type="primary" link @click="editAddress(row)">
            修改地址
          </el-button>
          <el-button v-if="row.status === 2" type="success" link @click="deliverOrder(row)">
            发货
          </el-button>
          <el-button type="info" link @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="filteredOrderList.length"
        :page-size="pageSize" :current-page="currentPage" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" :page-sizes="[10, 20, 50, 100]" />
    </div>

    <!-- 修改地址弹窗 -->
    <el-dialog v-model="addressDialogVisible" title="修改收货地址" width="500px">
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="80px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="addressForm.address" type="textarea" rows="3" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="addressForm.remark" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddress" :loading="submitLoading">确定修改</el-button>
      </template>
    </el-dialog>

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
            <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥{{ ((row.price || 0) * (row.quantity || 0)).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
        <div class="order-total">实付金额：¥{{ ((currentOrder.actualAmount || currentOrder.totalAmount) || 0).toFixed(2) }}
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'
import { message } from '@/utils/message'

const loading = ref(false)
const submitLoading = ref(false)
const orderList = ref<any[]>([])
const addressDialogVisible = ref(false)
const detailVisible = ref(false)
const addressFormRef = ref()
const currentOrder = ref<any>(null)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = ref({
  orderNo: '',
  receiverName: '',
  status: null as number | null
})

const addressForm = ref({
  orderId: null as number | null,
  receiverName: '',
  receiverPhone: '',
  address: '',
  remark: ''
})

const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.replace('T', ' ').replace(/\.\d+.*$/, '')
}

// 筛选后的订单列表（全量，用于分页）
const filteredOrderList = computed(() => {
  let list = [...orderList.value]

  // 过滤掉没有收货人名字的订单
  list = list.filter(o => o.receiverName && o.receiverName.trim() !== '')
  // 过滤掉待付款(1)和已取消(5)的订单
  list = list.filter(o => o.status !== 1 && o.status !== 5)

  if (searchForm.value.orderNo) {
    list = list.filter(o => o.orderNo.includes(searchForm.value.orderNo))
  }
  if (searchForm.value.receiverName) {
    list = list.filter(o => (o.receiverName || '').includes(searchForm.value.receiverName))
  }
  if (searchForm.value.status !== null) {
    list = list.filter(o => o.status === searchForm.value.status)
  }
  return list
})

// 当前页显示的订单（分页切片）
const paginatedOrderList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrderList.value.slice(start, end)
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

const fetchOrders = async () => {
  loading.value = true
  try {
    const res: any = await orderApi.adminGetAllOrders()
    if (res.code === 200) {
      orderList.value = res.data || []
      // 重置分页到第一页
      currentPage.value = 1
    }
  } catch (error) {
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

const searchOrders = () => {
  currentPage.value = 1  // 搜索后重置页码
}

const resetSearch = () => {
  searchForm.value = { orderNo: '', receiverName: '', status: null }
  currentPage.value = 1
}

// 分页回调
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

const editAddress = (order: any) => {
  if (order.status !== 2) {
    ElMessage.warning('只有已付款的订单才能修改地址')
    return
  }
  currentOrder.value = order
  addressForm.value = {
    orderId: order.id,
    receiverName: order.receiverName || '',
    receiverPhone: order.receiverPhone || '',
    address: order.address || '',
    remark: order.remark || ''
  }
  addressDialogVisible.value = true
}

const submitAddress = async () => {
  try {
    await addressFormRef.value?.validate()
    submitLoading.value = true

    const res: any = await orderApi.updateAddress(addressForm.value.orderId!, {
      receiverName: addressForm.value.receiverName,
      receiverPhone: addressForm.value.receiverPhone,
      address: addressForm.value.address,
      remark: addressForm.value.remark
    })

    if (res.code === 200) {
      ElMessage.success('地址修改成功')
      addressDialogVisible.value = false
      fetchOrders()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    submitLoading.value = false
  }
}

const deliverOrder = async (order: any) => {
  if (order.status !== 2) {
    ElMessage.warning('只有已付款的订单才能发货')
    return
  }
  ElMessageBox.confirm(`确认发货订单 ${order.orderNo} 吗？`, '发货确认', { type: 'info' }).then(async () => {
    try {
      const res: any = await orderApi.deliver(order.id)
      if (res.code === 200) {
        message.success('发货成功')
        fetchOrders()
      } else {
        message.error(res.message || '发货失败')
      }
    } catch {
      message.error('发货失败')
    }
  }).catch(() => { })
}

const viewDetail = (order: any) => {
  currentOrder.value = order
  detailVisible.value = true
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-manage {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

.filter-card {
  margin-bottom: 20px;
}

.order-total {
  text-align: right;
  font-size: 18px;
  font-weight: bold;
  margin-top: 20px;
  color: #ff6b6b;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
