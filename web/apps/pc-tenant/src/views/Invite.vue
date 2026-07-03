<template>
  <el-row :gutter="18">
    <el-col :span="10">
      <el-card shadow="never" header="发起预约邀请">
        <el-form :model="form" label-width="90px">
          <el-form-item label="访客姓名" required><el-input v-model="form.visitorName" /></el-form-item>
          <el-form-item label="访客手机" required><el-input v-model="form.visitorMobile" /></el-form-item>
          <el-form-item label="来访单位"><el-input v-model="form.company" /></el-form-item>
          <el-form-item label="来访事由"><el-input v-model="form.reason" placeholder="如 物料送货" /></el-form-item>
          <el-form-item label="被访人"><el-input v-model="form.hostName" /></el-form-item>
          <el-form-item label="来访日期"><el-input v-model="form.visitDate" placeholder="2026-07-05" /></el-form-item>
          <el-form-item><el-button type="primary" :loading="loading" @click="submit">生成并发送邀请</el-button></el-form-item>
        </el-form>
        <el-alert v-if="last" type="success" :closable="false"
          :title="`邀请已生成：邀请码 ${last.inviteCode}（到场扫码/报码免审入场）`" />
      </el-card>
    </el-col>
    <el-col :span="14">
      <el-card shadow="never" header="邀请记录">
        <el-table :data="list" v-loading="loading">
          <el-table-column prop="visitorName" label="受邀访客" />
          <el-table-column prop="reason" label="事由" />
          <el-table-column prop="hostName" label="接待人" />
          <el-table-column prop="inviteCode" label="邀请码" />
          <el-table-column label="状态"><template #default="{ row }"><span class="fv-badge" :class="row.status==='PENDING'?'orange':row.status==='ARRIVED'?'green':'gray'">{{ statusText(row.status) }}</span></template></el-table-column>
          <el-table-column label="操作" align="right">
            <template #default="{ row }">
              <el-button size="small" @click="resend(row)">重发</el-button>
              <el-button size="small" type="danger" plain @click="cancel(row)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const loading = ref(false)
const list = ref<any[]>([])
const last = ref<any>(null)
const form = reactive<any>({ visitorName: '', visitorMobile: '', company: '', reason: '洽谈业务', hostName: '', visitDate: '' })
function statusText(s: string) { return ({ PENDING: '待到访', ARRIVED: '已到访', EXPIRED: '已过期', CANCELLED: '已取消' } as any)[s] || s }

async function submit() {
  if (!form.visitorName || !form.visitorMobile) { ElMessage.warning('请填写访客姓名与手机'); return }
  loading.value = true
  try { last.value = await request.post('/tenant/invitations', { ...form, status: 'PENDING' }); ElMessage.success('已生成邀请'); load() }
  finally { loading.value = false }
}
async function load() { list.value = await request.get('/tenant/invitations') }
async function cancel(row: any) { await request.post(`/tenant/invitations/${row.id}/cancel`); ElMessage.success('已取消'); load() }
async function resend(row: any) { await request.post(`/tenant/invitations/${row.id}/resend`); ElMessage.success('已重发') }
onMounted(load)
</script>
