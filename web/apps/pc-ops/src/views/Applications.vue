<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;gap:12px;align-items:center">
        <span style="font-weight:700">开通申请</span>
        <el-select v-model="status" size="small" style="width:130px" @change="load">
          <el-option label="待审核" :value="1" /><el-option label="已开通" :value="2" /><el-option label="已驳回" :value="3" /><el-option label="全部" :value="undefined" />
        </el-select>
      </div>
    </template>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="appNo" label="申请编号" width="180" />
      <el-table-column prop="companyName" label="企业" />
      <el-table-column prop="contactName" label="联系人" />
      <el-table-column prop="contactMobile" label="手机" />
      <el-table-column prop="planCode" label="套餐" />
      <el-table-column label="状态"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column label="操作" align="right" width="220">
        <template #default="{ row }">
          <template v-if="row.status===1">
            <el-button size="small" type="primary" @click="approve(row)">审核开通</el-button>
            <el-button size="small" type="danger" plain @click="reject(row)">驳回</el-button>
          </template>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="show" title="审核开通" width="440">
      <p>企业：<b>{{ curr?.companyName }}</b></p>
      <el-form label-width="90px">
        <el-form-item label="专属域名"><el-input v-model="domain" placeholder="如 hengda" /><small class="muted">.faccess.cn</small></el-form-item>
        <el-form-item label="审核备注"><el-input v-model="remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="show=false">取消</el-button><el-button type="primary" @click="doApprove">通过并开通</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const list = ref<any[]>([])
const loading = ref(false)
const status = ref<number | undefined>(1)
const show = ref(false)
const curr = ref<any>(null)
const domain = ref('')
const remark = ref('')

function statusText(s: number) { return { 1: '待审核', 2: '已开通', 3: '已驳回', 4: '补充材料' }[s] || s }
function statusType(s: number) { return s === 2 ? 'success' : s === 3 ? 'danger' : 'warning' }

async function load() { loading.value = true; try { const p = await request.get('/ops/applications', { params: { status: status.value, page: 1, size: 50 } }); list.value = p.list || [] } finally { loading.value = false } }
function approve(row: any) { curr.value = row; domain.value = ''; remark.value = ''; show.value = true }
async function doApprove() {
  const r: any = await request.post(`/ops/applications/${curr.value.id}/approve`, { domain: domain.value, remark: remark.value })
  show.value = false
  ElMessage.success(`已开通：域名 ${r.domain} · 管理员 ${r.adminUsername}`)
  load()
}
async function reject(row: any) { await ElMessageBox.confirm(`驳回 ${row.companyName} 的申请?`, '提示'); await request.post(`/ops/applications/${row.id}/reject`, { remark: '资料不符' }); ElMessage.success('已驳回'); load() }
onMounted(load)
</script>
<style scoped>.muted{color:#9aa3b5}</style>
