import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

/**
 * 路由表（19 页 + 404）。
 *
 * - 使用 hash 路由：无需服务端 history 配置（PRD 部署形态为本地裸机双进程）。
 * - 守卫仅作体验层拦截，真正的安全边界在后端接口（架构 5.6）。
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { guestOnly: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/Register.vue'),
    meta: { guestOnly: true, title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layout/BasicLayout.vue'),
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'items/:id',
        name: 'item-detail',
        component: () => import('@/views/ItemDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'publish',
        name: 'publish',
        component: () => import('@/views/Publish.vue'),
        meta: { requiresAuth: true, roles: ['USER'], title: '发布商品' }
      },
      {
        path: 'orders/:id',
        name: 'order-detail',
        component: () => import('@/views/OrderDetail.vue'),
        meta: { requiresAuth: true, title: '订单详情' }
      },
      {
        path: 'checkout/:orderId',
        name: 'checkout',
        component: () => import('@/views/Checkout.vue'),
        meta: { requiresAuth: true, roles: ['USER'], title: '收银台' }
      },
      {
        path: 'messages',
        name: 'messages',
        component: () => import('@/views/Messages.vue'),
        meta: { requiresAuth: true, title: '私信' }
      }
    ]
  },
  {
    path: '/my',
    component: () => import('@/layout/UserCenterLayout.vue'),
    children: [
      {
        path: 'items',
        name: 'my-items',
        component: () => import('@/views/MyItems.vue'),
        meta: { requiresAuth: true, roles: ['USER'], title: '我的商品' }
      },
      {
        path: 'favorites',
        name: 'my-favorites',
        component: () => import('@/views/MyFavorites.vue'),
        meta: { requiresAuth: true, title: '我的收藏' }
      },
      {
        path: 'orders',
        name: 'my-orders',
        component: () => import('@/views/MyOrders.vue'),
        meta: { requiresAuth: true, title: '我的订单' }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/Profile.vue'),
        meta: { requiresAuth: true, title: '个人中心' }
      }
    ]
  },
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/views/admin/AdminLogin.vue'),
    meta: { guestOnly: true, title: '后台登录' }
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { requiresAuth: true, roles: ['OPERATOR'], title: '数据大屏' }
      },
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('@/views/admin/AdminUsers.vue'),
        meta: { requiresAuth: true, roles: ['OPERATOR'], title: '用户管理' }
      },
      {
        path: 'items',
        name: 'admin-items',
        component: () => import('@/views/admin/AdminItems.vue'),
        meta: { requiresAuth: true, roles: ['REVIEWER'], title: '商品审核' }
      },
      {
        path: 'orders',
        name: 'admin-orders',
        component: () => import('@/views/admin/AdminOrders.vue'),
        meta: { requiresAuth: true, roles: ['OPERATOR'], title: '订单管理' }
      },
      {
        path: 'categories',
        name: 'admin-categories',
        component: () => import('@/views/admin/AdminCategories.vue'),
        meta: { requiresAuth: true, roles: ['OPERATOR'], title: '分类管理' }
      },
      {
        path: 'reports',
        name: 'admin-reports',
        component: () => import('@/views/admin/AdminReports.vue'),
        meta: { requiresAuth: true, roles: ['REVIEWER'], title: '举报处理' }
      },
      {
        // 私信记录：纠纷取证（只读），运营与审核员均可见
        path: 'chats',
        name: 'admin-chats',
        component: () => import('@/views/admin/AdminChats.vue'),
        meta: { requiresAuth: true, roles: ['OPERATOR', 'REVIEWER'], title: '私信记录' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ left: 0, top: 0 })
})

/** 各角色默认落地页 */
function homeOf(role: string): string {
  if (role === 'OPERATOR') return '/admin/dashboard'
  if (role === 'REVIEWER') return '/admin/items'
  return '/'
}

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  // 1. 有 token 但没有用户信息 → 拉一次 /auth/me
  if (auth.token && !auth.user) {
    try {
      await auth.fetchMe()
    } catch (error) {
      auth.clear()
    }
  }

  // 2. 仅游客可访问（登录 / 注册）
  if (to.meta.guestOnly && auth.isLogin) {
    return homeOf(auth.role)
  }

  // 3. 需要登录
  if (to.meta.requiresAuth && !auth.isLogin) {
    return { path: '/login', query: { redirect: encodeURIComponent(to.fullPath) } }
  }

  // 4. 后台路径：USER 一律拦回后台登录页
  if (to.path.startsWith('/admin') && to.path !== '/admin/login' && auth.role === 'USER') {
    auth.clear()
    return '/admin/login'
  }

  // 5. 角色不匹配 → 回到该角色首页（REVIEWER 与 OPERATOR 互不越界）
  const roles = to.meta.roles as string[] | undefined
  if (roles && roles.length && !roles.includes(auth.role)) {
    return homeOf(auth.role)
  }

  // 6. 设置标题
  const title = (to.meta.title as string) || '校园二手交易平台'
  document.title = `${title} · Brix Campus Market`
  return true
})

export default router
