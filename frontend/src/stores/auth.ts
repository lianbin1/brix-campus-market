import { defineStore } from 'pinia'
import request from '@/utils/request'
import { ADMIN_MENUS } from '@/constants'

/** 用户信息（id 为字符串：后端 Long → String） */
export interface UserInfo {
  id: string
  username: string
  nickname: string
  avatar: string
  school?: string
  campus?: string
  phone?: string
  bio?: string
  role: 'USER' | 'REVIEWER' | 'OPERATOR'
  roleDesc?: string
  status: 'NORMAL' | 'DISABLED'
  statusDesc?: string
  creditScore: number
  createdAt?: string
  stats?: {
    itemCount: number
    onSaleCount: number
    soldCount: number
  }
}

/** 后台菜单项 */
export interface MenuItem {
  key: string
  title: string
  path: string
  icon: string
}

/** 登录返回体 */
interface LoginResult {
  token: string
  user: UserInfo
  menus: MenuItem[]
}

/** 登录入参 */
export interface LoginPayload {
  username: string
  password: string
}

const TOKEN_KEY = 'brix_token'
const USER_KEY = 'brix_user'

/**
 * 只保留一个 store：认证态。
 * token 与 user 持久化到 localStorage（刷新保持登录）；menus 与 unreadCount 不持久化。
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: (localStorage.getItem(TOKEN_KEY) || '') as string,
    user: null as UserInfo | null,
    menus: [] as MenuItem[],
    unreadCount: 0
  }),

  getters: {
    /** 当前角色码 */
    role: (state): string => state.user?.role || '',
    /** 是否已登录 */
    isLogin: (state): boolean => !!state.token,
    /** 是否后台角色 */
    isAdmin: (state): boolean => {
      const role = state.user?.role
      return role === 'REVIEWER' || role === 'OPERATOR'
    }
  },

  actions: {
    /**
     * 前台登录。
     * @param payload 账号密码
     */
    async login(payload: LoginPayload): Promise<void> {
      const data = (await request.post('/auth/login', payload)) as LoginResult
      this.applyLogin(data)
    },

    /**
     * 后台登录。
     * @param payload 账号密码
     */
    async adminLogin(payload: LoginPayload): Promise<void> {
      const data = (await request.post('/admin/login', payload)) as LoginResult
      this.applyLogin(data)
    },

    /**
     * 注册（注册即登录）。
     * @param payload 注册信息
     */
    async register(payload: Record<string, unknown>): Promise<void> {
      const data = (await request.post('/auth/register', payload)) as LoginResult
      this.applyLogin(data)
    },

    /**
     * 写入登录态并持久化。
     * @param data 登录返回体
     */
    applyLogin(data: LoginResult): void {
      this.token = data.token
      this.user = data.user
      this.menus = data.menus && data.menus.length ? data.menus : this.rebuildMenus(data.user.role)
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(USER_KEY, JSON.stringify(data.user))
    },

    /** 刷新页面后按 role 本地重建后台菜单 */
    rebuildMenus(role: string): MenuItem[] {
      if (role === 'OPERATOR') return [...ADMIN_MENUS.OPERATOR]
      if (role === 'REVIEWER') return [...ADMIN_MENUS.REVIEWER]
      return []
    },

    /** 拉取当前用户信息（含个人数据统计） */
    async fetchMe(): Promise<void> {
      const data = (await request.get('/auth/me')) as UserInfo
      this.user = data
      localStorage.setItem(USER_KEY, JSON.stringify(data))
      if (!this.menus.length) {
        this.menus = this.rebuildMenus(data.role)
      }
    },

    /** 设置全局未读数 */
    setUnread(count: number): void {
      this.unreadCount = Math.max(0, Number(count) || 0)
    },

    /** 未读数 +1 */
    incUnread(): void {
      this.unreadCount += 1
    },

    /** 清除登录态 */
    clear(): void {
      this.token = ''
      this.user = null
      this.menus = []
      this.unreadCount = 0
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },

    /** 退出登录 */
    logout(): void {
      this.clear()
    }
  }
})
