<template>
  <el-card shadow="never" header="登记规则" style="max-width:720px">
    <el-form label-width="180px">
      <el-divider content-position="left">信息填写</el-divider>
      <el-form-item label="手机号必填"><el-switch v-model="info.mobileRequired" /></el-form-item>
      <el-form-item label="来访单位必填"><el-switch v-model="info.companyRequired" /></el-form-item>
      <el-divider content-position="left">审批与放行</el-divider>
      <el-form-item label="被访人确认后方可入场"><el-switch v-model="approval.hostConfirm" /></el-form-item>
      <el-form-item label="允许门卫代审批"><el-switch v-model="approval.guardProxy" /></el-form-item>
      <el-divider content-position="left">在场与时长</el-divider>
      <el-form-item label="超时滞留提醒(小时)"><el-input-number v-model="stay.overstayHours" :min="1" :max="24" /></el-form-item>
      <el-form-item label="当日自动离场时间"><el-input v-model="stay.autoLeave" placeholder="23:00" style="width:140px" /></el-form-item>
      <el-divider content-position="left">访客牌</el-divider>
      <el-form-item label="入场自动分配访客牌"><el-switch v-model="badge.auto" /></el-form-item>
      <el-form-item label="离场必须归还访客牌"><el-switch v-model="badge.returnRequired" /></el-form-item>
      <el-form-item><el-button type="primary" @click="save">保存设置</el-button></el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const info = reactive<any>({ mobileRequired: true, companyRequired: true })
const approval = reactive<any>({ hostConfirm: true, guardProxy: true })
const stay = reactive<any>({ overstayHours: 4, autoLeave: '23:00' })
const badge = reactive<any>({ auto: true, returnRequired: true })

function parse(s: string, target: any) { try { if (s) Object.assign(target, JSON.parse(s)) } catch {} }

onMounted(async () => {
  try {
    const r = await request.get('/tenant/rules')
    parse(r.infoRules, info); parse(r.approvalRules, approval); parse(r.stayRules, stay); parse(r.badgeRules, badge)
  } catch {}
})

async function save() {
  await request.put('/tenant/rules', {
    infoRules: JSON.stringify(info),
    approvalRules: JSON.stringify(approval),
    stayRules: JSON.stringify(stay),
    badgeRules: JSON.stringify(badge)
  })
  ElMessage.success('登记规则已保存')
}
</script>
