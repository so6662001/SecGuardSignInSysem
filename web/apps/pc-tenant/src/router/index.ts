import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/workbench',
    children: [
      { path: 'workbench', name: 'workbench', component: () => import('@/views/Workbench.vue'), meta: { title: '工作台' } },
      { path: 'checkin', name: 'checkin', component: () => import('@/views/Checkin.vue'), meta: { title: '入场登记' } },
      { path: 'onsite', name: 'onsite', component: () => import('@/views/Onsite.vue'), meta: { title: '在场访客' } },
      { path: 'records', name: 'records', component: () => import('@/views/Records.vue'), meta: { title: '进出记录' } },
      { path: 'placeholder', name: 'placeholder', component: () => import('@/views/Placeholder.vue'), meta: { title: '功能页' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const store = useUserStore()
  if (to.meta.public) return true
  if (!store.token) return { path: '/login' }
  return true
})

export default router
