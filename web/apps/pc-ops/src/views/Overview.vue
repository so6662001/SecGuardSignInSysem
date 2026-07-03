<template>
  <div>
    <el-row :gutter="18">
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#7c56e0">{{ tenantTotal }}</div><div class="lbl">租户企业</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#f5a623">{{ trialCount }}</div><div class="lbl">试用中</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#22b07d">￥{{ mrr }}</div><div class="lbl">月度经常性收入(估)</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#2f6bed">{{ pending }}</div><div class="lbl">待审核开通申请</div></div></el-col>
    </el-row>
    <el-card shadow="never" style="margin-top:18px" header="近 6 月收入趋势（MRR）">
      <Chart :option="revenueOption" height="240px" />
    </el-card>

    <el-card shadow="never" style="margin-top:18px" header="待审核开通申请">
      <el-table :data="apps" empty-text="暂无待审申请">
        <el-table-column prop="companyName" label="企业" />
        <el-table-column prop="contactName" label="联系人" />
        <el-table-column prop="planCode" label="套餐" />
        <el-table-column prop="createTime" label="申请时间"><template #default="{ row }">{{ String(row.createTime).replace('T',' ').slice(0,19) }}</template></el-table-column>
        <el-table-column align="right"><template #default><el-button size="small" type="primary" @click="$router.push('/applications')">去审核</el-button></template></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import request from '@/api/request'
import Chart from '@/components/Chart.vue'

const tenantTotal = ref(0)
const trialCount = ref(0)
const mrr = ref(0)
const pending = ref(0)
const apps = ref<any[]>([])
const revenue = ref<any[]>([])

const revenueOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 50, right: 20, top: 20, bottom: 30 },
  xAxis: { type: 'category', data: revenue.value.map((r) => r.month) },
  yAxis: { type: 'value' },
  series: [{
    type: 'line', smooth: true, data: revenue.value.map((r) => Number(r.revenue)),
    areaStyle: { color: 'rgba(124,86,224,0.15)' }, itemStyle: { color: '#7c56e0' }
  }]
}))

onMounted(async () => {
  const o = await request.get('/ops/overview')
  tenantTotal.value = o.tenantTotal || 0
  trialCount.value = o.trial || 0
  mrr.value = o.mrr || 0
  pending.value = o.pending || 0
  const a = await request.get('/ops/applications', { params: { status: 1, page: 1, size: 20 } })
  apps.value = a.list || []
  revenue.value = await request.get('/ops/revenue-trend')
})
</script>
