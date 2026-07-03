<template>
  <div>
    <el-row :gutter="18">
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#2f6bed">{{ overview.todayVisits ?? 0 }}</div><div class="lbl">今日到访</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#22b07d">{{ overview.onsite ?? 0 }}</div><div class="lbl">当前在场</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#f5a623">{{ overview.pending ?? 0 }}</div><div class="lbl">待审核</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#7c56e0">{{ deviceCount }}</div><div class="lbl">接入设备</div></div></el-col>
    </el-row>

    <el-row :gutter="18" style="margin-top:18px">
      <el-col :span="14">
        <el-card shadow="never" header="近 7 日到访趋势">
          <div class="bars">
            <div v-for="t in trend" :key="t.date" class="bar-col">
              <div class="bar" :style="{ height: barHeight(t.count) }"></div>
              <div class="bx">{{ t.date.slice(5) }}</div>
            </div>
          </div>
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
import request from '@/api/request'

const overview = ref<any>({})
const trend = ref<any[]>([])
const deviceCount = ref(0)

const maxCount = computed(() => Math.max(1, ...trend.value.map((t) => t.count)))
function barHeight(c: number) {
  return Math.round((c / maxCount.value) * 100) + '%'
}

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
