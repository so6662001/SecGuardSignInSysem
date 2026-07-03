<template>
  <el-row :gutter="18">
    <el-col :span="10">
      <el-card shadow="never" header="通知渠道与超时升级">
        <el-form v-if="cfg" label-width="150px">
          <el-form-item label="渠道优先级">
            <el-input v-model="cfg.channelOrder" placeholder="PUSH,SMS,VOICE" />
          </el-form-item>
          <el-form-item label="首次响应时限(秒)"><el-input-number v-model="cfg.firstTimeout" :min="30" :step="30" /></el-form-item>
          <el-form-item label="二次提醒换渠道"><el-switch v-model="cfg.secondRemind" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-form-item label="升级备用审批人"><el-switch v-model="cfg.escalateBackup" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-form-item label="门卫代批兜底"><el-switch v-model="cfg.guardProxy" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-form-item label="预约邀请码免审"><el-switch v-model="cfg.invitePass" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-form-item label="白名单免审"><el-switch v-model="cfg.whitelistPass" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-form-item><el-button type="primary" @click="save">保存规则</el-button></el-form-item>
        </el-form>
      </el-card>
    </el-col>
    <el-col :span="14">
      <el-card shadow="never">
        <template #header><div style="display:flex;justify-content:space-between"><span>待处理审批</span><el-button size="small" @click="load">刷新</el-button></div></template>
        <el-table :data="pending" v-loading="loading" empty-text="暂无待处理审批">
          <el-table-column prop="id" label="审批ID" width="180" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }"><el-tag :type="row.status==='ESCALATED'?'danger':'warning'">{{ row.status }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" align="right">
            <template #default="{ row }">
              <el-button size="small" type="success" @click="decide(row,true)">同意</el-button>
              <el-button size="small" type="danger" plain @click="decide(row,false)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const cfg = ref<any>(null)
const pending = ref<any[]>([])
const loading = ref(false)

async function loadCfg() { try { cfg.value = await request.get('/tenant/notify-config') } catch { cfg.value = { channelOrder: 'PUSH,SMS', firstTimeout: 180, secondRemind: 1, escalateBackup: 1, guardProxy: 1, invitePass: 1, whitelistPass: 1 } } }
async function save() { await request.put('/tenant/notify-config', cfg.value); ElMessage.success('已保存') }
async function load() { loading.value = true; try { pending.value = await request.get('/tenant/approvals/pending') } finally { loading.value = false } }
async function decide(row: any, approve: boolean) { await request.post(`/tenant/approvals/${row.id}/decision`, { approve }); ElMessage.success(approve ? '已同意' : '已拒绝'); load() }
onMounted(() => { loadCfg(); load() })
</script>
