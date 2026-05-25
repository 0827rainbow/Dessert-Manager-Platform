<template>
  <div class="home-page">
    <div class="banner-section">
      <el-carousel height="400px" :interval="5000" arrow="always">
        <el-carousel-item v-for="(item, index) in banners" :key="index">
          <div class="banner-item" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="banner-content">
              <h2 :style="{ color: item.titleColor }">{{ item.title }}</h2>
              <p :style="{ color: item.subtitleColor }">{{ item.subtitle }}</p>
              <el-button type="primary" round @click="goShop">立即选购</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="section">
      <div class="container">
        <div class="section-header">
          <h2>🔥 热门推荐</h2>
          <router-link to="/dessert-list" class="more">查看更多 →</router-link>
        </div>
        <div class="dessert-grid" v-loading="recommendLoading">
          <DessertCard v-for="item in recommendList" :key="item.id" :dessert="item" @add-cart="addCart" />
        </div>
      </div>
    </div>

    <div class="section">
      <div class="container">
        <div class="section-header">
          <h2>✨ 新品上市</h2>
          <router-link to="/dessert-list" class="more">查看更多 →</router-link>
        </div>
        <div class="dessert-grid" v-loading="dessertLoading">
          <DessertCard v-for="item in dessertList" :key="item.id" :dessert="item" @add-cart="addCart" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { dessertApi } from '@/api/dessert'
import { cartApi } from '@/api/cart'
import DessertCard from '@/components/DessertCard.vue'
import { message } from '@/utils/message'

const router = useRouter()

// 动态获取图片 URL（适配 Vite）
const getImageUrl = (name: string) => {
  return new URL(`/src/assets/${name}.jpg`, import.meta.url).href
}

const banners = ref([
  { image: getImageUrl('001'), title: '春日限定甜点', subtitle: '一口尝尽春天的味道', titleColor: '#00CED1', subtitleColor: '#00CED1' },
  { image: getImageUrl('002'), title: '全场满减', subtitle: '满100减20，满200减50', titleColor: '#FFFFFF', subtitleColor: '#FFFFFF' },
  { image: getImageUrl('003'), title: '新品上架', subtitle: '尝鲜价8折优惠', titleColor: '#FF69B4', subtitleColor: '#FF69B4' }
])

const recommendList = ref<any[]>([])
const dessertList = ref<any[]>([])
const recommendLoading = ref(false)
const dessertLoading = ref(false)

const fetchRecommend = async () => {
  recommendLoading.value = true
  try {
    const res: any = await dessertApi.getRecommend(8)
    recommendList.value = res.data || res.rows || []
  } catch (error) {
    console.error('获取推荐失败', error)
  } finally {
    recommendLoading.value = false
  }
}

const fetchDesserts = async () => {
  dessertLoading.value = true
  try {
    const res: any = await dessertApi.getList({ pageNum: 1, pageSize: 8, status: 1 })
    dessertList.value = res.rows || []
  } catch (error) {
    console.error('获取甜点失败', error)
  } finally {
    dessertLoading.value = false
  }
}

const addCart = async (dessert: any) => {
  try {
    await cartApi.add(dessert.id, 1)
    message.success('已添加到购物车')
    window.dispatchEvent(new Event('cart-updated'))
  } catch {
    message.error('添加失败')
  }
}

const goShop = () => {
  router.push('/dessert-list')
}

onMounted(() => {
  fetchRecommend()
  fetchDesserts()
})
</script>

<style scoped>
.banner-section { margin-bottom: 40px; }
.banner-item {
  height: 400px;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
}
.banner-content { text-align: center; text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5); }
.banner-content h2 { font-size: 48px; margin-bottom: 16px; }
.banner-content p { font-size: 18px; margin-bottom: 24px; }
.section { margin-bottom: 60px; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.section-header h2 { font-size: 24px; }
.more { color: #667eea; text-decoration: none; }
.dessert-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 20px; }
</style>
