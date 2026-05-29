<template>
  <div class="login-container" :style="{ backgroundImage: `url(${bgImage})` }">
    <div class="login-overlay"></div>
    <el-card class="login-card">
      <h2>🍰 甜点管理系统</h2>
      <h3>登录</h3>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" :type="showPassword ? 'text' : 'password'" placeholder="密码"
            prefix-icon="Lock">
            <template #suffix>
              <el-icon class="password-eye" @click="togglePasswordVisibility" :style="{ cursor: 'pointer' }">
                <View v-if="!showPassword" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">登录</el-button>
        </el-form-item>
        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
        <div class="demo-accounts">
          <p>测试账号：</p>
          <p>管理员：admin / admin123</p>
          <p>普通用户：user / 123456</p>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { View, Hide } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/api/request'
import { message } from '@/utils/message'
import bgImage from '@/assets/背景图.jpg'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const showPassword = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

const handleLogin = async () => {
  if (!formRef.value) return

  // 注意：validate 返回 Promise，必须 await
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res: any = await request.post('/user/login', {
      username: form.username,
      password: form.password
    })

    if (res.code === 200) {
      const { token, user } = res.data
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))

      message.success('登录成功')

      if (user.role === 1) {
        await router.push('/admin-home')
      } else {
        await router.push('/home')
      }
    } else {
      message.error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败', error)
    message.error('登录失败，请检查网络')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* 添加半透明遮罩层，使卡片更清晰 */
.login-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.login-card {
  position: relative;
  z-index: 1;
  width: 400px;
  padding: 20px;
  border-radius: 16px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.login-card h2 {
  text-align: center;
  margin-bottom: 10px;
  color: #333;
}

.login-card h3 {
  text-align: center;
  margin-bottom: 20px;
  color: #666;
}

.register-link {
  text-align: center;
  margin-top: 10px;
}

.demo-accounts {
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  font-size: 12px;
  color: #999;
}

.demo-accounts p {
  margin: 5px 0;
}

.password-eye {
  font-size: 18px;
  color: #909399;
  transition: color 0.2s;
}

.password-eye:hover {
  color: #409eff;
}
</style>
