<template>
  <div class="container" v-loading="loading">
    <div v-if="dessert.id" class="detail-content">
      <div class="detail-left">
        <div class="main-image">
          <img :src="imageUrl" :alt="dessert.name" />
        </div>
      </div>
      <div class="detail-right">
        <h1 class="product-name">{{ dessert.name }}</h1>
        <div class="price-section">
          <div class="price">
            <span class="label">价格</span>
            <span class="current">¥{{ dessert.price }}</span>
            <!-- 已删除原价显示 -->
          </div>
          <div class="sales-info">
            <span>月销 {{ dessert.sales || 0 }} 件</span>
            <span>库存 {{ dessert.stock || 0 }} 件</span>
          </div>
        </div>
        <div class="info-item">
          <span class="label">分类</span>
          <span>{{ dessert.categoryName }}</span>
        </div>
        <div class="info-item">
          <span class="label">描述</span>
          <span>{{ dessert.descp || '暂无描述' }}</span>
        </div>
        <div class="quantity-section">
          <span class="label">数量</span>
          <el-input-number v-model="quantity" :min="1" :max="dessert.stock || 99" size="large" />
        </div>
        <div class="action-buttons">
          <el-button v-if="dessert.stock > 0" size="large" @click="addToCart" :loading="cartLoading">加入购物车</el-button>
          <el-button v-if="dessert.stock > 0" type="primary" size="large" @click="buyNow" :loading="buyLoading">立即购买</el-button>
          <el-button v-else type="info" size="large" disabled>商品已售罄</el-button>
        </div>
      </div>
    </div>

    <!-- 评价组件 -->
    <div v-if="dessert.id" class="reviews-wrapper">
      <DessertReviews
        :dessertId="dessert.id"
        :avgRating="dessert.rating || 0"
        :reviewCount="dessert.reviewCount || 0"
      />
    </div>

    <el-empty v-else description="商品不存在" />

    <!-- 收货地址对话框 -->
    <el-dialog v-model="buyDialogVisible" title="填写收货信息" width="500px">
      <el-form :model="buyForm" :rules="addressRules" ref="buyFormRef" label-width="100px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="buyForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="buyForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="buyForm.address" type="textarea" rows="2" placeholder="请输入详细收货地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="buyForm.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBuyOrder" :loading="buyLoading">确认购买</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { dessertApi } from '@/api/dessert'
import { cartApi } from '@/api/cart'
import { orderApi } from '@/api/order'
import DessertReviews from '@/components/DessertReviews.vue'
import { message } from '@/utils/message'

const route = useRoute()
const router = useRouter()
const baseUrl = 'http://localhost:8080'

const dessert = ref<any>({})
const loading = ref(false)
const quantity = ref(1)
const cartLoading = ref(false)
const buyLoading = ref(false)
const buyDialogVisible = ref(false)
const buyFormRef = ref()

const buyForm = ref({
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

const imageUrl = computed(() => {
  if (dessert.value.photoUrl) {
    if (dessert.value.photoUrl.startsWith('http')) return dessert.value.photoUrl
    return baseUrl + dessert.value.photoUrl
  }
  return 'https://picsum.photos/400/400?random=1'
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res: any = await dessertApi.getById(id)
    dessert.value = res.data || {}
  } catch (error) {
    message.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

const addToCart = async () => {
  if (dessert.value.stock === 0) {
    message.warning('商品已售罄')
    return
  }
  cartLoading.value = true
  try {
    await cartApi.add(dessert.value.id, quantity.value)
    message.success('已添加到购物车')
    window.dispatchEvent(new Event('cart-updated'))
  } catch (error) {
    message.error('添加失败')
  } finally {
    cartLoading.value = false
  }
}

const buyNow = () => {
  if (dessert.value.stock === 0) {
    message.warning('商品已售罄')
    return
  }
  buyDialogVisible.value = true
}

const submitBuyOrder = async () => {
  try {
    await buyFormRef.value?.validate()
    buyDialogVisible.value = false
    buyLoading.value = true

    const res: any = await orderApi.createDirect({
      dessertId: dessert.value.id,
      quantity: quantity.value,
      address: buyForm.value.address,
      receiverName: buyForm.value.receiverName,
      receiverPhone: buyForm.value.receiverPhone,
      remark: buyForm.value.remark
    })

    if (res.code === 200) {
      message.success('订单创建成功')
      router.push('/orders')
    } else {
      message.error(res.message || '创建订单失败')
    }
  } catch (error: any) {
    message.error(error?.message || '创建订单失败')
  } finally {
    buyLoading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.detail-content { display: flex; gap: 40px; background: white; padding: 30px; border-radius: 16px; }
.detail-left { flex: 1; }
.main-image { width: 100%; height: 400px; border-radius: 12px; overflow: hidden; background: #f5f5f5; }
.main-image img { width: 100%; height: 100%; object-fit: cover; }
.detail-right { flex: 1; }
.product-name { font-size: 24px; color: #333; margin-bottom: 20px; }
.price-section { background: #f9f9f9; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
.price { display: flex; align-items: baseline; gap: 12px; margin-bottom: 12px; }
.label { color: #999; font-size: 14px; width: 60px; }
.current { font-size: 28px; font-weight: bold; color: #ff6b6b; }
.sales-info { display: flex; gap: 20px; color: #999; font-size: 14px; }
.info-item { margin-bottom: 20px; display: flex; align-items: center; }
.quantity-section { margin-bottom: 20px; display: flex; align-items: center; }
.action-buttons { display: flex; gap: 20px; margin-top: 30px; }
.action-buttons .el-button { flex: 1; height: 48px; font-size: 16px; }

/* 评价区域样式 */
.reviews-wrapper {
  margin-top: 30px;
  background: white;
  padding: 20px 30px;
  border-radius: 16px;
}
</style>
