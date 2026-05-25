<template>
  <div class="reviews-section">
    <div class="reviews-header">
      <h3>用户评价</h3>
      <!-- 已删除评分显示 -->
    </div>

    <div v-if="reviews.length > 0" class="reviews-list">
      <div v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-header">
          <div class="user-info">
            <el-avatar :size="32" :icon="UserFilled" />
            <span class="nickname">{{ review.userNickname || '匿名用户' }}</span>
          </div>
          <div class="rating-time">
            <!-- 已删除评分显示，只保留时间 -->
            <span class="time">{{ formatTime(review.createTime) }}</span>
          </div>
        </div>
        <div class="review-content">{{ review.content }}</div>
      </div>
    </div>
    <div v-else class="reviews-empty">
      <el-empty description="暂无评价，快来抢沙发吧~" :image-size="80" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'
import { reviewApi } from '@/api/review'

const props = defineProps<{
  dessertId: number
  avgRating?: number     // 保留但不再使用
  reviewCount?: number   // 保留但不再使用
}>()

interface Review {
  id: number
  rating?: number        // 保留但不显示
  content: string
  userNickname: string
  createTime: string
}

const reviews = ref<Review[]>([])

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const fetchReviews = async () => {
  if (!props.dessertId) return
  try {
    const res: any = await reviewApi.getByDessert(props.dessertId)
    if (res.code === 200) {
      reviews.value = res.data || []
    }
  } catch (error) {
    console.error('获取评价失败', error)
  }
}

onMounted(() => {
  fetchReviews()
})

watch(() => props.dessertId, () => {
  fetchReviews()
}, { immediate: true })
</script>

<style scoped>
.reviews-section {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.reviews-header h3 {
  font-size: 18px;
  color: #333;
}
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.review-item {
  padding: 16px;
  background: #f9f9f9;
  border-radius: 12px;
}
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.nickname {
  font-weight: 500;
  color: #333;
}
.rating-time {
  display: flex;
  align-items: center;
  gap: 12px;
}
.time {
  color: #999;
  font-size: 12px;
}
.review-content {
  color: #666;
  line-height: 1.6;
}
.reviews-empty {
  padding: 20px;
}
</style>
