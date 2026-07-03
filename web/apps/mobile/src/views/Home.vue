<template>
  <div class="page">
    <div class="appbar">
      <div class="t">厂智访客 · 移动端</div>
      <div class="s">访客自助登记 / 被访人审批 / 员工端</div>
    </div>
    <div class="pad">
      <van-cell-group inset>
        <van-cell title="访客扫码自助登记" is-link label="演示门岗码 DEMO" @click="go('/register/DEMO')" />
        <van-cell title="员工 / 管理端审批" is-link label="登录后处理待办" @click="go('/staff')" />
      </van-cell-group>

      <van-cell-group inset title="被访人免登录审批" style="margin-top:12px">
        <van-field v-model="token" label="Token" placeholder="粘贴短信链接中的 token" />
        <div style="padding:12px"><van-button type="primary" block round @click="openApprove">打开审批</van-button></div>
      </van-cell-group>

      <div style="padding:16px;color:#9aa3b5;font-size:12px;line-height:1.7">
        访客登记与免登录审批为公开接口；员工端复用租户账号登录。
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
const router = useRouter()
const token = ref('')
function go(p: string) { router.push(p) }
function openApprove() { if (!token.value) { showToast('请输入 token'); return } router.push('/approve/' + token.value.trim()) }
</script>
