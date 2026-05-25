<template>
  <div class="container">
    <h2>🛒 购物车</h2>
    <el-card v-if="cartList.length" class="cart-card">
      <el-table :data="cartList" stripe>
        <!-- 新增：选中列 -->
        <el-table-column width="55">
          <template #header>
            <el-checkbox
              :model-value="isAllSelected"
              @change="toggleSelectAll"
            />
          </template>
          <template #default="{ row }">
            <el-checkbox
              :model-value="selectedItems.includes(row.id)"
              @change="toggleSelect(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="商品" width="300">
          <template #default="{ row }">
            <div class="product">
              <img :src="getImageUrl(row.dessertPhoto)" class="product-img" />
              <span>{{ row.dessertName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">¥{{ row.dessertPrice }}</template>
        </el-table-column>
        <el-table-column label="数量" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="99" size="small" @change="updateQty(row)" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">¥{{ (row.dessertPrice * row.quantity).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="danger" text @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="summary">
        <div class="total">
          已选 <span class="selected-count">{{ selectedCount }}</span> 件商品，共计：
          <span>¥{{ selectedTotal }}</span>
        </div>
        <el-button type="primary" size="large" :loading="checkoutLoading" @click="checkout">
          去结算 ({{ selectedCount }})
        </el-button>
      </div>
    </el-card>
    <el-empty v-else description="购物车空空如也">
      <el-button type="primary" @click="$router.push('/dessert-list')">去逛逛</el-button>
    </el-empty>

    <!-- 结算弹窗 -->
    <el-dialog v-model="addressDialogVisible" title="填写收货信息" width="500px">
      <el-form :model="checkoutForm" :rules="addressRules" ref="checkoutFormRef" label-width="100px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="checkoutForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="checkoutForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="checkoutForm.address" type="textarea" rows="2" placeholder="请输入详细收货地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="checkoutForm.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="checkoutLoading">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { cartApi } from '@/api/cart'
import { orderApi } from '@/api/order'
import { message } from '@/utils/message'

interface CartItem {
  id: number
  dessertId: number
  dessertName: string
  dessertPhoto: string
  dessertPrice: number
  quantity: number
}

const router = useRouter()
const baseUrl = 'http://localhost:8080'
const cartList = ref<CartItem[]>([])
const checkoutLoading = ref(false)
const addressDialogVisible = ref(false)
const checkoutFormRef = ref()

// 选中的商品ID列表
const selectedItems = ref<number[]>([])

const checkoutForm = ref({
  receiverName: '',
  receiverPhone: '',
  address: '',
  remark: ''
})

const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

// 获取选中的商品列表
const selectedCartItems = computed(() => {
  return cartList.value.filter(item => selectedItems.value.includes(item.id))
})

// 是否全选
const isAllSelected = computed(() => {
  return cartList.value.length > 0 && selectedItems.value.length === cartList.value.length
})

// 选中商品数量
const selectedCount = computed(() => {
  return selectedCartItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

// 选中商品总价
const selectedTotal = computed(() => {
  return selectedCartItems.value.reduce((sum, item) => sum + item.dessertPrice * item.quantity, 0).toFixed(2)
})

const getImageUrl = (url: string) => {
  if (!url) return 'https://picsum.photos/200/200?random=1'
  if (url.startsWith('http')) return url
  return baseUrl + url
}

const fetchCart = async () => {
  try {
    const res: any = await cartApi.get()
    cartList.value = res.data || res.rows || []
  } catch (error) {
    message.error('获取购物车失败')
  }
}

const updateQty = async (item: CartItem) => {
  try {
    await cartApi.updateQuantity(item.id, item.quantity)
    await fetchCart()
    window.dispatchEvent(new Event('cart-updated'))
  } catch {
    message.error('更新失败')
  }
}

const remove = async (id: number) => {
  try {
    await cartApi.remove(id)
    // 移除后清除该商品的选中状态
    selectedItems.value = selectedItems.value.filter(itemId => itemId !== id)
    await fetchCart()
    window.dispatchEvent(new Event('cart-updated'))
    message.success('删除成功')
  } catch {
    message.error('删除失败')
  }
}

// 切换单个商品选中状态
const toggleSelect = (row: CartItem) => {
  const index = selectedItems.value.indexOf(row.id)
  if (index === -1) {
    selectedItems.value.push(row.id)
  } else {
    selectedItems.value.splice(index, 1)
  }
}

// 全选/取消全选
const toggleSelectAll = (checked: boolean) => {
  if (checked) {
    selectedItems.value = cartList.value.map(item => item.id)
  } else {
    selectedItems.value = []
  }
}

// 去结算
const checkout = () => {
  if (selectedCartItems.value.length === 0) {
    message.warning('请先选择要结算的商品')
    return
  }
  addressDialogVisible.value = true
}

// 提交订单（只提交选中的商品）
const submitOrder = async () => {
  try {
    await checkoutFormRef.value?.validate()
    addressDialogVisible.value = false
    checkoutLoading.value = true

    // 获取选中的商品ID列表
    const selectedIds = selectedCartItems.value.map(item => item.id)

    const res: any = await orderApi.createFromCart({
      cartItemIds: selectedIds,
      address: checkoutForm.value.address,
      receiverName: checkoutForm.value.receiverName,
      receiverPhone: checkoutForm.value.receiverPhone,
      remark: checkoutForm.value.remark
    })

    if (res.code === 200) {
      message.success('订单创建成功')
      // 清空购物车中已下单的商品
      await fetchCart()
      selectedItems.value = []
      window.dispatchEvent(new Event('cart-updated'))
      router.push('/orders')
    } else {
      message.error(res.message || '创建订单失败')
    }
  } catch (error: any) {
    message.error(error?.message || '创建订单失败')
  } finally {
    checkoutLoading.value = false
  }
}

// 刷新页面时保持选中状态
const initSelectedItems = () => {
  selectedItems.value = []
}

onMounted(() => {
  fetchCart()
  initSelectedItems()
})
</script>

<style scoped>
.container { max-width: 1000px; margin: 0 auto; padding: 0 20px; }
h2 { margin-bottom: 20px; color: #333; }
.cart-card { border-radius: 16px; }
.product { display: flex; align-items: center; gap: 12px; }
.product-img { width: 60px; height: 60px; object-fit: cover; border-radius: 8px; }
.summary { margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 16px; color: #666; }
.total span { font-size: 24px; color: #ff6b6b; font-weight: bold; }
.selected-count { font-size: 18px; color: #409eff; font-weight: bold; }
</style>
