<template>
  <el-row :gutter="18">
    <el-col :span="10">
      <el-card shadow="never" header="磅房过磅">
        <el-form :model="form" label-width="120px">
          <el-form-item label="车辆到厂ID" required><el-input v-model.number="form.vehicleVisitId" placeholder="排队车辆的ID" /></el-form-item>
          <el-form-item label="称重类型">
            <el-radio-group v-model="form.weighType">
              <el-radio-button label="TARE">皮重</el-radio-button>
              <el-radio-button label="GROSS">毛重</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="读数(吨)" required><el-input-number v-model="form.weight" :min="0" :step="0.01" /></el-form-item>
          <el-form-item><el-button type="primary" :loading="loading" @click="submit">记录过磅</el-button></el-form-item>
        </el-form>
        <el-descriptions v-if="last" :column="1" border>
          <el-descriptions-item label="皮重">{{ last.tareWeight ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="毛重">{{ last.grossWeight ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="净重">{{ last.netWeight ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="扣杂率">{{ last.deductRate ?? 0 }}%</el-descriptions-item>
          <el-descriptions-item label="结算净重">{{ last.settleWeight ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="异常"><el-tag v-if="last.abnormal" type="danger">{{ last.abnormal }}</el-tag><span v-else>无</span></el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-col>
    <el-col :span="14">
      <el-card shadow="never">
        <template #header><div style="display:flex;justify-content:space-between"><span>今日磅单流水</span><el-button size="small" @click="load">刷新</el-button></div></template>
        <el-table :data="records" size="small" empty-text="暂无磅单">
          <el-table-column prop="vehicleVisitId" label="车辆到厂ID" width="180" />
          <el-table-column prop="tareWeight" label="皮重" />
          <el-table-column prop="grossWeight" label="毛重" />
          <el-table-column prop="netWeight" label="净重" />
          <el-table-column prop="settleWeight" label="结算净重" />
          <el-table-column label="回写"><template #default="{ row }"><el-tag size="small" :type="row.writtenBack===1?'success':'info'">{{ row.writtenBack===1?'已回写':'未回写' }}</el-tag></template></el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const form = reactive<any>({ vehicleVisitId: undefined, weighType: 'TARE', weight: 0 })
const last = ref<any>(null)
const records = ref<any[]>([])
const loading = ref(false)

async function submit() {
  if (!form.vehicleVisitId) { ElMessage.warning('请填写车辆到厂ID'); return }
  loading.value = true
  try { last.value = await request.post('/tenant/steel/weigh', form); ElMessage.success('过磅已记录'); load() }
  finally { loading.value = false }
}
async function load() { records.value = await request.get('/tenant/steel/weigh/records') }
onMounted(load)
</script>
