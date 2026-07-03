<template>
  <div>
    <el-row :gutter="18">
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#7c56e0">{{ tenantTotal }}</div><div class="lbl">租户企业</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#f5a623">{{ trialCount }}</div><div class="lbl">试用中</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#22b07d">￥{{ mrr }}</div><div class="lbl">月度经常性收入(估)</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="num" style="color:#2f6bed">{{ pending }}</div><div class="lbl">待审核开通申请</div></div></el-col>
    </el-row>
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
import { onMounted, ref } from 'vue'
import request from '@/api/request'

const tenantTotal = ref(0)
const trialCount = ref(0)
const mrr = ref(0)
const pending = ref(0)
const apps = ref<any[]>([])

onMounted(async () => {
  const t = await request.get('/ops/tenants', { params: { page: 1, size: 100 } })
  const list = t.list || []
  tenantTotal.value = t.total || list.length
  trialCount.value = list.filter((x: any) => x.status === 1).length
  mrr.value = list.reduce((s: number, x: any) => s + (x.status === 2 || x.status === 3 ? (x.siteCount || 1) * 60 : 0), 0)
  const a = await request.get('/ops/applications', { params: { status: 1, page: 1, size: 20 } })
  apps.value = a.list || []
  pending.value = a.total || 0
})
</script>
