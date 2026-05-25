import request from './request'

export const cartApi = {
  get() {
    return request.get('/cart')
  },
  getCount() {
    return request.get('/cart/count')
  },
  add(dessertId: number, quantity = 1) {
    return request.post('/cart/add', { dessertId, quantity })
  },
  updateQuantity(id: number, quantity: number) {
    return request.put(`/cart/${id}`, { quantity })
  },
  remove(id: number) {
    return request.delete(`/cart/${id}`)
  },
  clear() {
    return request.delete('/cart/clear')
  }
}
