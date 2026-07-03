<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>集团总览 · 多厂区</span>
        <el-button size="small" type="primary" @click="showCreate=true">新增厂区</el-button>
      </div>
    </template>
    <el-row :gutter="18" style="margin-bottom:16px">
      <el-col :span="8"><div class="stat-card"><div class="num" style="color:#2f6bed">{{ overview.siteCount ?? 0 }}</div><div class="lbl">厂区数量</div></div></el-col>
      <el-col :span="8"><div class="stat-card"><div class="num" style="color:#22b07d">{{ overview.gateCount ?? 0 }}</div><div class="lbl">门岗总数</div></div></el-col>
      <el-col :span="8"><div class="stat-card"><div class="num" style="color:#7c56e0">{{ (overview.sites||[]).length }}</div><div class="lbl">在管厂区</div></div></el-col>
    </el-row>
    <el-table :data="overview.sites || []" empty-text="暂无厂区">
      <el-table-column prop="name" label="厂区名称" />
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="manager" label="负责人" />
      <el-table-column label="状态"><template #default="{ row }"><el-tag :type="row.status===1?'success':'info'">{{ row.status===1?'启用':'停用' }}</el-tag></template></el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="showCreate" title="新增厂区" width="420">
    <el-form :model="form" label-width="80px">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="负责人"><el-input v-model="form.manager" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="showCreate=false">取消</el-button><el-button type="primary" @click="create">创建</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const overview = ref<any>({ sites: [] })
const showCreate = ref(false)
const form = reactive<any>({ name: '', address: '', manager: '' })

async function load() { overview.value = await request.get('/tenant/sites/overview') }
async function create() { await request.post('/tenant/sites', form); showCreate.value = false; ElMessage.success('已新增厂区'); load() }
onMounted(load)
</script>
