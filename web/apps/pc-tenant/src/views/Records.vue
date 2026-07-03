<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;gap:12px;align-items:center">
        <span style="font-weight:700">进出记录</span>
        <el-select v-model="status" placeholder="全部状态" clearable size="small" style="width:140px" @change="load">
          <el-option label="待审" value="PENDING" />
          <el-option label="在场" value="ONSITE" />
          <el-option label="已离场" value="LEFT" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-input v-model="keyword" placeholder="姓名/手机/单位" size="small" style="width:200px" @keyup.enter="load" />
        <el-button size="small" type="primary" @click="load">查询</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading" empty-text="暂无记录">
      <el-table-column label="访客" min-width="150">
        <template #default="{ row }">
          <div class="person-cell">
            <div class="pa">{{ (row.visitorName || '?').charAt(0) }}</div>
            <div><div class="pn">{{ row.visitorName }}</div><div class="pm">{{ row.company || '—' }}</div></div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="事由" />
      <el-table-column prop="hostName" label="被访人" />
      <el-table-column label="访客牌"><template #default="{ row }"><span class="fv-badge blue">{{ row.badgeNo }}</span></template></el-table-column>
      <el-table-column label="入场"><template #default="{ row }">{{ fmt(row.inTime) }}</template></el-table-column>
      <el-table-column label="离场"><template #default="{ row }">{{ fmt(row.outTime) }}</template></el-table-column>
      <el-table-column label="状态"><template #default="{ row }"><span class="fv-badge" :class="badgeClass(row.status)">{{ statusText(row.status) }}</span></template></el-table-column>
      <el-table-column label="审核" align="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button size="small" type="success" @click="audit(row, true)">通过</el-button>
            <el-button size="small" type="danger" plain @click="audit(row, false)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:14px;justify-content:flex-end" layout="total, prev, pager, next"
      :total="total" :page-size="size" :current-page="page" @current-change="(p:number)=>{page=p;load()}" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const status = ref('')
const keyword = ref('')
const loading = ref(false)

function fmt(t: string) { return t ? String(t).replace('T', ' ').slice(0, 19) : '—' }
function badgeClass(s: string) { return s === 'ONSITE' ? 'green' : s === 'PENDING' ? 'orange' : s === 'REJECTED' ? 'red' : 'gray' }
function statusText(s: string) { return ({ PENDING: '待审', ONSITE: '在场', LEFT: '已离场', REJECTED: '已驳回', OVERSTAY: '超时滞留' } as any)[s] || s }

async function load() {
  loading.value = true
  try {
    const p = await request.get('/tenant/visits', { params: { status: status.value || undefined, keyword: keyword.value || undefined, page: page.value, size: size.value } })
    list.value = p.list || []
    total.value = p.total || 0
  } finally {
    loading.value = false
  }
}
async function audit(row: any, pass: boolean) {
  await request.post(`/tenant/visits/${row.id}/audit`, { pass })
  ElMessage.success(pass ? '已通过' : '已驳回')
  load()
}
onMounted(load)
</script>
