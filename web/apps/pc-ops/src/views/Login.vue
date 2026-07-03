<template>
  <div class="wrap">
    <div class="card">
      <div class="brand"><div class="logo">运</div><div><div class="t">厂智访客 · 运营后台</div><div class="s">PLATFORM CONSOLE</div></div></div>
      <el-form :model="form" style="margin-top:20px">
        <el-form-item><el-input v-model="form.username" size="large" placeholder="运营账号" /></el-form-item>
        <el-form-item><el-input v-model="form.password" size="large" type="password" show-password placeholder="密码" @keyup.enter="login" /></el-form-item>
        <el-button type="primary" size="large" style="width:100%;background:#7c56e0;border-color:#7c56e0" :loading="loading" @click="login">登 录</el-button>
      </el-form>
      <div class="tip">演示账号：admin / admin123</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })

async function login() {
  loading.value = true
  try {
    const data: any = await request.post('/ops/auth/login', form)
    localStorage.setItem('fv_ops_token', data.accessToken)
    localStorage.setItem('fv_ops_user', JSON.stringify(data.user))
    ElMessage.success('登录成功')
    router.push('/overview')
  } finally { loading.value = false }
}
</script>

<style scoped>
.wrap { min-height: 100vh; display: grid; place-items: center; background: radial-gradient(circle at 30% 20%, #2a1f52, #12122a); }
.card { width: 380px; background: #fff; border-radius: 18px; padding: 34px; box-shadow: 0 20px 60px rgba(0,0,0,.3); }
.brand { display: flex; align-items: center; gap: 12px; }
.logo { width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg,#7c56e0,#a78bfa); color: #fff; display: grid; place-items: center; font-weight: 800; font-size: 18px; }
.brand .t { font-weight: 800; font-size: 17px; }
.brand .s { font-size: 11px; color: #9aa3b5; letter-spacing: 2px; }
.tip { color: #9aa3b5; font-size: 12px; margin-top: 14px; text-align: center; }
</style>
