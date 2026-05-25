import request from './request'

export const dashboardApi = {
  // 获取统计数据（管理员）
  getStatistics() {
    return request.get('/dashboard/statistics')
  }
}
