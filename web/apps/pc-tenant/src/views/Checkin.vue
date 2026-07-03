<template>
  <el-card shadow="never" header="访客入场登记">
    <el-form :model="form" label-width="110px" style="max-width:640px">
      <el-form-item label="访客姓名" required>
        <el-input v-model="form.visitorName" placeholder="请输入访客姓名" />
      </el-form-item>
      <el-form-item label="手机号码">
        <el-input v-model="form.visitorMobile" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="来访单位">
        <el-input v-model="form.company" placeholder="如：顺丰速运" />
      </el-form-item>
      <el-form-item label="来访事由" required>
        <el-radio-group v-model="form.reason">
          <el-radio-button v-for="r in reasons" :key="r" :label="r" />
        </el-radio-group>
      </el-form-item>
      <el-form-item label="被访人" required>
        <el-input v-model="form.hostName" placeholder="被访人姓名" style="width:200px" />
        <el-input v-model.number="form.hostUserId" placeholder="被访人用户ID（选填）" style="width:200px;margin-left:10px" />
      </el-form-item>
      <el-form-item label="随行人数">
        <el-input-number v-model="form.companions" :min="1" :max="20" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">确认入场登记</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert v-if="lastResult" type="success" :closable="false"
      :title="`登记成功：${lastResult.visitorName} · 访客牌 ${lastResult.badgeNo} · 状态 ${lastResult.status}（已通知被访人）`" />
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const reasons = ['洽谈业务', '设备维修', '物料送货', '面试应聘', '参观考察', '其他']
const loading = ref(false)
const lastResult = ref<any>(null)

const form = reactive<any>({
  visitorName: '', visitorMobile: '', company: '', reason: '洽谈业务',
  hostName: '', hostUserId: undefined, companions: 1
})

async function submit() {
  if (!form.visitorName || !form.reason || !form.hostName) {
    ElMessage.warning('请填写访客姓名、来访事由、被访人')
    return
  }
  loading.value = true
  try {
    lastResult.value = await request.post('/tenant/visits', form)
    ElMessage.success('登记成功，已通知被访人')
  } finally {
    loading.value = false
  }
}
function reset() {
  Object.assign(form, { visitorName: '', visitorMobile: '', company: '', reason: '洽谈业务', hostName: '', hostUserId: undefined, companions: 1 })
  lastResult.value = null
}
</script>
