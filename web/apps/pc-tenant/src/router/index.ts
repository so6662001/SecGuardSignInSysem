import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/workbench',
    children: [
      { path: 'workbench', component: () => import('@/views/Workbench.vue'), meta: { title: '工作台' } },
      { path: 'dashboard', component: () => import('@/views/Workbench.vue'), meta: { title: '数据看板' } },
      { path: 'checkin', component: () => import('@/views/Checkin.vue'), meta: { title: '入场登记' } },
      { path: 'invite', component: () => import('@/views/Invite.vue'), meta: { title: '预约邀请' } },
      { path: 'onsite', component: () => import('@/views/Onsite.vue'), meta: { title: '在场访客' } },
      { path: 'records', component: () => import('@/views/Records.vue'), meta: { title: '进出记录' } },
      { path: 'approval', component: () => import('@/views/Approval.vue'), meta: { title: '审批与通知' } },
      { path: 'field-config', component: () => import('@/views/FieldConfig.vue'), meta: { title: '登记项配置' } },
      { path: 'rules', component: () => import('@/views/Rules.vue'), meta: { title: '登记规则' } },
      { path: 'gate-config', component: () => import('@/views/GateConfig.vue'), meta: { title: '门岗管理' } },
      { path: 'multi-site', component: () => import('@/views/MultiSite.vue'), meta: { title: '集团总览' } },
      { path: 'settings', component: () => import('@/views/Settings.vue'), meta: { title: '系统设置' } },
      { path: 'steel/checkin', component: () => import('@/views/steel/SteelCheckin.vue'), meta: { title: '车辆到厂' } },
      { path: 'steel/weigh', component: () => import('@/views/steel/SteelWeigh.vue'), meta: { title: '磅房过磅' } },
      { path: 'steel/carrier', component: () => import('@/views/steel/Carrier.vue'), meta: { title: '承运商管理' } },
      { path: 'placeholder', component: () => import('@/views/Placeholder.vue'), meta: { title: '功能页' } }
    ]
  }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to) => {
  const store = useUserStore()
  if (to.meta.public) return true
  if (!store.token) return { path: '/login' }
  return true
})

export default router
