<template>
  <div class="container">
    <div class="filter-bar">
      <el-input v-model="params.name" placeholder="搜索甜点" prefix-icon="Search" style="width: 250px" @keyup.enter="search" />
      <el-select v-model="params.catId" placeholder="全部分类" clearable style="width: 150px">
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
      <el-button @click="reset">重置</el-button>
    </div>
    <div class="dessert-grid" v-loading="loading">
      <DessertCard v-for="dessert in list" :key="dessert.id" :dessert="dessert" @add-cart="addCart" />
    </div>
    <el-empty v-if="!loading && list.length === 0" description="暂无商品" />
    <div class="pagination">
      <el-pagination
        :current-page="params.pageNum"
        :page-size="params.pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { dessertApi } from '@/api/dessert'
import { cartApi } from '@/api/cart'
import { CategoryService } from '@/api/CategoryService'
import DessertCard from '@/components/DessertCard.vue'

const params = ref({ pageNum: 1, pageSize: 12, name: '', catId: null as number | null })
const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const categories = ref<any[]>([])

const fetchCategories = async () => {
  try {
    const res: any = await CategoryService.list({ pageNum: 1, pageSize: 100 })
    categories.value = res.rows || []
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await dessertApi.getList(params.value)
    list.value = res.rows || []
    total.value = res.total || 0
  } catch {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const search = () => {
  params.value.pageNum = 1
  fetchList()
}

const reset = () => {
  params.value = { pageNum: 1, pageSize: 12, name: '', catId: null }
  fetchList()
}

const handlePageChange = (page: number) => {
  params.value.pageNum = page
  fetchList()
}

const addCart = async (dessert: any) => {
  try {
    await cartApi.add(dessert.id, 1)
    ElMessage.success('已添加到购物车')
    window.dispatchEvent(new Event('cart-updated'))
  } catch {
    ElMessage.error('添加失败')
  }
}

onMounted(() => {
  fetchCategories()
  fetchList()
})
</script>

<style scoped>
.container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
.filter-bar { background: white; padding: 20px; border-radius: 16px; margin-bottom: 24px; display: flex; gap: 12px; flex-wrap: wrap; align-items: center; }
.dessert-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 20px; }
.pagination { margin-top: 40px; display: flex; justify-content: center; }
</style>
