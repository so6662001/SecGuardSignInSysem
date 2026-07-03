<template>
  <el-card shadow="never" header="租户企业列表">
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="name" label="企业" />
      <el-table-column prop="domain" label="专属域名" />
      <el-table-column prop="siteCount" label="厂区" width="70" />
      <el-table-column label="状态"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column prop="openDate" label="开通日期" />
      <el-table-column label="操作" align="right"><template #default="{ row }"><el-button size="small" @click="detail(row)">详情</el-button></template></el-table-column>
    </el-table>

    <el-drawer v-model="show" :title="curr?.tenant?.name" size="420">
      <template v-if="curr">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="企业">{{ curr.tenant.name }}</el-descriptions-item>
          <el-descriptions-item label="专属域名">{{ curr.tenant.domain }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(curr.tenant.status) }}</el-descriptions-item>
          <el-descriptions-item label="厂区数">{{ curr.tenant.siteCount }}</el-descriptions-item>
          <el-descriptions-item label="套餐" v-if="curr.subscription">{{ curr.subscription.planCode }}</el-descriptions-item>
          <el-descriptions-item label="月费" v-if="curr.subscription">￥{{ curr.subscription.monthlyFee }}</el-descriptions-item>
          <el-descriptions-item label="试用到期" v-if="curr.subscription">{{ curr.subscription.trialEnd }}</el-descriptions-item>
          <el-descriptions-item label="下次续费" v-if="curr.subscription">{{ curr.subscription.nextRenew }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import request from '@/api/request'

const list = ref<any[]>([])
const loading = ref(false)
const show = ref(false)
const curr = ref<any>(null)

function statusText(s: number) { return { 1: '试用', 2: '正常', 3: '即将到期', 4: '欠费', 5: '停用' }[s] || s }
function statusType(s: number) { return s === 2 ? 'success' : s === 1 ? 'primary' : s >= 4 ? 'danger' : 'warning' }

async function load() { loading.value = true; try { const p = await request.get('/ops/tenants', { params: { page: 1, size: 50 } }); list.value = p.list || [] } finally { loading.value = false } }
async function detail(row: any) { curr.value = await request.get(`/ops/tenants/${row.id}`); show.value = true }
onMounted(load)
</script>
