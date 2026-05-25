<template>
  <div class="not-found">
    <div class="content">
      <h1>404</h1>
      <h2>页面不存在</h2>
      <p>抱歉，您访问的页面不存在或已被删除</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const goHome = () => router.push('/')
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.content {
  text-align: center;
  background: white;
  padding: 60px;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}
h1 { font-size: 120px; margin: 0; color: #409eff; }
h2 { font-size: 32px; margin: 16px 0; color: #333; }
p { color: #666; margin-bottom: 24px; }
</style>
