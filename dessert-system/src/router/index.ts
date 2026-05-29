import { createRouter, createWebHistory } from 'vue-router'

// 直接导入组件（替代动态导入）
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Layout from '@/views/Layout.vue'
import Home from '@/views/Home.vue'
import DessertList from '@/views/DessertList.vue'
import DessertDetail from '@/views/DessertDetail.vue'
import Cart from '@/views/Cart.vue'
import Orders from '@/views/Orders.vue'
import Category from '@/views/Category.vue'
import Dessert from '@/views/Dessert.vue'
import NotFound from '@/views/NotFound.vue'
import AdminHome from '@/views/AdminHome.vue'
import OrderManage from '@/views/OrderManage.vue'

// 定义用户类型
interface User {
  id: number
  username: string
  role: number
  nickname?: string
  [key: string]: any
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false, title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false, title: '注册' },
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: () => {
          // 根据用户角色动态重定向
          const userStr = localStorage.getItem('user')
          if (userStr) {
            try {
              const user = JSON.parse(userStr) as User
              if (user.role === 1) {
                return '/admin-home'
              }
            } catch (e) {
              console.error('解析用户信息失败', e)
            }
          }
          return '/home'
        },
      },
      {
        path: 'home',
        name: 'Home',
        component: Home,
        meta: { title: '首页', requiresAuth: true },
      },
      {
        path: 'admin-home',
        name: 'AdminHome',
        component: AdminHome,
        meta: { title: '数据面板', requiresAuth: true, requiresAdmin: true },
      },
      {
        path: 'dessert-list',
        name: 'DessertList',
        component: DessertList,
        meta: { title: '甜点商城', requiresAuth: true, userOnly: true },
      },
      {
        path: 'dessert-detail/:id',
        name: 'DessertDetail',
        component: DessertDetail,
        meta: { title: '商品详情', requiresAuth: true, userOnly: true },
      },
      {
        path: 'cart',
        name: 'Cart',
        component: Cart,
        meta: { title: '购物车', requiresAuth: true, userOnly: true },
      },
      {
        path: 'orders',
        name: 'Orders',
        component: Orders,
        meta: { title: '我的订单', requiresAuth: true },
      },
      {
        path: 'category',
        name: 'Category',
        component: Category,
        meta: { title: '分类管理', requiresAuth: true, requiresAdmin: true },
      },
      {
        path: 'dessert',
        name: 'Dessert',
        component: Dessert,
        meta: { title: '甜点管理', requiresAuth: true, requiresAdmin: true },
      },
      {
        path: 'order-manage',
        name: 'OrderManage',
        component: OrderManage,
        meta: { title: '订单管理', requiresAuth: true, requiresAdmin: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '404', requiresAuth: false },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  // 新增：路由切换后滚动到顶部（解决点击甜点卡片后页面停留在底部的问题）
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  let userRole = 0

  if (userStr) {
    try {
      const user = JSON.parse(userStr) as User
      userRole = user.role || 0
    } catch (e) {
      console.error('解析用户信息失败', e)
    }
  }

  // 设置页面标题
  document.title = to.meta.title ? `甜点管理系统 - ${to.meta.title}` : '甜点管理系统'

  // 需要登录的页面
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 已登录访问登录/注册页，跳转到对应首页
  if ((to.path === '/login' || to.path === '/register') && token) {
    if (userRole === 1) {
      next('/admin-home')
    } else {
      next('/home')
    }
    return
  }

  // 管理员不能访问普通用户专用页面（userOnly）
  if (to.meta.userOnly && userRole === 1) {
    next('/admin-home')
    return
  }

  // 普通用户不能访问管理员页面（requiresAdmin）
  if (to.meta.requiresAdmin && userRole !== 1) {
    next('/home')
    return
  }

  next()
})

export default router
