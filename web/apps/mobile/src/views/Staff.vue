<template>
  <div class="page">
    <div class="appbar">
      <div class="t">员工 / 管理端</div>
      <div class="s">待办审批</div>
    </div>
    <div class="pad">
      <van-cell-group inset v-if="!token" title="登录">
        <van-field v-model="login.domain" label="企业域名" placeholder="desheng" />
        <van-field v-model="login.username" label="账号" placeholder="手机号" />
        <van-field v-model="login.password" label="密码" type="password" />
        <div style="padding:12px"><van-button type="primary" block round @click="doLogin" :loading="logging">登录</van-button></div>
      </van-cell-group>

      <template v-else>
        <div style="display:flex;justify-content:space-between;align-items:center;padding:4px 8px">
          <span style="color:#5a6478">待处理 {{ list.length }} 条</span>
          <van-button size="small" @click="load">刷新</van-button>
        </div>
        <van-cell-group inset>
          <van-cell v-for="a in list" :key="a.id" :title="'审批 #' + a.id" :label="a.status">
            <template #right-icon>
              <van-button size="mini" type="success" style="margin-right:6px" @click="decide(a, true)">同意</van-button>
              <van-button size="mini" @click="decide(a, false)">拒绝</van-button>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-if="!list.length" description="暂无待办" />
        <div style="padding:16px"><van-button block round @click="logout">退出登录</van-button></div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { showSuccessToast } from 'vant'
import request from '@/api/request'

const token = ref(localStorage.getItem('fv_m_token') || '')
const logging = ref(false)
const list = ref<any[]>([])
const login = reactive({ domain: 'desheng', username: '', password: '' })

async function doLogin() {
  logging.value = true
  try {
    const d: any = await request.post('/tenant/auth/login', login)
    localStorage.setItem('fv_m_token', d.accessToken)
    token.value = d.accessToken
    showSuccessToast('登录成功')
    load()
  } finally { logging.value = false }
}
async function load() { list.value = await request.get('/tenant/approvals/pending') }
async function decide(a: any, approve: boolean) {
  await request.post(`/tenant/approvals/${a.id}/decision`, { approve })
  showSuccessToast(approve ? '已同意' : '已拒绝')
  load()
}
function logout() { localStorage.removeItem('fv_m_token'); token.value = '' }
onMounted(() => { if (token.value) load() })
</script>
