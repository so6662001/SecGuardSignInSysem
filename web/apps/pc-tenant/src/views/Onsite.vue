<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>在场访客</span>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading" empty-text="暂无在场访客">
      <el-table-column label="访客" min-width="160">
        <template #default="{ row }">
          <div class="person-cell">
            <div class="pa">{{ (row.visitorName || '?').charAt(0) }}</div>
            <div><div class="pn">{{ row.visitorName }}</div><div class="pm">{{ row.visitorMobile || '—' }}</div></div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="company" label="来访单位" />
      <el-table-column prop="reason" label="事由" />
      <el-table-column prop="hostName" label="被访人" />
      <el-table-column label="访客牌"><template #default="{ row }"><span class="fv-badge blue">{{ row.badgeNo }}</span></template></el-table-column>
      <el-table-column label="入场时间">
        <template #default="{ row }">{{ (row.inTime || '').replace('T', ' ').slice(0, 19) }}</template>
      </el-table-column>
      <el-table-column label="状态">
        <template #default="{ row }"><span class="fv-badge" :class="row.status==='OVERSTAY'?'orange':'green'">{{ statusText(row.status) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="checkout(row)">离场</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const list = ref<any[]>([])
const loading = ref(false)

function statusText(s: string) { return s === 'OVERSTAY' ? '超时滞留' : '在场' }

async function load() {
  loading.value = true
  try {
    const page = await request.get('/tenant/visits/onsite')
    list.value = page.list || []
  } finally {
    loading.value = false
  }
}
async function checkout(row: any) {
  await request.post(`/tenant/visits/${row.id}/checkout`)
  ElMessage.success(`${row.visitorName} 已离场，访客牌已回收`)
  load()
}
onMounted(load)
</script>
