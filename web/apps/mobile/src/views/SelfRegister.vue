<template>
  <div class="page">
    <div class="appbar">
      <div class="s">{{ schema?.gateName || '厂区门岗' }}</div>
      <div class="t">访客自助登记</div>
    </div>
    <div class="pad">
      <van-form @submit="submit">
        <van-cell-group inset>
          <van-field v-model="form.visitorName" label="姓名" placeholder="请输入姓名" :rules="[{ required: true }]" />
          <van-field v-model="form.visitorMobile" label="手机号" placeholder="用于接收放行通知" />
          <van-field v-model="form.company" label="来访单位" placeholder="您所在公司" />
          <van-field v-model="form.reason" label="来访事由" placeholder="如 洽谈业务" :rules="[{ required: true }]" />
          <van-field v-model="form.hostName" label="被访人" placeholder="要拜访的人/部门" :rules="[{ required: true }]" />
          <van-field v-model.number="form.companions" label="随行人数" type="digit" />
        </van-cell-group>
        <div style="padding:12px">
          <van-checkbox v-model="agree" shape="square">我已阅读并同意《访客须知与信息授权》</van-checkbox>
        </div>
        <div style="padding:0 12px">
          <van-button type="primary" block round native-type="submit" :loading="loading">提交登记</van-button>
        </div>
      </van-form>

      <van-dialog v-model:show="done" title="提交成功" :show-cancel-button="false">
        <div style="padding:20px;text-align:center">
          已提交，请在门岗等候放行。<br>访客牌：<b>{{ result?.badgeNo }}</b>
        </div>
      </van-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import request from '@/api/request'

const route = useRoute()
const gateCode = route.params.gateCode as string
const schema = ref<any>(null)
const loading = ref(false)
const done = ref(false)
const agree = ref(true)
const result = ref<any>(null)
const form = reactive<any>({ visitorName: '', visitorMobile: '', company: '', reason: '', hostName: '', companions: 1 })

onMounted(async () => {
  try { schema.value = await request.get(`/public/gate/${gateCode}/template`) } catch {}
})

async function submit() {
  if (!agree.value) { showToast('请先同意访客须知'); return }
  loading.value = true
  try {
    result.value = await request.post(`/public/gate/${gateCode}/self-register`, form)
    done.value = true
  } finally { loading.value = false }
}
</script>
