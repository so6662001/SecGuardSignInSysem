<template>
  <el-row :gutter="18">
    <el-col :span="8">
      <el-card shadow="never">
        <template #header><div style="display:flex;justify-content:space-between"><span>登记模板</span><el-button size="small" type="primary" @click="showCreate=true">新建</el-button></div></template>
        <el-table :data="templates" highlight-current-row @current-change="select" v-loading="loading">
          <el-table-column prop="name" label="模板" />
          <el-table-column prop="visitType" label="类型" width="130" />
          <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag size="small" :type="row.status===1?'success':'info'">{{ row.status===1?'已发布':'草稿' }}</el-tag></template></el-table-column>
        </el-table>
      </el-card>
    </el-col>
    <el-col :span="16">
      <el-card shadow="never">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>字段配置 {{ current ? '· ' + current.name : '' }}</span>
            <div v-if="current">
              <el-button size="small" @click="addField">添加字段</el-button>
              <el-button size="small" type="primary" @click="saveFields">保存字段</el-button>
              <el-button size="small" type="success" @click="publish">发布</el-button>
            </div>
          </div>
        </template>
        <el-empty v-if="!current" description="请选择左侧模板" />
        <el-table v-else :data="fields">
          <el-table-column label="字段标识"><template #default="{ row }"><el-input v-model="row.fieldKey" size="small" /></template></el-table-column>
          <el-table-column label="名称"><template #default="{ row }"><el-input v-model="row.label" size="small" /></template></el-table-column>
          <el-table-column label="类型" width="150"><template #default="{ row }">
            <el-select v-model="row.fieldType" size="small"><el-option v-for="t in types" :key="t" :label="t" :value="t" /></el-select>
          </template></el-table-column>
          <el-table-column label="必填" width="80"><template #default="{ row }"><el-switch v-model="row.required" :active-value="1" :inactive-value="0" /></template></el-table-column>
          <el-table-column label="模块" width="120"><template #default="{ row }"><el-input v-model="row.module" size="small" /></template></el-table-column>
          <el-table-column width="60"><template #default="{ $index }"><el-button size="small" type="danger" plain @click="fields.splice($index,1)">删</el-button></template></el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>

  <el-dialog v-model="showCreate" title="新建登记模板" width="420">
    <el-form :model="newTpl" label-width="90px">
      <el-form-item label="模板名"><el-input v-model="newTpl.name" /></el-form-item>
      <el-form-item label="来访类型">
        <el-select v-model="newTpl.visitType">
          <el-option label="普通访客" value="NORMAL" /><el-option label="承包商/施工" value="CONTRACTOR" />
          <el-option label="供应商送货" value="SUPPLIER" /><el-option label="面试应聘" value="INTERVIEW" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer><el-button @click="showCreate=false">取消</el-button><el-button type="primary" @click="create">创建</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const types = ['TEXT', 'TEXTAREA', 'RADIO', 'CHECKBOX', 'NUMBER', 'DATE', 'PHOTO', 'SIGN', 'SWITCH']
const templates = ref<any[]>([])
const fields = ref<any[]>([])
const current = ref<any>(null)
const loading = ref(false)
const showCreate = ref(false)
const newTpl = reactive<any>({ name: '', visitType: 'NORMAL' })

async function load() { loading.value = true; try { templates.value = await request.get('/tenant/templates') } finally { loading.value = false } }
async function select(row: any) {
  if (!row) return
  current.value = row
  const d = await request.get(`/tenant/templates/${row.id}`)
  fields.value = d.fields || []
}
function addField() { fields.value.push({ fieldKey: 'field' + (fields.value.length + 1), label: '新字段', fieldType: 'TEXT', required: 0, module: 'CUSTOM' }) }
async function saveFields() { await request.put(`/tenant/templates/${current.value.id}/fields`, fields.value); ElMessage.success('字段已保存') }
async function publish() { await request.post(`/tenant/templates/${current.value.id}/publish`); ElMessage.success('已发布'); load() }
async function create() {
  await request.post('/tenant/templates', newTpl)
  showCreate.value = false
  ElMessage.success('已创建')
  load()
}
onMounted(load)
</script>
