import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/store/modules/user'
import axios from 'axios'

NProgress.configure({ showSpinner: false })

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/components/Layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页看板', icon: 'Odometer', roles: ['ADMIN', 'LIBRARIAN', 'READER'] },
      },
      {
        path: 'book-catalog',
        name: 'BookCatalog',
        component: () => import('@/views/book-catalog/index.vue'),
        meta: { title: '图书借阅', icon: 'Reading', roles: ['ADMIN', 'LIBRARIAN', 'READER'] },
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] },
      },
      {
        path: 'books',
        name: 'BookManagement',
        component: () => import('@/views/book/index.vue'),
        meta: { title: '图书管理', icon: 'Reading', roles: ['ADMIN', 'LIBRARIAN'] },
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '分类管理', icon: 'Menu', roles: ['ADMIN', 'LIBRARIAN'] },
      },
      {
        path: 'book-normalize',
        name: 'BookNormalize',
        component: () => import('@/views/book-normalize/index.vue'),
        meta: { title: '图书规范化', icon: 'EditPen', roles: ['ADMIN', 'LIBRARIAN'] },
      },
      {
        path: 'borrows',
        name: 'BorrowManagement',
        component: () => import('@/views/borrow/index.vue'),
        meta: { title: '借阅管理', icon: 'Tickets', roles: ['ADMIN', 'LIBRARIAN'] },
      },
      {
        path: 'my-borrows',
        name: 'MyBorrows',
        component: () => import('@/views/borrow/my-borrows.vue'),
        meta: { title: '我的借阅', icon: 'DocumentChecked', roles: ['READER'] },
      },
      {
        path: 'inventory',
        name: 'InventoryManagement',
        component: () => import('@/views/inventory/index.vue'),
        meta: { title: '库存管理', icon: 'Box', roles: ['ADMIN', 'LIBRARIAN'] },
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '统计报表', icon: 'DataAnalysis', roles: ['ADMIN', 'LIBRARIAN'] },
      },
      {
        path: 'publishers',
        name: 'PublisherManagement',
        component: () => import('@/views/publisher/index.vue'),
        meta: { title: '出版社管理', icon: 'OfficeBuilding', roles: ['ADMIN', 'LIBRARIAN'] },
      },
      {
        path: 'bookshelves',
        name: 'BookshelfManagement',
        component: () => import('@/views/bookshelf/index.vue'),
        meta: { title: '书架管理', icon: 'Grid', roles: ['ADMIN', 'LIBRARIAN'] },
      },
      {
        path: 'purchases',
        name: 'PurchaseManagement',
        component: () => import('@/views/purchase/index.vue'),
        meta: { title: '采购管理', icon: 'ShoppingCart', roles: ['ADMIN', 'LIBRARIAN'] },
      },
      {
        path: 'notices',
        name: 'NoticeManagement',
        component: () => import('@/views/notice/index.vue'),
        meta: { title: '通知公告', icon: 'Bell', roles: ['ADMIN', 'LIBRARIAN', 'READER'] },
      },
      {
        path: 'borrow-code',
        name: 'BorrowCode',
        component: () => import('@/views/borrow/code.vue'),
        meta: { title: '借阅码', icon: 'Key', roles: ['ADMIN', 'LIBRARIAN', 'READER'] },
      },
      {
        path: 'permissions',
        name: 'PermissionManagement',
        component: () => import('@/views/permission/index.vue'),
        meta: { title: '权限管理', icon: 'Lock', roles: ['ADMIN'] },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'UserFilled', roles: ['ADMIN', 'LIBRARIAN', 'READER'] },
      },
    ],
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '403', requiresAuth: false },
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', requiresAuth: false },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

const whiteList = ['/login', '/403', '/404']

let userInfoFetched = false

router.beforeEach(async (to, _from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || ''} - 图书馆管理系统`

  if (to.meta.requiresAuth === false || whiteList.includes(to.path)) {
    next()
    return
  }

  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('token')

  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (to.path === '/login') {
    next({ path: '/' })
    return
  }

  if (!userStore.userInfo && !userInfoFetched) {
    try {
      const token = localStorage.getItem('token')
      const res = await axios.get('/api/auth/me', {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      })
      const data = res.data?.data
      if (data) {
        userStore.setUserInfo({
          id: data.id,
          username: data.username,
          realName: data.realName,
          role: data.role,
          phone: data.phone || '',
          email: data.email || '',
          avatar: data.avatar || '',
        })
        userInfoFetched = true
      }
    } catch {
      userInfoFetched = false
      userStore.logout()
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }

  if (!userStore.userInfo) {
    userStore.initUserInfo()
  }

  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && requiredRoles.length > 0) {
    const userRole = userStore.userInfo?.role
    if (!userRole) {
      next('/login')
      return
    }
    if (!requiredRoles.includes(userRole)) {
      next('/403')
      return
    }
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

router.onError((error) => {
  NProgress.done()
  console.error('路由错误:', error)
})

export default router
