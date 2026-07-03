<template>
  <div class="auth">
    <div class="auth-side">
      <div class="brand">
        <div class="logo">厂</div>
        <div class="name">厂智访客<small>FACTORY VISITOR</small></div>
      </div>
      <div class="side-body">
        <h1>让工厂访客登记<br>更简单、更智能</h1>
        <p>替代手工登记本，进出记录实时同步至管理人员，随时查看与审核。</p>
      </div>
      <div class="copyright">© 2026 厂智访客 · 保留所有权利</div>
    </div>

    <div class="auth-form">
      <div class="form-box">
        <h2>欢迎回来 👋</h2>
        <p class="muted">请选择身份并登录您的工作台</p>

        <el-radio-group v-model="mode" class="mode">
          <el-radio-button label="tenant">企业租户</el-radio-button>
          <el-radio-button label="ops">平台运营</el-radio-button>
        </el-radio-group>

        <el-form :model="form" @submit.prevent>
          <el-form-item v-if="mode === 'tenant'">
            <el-input v-model="form.domain" placeholder="企业专属域名，如 desheng" size="large">
              <template #prepend>域名</template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.username" placeholder="账号 / 手机号" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" :prefix-icon="Lock" show-password @keyup.enter="onLogin" />
          </el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="onLogin">登 录</el-button>
        </el-form>

        <div class="tip muted">
          演示账号：运营 admin / admin123；租户 域名 desheng · 手机号 · 申请时设置的密码
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const store = useUserStore()
const mode = ref<'tenant' | 'ops'>('tenant')
const loading = ref(false)
const form = reactive({ domain: 'desheng', username: '', password: '' })

async function onLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await store.login({ mode: mode.value, domain: form.domain, username: form.username, password: form.password })
    if (mode.value === 'tenant') await store.fetchMenus()
    ElMessage.success('登录成功')
    router.push('/workbench')
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.auth { min-height: 100vh; display: grid; grid-template-columns: 1.05fr 0.95fr; }
.auth-side {
  background: linear-gradient(150deg, #12224a, #0c1428);
  color: #fff; padding: 56px; display: flex; flex-direction: column;
}
.auth-side .brand { display: flex; align-items: center; gap: 12px; }
.auth-side .logo { width: 40px; height: 40px; border-radius: 11px; background: linear-gradient(135deg, #2f6bed, #5b8dff); display: grid; place-items: center; font-weight: 800; }
.auth-side .name { font-weight: 700; font-size: 16px; }
.auth-side .name small { display: block; font-weight: 400; font-size: 11px; color: #8592b0; letter-spacing: 2px; }
.side-body { margin: auto 0; }
.side-body h1 { font-size: 32px; line-height: 1.3; }
.side-body p { color: #9fb0d6; margin-top: 16px; max-width: 420px; }
.copyright { color: #6b7690; font-size: 13px; }
.auth-form { display: flex; align-items: center; justify-content: center; padding: 40px; }
.form-box { width: 380px; max-width: 100%; }
.form-box h2 { font-size: 24px; font-weight: 800; margin: 0; }
.muted { color: #9aa3b5; }
.mode { margin: 18px 0 22px; }
.tip { font-size: 12px; margin-top: 18px; line-height: 1.6; }
@media (max-width: 860px) { .auth { grid-template-columns: 1fr; } .auth-side { display: none; } }
</style>
