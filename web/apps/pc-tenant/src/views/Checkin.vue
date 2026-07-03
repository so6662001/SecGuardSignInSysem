<template>
  <el-card shadow="never" header="访客入场登记">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" style="max-width:640px">
      <el-form-item label="访客姓名" prop="visitorName">
        <el-input v-model="form.visitorName" placeholder="请输入访客姓名" />
      </el-form-item>
      <el-form-item label="手机号码" prop="visitorMobile">
        <el-input v-model="form.visitorMobile" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="来访单位">
        <el-input v-model="form.company" placeholder="如：顺丰速运" />
      </el-form-item>
      <el-form-item label="来访事由" prop="reason">
        <el-radio-group v-model="form.reason">
          <el-radio-button v-for="r in reasons" :key="r" :label="r" />
        </el-radio-group>
      </el-form-item>
      <el-form-item label="被访人" prop="hostName">
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
import { ElMessage, type FormInstance } from 'element-plus'
import request from '@/api/request'

const reasons = ['洽谈业务', '设备维修', '物料送货', '面试应聘', '参观考察', '其他']
const loading = ref(false)
const lastResult = ref<any>(null)
const formRef = ref<FormInstance>()

const rules = {
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  visitorMobile: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  reason: [{ required: true, message: '请选择来访事由', trigger: 'change' }],
  hostName: [{ required: true, message: '请填写被访人', trigger: 'blur' }]
}

const form = reactive<any>({
  visitorName: '', visitorMobile: '', company: '', reason: '洽谈业务',
  hostName: '', hostUserId: undefined, companions: 1
})

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      lastResult.value = await request.post('/tenant/visits', form)
      ElMessage.success('登记成功，已通知被访人')
    } finally {
      loading.value = false
    }
  })
}
function reset() {
  Object.assign(form, { visitorName: '', visitorMobile: '', company: '', reason: '洽谈业务', hostName: '', hostUserId: undefined, companions: 1 })
  lastResult.value = null
}
</script>
