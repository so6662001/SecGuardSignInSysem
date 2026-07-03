<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>门岗管理</span>
        <el-button type="primary" size="small" @click="showCreate=true">新增门岗</el-button>
      </div>
    </template>
    <el-row :gutter="16">
      <el-col v-for="g in gates" :key="g.id" :span="8" style="margin-bottom:16px">
        <el-card shadow="hover">
          <div style="display:flex;justify-content:space-between;align-items:center">
            <b>{{ g.name }}</b>
            <el-tag size="small" :type="g.status===1?'success':'info'">{{ g.status===1?'启用':'停用' }}</el-tag>
          </div>
          <div class="muted">{{ typeText(g.type) }} · {{ g.location || '未设置位置' }}</div>
          <div class="code">自助登记码：{{ g.gateCode }}</div>
          <div style="margin-top:10px">
            <el-button size="small" @click="copyQr(g)">复制自助登记链接</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!gates.length" description="暂无门岗，请新增" />

    <el-divider content-position="left">接入设备</el-divider>
    <el-table :data="devices" size="small">
      <el-table-column prop="name" label="设备" />
      <el-table-column prop="type" label="类型" />
      <el-table-column prop="deviceNo" label="设备编号" />
      <el-table-column label="在线"><template #default="{ row }"><el-tag size="small" :type="row.online===1?'success':'info'">{{ row.online===1?'在线':'离线' }}</el-tag></template></el-table-column>
    </el-table>
    <el-button size="small" style="margin-top:10px" @click="showDevice=true">注册设备</el-button>
  </el-card>

  <el-dialog v-model="showCreate" title="新增门岗" width="420">
    <el-form :model="form" label-width="90px">
      <el-form-item label="门岗名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="类型"><el-select v-model="form.type"><el-option label="人行" :value="1" /><el-option label="车行" :value="2" /><el-option label="人车混合" :value="3" /></el-select></el-form-item>
      <el-form-item label="位置"><el-input v-model="form.location" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="showCreate=false">取消</el-button><el-button type="primary" @click="createGate">创建</el-button></template>
  </el-dialog>

  <el-dialog v-model="showDevice" title="注册设备" width="420">
    <el-form :model="dev" label-width="90px">
      <el-form-item label="设备名称"><el-input v-model="dev.name" /></el-form-item>
      <el-form-item label="类型"><el-select v-model="dev.type">
        <el-option label="登记平板" value="TABLET" /><el-option label="车牌道闸" value="LPR_GATE" />
        <el-option label="人脸一体机" value="FACE" /><el-option label="地磅" value="WEIGHBRIDGE" /><el-option label="摄像头" value="CAMERA" />
      </el-select></el-form-item>
    </el-form>
    <template #footer><el-button @click="showDevice=false">取消</el-button><el-button type="primary" @click="createDevice">注册</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const gates = ref<any[]>([])
const devices = ref<any[]>([])
const showCreate = ref(false)
const showDevice = ref(false)
const form = reactive<any>({ name: '', type: 3, location: '' })
const dev = reactive<any>({ name: '', type: 'TABLET' })

function typeText(t: number) { return t === 1 ? '人行' : t === 2 ? '车行' : '人车混合' }
async function load() { gates.value = await request.get('/tenant/gates'); devices.value = await request.get('/tenant/devices') }
async function createGate() { await request.post('/tenant/gates', form); showCreate.value = false; ElMessage.success('已创建门岗'); load() }
async function createDevice() { const d = await request.post('/tenant/devices', dev); showDevice.value = false; ElMessage.success('已注册设备 ' + d.deviceNo); load() }
function copyQr(g: any) {
  const url = location.origin + g.qrcodeUrl
  navigator.clipboard?.writeText(url)
  ElMessage.success('已复制：' + url)
}
onMounted(load)
</script>

<style scoped>
.muted { color: #9aa3b5; font-size: 13px; margin-top: 4px; }
.code { margin-top: 8px; font-size: 12px; color: #2f6bed; background: #f4f8ff; padding: 6px 8px; border-radius: 8px; }
</style>
