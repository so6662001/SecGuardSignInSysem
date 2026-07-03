<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>承运商与车辆管理</span>
        <el-button type="primary" size="small" @click="show=true">新增承运商</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading" empty-text="暂无承运商">
      <el-table-column prop="name" label="承运商" />
      <el-table-column prop="allowedGoods" label="准运货物" />
      <el-table-column label="交易平台"><template #default="{ row }"><el-tag :type="row.tradeJoined===1?'success':'info'">{{ row.tradeJoined===1?'已入驻':'未入驻' }}</el-tag></template></el-table-column>
      <el-table-column label="白名单"><template #default="{ row }"><el-tag v-if="row.whitelist===1" type="success">免审</el-tag><span v-else>—</span></template></el-table-column>
      <el-table-column label="操作" align="right">
        <template #default="{ row }">
          <el-button v-if="row.tradeJoined!==1" size="small" type="primary" @click="invite(row)">邀请入驻运力池</el-button>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="show" title="新增承运商" width="420">
    <el-form :model="form" label-width="90px">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="准运货物"><el-input v-model="form.allowedGoods" placeholder="钢材·废钢" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="show=false">取消</el-button><el-button type="primary" @click="create">创建</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const list = ref<any[]>([])
const loading = ref(false)
const show = ref(false)
const form = reactive<any>({ name: '', allowedGoods: '' })

async function load() { loading.value = true; try { list.value = await request.get('/tenant/steel/carriers') } finally { loading.value = false } }
async function create() { await request.post('/tenant/steel/carriers', form); show.value = false; ElMessage.success('已新增'); load() }
async function invite(row: any) { await request.post(`/tenant/steel/carriers/${row.id}/invite-trade`); ElMessage.success('已邀请入驻交易平台运力池'); load() }
onMounted(load)
</script>

<style scoped>.muted{color:#9aa3b5}</style>
