<template>
  <el-row :gutter="18">
    <el-col :span="13">
      <el-card shadow="never" header="车辆到厂登记">
        <el-form :model="form" label-width="110px">
          <el-form-item label="方向">
            <el-radio-group v-model="form.direction">
              <el-radio-button label="OUTBOUND">提货出厂</el-radio-button>
              <el-radio-button label="INBOUND">送货入厂</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="车牌号" required><el-input v-model="form.plateNo" placeholder="粤B·8Z326" /></el-form-item>
          <el-form-item :label="form.direction==='INBOUND'?'采购单号':'销售订单号'">
            <el-input v-model="form.orderNo" placeholder="SO-260702-0391">
              <template #append><el-button @click="verify">从交易平台核验</el-button></template>
            </el-input>
          </el-form-item>
          <el-alert v-if="order" type="success" :closable="false" style="margin-bottom:14px"
            :title="`${order.partnerName} · ${order.goodsName} · 应${form.direction==='INBOUND'?'收':'提'}${order.planQty} 已${order.doneQty} 剩余${order.remainQty} · ${order.settleStatus}`" />
          <el-form-item label="计划重量(吨)"><el-input-number v-model="form.planWeight" :min="0" /></el-form-item>
          <el-form-item label="仓库/料场"><el-input v-model="form.warehouse" /></el-form-item>
          <el-form-item label="扣杂率(%)"><el-input-number v-model="form.deductRate" :min="0" :max="100" :step="0.5" /></el-form-item>
          <el-form-item><el-button type="primary" :loading="loading" @click="submit">确认入厂 · 排队</el-button></el-form-item>
        </el-form>
      </el-card>
    </el-col>
    <el-col :span="11">
      <el-card shadow="never">
        <template #header><div style="display:flex;justify-content:space-between"><span>在厂车辆排队</span><el-button size="small" @click="loadQueue">刷新</el-button></div></template>
        <el-table :data="queue" size="small" empty-text="暂无在厂车辆">
          <el-table-column prop="queueNo" label="排号" width="70" />
          <el-table-column prop="plateNo" label="车牌" />
          <el-table-column label="方向" width="90"><template #default="{ row }">{{ row.direction==='INBOUND'?'送货':'提货' }}</template></el-table-column>
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column label="操作" align="right" width="90">
            <template #default="{ row }"><el-button size="small" type="success" @click="release(row)">放行</el-button></template>
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

const form = reactive<any>({ direction: 'OUTBOUND', plateNo: '', orderNo: '', planWeight: 0, warehouse: '', deductRate: 0 })
const order = ref<any>(null)
const queue = ref<any[]>([])
const loading = ref(false)

async function verify() {
  if (!form.orderNo) { ElMessage.warning('请填写单据号'); return }
  const type = form.direction === 'INBOUND' ? 'PURCHASE' : 'SALES'
  order.value = await request.get(`/tenant/steel/trade/order/${encodeURIComponent(form.orderNo)}`, { params: { type } })
  ElMessage.success('已带出交易平台单据')
}
async function submit() {
  if (!form.plateNo) { ElMessage.warning('请填写车牌'); return }
  loading.value = true
  try { await request.post('/tenant/steel/vehicle-visits', form); ElMessage.success('已登记入厂并排队'); loadQueue() }
  finally { loading.value = false }
}
async function loadQueue() { const p = await request.get('/tenant/steel/vehicle-visits'); queue.value = p.list || [] }
async function release(row: any) {
  try { await request.post(`/tenant/steel/vehicle-visits/${row.id}/release`); ElMessage.success('已放行，净重已回写交易平台'); loadQueue() }
  catch {}
}
onMounted(loadQueue)
</script>
