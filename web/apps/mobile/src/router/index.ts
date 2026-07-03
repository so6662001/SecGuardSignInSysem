import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/', component: () => import('@/views/Home.vue') },
  { path: '/register/:gateCode', component: () => import('@/views/SelfRegister.vue') },
  { path: '/approve/:token', component: () => import('@/views/Approve.vue') },
  { path: '/staff', component: () => import('@/views/Staff.vue') }
]

export default createRouter({ history: createWebHashHistory(), routes })
