<template>
  <el-dialog v-model="visible" title="评价商品" width="500px" @close="close">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="评分" prop="rating">
        <el-rate v-model="form.rating" :texts="rateTexts" show-text />
      </el-form-item>
      <el-form-item label="评价内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          rows="4"
          placeholder="请分享您的使用感受..."
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :loading="loading">提交评价</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { reviewApi } from '@/api/review'

const props = defineProps<{
  dessertId: number
  orderId: number
  dessertName?: string
}>()

const emit = defineEmits(['success'])

const visible = ref(false)
const loading = ref(false)
const formRef = ref()

const rateTexts = ['很差', '较差', '一般', '满意', '超赞']

const form = reactive({
  rating: 5,
  content: ''
})

const rules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}

const open = () => {
  visible.value = true
  form.rating = 5
  form.content = ''
}

const close = () => {
  formRef.value?.resetFields()
}

const submit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true

    const res: any = await reviewApi.addReview({
      dessertId: props.dessertId,
      orderId: props.orderId,
      rating: form.rating,
      content: form.content
    })

    if (res.code === 200) {
      ElMessage.success('评价成功，感谢您的分享！')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(res.message || '评价失败')
    }
  } catch {
    ElMessage.error('评价失败')
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
