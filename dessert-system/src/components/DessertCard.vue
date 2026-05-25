<template>
  <el-card class="dessert-card" shadow="hover">
    <div class="dessert-image" @click="goDetail">
      <img :src="getImageUrl(dessert.photoUrl)" :alt="dessert.name" />
      <div v-if="dessert.isRecommend === 1" class="recommend-badge">🔥 推荐</div>
    </div>
    <div class="dessert-info" @click="goDetail">
      <h3 class="dessert-name">{{ dessert.name }}</h3>
      <p class="dessert-desc">{{ dessert.descp || '暂无描述' }}</p>
      <div class="price-row">
        <span class="current-price">¥{{ dessert.price }}</span>
        <!-- 已删除原价显示 -->
      </div>
    </div>
    <div class="card-actions">
      <el-button type="primary" size="small" @click.stop="addToCart">加入购物车</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps<{
  dessert: any
}>()

const emit = defineEmits(['add-cart'])
const router = useRouter()
const baseUrl = 'http://localhost:8080'

const getImageUrl = (url: string) => {
  if (!url) return 'https://picsum.photos/200/200?random=1'
  if (url.startsWith('http')) return url
  return baseUrl + url
}

const goDetail = () => {
  router.push(`/dessert-detail/${props.dessert.id}`)
}

const addToCart = () => {
  emit('add-cart', props.dessert)
}
</script>

<style scoped>
.dessert-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
}
.dessert-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}
.dessert-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}
.dessert-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.dessert-card:hover .dessert-image img {
  transform: scale(1.05);
}
.recommend-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  color: white;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
}
.dessert-info {
  padding: 15px;
}
.dessert-name {
  font-size: 16px;
  margin-bottom: 8px;
  color: #333;
}
.dessert-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.current-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
}
.card-actions {
  padding: 0 15px 15px;
}
</style>
