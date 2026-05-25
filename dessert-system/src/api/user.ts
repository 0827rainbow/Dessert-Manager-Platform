import request from './request'

export const userApi = {
  login(data: { username: string; password: string }) {
    return request.post('/user/login', data)
  },
  register(data: any) {
    return request.post('/user/register', data)
  },
  getProfile() {
    return request.get('/user/profile')
  },
  logout() {
    return request.get('/user/logout')
  }
}
