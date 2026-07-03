<template>
  <el-container class="layout">
    <el-aside width="230px" class="sidebar">
      <div class="brand">
        <div class="logo">厂</div>
        <div class="name">厂智访客</div>
      </div>
      <el-menu :default-active="activePath" router background-color="transparent" text-color="#b4bdd4" active-text-color="#fff">
        <el-menu-item v-for="m in menuList" :key="m.id" :index="resolvePath(m)">
          <el-icon><Menu /></el-icon>
          <span>{{ m.name }}</span>
        </el-menu-item>
      </el-menu>
      <div class="rent">
        <div class="tag">已订阅</div>
        <div class="price">￥60<small>/月</small></div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div class="title">{{ currentTitle }}</div>
        <div class="right">
          <div class="tenant">
            <span class="tn">{{ user?.realName || '当前用户' }}</span>
            <span class="tp">{{ roleText }}</span>
          </div>
          <el-dropdown @command="onCommand">
            <el-avatar :size="34" style="background: linear-gradient(135deg,#5b8dff,#2f6bed); cursor:pointer">
              {{ (user?.realName || 'U').charAt(0) }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const route = useRoute()
const router = useRouter()
const user = computed(() => store.user)
const roleText = computed(() => (store.roles.includes('TENANT_ADMIN') ? '企业管理员' : store.roles.join(',')))

// 已实现的前端页面路径（与 router 对应）
const implemented = new Set([
  '/checkin', '/invite', '/onsite', '/records', '/dashboard', '/approval',
  '/field-config', '/rules', '/gate-config', '/multi-site', '/settings',
  '/steel/checkin', '/steel/weigh', '/steel/carrier'
])

const menuList = computed(() => {
  const base = [{ id: 0, parentId: 0, name: '工作台', path: '/workbench' } as any]
  // 扁平展示后端下发的全部菜单（含二级）
  return base.concat(store.menus)
})

function resolvePath(m: any) {
  if (m.path === '/workbench') return '/workbench'
  return implemented.has(m.path) ? m.path : '/placeholder'
}

const activePath = computed(() => route.path)
const currentTitle = computed(() => (route.meta.title as string) || '工作台')

function onCommand(cmd: string) {
  if (cmd === 'logout') {
    store.logout()
    router.push('/login')
  }
}

onMounted(() => {
  if (store.token && store.menus.length === 0) store.fetchMenus()
})
</script>

<style scoped lang="scss">
.layout { height: 100vh; }
.sidebar {
  background: #101a33; color: #c7d0e4; display: flex; flex-direction: column;
  padding: 16px 12px;
}
.brand { display: flex; align-items: center; gap: 10px; padding: 6px 8px 18px; }
.brand .logo { width: 36px; height: 36px; border-radius: 10px; background: linear-gradient(135deg, #2f6bed, #5b8dff); color: #fff; display: grid; place-items: center; font-weight: 800; }
.brand .name { color: #fff; font-weight: 700; }
.sidebar :deep(.el-menu) { border: none; flex: 1; }
.sidebar :deep(.el-menu-item.is-active) { background: linear-gradient(135deg, #2f6bed, #4f82ff); border-radius: 10px; }
.rent { margin-top: auto; background: #17264a; border-radius: 12px; padding: 12px; }
.rent .tag { display: inline-block; background: #22b07d; color: #fff; font-size: 11px; padding: 2px 8px; border-radius: 20px; }
.rent .price { color: #fff; font-size: 20px; font-weight: 800; margin-top: 6px; }
.rent .price small { font-size: 12px; color: #97a2bd; }
.topbar {
  background: #fff; border-bottom: 1px solid #eceff5; display: flex; align-items: center;
  justify-content: space-between; padding: 0 24px;
}
.topbar .title { font-size: 18px; font-weight: 700; }
.topbar .right { display: flex; align-items: center; gap: 14px; }
.tenant { text-align: right; line-height: 1.2; }
.tenant .tn { font-weight: 700; display: block; font-size: 13.5px; }
.tenant .tp { font-size: 11px; color: #9aa3b5; }
.content { background: #f4f6fb; padding: 22px; }
</style>
