<template>
  <div class="page">
    <div class="appbar">
      <div class="s">宏远制造厂 · 访客审批</div>
      <div class="t">请确认是否接待</div>
    </div>
    <div class="pad">
      <van-cell-group inset v-if="info">
        <van-cell title="访客" :value="info.visitorName" />
        <van-cell title="单位" :value="info.company" />
        <van-cell title="事由" :value="info.reason" />
        <van-cell title="拜访对象" :value="info.hostName" />
        <van-cell title="随行人数" :value="String(info.companions)" />
        <van-cell title="申请时间" :value="fmt(info.applyTime)" />
      </van-cell-group>
      <van-empty v-else description="加载中或链接已失效" />

      <div v-if="info && !info.expired" style="padding:16px;display:flex;gap:12px">
        <van-button block round @click="decide(false)">拒绝</van-button>
        <van-button type="primary" block round @click="decide(true)">同意接待</van-button>
      </div>
      <div v-else-if="info?.expired" style="padding:16px;text-align:center;color:#9aa3b5">该申请已处理或已失效</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { showSuccessToast } from 'vant'
import request from '@/api/request'

const route = useRoute()
const token = route.params.token as string
const info = ref<any>(null)

function fmt(t: string) { return t ? String(t).replace('T', ' ').slice(0, 19) : '' }

async function load() { try { info.value = await request.get(`/public/approve/${token}`) } catch { info.value = null } }
async function decide(approve: boolean) {
  await request.post(`/public/approve/${token}/decision`, { approve })
  showSuccessToast(approve ? '已同意接待，门卫已收到通知' : '已拒绝')
  load()
}
onMounted(load)
</script>
