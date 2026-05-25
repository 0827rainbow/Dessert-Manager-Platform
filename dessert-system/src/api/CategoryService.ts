import request from './request'

export interface Category {
  id?: number
  name: string
  descp: string
}

export const CategoryService = {
  list(params: { pageNum: number; pageSize: number; name?: string; descp?: string }) {
    return request.get('/category', { params })
  },
  add(data: Category) {
    return request.post('/category', data)
  },
  edit(data: Category) {
    return request.put('/category', data)
  },
  del(ids: number | string) {
    return request.delete(`/category/${ids}`)
  }
}
