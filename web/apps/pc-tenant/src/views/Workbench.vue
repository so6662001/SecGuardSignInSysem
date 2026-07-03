<template>
  <div>
    <el-row :gutter="18">
      <el-col :span="6"><StatCard :value="overview.todayVisits ?? 0" label="今日到访" color="blue" num-color="#2f6bed" :icon="User" /></el-col>
      <el-col :span="6"><StatCard :value="overview.onsite ?? 0" label="当前在场" color="green" num-color="#22b07d" :icon="Clock" /></el-col>
      <el-col :span="6"><StatCard :value="overview.pending ?? 0" label="待审核" color="orange" num-color="#f5a623" :icon="Bell" /></el-col>
      <el-col :span="6"><StatCard :value="deviceCount" label="接入设备" color="purple" num-color="#7c56e0" :icon="Cpu" /></el-col>
    </el-row>

    <el-row :gutter="18" style="margin-top:18px">
      <el-col :span="14">
        <el-card shadow="never" header="近 7 日到访趋势">
          <Chart :option="trendOption" height="260px" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" header="快捷操作">
          <el-space wrap>
            <el-button type="primary" @click="$router.push('/checkin')">访客入场登记</el-button>
            <el-button @click="$router.push('/onsite')">在场访客</el-button>
            <el-button @click="$router.push('/records')">进出记录</el-button>
          </el-space>
          <el-alert style="margin-top:16px" type="success" :closable="false"
            title="后端已跑通：登记 → 通知被访人 → 审批 → 在场 → 离场 → 台账；钢铁：车辆到厂 → 过磅 → 放行回写。" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { User, Clock, Bell, Cpu } from '@element-plus/icons-vue'
import request from '@/api/request'
import Chart from '@/components/Chart.vue'
import StatCard from '@/components/StatCard.vue'

const overview = ref<any>({})
const trend = ref<any[]>([])
const deviceCount = ref(0)

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 40, right: 20, top: 20, bottom: 30 },
  xAxis: { type: 'category', data: trend.value.map((t) => t.date.slice(5)) },
  yAxis: { type: 'value' },
  series: [{
    type: 'bar', data: trend.value.map((t) => t.count), barWidth: '45%',
    itemStyle: { color: '#2f6bed', borderRadius: [6, 6, 0, 0] }
  }]
}))

onMounted(async () => {
  try { overview.value = await request.get('/tenant/dashboard/overview') } catch {}
  try { trend.value = await request.get('/tenant/dashboard/trend') } catch {}
  try { const list = await request.get('/tenant/devices'); deviceCount.value = (list || []).length } catch {}
})
</script>

<style scoped lang="scss">
.bars { display: flex; align-items: flex-end; gap: 14px; height: 200px; }
.bar-col { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: flex-end; height: 100%; gap: 8px; }
.bar { width: 60%; max-width: 34px; min-height: 4px; border-radius: 8px 8px 3px 3px; background: linear-gradient(180deg, #5b8dff, #2f6bed); }
.bx { font-size: 12px; color: #9aa3b5; }
</style>
