import request from './request'

export const reviewApi = {
  getByDessert(dessertId: number) {
    return request.get(`/review/dessert/${dessertId}`)
  },

  getMyReviews() {
    return request.get('/review/my')
  },

  checkReviewed(orderId: number, dessertId: number) {
    return request.get(`/review/check/${orderId}/${dessertId}`)
  },

  addReview(data: { orderId: number; dessertId: number; rating: number; content: string }) {
    return request.post('/review', data)
  }
}
