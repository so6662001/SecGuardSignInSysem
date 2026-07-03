import { defineStore } from 'pinia'
import request from '@/api/request'

interface MenuItem {
  id: number
  parentId: number
  name: string
  path: string
  icon?: string
  permCode?: string
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('fv_token') || '',
    user: JSON.parse(localStorage.getItem('fv_user') || 'null') as any,
    menus: [] as MenuItem[]
  }),
  getters: {
    perms: (s) => (s.user?.perms || []) as string[],
    roles: (s) => (s.user?.roles || []) as string[]
  },
  actions: {
    async login(payload: { mode: 'tenant' | 'ops'; domain?: string; username: string; password: string }) {
      const url = payload.mode === 'ops' ? '/ops/auth/login' : '/tenant/auth/login'
      const data: any = await request.post(url, {
        username: payload.username,
        password: payload.password,
        domain: payload.domain
      })
      this.token = data.accessToken
      this.user = data.user
      localStorage.setItem('fv_token', this.token)
      localStorage.setItem('fv_user', JSON.stringify(this.user))
    },
    async fetchMenus() {
      try {
        this.menus = await request.get('/tenant/me/menus')
      } catch {
        this.menus = []
      }
    },
    logout() {
      this.token = ''
      this.user = null
      this.menus = []
      localStorage.removeItem('fv_token')
      localStorage.removeItem('fv_user')
    }
  }
})
