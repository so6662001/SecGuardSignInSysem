<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>系统设置 · 人员与权限</span>
        <el-button type="primary" size="small" @click="openCreate">新增成员</el-button>
      </div>
    </template>
    <el-table :data="members" v-loading="loading">
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="mobileMask" label="手机号" />
      <el-table-column prop="jobTitle" label="职务" />
      <el-table-column label="状态"><template #default="{ row }"><el-tag :type="row.status===1?'success':'info'">{{ row.status===1?'启用':'停用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" align="right">
        <template #default="{ row }">
          <el-button size="small" @click="resetPwd(row)">重置密码</el-button>
          <el-button size="small" type="danger" plain @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-divider content-position="left">订阅与账单</el-divider>
    <el-descriptions :column="3" border>
      <el-descriptions-item label="当前套餐">全功能版</el-descriptions-item>
      <el-descriptions-item label="计费">￥60 / 厂区 / 月</el-descriptions-item>
      <el-descriptions-item label="快捷入口">
        <el-link type="primary" @click="$router.push('/rules')">登记规则</el-link> ·
        <el-link type="primary" @click="$router.push('/gate-config')">门岗管理</el-link> ·
        <el-link type="primary" @click="$router.push('/approval')">审批与通知</el-link>
      </el-descriptions-item>
    </el-descriptions>
  </el-card>

  <el-dialog v-model="show" title="新增成员" width="460">
    <el-form :model="form" label-width="80px">
      <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
      <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="form.mobile" /></el-form-item>
      <el-form-item label="职务"><el-input v-model="form.jobTitle" /></el-form-item>
      <el-form-item label="初始密码"><el-input v-model="form.password" placeholder="留空默认 123456" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="show=false">取消</el-button><el-button type="primary" @click="create">创建</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const members = ref<any[]>([])
const loading = ref(false)
const show = ref(false)
const form = reactive<any>({ realName: '', username: '', mobile: '', jobTitle: '', password: '' })

async function load() { loading.value = true; try { const p = await request.get('/tenant/members'); members.value = p.list || [] } finally { loading.value = false } }
function openCreate() { Object.assign(form, { realName: '', username: '', mobile: '', jobTitle: '', password: '' }); show.value = true }
async function create() { await request.post('/tenant/members', form); show.value = false; ElMessage.success('已创建'); load() }
async function del(row: any) { await ElMessageBox.confirm(`确认删除成员 ${row.realName}?`, '提示'); await request.delete(`/tenant/members/${row.id}`); ElMessage.success('已删除'); load() }
async function resetPwd(row: any) { await request.post(`/tenant/members/${row.id}/reset-password`, { password: '123456' }); ElMessage.success('已重置为 123456') }
onMounted(load)
</script>
