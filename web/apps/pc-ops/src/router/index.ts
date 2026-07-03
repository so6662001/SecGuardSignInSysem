import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/OpsLayout.vue'),
    redirect: '/overview',
    children: [
      { path: 'overview', component: () => import('@/views/Overview.vue'), meta: { title: '运营概览' } },
      { path: 'applications', component: () => import('@/views/Applications.vue'), meta: { title: '开通申请' } },
      { path: 'tenants', component: () => import('@/views/Tenants.vue'), meta: { title: '租户管理' } }
    ]
  }
]

const router = createRouter({ history: createWebHashHistory(), routes })
router.beforeEach((to) => {
  if (to.meta.public) return true
  if (!localStorage.getItem('fv_ops_token')) return { path: '/login' }
  return true
})
export default router
