import { ElMessage } from 'element-plus'

// 默认显示时长（毫秒）
const DEFAULT_DURATION = 1500

export const message = {
  success(text: string, duration: number = DEFAULT_DURATION) {
    ElMessage.success({
      message: text,
      duration,
      showClose: false
    })
  },

  error(text: string, duration: number = DEFAULT_DURATION) {
    ElMessage.error({
      message: text,
      duration,
      showClose: false
    })
  },

  warning(text: string, duration: number = DEFAULT_DURATION) {
    ElMessage.warning({
      message: text,
      duration,
      showClose: false
    })
  },

  info(text: string, duration: number = DEFAULT_DURATION) {
    ElMessage.info({
      message: text,
      duration,
      showClose: false
    })
  }
}
