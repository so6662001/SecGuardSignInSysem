<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="brand"><div class="logo">运</div><div class="name">运营后台</div></div>
      <el-menu :default-active="$route.path" router background-color="transparent" text-color="#c9c2e4" active-text-color="#fff">
        <el-menu-item index="/overview">运营概览</el-menu-item>
        <el-menu-item index="/applications">开通申请</el-menu-item>
        <el-menu-item index="/tenants">租户管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="title">{{ $route.meta.title || '运营后台' }}</div>
        <el-dropdown @command="onCmd">
          <el-avatar :size="34" style="background:linear-gradient(135deg,#a78bfa,#7c56e0);cursor:pointer">运</el-avatar>
          <template #dropdown><el-dropdown-menu><el-dropdown-item command="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
        </el-dropdown>
      </el-header>
      <el-main style="background:#f4f6fb"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
function onCmd(c: string) { if (c === 'logout') { localStorage.removeItem('fv_ops_token'); router.push('/login') } }
</script>

<style scoped>
.layout { height: 100vh; }
.sidebar { background: #1a1230; padding: 16px 12px; }
.brand { display: flex; align-items: center; gap: 10px; padding: 6px 8px 18px; }
.brand .logo { width: 36px; height: 36px; border-radius: 10px; background: linear-gradient(135deg,#7c56e0,#a78bfa); color: #fff; display: grid; place-items: center; font-weight: 800; }
.brand .name { color: #fff; font-weight: 700; }
.sidebar :deep(.el-menu) { border: none; }
.sidebar :deep(.el-menu-item.is-active) { background: linear-gradient(135deg,#7c56e0,#a78bfa); border-radius: 10px; }
.topbar { background: #fff; border-bottom: 1px solid #eceff5; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; }
.topbar .title { font-weight: 700; font-size: 18px; }
</style>
