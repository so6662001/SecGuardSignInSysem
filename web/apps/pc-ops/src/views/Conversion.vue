<template>
  <div>
    <el-card shadow="never" header="引流转化漏斗（厂智访客 → 主营 / 交易平台）">
      <div class="funnel">
        <div v-for="(s, i) in stages" :key="s.key" class="frow" :style="{ width: widthOf(i) }">
          <div class="fbar" :style="{ background: colors[i] }">
            <span>{{ s.label }}</span><b>{{ funnel[s.key] || 0 }}</b>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-top:18px">
      <template #header><div style="display:flex;justify-content:space-between"><span>高潜转化线索</span><el-button size="small" @click="load">刷新</el-button></div></template>
      <el-table :data="leads" empty-text="暂无线索">
        <el-table-column prop="companyName" label="企业（访客租户）" />
        <el-table-column prop="signalTag" label="行业信号"><template #default="{ row }"><el-tag size="small">{{ row.signalTag }}</el-tag></template></el-table-column>
        <el-table-column prop="recommend" label="推荐产品" />
        <el-table-column label="意向分"><template #default="{ row }"><el-progress :percentage="row.intentScore" :stroke-width="10" /></template></el-table-column>
        <el-table-column label="状态"><template #default="{ row }"><el-tag :type="row.status===3?'success':row.status===2?'primary':'warning'">{{ statusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column align="right"><template #default="{ row }"><el-button v-if="row.status===1" size="small" type="primary" @click="assign(row)">派发跟进</el-button></template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const stages = [
  { key: 'REGISTER', label: '注册厂智访客企业' },
  { key: 'ACTIVE', label: '活跃使用' },
  { key: 'POTENTIAL', label: '识别为高潜' },
  { key: 'OPPORTUNITY', label: '商机跟进' },
  { key: 'DEAL', label: '成交(主营/交易平台)' }
]
const colors = ['#2f6bed', '#4a9fe0', '#22b07d', '#f5a623', '#7c56e0']
const funnel = ref<Record<string, number>>({})
const leads = ref<any[]>([])

function widthOf(i: number) { return (100 - i * 14) + '%' }
function statusText(s: number) { return { 1: '待跟进', 2: '跟进中', 3: '成交', 4: '观察' }[s] || s }

async function load() {
  funnel.value = await request.get('/ops/funnel')
  leads.value = await request.get('/ops/leads')
}
async function assign(row: any) { await request.post(`/ops/leads/${row.id}/assign`); ElMessage.success('已派发跟进'); load() }
onMounted(load)
</script>

<style scoped>
.funnel { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 10px 0; }
.frow { margin: 0 auto; }
.fbar { color: #fff; border-radius: 12px; padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
.fbar b { font-size: 20px; }
</style>
