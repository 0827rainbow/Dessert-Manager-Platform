import request from './request'

export const orderApi = {
  // 获取我的订单列表
  getList() {
    return request.get('/order/list')
  },

  // 管理员获取所有订单
  adminGetAllOrders() {
    return request.get('/order/admin/list')
  },
// 在 order.ts 中添加
  createFromSelectedCart(data: { cartItemIds: number[], address: string, receiverName: string, receiverPhone: string, remark: string }) {
    return request.post('/order/createFromSelectedCart', data)
  },
  // 从购物车创建订单
  createFromCart(data: any) {
    return request.post('/order/createFromCart', data)
  },

  // 直接购买
  createDirect(data: any) {
    return request.post('/order/createDirect', data)
  },

  // 支付订单
  pay(orderId: number) {
    return request.post(`/order/pay/${orderId}`)
  },

  // 取消订单
  cancel(orderId: number) {
    return request.post(`/order/cancel/${orderId}`)
  },

  // 删除订单
  deleteOrder(orderId: number) {
    return request.delete(`/order/${orderId}`)
  },

  // 发货（管理员）
  deliver(orderId: number) {
    return request.put(`/order/deliver/${orderId}`)
  },

  // 确认收货（用户）
  confirm(orderId: number) {
    return request.put(`/order/confirm/${orderId}`)
  },

  // 修改订单地址（管理员）
  updateAddress(orderId: number, data: any) {
    return request.put(`/order/address/${orderId}`, data)
  }
}
