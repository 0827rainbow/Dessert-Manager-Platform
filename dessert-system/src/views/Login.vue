<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>🍰 甜点管理系统</h2>
      <h3>登录</h3>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large"
            show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">登录</el-button>
        </el-form-item>
        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/api/request'
import { message } from '@/utils/message'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
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

          // 关键：根据角色跳转到不同页面
          if (user.role === 1) {
            console.log('管理员登录，跳转 /admin-home')
            router.push('/admin-home')
          } else {
            console.log('普通用户登录，跳转 /home')
            router.push('/home')
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
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 20px;
  border-radius: 16px;
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
</style>
