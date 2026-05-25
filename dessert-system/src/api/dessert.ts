import request from './request'

export interface Dessert {
  id: number
  name: string
  descp: string
  photoUrl: string
  price: number
  originalPrice: number
  stock: number
  sales: number
  status: number
  isRecommend: number
  catId: number
  categoryName: string
}

export const dessertApi = {
  getList(params: any) {
    return request.get('/dessert/list', { params })
  },
  getById(id: number) {
    return request.get(`/dessert/${id}`)
  },
  getRecommend(limit = 6) {
    return request.get('/dessert/recommend', { params: { limit } })
  },
  add(data: any) {
    return request.post('/dessert', data)
  },
  update(data: any) {
    return request.put('/dessert', data)
  },
  delete(id: number) {
    return request.delete(`/dessert/${id}`)
  },
  upload(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/dessert/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
