<template>
  <el-row :gutter="18">
    <el-col :span="13">
      <el-card shadow="never" header="配置报价">
        <el-form label-width="110px">
          <el-form-item label="版本">
            <el-select v-model="planCode">
              <el-option label="免费版" value="FREE" /><el-option label="标准版 ¥60" value="STANDARD" />
              <el-option label="钢铁厂专属版 ¥599" value="STEEL" /><el-option label="集团版(定制)" value="GROUP" />
            </el-select>
          </el-form-item>
          <el-form-item label="厂区数量"><el-input-number v-model="siteCount" :min="1" /></el-form-item>
          <el-form-item label="增值模块">
            <el-checkbox-group v-model="addons">
              <el-checkbox label="LPR">车牌识别¥199</el-checkbox>
              <el-checkbox label="FACE">人脸¥159</el-checkbox>
              <el-checkbox label="OCR">OCR¥99</el-checkbox>
              <el-checkbox label="TRADE">磅单+交易打通¥399</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="硬件">
            <div v-for="h in hardware" :key="h.code" style="display:flex;gap:10px;align-items:center;margin-bottom:8px">
              <span style="width:130px">{{ h.name }}</span>
              <el-radio-group v-model="h.mode" size="small"><el-radio-button label="buy">购买</el-radio-button><el-radio-button v-if="h.rent" label="rent">租赁</el-radio-button></el-radio-group>
              <el-input-number v-model="h.qty" :min="0" size="small" />
            </div>
          </el-form-item>
          <el-divider content-position="left">客户信息</el-divider>
          <el-form-item label="客户名称"><el-input v-model="customer.customerName" /></el-form-item>
          <el-form-item label="联系人"><el-input v-model="customer.contactName" /></el-form-item>
          <el-form-item><el-button type="primary" @click="gen">生成报价单</el-button></el-form-item>
        </el-form>
      </el-card>
    </el-col>
    <el-col :span="11">
      <el-card shadow="never" header="报价单">
        <el-empty v-if="!quote" description="填写左侧并生成报价单" />
        <template v-else>
          <p>报价单号：<b>{{ quote.quoteNo }}</b></p>
          <el-table :data="items" size="small">
            <el-table-column prop="name" label="项目" />
            <el-table-column prop="qty" label="数量" width="60" />
            <el-table-column prop="mode" label="计费" />
            <el-table-column prop="sub" label="小计" align="right" />
          </el-table>
          <div class="totals">
            <div>月度费用合计：<b>￥{{ quote.monthlyTotal }}</b></div>
            <div>一次性硬件合计：<b>￥{{ quote.onetimeTotal }}</b></div>
            <div class="year">首年预估总额：<b>{{ quote.isCustom ? '面议' : '￥' + quote.firstYear }}</b></div>
          </div>
          <div style="margin-top:14px">
            <el-button type="primary" @click="send">发送给客户</el-button>
            <el-button @click="print">打印</el-button>
          </div>
        </template>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const planCode = ref('STEEL')
const siteCount = ref(1)
const addons = ref<string[]>(['TRADE'])
const hardware = reactive([
  { code: 'TABLET', name: '登记平板', mode: 'buy', qty: 1, rent: true },
  { code: 'LPR_GATE', name: '车牌道闸', mode: 'rent', qty: 1, rent: true },
  { code: 'QR_STAND', name: '自助登记码立牌', mode: 'buy', qty: 2, rent: false }
])
const customer = reactive({ customerName: '德胜钢铁有限公司', contactName: '李经理' })
const quote = ref<any>(null)
const items = computed(() => { try { return JSON.parse(quote.value?.items || '[]') } catch { return [] } })

async function gen() {
  quote.value = await request.post('/ops/quotes', {
    planCode: planCode.value, siteCount: siteCount.value, addons: addons.value,
    hardware: hardware.filter(h => h.qty > 0).map(h => ({ code: h.code, mode: h.mode, qty: h.qty })),
    ...customer
  })
  ElMessage.success('已生成报价单')
}
async function send() { await request.post(`/ops/quotes/${quote.value.id}/send`); ElMessage.success('已发送给客户') }
function print() { window.print() }
</script>

<style scoped>
.totals { margin-top: 14px; line-height: 2; }
.totals .year { font-size: 18px; color: #2f6bed; border-top: 2px solid #2f6bed; padding-top: 8px; margin-top: 6px; }
</style>
