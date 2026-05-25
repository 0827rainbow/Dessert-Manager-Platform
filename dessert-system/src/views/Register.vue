<template>
  <div class="register-page">
    <div class="register-card">
      <div class="logo">🍰</div>
      <h1>注册账号</h1>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item prop="confirmPwd">
          <el-input v-model="form.confirmPwd" type="password" placeholder="确认密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleRegister" class="register-btn">注册</el-button>
        <div class="footer"><router-link to="/login">已有账号？去登录</router-link></div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '', confirmPwd: '' })

const validateConfirm = (_: any, value: string, callback: any) => {
  if (value !== form.password) callback(new Error('密码不一致'))
  else callback()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPwd: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }]
}

const handleRegister = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    const res: any = await userApi.register({ username: form.username, password: form.password })
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch {
    ElMessage.error('注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.register-card {
  width: 450px;
  background: white;
  border-radius: 32px;
  padding: 48px 40px;
  text-align: center;
}
.logo { font-size: 56px; margin-bottom: 16px; }
.register-card h1 { font-size: 28px; margin-bottom: 30px; }
.register-btn { width: 100%; height: 48px; font-size: 16px; background: linear-gradient(135deg, #667eea, #764ba2); border: none; margin: 20px 0; }
.footer a { color: #667eea; text-decoration: none; }
</style>
